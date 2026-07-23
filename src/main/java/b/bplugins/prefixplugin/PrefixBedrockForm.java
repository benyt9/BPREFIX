package b.bplugins.prefixplugin;

import b.bplugins.prefixplugin.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.geysermc.cumulus.form.SimpleForm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrefixBedrockForm {

    private final Main plugin;

    public PrefixBedrockForm(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        if (!BukkitIsFloodgateAvailable()) {
            new PrefixMenu(plugin).open(player);
            return;
        }

        FloodgatePlayer fPlayer = FloodgateApi.getInstance().getPlayer(player.getUniqueId());
        if (fPlayer == null) {
            new PrefixMenu(plugin).open(player);
            return;
        }

        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("prefixes");

        SimpleForm.Builder formBuilder = SimpleForm.builder()
                .title("Prefix Auswahl")
                .content("Wähle deinen gewünschten Prefix aus:")
                .button("§c✘ Prefix entfernen");

        List<String> prefixKeys = new ArrayList<>();

        if (section != null) {
            for (String key : section.getKeys(false)) {
                String permission = section.getString(key + ".permission");
                if (permission == null || player.hasPermission(permission)) {
                    prefixKeys.add(key);
                    String displayName = section.getString(key + ".display_name", key);
                    formBuilder.button(displayName.replaceAll("<[^>]*>", ""));
                }
            }
        }

        formBuilder.validResultHandler((form, response) -> {
            int clickedButton = response.clickedButtonId();
            UUID targetUuid = Main.getPlayerUuid(player); // Universelle UUID nutzen

            if (clickedButton == 0) {
                Main.playerColors.put(targetUuid, "");
                player.sendMessage(MessageUtils.getMessage("prefix-reset"));
                return;
            }

            int prefixIndex = clickedButton - 1;
            if (prefixIndex >= 0 && prefixIndex < prefixKeys.size()) {
                String key = prefixKeys.get(prefixIndex);
                String colorCode = section.getString(key + ".color_code");
                Main.playerColors.put(targetUuid, colorCode);

                String rawNameFromConfig = section.getString(key + ".display_name");
                player.sendMessage(MessageUtils.getMessage("prefix-selected", "%name%", rawNameFromConfig));
            }
        });

        fPlayer.sendForm(formBuilder.build());
    }

    private boolean BukkitIsFloodgateAvailable() {
        try {
            return org.bukkit.Bukkit.getPluginManager().isPluginEnabled("floodgate")
                    && FloodgateApi.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }
}