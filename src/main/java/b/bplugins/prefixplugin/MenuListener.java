package b.bplugins.prefixplugin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

public class MenuListener implements Listener {
    private final Main plugin;

    public MenuListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§8Prefix Menü")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Reset Button
        if (clicked.getType() == Material.BARRIER) {
            Main.playerColors.put(player.getUniqueId(), "");
            plugin.send(player, "prefix-reset");
            player.closeInventory();
            return;
        }

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String path = "prefixes." + key + ".";
                Material configMat = Material.matchMaterial(config.getString(path + "material", ""));

                // Wir vergleichen Material und CustomModelData statt dem bunten Namen
                if (clicked.getType() == configMat) {
                    int configModelData = config.getInt(path + "custom_model_data", -1);
                    int clickedModelData = -1;

                    if (clicked.hasItemMeta() && clicked.getItemMeta().hasCustomModelData()) {
                        clickedModelData = clicked.getItemMeta().getCustomModelData();
                    }

                    if (configModelData == clickedModelData) {
                        // Im MenuListener (in der Schleife für die Prefixe):
                        String rawNameFromConfig = section.getString(key + ".display_name");
                        Main.playerColors.put(player.getUniqueId(), section.getString(key + ".color_code"));

                        // WICHTIG: rawNameFromConfig schicken, NICHT die farbige Version!
                        plugin.send(player, "prefix-selected", "%name%", rawNameFromConfig);

                        player.closeInventory();
                        return;
                    }
                }
            }
        }
    }
}