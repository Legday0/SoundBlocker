package me.soundblocker.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

public class SoundListener extends PacketAdapter {

    public SoundListener(SoundBlocker plugin) {
        super(plugin, ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_SOUND_EFFECT,
                PacketType.Play.Server.ENTITY_SOUND);
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        try {
            SoundBlocker pl = SoundBlocker.getInstance();
            String soundKey = getSoundKey(event.getPacket());
            if (soundKey == null || soundKey.isEmpty()) return;

            // 1. حجب
            if (pl.isSoundBlocked(soundKey)) {
                event.setCancelled(true);
                return;
            }

            // 2. تبديل
            SoundReplacement rep = pl.getReplacement(soundKey);
            if (rep != null) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.playSound(player.getLocation(), rep.getSound(), rep.getVolume(), rep.getPitch());
            }
        } catch (Exception ignored) {}
    }

    private String getSoundKey(PacketContainer packet) {
        try {
            Object soundField = packet.getModifier().readSafely(0);
            if (soundField != null) {
                String raw = soundField.toString();
                if (raw.contains("/ minecraft:")) {
                    int start = raw.lastIndexOf("/ minecraft:") + "/ minecraft:".length();
                    int end = raw.indexOf("]", start);
                    if (end > start) return raw.substring(start, end).trim();
                }
                if (raw.contains("minecraft:")) {
                    int start = raw.lastIndexOf("minecraft:") + "minecraft:".length();
                    int end = raw.indexOf(",", start);
                    if (end == -1) end = raw.indexOf("]", start);
                    if (end == -1) end = raw.indexOf("}", start);
                    if (end == -1) end = raw.indexOf(" ", start);
                    if (end > start) return raw.substring(start, end).trim();
                }
            }
        } catch (Exception ignored) {}
        try {
            var key = packet.getMinecraftKeys().readSafely(0);
            if (key != null) return key.getKey();
        } catch (Exception ignored) {}
        return null;
    }

    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(SoundBlocker.getInstance());
    }
}
