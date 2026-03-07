package me.soundblocker.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.Sound;

public class SoundListener {

    private final SoundBlocker plugin;
    private final ProtocolManager protocolManager;

    public SoundListener(SoundBlocker plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();
    }

    private void registerPacketListener() {
        final SoundBlocker pl = this.plugin;

        protocolManager.addPacketListener(new PacketAdapter(
                pl,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_SOUND_EFFECT,
                PacketType.Play.Server.ENTITY_SOUND
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
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
        });
    }

    private String getSoundKey(PacketContainer packet) {
        // الطريقة الصح لـ 1.21.11 - نقرأ الـ SoundEffect object مباشرة
        try {
            // الـ field الأول في الـ packet هو الـ sound
            Object soundField = packet.getModifier().readSafely(0);
            if (soundField != null) {
                String raw = soundField.toString();
                // raw بيبقى: Reference{ResourceKey[minecraft:sound_event / minecraft:entity.sheep.step]=...}
                // نعمل extract للجزء بعد آخر /
                if (raw.contains("/ minecraft:")) {
                    int start = raw.lastIndexOf("/ minecraft:") + "/ minecraft:".length();
                    int end = raw.indexOf("]", start);
                    if (end > start) {
                        return raw.substring(start, end).trim();
                    }
                }
                // لو مش فيه / جرب نقرأه مباشرة
                if (raw.contains("minecraft:")) {
                    int start = raw.lastIndexOf("minecraft:") + "minecraft:".length();
                    int end = raw.indexOf(",", start);
                    if (end == -1) end = raw.indexOf("]", start);
                    if (end == -1) end = raw.indexOf("}", start);
                    if (end == -1) end = raw.indexOf(" ", start);
                    if (end > start) {
                        return raw.substring(start, end).trim();
                    }
                }
            }
        } catch (Exception ignored) {}

        // fallback: MinecraftKeys
        try {
            var key = packet.getMinecraftKeys().readSafely(0);
            if (key != null) return key.getKey();
        } catch (Exception ignored) {}

        return null;
    }

    public void unregister() {
        protocolManager.removePacketListeners(plugin);
    }
}
