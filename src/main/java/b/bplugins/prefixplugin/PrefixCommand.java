package b.bplugins.prefixplugin;

import b.bplugins.prefixplugin.utils.MessageUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class PrefixCommand {

    public static LiteralCommandNode<CommandSourceStack> createNode(Main plugin) {
        return Commands.literal("prefix")
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    if (sender instanceof Player player) {
                        // Prüfen ob Bedrock (Geyser / Floodgate) aktiv ist
                        try {
                            if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("floodgate")
                                    && org.geysermc.floodgate.api.FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                                new PrefixBedrockForm(plugin).open(player);
                                return Command.SINGLE_SUCCESS;
                            }
                        } catch (Exception ignored) {}

                        // Ansonsten das normale Java-Inventar öffnen
                        new PrefixMenu(plugin).open(player);
                    } else {
                        sender.sendMessage("§cNur für Spieler!");
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("bprefix.admin"))
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            String lang = plugin.getConfig().getString("language", "de");
                            MessageUtils.load(lang);
                            plugin.reloadConfig();

                            if (sender instanceof Player player) {
                                player.sendMessage(MessageUtils.getMessage("prefix-reload-success"));
                            } else {
                                sender.sendMessage("§a[BPREFIX] Konfigurationen wurden erfolgreich neu geladen!");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("create")
                        .requires(source -> source.getSender().hasPermission("bprefix.command.createprefix"))
                        .then(Commands.argument("id", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("color", StringArgumentType.string())
                                                .then(Commands.argument("permission", StringArgumentType.word())
                                                        .executes(ctx -> {
                                                            CommandSender sender = ctx.getSource().getSender();
                                                            if (!(sender instanceof Player player)) {
                                                                sender.sendMessage("§cNur für Spieler!");
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                                                            if (itemInHand.getType() == Material.AIR) {
                                                                player.sendMessage(MessageUtils.getMessage("no-item-in-hand"));
                                                                return Command.SINGLE_SUCCESS;
                                                            }

                                                            String id = StringArgumentType.getString(ctx, "id").toLowerCase();
                                                            String displayName = StringArgumentType.getString(ctx, "name");
                                                            String colorCode = StringArgumentType.getString(ctx, "color");
                                                            String permission = StringArgumentType.getString(ctx, "permission");

                                                            saveToPrefixConfig(plugin, id, displayName, colorCode, permission, itemInHand);
                                                            player.sendMessage(MessageUtils.getMessage("prefix-created", "%name%", displayName));
                                                            return Command.SINGLE_SUCCESS;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .build();
    }

    private static void saveToPrefixConfig(Main plugin, String id, String name, String color, String perm, ItemStack item) {
        File file = new File(plugin.getDataFolder(), "prefixes.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = "prefixes." + id + ".";
        config.set(path + "display_name", name);
        config.set(path + "color_code", color);
        config.set(path + "permission", perm);
        config.set(path + "material", item.getType().name());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasCustomModelData()) {
                config.set(path + "custom_model_data", meta.hasCustomModelData() ? meta.getCustomModelData() : null);
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Konnte prefixes.yml nicht speichern!", e);
        }
    }
}