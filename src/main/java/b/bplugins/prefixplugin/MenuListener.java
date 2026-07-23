package b.bplugins.prefixplugin;

import b.bplugins.prefixplugin.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class MenuListener implements Listener {
    private final Main plugin;

    public MenuListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Fix: Menü zuverlässig über den Holder erkennen statt über einen Titel-String-Vergleich
        if (!(event.getInventory().getHolder() instanceof PrefixMenuHolder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Reset Button
        if (clicked.getType() == Material.BARRIER) {
            Main.playerColors.put(player.getUniqueId(), "");
            player.sendMessage(MessageUtils.getMessage("prefix-reset"));
            player.closeInventory();
            return;
        }

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "prefixes." + key + ".";

                String matString = config.getString(path + "material", "");
                Material configMat = Material.matchMaterial(matString);

                if (clicked.getType() == configMat) {
                    int configModelData = config.getInt(path + "custom_model_data", -1);
                    int clickedModelData = -1;

                    if (clicked.hasItemMeta() && clicked.getItemMeta().hasCustomModelData()) {
                        clickedModelData = clicked.getItemMeta().getCustomModelData();
                    }

                    if (configModelData == clickedModelData) {
                        // Fix: Permission auch beim Klick prüfen, nicht nur beim Befüllen des Menüs
                        String permission = section.getString(key + ".permission");
                        if (permission != null && !player.hasPermission(permission)) {
                            continue;
                        }

                        String colorCode = section.getString(key + ".color_code");
                        Main.playerColors.put(player.getUniqueId(), colorCode);

                        String rawNameFromConfig = section.getString(key + ".display_name");
                        player.sendMessage(MessageUtils.getMessage("prefix-selected", "%name%", rawNameFromConfig));

                        player.closeInventory();
                        return;
                    }
                }
            }
        }
    }
}