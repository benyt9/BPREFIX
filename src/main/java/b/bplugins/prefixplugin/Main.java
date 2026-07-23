package b.bplugins.prefixplugin;

import b.bplugins.prefixplugin.utils.MessageUtils;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Main extends JavaPlugin {

    // Fix: ConcurrentHashMap statt HashMap. Das Plugin deklariert folia-supported: true
    // in der paper-plugin.yml, d.h. mehrere Region-Threads können gleichzeitig auf diese
    // Map zugreifen. Eine normale HashMap ist dabei nicht thread-safe (ConcurrentModificationException /
    // Datenverlust möglich).
    public static ConcurrentHashMap<UUID, String> playerColors = new ConcurrentHashMap<>();
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        saveDefaultConfig();
        String lang = getConfig().getString("language", "de");
        MessageUtils.load(lang);

        File prefixesFile = new File(getDataFolder(), "prefixes.yml");
        if (!prefixesFile.exists()) {
            try { prefixesFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }

        loadPlayerData();

        // Paper-Native Befehlsregistrierung über den LifecycleManager
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(
                    PrefixCommand.createNode(this),
                    "Hauptbefehl für das BPREFIX System.",
                    List.of("bp")
            );
        });

        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // PlaceholderAPI absolut sicher einbinden, ohne Classloader-Crash
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    new PrefixPlaceholder(this).register();
                    getLogger().info("PlaceholderAPI Support erfolgreich aktiviert!");
                }
            } catch (NoClassDefFoundError e) {
                getLogger().warning("PlaceholderAPI Klassen konnten nicht geladen werden.");
            }
        }

        getLogger().info("BPREFIX (Paper-Native) wurde erfolgreich aktiviert!");
    }

    @Override
    public void onDisable() {
        savePlayerData();
    }

    public static Main getInstance() {
        return instance;
    }

    private void savePlayerData() {
        File file = new File(getDataFolder(), "data.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("users", null);
        for (UUID uuid : playerColors.keySet()) {
            config.set("users." + uuid.toString(), playerColors.get(uuid));
        }
        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadPlayerData() {
        File file = new File(getDataFolder(), "data.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("users") == null) return;
        for (String uuidString : config.getConfigurationSection("users").getKeys(false)) {
            playerColors.put(UUID.fromString(uuidString), config.getString("users." + uuidString));
        }
    }
}