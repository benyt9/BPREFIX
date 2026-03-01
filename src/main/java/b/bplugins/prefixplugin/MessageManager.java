package b.bplugins.prefixplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class MessageManager {
    private final Main plugin;
    private FileConfiguration config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageManager(Main plugin) {
        this.plugin = plugin;
        reloadMessages();
    }

    public void reloadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");

        // Falls die Datei nicht existiert, aus dem Jar kopieren
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Datei frisch von der Festplatte laden
        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Für den Chat: Verarbeitet erst Platzhalter, dann & Codes, dann MiniMessage
     */
    public Component getComponent(String path, String... replacements) {
        String message = config.getString(path, "Missing message: " + path);
        String prefix = config.getString("prefix", "&8[&bBPREFIX&8] &7");

        // 1. Platzhalter einsetzen
        String content = message.replace("%prefix%", prefix);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                content = content.replace(replacements[i], replacements[i + 1]);
            }
        }

        // 2. & zu § machen, dann zu Component, dann MiniMessage (Gradients retten)
        return miniMessage.deserialize(translateToMiniNative(content));
    }

    /**
     * Für das GUI: Wandelt alles in §-Codes um
     */
    public String parseToLegacy(String input) {
        if (input == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(translateToMiniNative(input)));
    }

    /**
     * Dieser Helper sorgt dafür, dass &b zu <aqua> wird,
     * ohne die echten <gradient> Tags zu zerstören.
     */
    private String translateToMiniNative(String input) {
        // Wandelt & in echte Farben um und serialisiert es so, dass MiniMessage es versteht
        Component legacy = LegacyComponentSerializer.legacyAmpersand().deserialize(input);
        return miniMessage.serialize(legacy)
                .replace("\\<", "<")  // Verhindert das MiniMessage Tags "escaped" werden
                .replace("\\>", ">");
    }
}