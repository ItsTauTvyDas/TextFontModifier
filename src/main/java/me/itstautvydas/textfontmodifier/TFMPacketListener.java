package me.itstautvydas.textfontmodifier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.Optional;

@SuppressWarnings("JavaReflectionMemberAccess")
@RequiredArgsConstructor
@Getter
public class TFMPacketListener implements PacketListener {

    private final TextFontModifierPlugin plugin;
    private final GsonComponentSerializer componentSerializer = GsonComponentSerializer.builder().build();;

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        var packet = packetEvent.getPacket();
        var type = packet.getType();

        try {
            if (type == PacketType.Play.Server.SET_ACTION_BAR_TEXT) {
                if (!plugin.getConfig().getBoolean("packets.action-bar"))
                    return;
                var component = (net.kyori.adventure.text.Component) packet.getModifier().read(1);
                var json = componentSerializer.serializeToTree(component);
                plugin.getTextProcessor().modifyFontJson(json, null);
                packet.getModifier().write(1, componentSerializer.deserializeFromTree(json));
            } else if (type == PacketType.Play.Server.BOSS) {
                if (!plugin.getConfig().getBoolean("packets.boss-bar"))
                    return;

                var struct = packet.getStructures().read(0);
                if (struct.getChatComponents().size() >= 1) {
                    WrappedChatComponent prefix = struct.getChatComponents().read(0);
                    var json = new Gson().fromJson(prefix.getJson(), JsonElement.class);
                    plugin.getTextProcessor().modifyFontJson(json, plugin.getTextProcessor().getSpecialSymbolForScoreboard());
                    struct.getChatComponents().write(1, WrappedChatComponent.fromJson(json.getAsString()));
                    packet.getStructures().write(0, struct);
                }
            } else if (type == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
                if (!plugin.getConfig().getBoolean("packets.scoreboard-title"))
                    return;
                var component = packet.getChatComponents().read(0);
                var json = plugin.getTextProcessor().modifyFontString(component.getJson());
                packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
            } else if (type == PacketType.Play.Server.SCOREBOARD_TEAM) {
                if (!plugin.getConfig().getBoolean("packets.scoreboard-scores"))
                    return;

                // Don't process useless scoreboard teams
                if (packet.getStrings().read(0).contains("CIT"))
                    return;

                try {
                    var structOptional = packet.getOptionalStructures().readSafely(0);
                    if (structOptional != null && structOptional.isPresent()) {
                        var struct = structOptional.get();
                        WrappedChatComponent prefix = struct.getChatComponents().read(1);
                        var json = new Gson().fromJson(prefix.getJson(), JsonElement.class);
                        plugin.getTextProcessor().modifyFontJson(json, plugin.getTextProcessor().getSpecialSymbolForScoreboard());
                        struct.getChatComponents().write(1, WrappedChatComponent.fromJson(json.getAsString()));
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

    @Override
    public void onPacketReceiving(PacketEvent packetEvent) {}

    @Override
    public ListeningWhitelist getSendingWhitelist() {
        return ListeningWhitelist.newBuilder().highest().types(
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_TEAM,
                PacketType.Play.Server.SET_ACTION_BAR_TEXT,
                PacketType.Play.Server.BOSS
        ).build();
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist() {
        return null;
    }
}
