package b.bplugins.prefixplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PrefixPlaceholder extends PlaceholderExpansion {

    private final Main plugin;

    public PrefixPlaceholder(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bprefix";
    }

    @Override
    public @NotNull String getAuthor() {
        return "bplugins";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(@NotNull OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("prefixcolor")) {
            return Main.playerColors.getOrDefault(player.getUniqueId(), "");
        }
        return null;
    }
}