package me.soundblocker.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

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
                String soundKey = getSoundKey(event.getPacket());
                if (soundKey == null || soundKey.isEmpty()) return;

                pl.getLogger().info("[Debug] Sound: " + soundKey);

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
            }
        });
    }

    private String getSoundKey(PacketContainer packet) {
        // الطريقة الصح لـ Paper 1.21.11
        // الـ packet بيجي: ResourceKey[minecraft:sound_event / minecraft:entity.cow.ambient]
        // محتاجين نقرأ الـ toString ونعمل له parse

        try {
            // اقرأ الـ packet كـ string كامل وعمل extract للاسم
            String raw = packet.toString();
            // بيبقى جوه: minecraft:entity.cow.ambient
            if (raw.contains("minecraft:sound_event / minecraft:")) {
                int start = raw.indexOf("minecraft:sound_event / minecraft:") 
                            + "minecraft:sound_event / minecraft:".length();
                int end = raw.indexOf("]", start);
                if (end == -1) end = raw.indexOf("}", start);
                if (end == -1) end = raw.indexOf(",", start);
                if (start > 0 && end > start) {
                    return raw.substring(start, end).trim();
                }
            }
        } catch (Exception ignored) {}

        // محاولة 2: SoundEffects
        try {
            var soundEffects = packet.getSoundEffects();
            if (soundEffects != null && soundEffects.size() > 0) {
                var sound = soundEffects.readSafely(0);
                if (sound != null) return sound.getKey().getKey();
            }
        } catch (Exception ignored) {}

        // محاولة 3: MinecraftKeys
        try {
            var keys = packet.getMinecraftKeys();
            if (keys != null && keys.size() > 0) {
                var key = keys.readSafely(0);
                if (key != null) return key.getKey();
            }
        } catch (Exception ignored) {}

        return null;
    }

    public void unregister() {
        protocolManager.removePacketListeners(plugin);
    }
}
