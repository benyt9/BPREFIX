package b.bplugins.prefixplugin.utils;

import b.bplugins.prefixplugin.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageUtils {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static volatile YamlConfiguration langConfig;

    private static String resolveColors(String message) {
        // Ersetzt <colors.message:key> durch Werte aus der YAML (Farb-Mappings)
        if (langConfig != null && langConfig.contains("mappings.colors.message")) {
            for (String key : langConfig.getConfigurationSection("mappings.colors.message").getKeys(false)) {
                String color = langConfig.getString("mappings.colors.message." + key, "");
                message = message.replace("<colors.message:" + key + ">", color);
            }
        }
        return message;
    }

    public static void load(String languageCode) {
        File folder = new File(Main.getInstance().getDataFolder(), "messages");
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, languageCode + ".yml");

        if (!file.exists()) {
            Main.getInstance().saveResource("messages/" + languageCode + ".yml", false);
        }

        if (!file.exists()) {
            Main.getInstance().getLogger().warning("Sprachdatei " + languageCode + ".yml konnte nicht gefunden/erstellt werden!");
            return;
        }
        langConfig = YamlConfiguration.loadConfiguration(file);
    }

    public static Component getMessage(String key, String... replacements) {
        if (langConfig == null) {
            return MM.deserialize("<red>Language configuration not loaded!");
        }
        String prefix = langConfig.getString("mappings.prefix", "");
        String raw = langConfig.getString("messages." + key, "<red>Missing: " + key);

        // Platzhalter ersetzen (z.B. %name%)
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                raw = raw.replace(replacements[i], replacements[i + 1]);
            }
        }

        // WICHTIG (Fix): Vorher wurde nur "raw" durch resolveColors gejagt, der Prefix selbst
        // nicht. Wenn mappings.prefix ein <colors.message:x>-Tag enthielt, blieb dieses Tag
        // als literaler Text sichtbar. Jetzt läuft die komplette Zeile durch resolveColors.
        return MM.deserialize(resolveColors(prefix + raw));
    }

    public static String parseLegacyRaw(String key) {
        if (langConfig == null) return "Config not loaded";
        String raw = langConfig.getString("messages." + key, "Missing: " + key);
        Component comp = MM.deserialize(resolveColors(raw));
        return LegacyComponentSerializer.legacySection().serialize(comp);
    }
}