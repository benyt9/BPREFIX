package b.bplugins.prefixplugin.api;

import b.bplugins.prefixplugin.Main;
import b.bplugins.prefixplugin.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BPrefixAPI {

    private static BPrefixAPI instance;
    private final Main plugin;

    public BPrefixAPI(Main plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static BPrefixAPI getInstance() {
        return instance;
    }

    /**
     * Ruft das aktuell aktive Prefix (den Farbcode/String) eines Spielers ab.
     */
    public String getActivePrefix(UUID uuid) {
        if (uuid == null) return null;
        return Main.playerColors.getOrDefault(uuid, "");
    }

    /**
     * Ruft das aktive Prefix direkt über das Player-Objekt ab (inkl. Bedrock/Floodgate-Unterstützung).
     */
    public String getActivePrefix(Player player) {
        if (player == null) return null;
        UUID targetUuid = Main.getPlayerUuid(player);
        return getActivePrefix(targetUuid);
    }

    /**
     * Setzt das aktive Prefix für einen Spieler dauerhaft und aktualisiert die Daten.
     */
    public void setActivePrefix(UUID uuid, String prefixId) {
        if (uuid == null || prefixId == null) return;

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Holt den echten color_code aus der prefixes.yml, falls eine ID übergeben wurde
        String colorCode = config.getString("prefixes." + prefixId + ".color_code", prefixId);

        Main.playerColors.put(uuid, colorCode);

        // Falls der Spieler online ist, benachrichtigen
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            String rawName = config.getString("prefixes." + prefixId + ".display_name", prefixId);
            player.sendMessage(MessageUtils.getMessage("prefix-selected", "%name%", rawName));
        }
    }

    /**
     * Setzt ein temporäres Prefix für einen Spieler.
     */
    public void setTemporaryPrefix(UUID uuid, String prefixId, long duration, TimeUnit unit) {
        if (uuid == null || prefixId == null) return;

        // Setzt zunächst das Prefix
        setActivePrefix(uuid, prefixId);

        // Beispiel für temporäre Logik mit einem Scheduler, der es nach Ablauf zurücksetzt
        long durationTicks = unit.toMillis(duration) / 50; // Millisekunden in Minecraft-Ticks umrechnen

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Nach Ablauf wieder zurücksetzen (z.B. auf leer oder Standard)
            if (Main.playerColors.containsKey(uuid)) {
                Main.playerColors.put(uuid, "");
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage("§cDein temporäres Prefix ist abgelaufen!");
                }
            }
        }, durationTicks);
    }

    /**
     * Prüft asynchron, ob ein Spieler ein bestimmtes Prefix tragen darf (Permission-Check).
     */
    public CompletableFuture<Boolean> hasUnlockedPrefix(UUID uuid, String prefixId) {
        return CompletableFuture.supplyAsync(() -> {
            if (uuid == null || prefixId == null) return false;

            Player player = Bukkit.getServer().getPlayer(uuid);
            if (player != null) {
                File file = new File(plugin.getDataFolder(), "prefixes.yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String permission = config.getString("prefixes." + prefixId + ".permission");

                return permission == null || player.hasPermission(permission);
            }
            return false;
        });
    }
}