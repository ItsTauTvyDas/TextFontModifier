package me.itstautvydas.textfontmodifier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TextFontModifierPlugin extends JavaPlugin {

    private TextProcessor textProcessor;

    @Override
    public void onEnable() {
        setupConfiguration();
        textProcessor = new TextProcessor(this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new TFMPacketListener(
                this, ListenerPriority.NORMAL,
                PacketType.Play.Server.SET_ACTION_BAR_TEXT,
                PacketType.Play.Server.BOSS,
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_TEAM));
    }

    private void setupConfiguration() {
        getConfig().addDefault("font", "namespace:key");
        getConfig().addDefault("regex", "[\\p{Print}&&[^~,],]+");
        getConfig().addDefault("invert-regex", false);
        getConfig().addDefault("packets.boss-bar", true);
        getConfig().addDefault("packets.action-bar", true);
        getConfig().addDefault("packets.scoreboard-title", true);
        getConfig().addDefault("packets.scoreboard-scores", true);
        getConfig().addDefault("special-symbol-for-scoreboards", "$u");
        getConfig().options().copyDefaults(true);
        saveConfig();
    }
}
