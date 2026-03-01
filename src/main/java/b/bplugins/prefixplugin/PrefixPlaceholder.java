package b.bplugins.prefixplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class PrefixPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public PrefixPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "bprefix";
    }

    @Override
    public String getAuthor() {
        return "bplugins";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("prefixcolor")) {
            // Gibt den Farbcode zurück oder einen leeren String, wenn nichts gesetzt ist
            return Main.playerColors.getOrDefault(player.getUniqueId(), "");
        }
        return null;
    }
}