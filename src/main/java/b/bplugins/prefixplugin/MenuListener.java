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
import java.util.UUID;

public class MenuListener implements Listener {
    private final Main plugin;

    public MenuListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PrefixMenuHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        UUID targetUuid = Main.getPlayerUuid(player); // Universelle UUID (unterstützt Floodgate/Bedrock sauber)

        // Klicks in der unteren Steuerungs-Reihe (Slots 45 bis 53)
        if (slot >= 45 && slot < 54) {
            if (slot == 49) { // Reset Button
                Main.playerColors.put(targetUuid, "");
                player.sendMessage(MessageUtils.getMessage("prefix-reset"));
                player.closeInventory();
                return;
            }
            if (slot == 48 && clicked.getType() == Material.ARROW) { // Vorherige Seite
                new PrefixMenu(plugin).openPage(player, holder.getPage() - 1);
                return;
            }
            if (slot == 50 && clicked.getType() == Material.ARROW) { // Nächste Seite
                new PrefixMenu(plugin).openPage(player, holder.getPage() + 1);
                return;
            }
            return; // Alle anderen Glas-Slots in der unteren Reihe ignorieren
        }

        // Reset-Sicherheit falls außerhalb geklickt
        if (clicked.getType() == Material.BARRIER) {
            Main.playerColors.put(targetUuid, "");
            player.sendMessage(MessageUtils.getMessage("prefix-reset"));
            player.closeInventory();
            return;
        }

        // Prefix-Auswahl-Logik (Berechnet das Item basierend auf Seite + Slot)
        int clickedIndex = holder.getPage() * PrefixMenu.ITEMS_PER_PAGE + slot;

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        if (section != null) {
            int currentIndex = 0;
            for (String key : section.getKeys(false)) {
                String permission = section.getString(key + ".permission");
                if (permission == null || player.hasPermission(permission)) {
                    if (currentIndex == clickedIndex) {
                        String colorCode = section.getString(key + ".color_code");
                        Main.playerColors.put(targetUuid, colorCode);

                        String rawNameFromConfig = section.getString(key + ".display_name");
                        player.sendMessage(MessageUtils.getMessage("prefix-selected", "%name%", rawNameFromConfig));

                        player.closeInventory();
                        return;
                    }
                    currentIndex++;
                }
            }
        }
    }
}