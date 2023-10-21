package me.itstautvydas.textfontmodifier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

@Getter
public class TFMPacketListener extends PacketAdapter {

    private final Gson gson = new Gson();
    private final GsonComponentSerializer componentSerializer = GsonComponentSerializer.builder().build();

    public TFMPacketListener(Plugin plugin, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        var packet = packetEvent.getPacket();
        var type = packet.getType();
        var plugin = (TextFontModifierPlugin) getPlugin();

        try {
            if (type == PacketType.Play.Server.SET_ACTION_BAR_TEXT) {
                if (!plugin.getConfig().getBoolean("packets.action-bar.enable"))
                    return;
                var component = packet.getChatComponents().read(0);
                var json = gson.fromJson(component.getJson(), JsonObject.class);
                plugin.getTextProcessor().modifyFontJson("action-bar", json);
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(gson.toJson(json)));
            } else if (type == PacketType.Play.Server.BOSS) {
                if (!plugin.getConfig().getBoolean("packets.boss-bar.enable"))
                    return;
                var struct = packet.getStructures().read(1);
                if (struct.getChatComponents().size() > 0) {
                    WrappedChatComponent prefix = struct.getChatComponents().read(0);
                    var json = gson.fromJson(prefix.getJson(), JsonObject.class);
                    plugin.getTextProcessor().modifyFontJson("boss-bar", json);
                    struct.getChatComponents().write(0, WrappedChatComponent.fromJson(gson.toJson(json)));
                    packet.getStructures().write(1, struct);
                }
            } else if (type == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
                if (!plugin.getConfig().getBoolean("packets.scoreboard-title.enable"))
                    return;
                var component = packet.getChatComponents().read(0);
                var json = gson.fromJson(component.getJson(), JsonObject.class);
                plugin.getTextProcessor().modifyFontJson("scoreboard-title", json);
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(gson.toJson(json)));
            } else if (type == PacketType.Play.Server.SCOREBOARD_TEAM) {
                if (!plugin.getConfig().getBoolean("packets.scoreboard-scores.enable"))
                    return;
                // Don't process useless scoreboard teams
                if (packet.getStrings().read(0).contains("CIT"))
                    return;
                try {
                    var structOptional = packet.getOptionalStructures().readSafely(0);
                    if (structOptional != null && structOptional.isPresent()) {
                        var struct = structOptional.get();
                        WrappedChatComponent prefix = struct.getChatComponents().read(1);
                        var json = gson.fromJson(prefix.getJson(), JsonObject.class);
                        plugin.getTextProcessor().modifyFontJson("scoreboard-scores", json);
                        struct.getChatComponents().write(1, WrappedChatComponent.fromJson(gson.toJson(json)));
                        packet.getOptionalStructures().write(0, Optional.of(struct));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
