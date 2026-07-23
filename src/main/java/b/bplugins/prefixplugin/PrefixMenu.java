package b.bplugins.prefixplugin;

import b.bplugins.prefixplugin.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrefixMenu {

    private final Main plugin;

    public PrefixMenu(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Component titleComponent = MessageUtils.getMessage("menu.title");

        // Fix: eigener Holder statt Bukkit.createInventory(null, ...), damit der
        // MenuListener das Menü zuverlässig erkennen kann.
        PrefixMenuHolder holder = new PrefixMenuHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, titleComponent);
        holder.setInventory(inv);

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String permission = section.getString(key + ".permission");
                if (permission == null || player.hasPermission(permission)) {
                    inv.addItem(createGuiItem(section, key));
                }
            }
        }

        // Reset-Button
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta meta = reset.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageUtils.parseLegacyRaw("menu.reset-button"));
            reset.setItemMeta(meta);
        }
        inv.setItem(49, reset);

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(ConfigurationSection section, String key) {
        String materialName = section.getString(key + ".material", "PAPER");
        Material mat = Material.matchMaterial(materialName);
        ItemStack item = new ItemStack(mat != null ? mat : Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String rawName = section.getString(key + ".display_name", "Unbekannter Prefix");
            meta.setDisplayName(LegacyComponentSerializer.legacySection()
                    .serialize(MiniMessage.miniMessage().deserialize(rawName)));

            if (section.contains(key + ".custom_model_data")) {
                meta.setCustomModelData(section.getInt(key + ".custom_model_data"));
            }

            List<String> lore = new ArrayList<>();
            lore.add(MessageUtils.parseLegacyRaw("menu.item-lore"));
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }
}