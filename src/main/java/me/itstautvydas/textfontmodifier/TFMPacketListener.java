package me.itstautvydas.textfontmodifier;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

@SuppressWarnings("JavaReflectionMemberAccess")
@RequiredArgsConstructor
@Getter
public class TFMPacketListener implements PacketListener {

    static Class<?> updateOperationClass, addOperationClass;
    static Field playerPrefixField;
    static Constructor<?> addOperationConstructorClass;

    static {
        try {
            updateOperationClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$e");
            addOperationClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$a");
            addOperationConstructorClass = addOperationClass.getDeclaredConstructor(BossEvent.class);
            addOperationConstructorClass.setAccessible(true);
            playerPrefixField = ClientboundSetPlayerTeamPacket.Parameters.class.getDeclaredField("b");
            playerPrefixField.setAccessible(true);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

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

                var currentOperation = packet.getModifier().read(1);
                var currentOperationClass = currentOperation.getClass();

                if (updateOperationClass != currentOperationClass && addOperationClass != currentOperationClass)
                    return;

                var currentOperationClassField = currentOperation.getClass().getDeclaredFields()[0];
                currentOperationClassField.setAccessible(true);

                var currentComponent = (Component) currentOperationClassField.get(currentOperation);

                var json = Component.Serializer.toJsonTree(currentComponent);
                plugin.getTextProcessor().modifyFontJson(json, null);
                Component fixedComponent = Component.Serializer.fromJson(json);

                if (currentOperationClass == addOperationClass) {
                    var boss = new ServerBossEvent(fixedComponent, null, null);
                    var bossFields = boss.getClass().getSuperclass().getDeclaredFields();
                    UUID uuid = (UUID) packet.getModifier().read(0);
                    bossFields[0].setAccessible(true);
                    bossFields[0].set(boss, uuid);

                    for (int i = 1; i < addOperationClass.getDeclaredFields().length; i++) {
                        var field = addOperationClass.getDeclaredFields()[i];
                        field.setAccessible(true);
                        var bossField = bossFields[i + 1];
                        bossField.setAccessible(true);
                        bossField.set(boss, field.get(currentOperation));
                    }

                    var addOperator = addOperationConstructorClass.newInstance(boss);
                    packet.getModifier().write(1, addOperator);
                } else {
                    var updateOperationConstructorClass = updateOperationClass.getDeclaredConstructor(Component.class);
                    updateOperationConstructorClass.setAccessible(true);
                    var updateOperation = updateOperationConstructorClass.newInstance(fixedComponent);
                    packet.getModifier().write(1, updateOperation);
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
                    var optional = packet.getOptionalStructures().read(0);
                    if (optional != null && optional.isPresent()) {
                        var params = (ClientboundSetPlayerTeamPacket.Parameters) optional.get().getHandle();
                        var component = params.getPlayerPrefix();
                        var json = Component.Serializer.toJsonTree(component);
                        plugin.getTextProcessor().modifyFontJson(json, plugin.getTextProcessor().getSpecialSymbolForScoreboard());
                        var fixedComponent = Component.Serializer.fromJson(json);
                        playerPrefixField.set(params, fixedComponent);
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
