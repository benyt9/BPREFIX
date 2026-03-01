package b.bplugins.prefixplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin {

    public static HashMap<UUID, String> playerColors = new HashMap<>();
    private static MessageManager messageManager;
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        if (!getDataFolder().exists()) getDataFolder().mkdirs();

        messageManager = new MessageManager(this);

        File prefixesFile = new File(getDataFolder(), "prefixes.yml");
        if (!prefixesFile.exists()) {
            try { prefixesFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }

        loadPlayerData();

        if (getCommand("prefix") != null) {
            getCommand("prefix").setExecutor(new PrefixCommand(this));
        }
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PrefixPlaceholder(this).register();
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

    public static MessageManager getMessages() {
        return messageManager;
    }

    public void send(Player player, String path, String... replacements) {
        player.sendMessage(messageManager.getComponent(path, replacements));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("prefix")) {
            // Check ob überhaupt Argumente da sind, um Errors zu vermeiden
            if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("bprefix.admin")) {
                    if (sender instanceof Player player) {
                        send(player, "no-permission");
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte!");
                    }
                    return true;
                }

                messageManager.reloadMessages();

                if (sender instanceof Player player) {
                    send(player, "prefix-reload-success");
                } else {
                    sender.sendMessage("§a[BPREFIX] Konfigurationen neu geladen!");
                }
                return true;
            }
        }
        return false;
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