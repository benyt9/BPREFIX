package b.bplugins.prefixplugin;

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
        // Erstellt ein 6-Reihen Inventar
        Inventory inv = Bukkit.createInventory(null, 54, "§8Prefix Menü");

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String permission = section.getString(key + ".permission");

                // Check, ob der Spieler das Recht für diesen Prefix hat
                if (permission == null || player.hasPermission(permission)) {
                    inv.addItem(createGuiItem(section, key));
                }
            }
        }

        // Reset-Button (Barriere) auf Slot 49 (unten mittig)
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta meta = reset.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cPrefix zurücksetzen");
            reset.setItemMeta(meta);
        }
        inv.setItem(49, reset);

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(ConfigurationSection section, String key) {
        // Material laden
        String materialName = section.getString(key + ".material", "PAPER");
        Material mat = Material.matchMaterial(materialName);
        ItemStack item = new ItemStack(mat != null ? mat : Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // DER ENTSCHEIDENDE FIX:
            // Wir nutzen parseToLegacy, um MiniMessage-Tags (<gradient> etc.)
            // für das Inventar-Display in §-Farbcodes umzuwandeln.
            String rawName = section.getString(key + ".display_name", "Unbekannter Prefix");
            meta.setDisplayName(Main.getMessages().parseToLegacy(rawName));

            // Custom Model Data (für eigene Texturen)
            if (section.contains(key + ".custom_model_data")) {
                meta.setCustomModelData(section.getInt(key + ".custom_model_data"));
            }

            // Lore (Beschreibung)
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke, um diese Farbe zu wählen.");
            meta.setLore(lore);

            item.setItemMeta(meta);
        }
        return item;
    }
}