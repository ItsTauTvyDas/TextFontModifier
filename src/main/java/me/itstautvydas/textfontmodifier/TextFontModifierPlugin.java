package me.itstautvydas.textfontmodifier;

import com.comphenix.protocol.ProtocolLibrary;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class TextFontModifierPlugin extends JavaPlugin {

    private TextProcessor textProcessor;

    @Override
    public void onEnable() {
        setupConfiguration();
        textProcessor = new TextProcessor(this);
        ProtocolLibrary.getProtocolManager().addPacketListener(new TFMPacketListener(this));
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
