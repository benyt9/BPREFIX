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
    public static final int ITEMS_PER_PAGE = 45;

    public PrefixMenu(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        openPage(player, 0);
    }

    public void openPage(Player player, int page) {
        Component titleComponent = MessageUtils.getMessage("menu.title");

        PrefixMenuHolder holder = new PrefixMenuHolder(page);
        Inventory inv = Bukkit.createInventory(holder, 54, titleComponent);
        holder.setInventory(inv);

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        List<ItemStack> allPrefixItems = new ArrayList<>();

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String permission = section.getString(key + ".permission");
                if (permission == null || player.hasPermission(permission)) {
                    allPrefixItems.add(createGuiItem(section, key));
                }
            }
        }

        // Berechne Seiten
        int totalPages = Math.max(1, (int) Math.ceil((double) allPrefixItems.size() / ITEMS_PER_PAGE));
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        // Items für die aktuelle Seite einfügen
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allPrefixItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            inv.setItem(i - startIndex, allPrefixItems.get(i));
        }

        // --- UNTERE REIHE (DEKORATION & STEUERUNG) ---
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }

        // Untere Reihe komplett mit Glas füllen (Slots 45 bis 53)
        for (int slot = 45; slot < 54; slot++) {
            inv.setItem(slot, glass);
        }

        // 1. Blätter-Pfeil LINKS (nur wenn es eine vorherige Seite gibt)[cite: 3]
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(MessageUtils.parseLegacyRaw("menu.previous-page"));
                prevButton.setItemMeta(prevMeta);
            }
            inv.setItem(48, prevButton); // Direkt links neben der Barrier (Slot 49)[cite: 3]
        }

        // 2. Reset-Button in der Mitte (Slot 49)[cite: 3]
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        if (resetMeta != null) {
            resetMeta.setDisplayName(MessageUtils.parseLegacyRaw("menu.reset-button"));
            reset.setItemMeta(resetMeta);
        }
        inv.setItem(49, reset);

        // 3. Blätter-Pfeil RECHTS (nur wenn es eine nächste Seite gibt)[cite: 3]
        if (page < totalPages - 1) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(MessageUtils.parseLegacyRaw("menu.next-page"));
                nextButton.setItemMeta(nextMeta);
            }
            inv.setItem(50, nextButton); // Direkt rechts neben der Barrier (Slot 49)[cite: 3]
        }

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