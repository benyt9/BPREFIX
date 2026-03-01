package b.bplugins.prefixplugin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PrefixCommand implements CommandExecutor {

    private final Main plugin;

    public PrefixCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // 1. RELOAD CHECK (Funktioniert für Spieler & Konsole)
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bprefix.admin")) {
                if (sender instanceof Player p) plugin.send(p, "no-permission");
                else sender.sendMessage("§cKeine Rechte!");
                return true;
            }

            Main.getMessages().reloadMessages();

            if (sender instanceof Player p) {
                plugin.send(p, "prefix-reload-success");
            } else {
                sender.sendMessage("§a[BPREFIX] Konfigurationen erfolgreich neu geladen!");
            }
            return true;
        }

        // Ab hier sind nur noch Befehle für Spieler
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cNur für Spieler!");
            return true;
        }

        // 2. CREATE CHECK
        if (args.length >= 5 && args[0].equalsIgnoreCase("create")) {
            if (!player.hasPermission("bprefix.command.createprefix")) {
                plugin.send(player, "no-permission");
                return true;
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.AIR) {
                plugin.send(player, "no-item-in-hand");
                return true;
            }

            String id = args[1].toLowerCase();
            String displayName = args[2];
            String colorCode = args[3];
            String permission = args[4];

            saveToPrefixConfig(id, displayName, colorCode, permission, itemInHand);

            plugin.send(player, "prefix-created", "%name%", displayName);
            return true;
        }

        // 3. MENU ÖFFNEN (Wenn kein Argument oder unbekanntes Argument)
        new PrefixMenu(plugin).open(player);
        return true;
    }

    private void saveToPrefixConfig(String id, String name, String color, String perm, ItemStack item) {
        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = "prefixes." + id + ".";
        config.set(path + "display_name", name);
        config.set(path + "color_code", color);
        config.set(path + "permission", perm);
        config.set(path + "material", item.getType().name());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                config.set(path + "custom_model_data", meta.getCustomModelData());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}