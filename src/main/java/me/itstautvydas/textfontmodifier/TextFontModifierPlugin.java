package me.itstautvydas.textfontmodifier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Getter
public final class TextFontModifierPlugin extends JavaPlugin implements CommandExecutor {

    private TextProcessor textProcessor;

    @Override
    public void onEnable() {
        migrateConfiguration();
        setupConfiguration();
        textProcessor = new TextProcessor(this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new TFMPacketListener(
                this, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_ACTION_BAR_TEXT,
                PacketType.Play.Server.BOSS,
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_TEAM));
        Objects.requireNonNull(this.getCommand("textfontmodifier")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        var commandObj = Objects.requireNonNull(this.getCommand("textfontmodifier"));
        if (commandObj.getPermission() != null && sender.hasPermission(commandObj.getPermission())) {
            textProcessor.reload();
            sender.sendMessage(NamedTextColor.GREEN + "TextFontModifier plugin has been reloaded!");
            return true;
        }
        return false;
    }

    private void noKeyFound(String ...keys) {
        getLogger().warning("Configuration migration: Couldn't find configuration key(s) %s, skipping...".formatted(String.join(", ", keys)));
    }

    private void migrateConfiguration() {
        getLogger().info("Checking for configuration migration...");
        var config = getConfig();
        if (config.getKeys(false).isEmpty()) {
            getLogger().info("Empty configuration, creating...");
            return;
        }
        if (config.isInt("config-version")) {
            getLogger().info("Configuration key config-version exists, assuming there's no need for migration :)");
            return;
        }

        if (config.isString("font") && config.isString("special-symbol-for-scoreboards")) {
            config.set("fonts.default-font.name", config.get("font"));
            config.set("fonts.default-font.special-symbol", config.get("special-symbol-for-scoreboards"));
        } else noKeyFound("font", "special-symbol-for-scoreboards");
        var regex = config.get("regex");
        config.set("regex", null);
        if (regex != null && config.isBoolean("invert-regex")) {
            config.set("regex.value", regex);
            config.set("regex.invert", config.get("invert-regex"));
        } else noKeyFound("font", "invert-regex");
        config.set("packets", null);
        config.set("invert-regex", null);
        config.set("font", null);
        config.set("special-symbol-for-scoreboards", null);
        config.set("config-version", 1);
        getLogger().info("Successfully migrated to a new configuration!");
        saveConfig();
    }

    private void setupConfiguration() {
        var config = getConfig();
        if (!config.isConfigurationSection("fonts")) {
            config.addDefault("fonts.default-font.name", "namespace:key");
            config.addDefault("fonts.default-font.special-symbol", "$u");
        }
        config.addDefault("regex.value", "");
        config.addDefault("regex.invert", false);
        for (var key : List.of("boss-bar", "action-bar", "scoreboard-title", "scoreboard-scores")) {
            config.addDefault("packets.%s.enable".formatted(key), true);
            config.addDefault("packets.%s.forced-font".formatted(key), key.equals("scoreboard-scores") ? "" : "default-font");
        }
        config.addDefault("config-version", 1);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
