package me.soundblocker.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
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

                // debug: طباعة اسم الصوت في الـ console
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
        // محاولة 1: SoundEffects (الطريقة الصح لـ 1.21+)
        try {
            var soundEffects = packet.getSoundEffects();
            if (soundEffects != null && soundEffects.size() > 0) {
                var sound = soundEffects.readSafely(0);
                if (sound != null) {
                    return sound.getKey().getKey();
                }
            }
        } catch (Exception ignored) {}

        // محاولة 2: MinecraftKeys
        try {
            var keys = packet.getMinecraftKeys();
            if (keys != null && keys.size() > 0) {
                var key = keys.readSafely(0);
                if (key != null) return key.getKey();
            }
        } catch (Exception ignored) {}

        // محاولة 3: Strings (طريقة قديمة)
        try {
            var strings = packet.getStrings();
            if (strings != null && strings.size() > 0) {
                String val = strings.readSafely(0);
                if (val != null) return val;
            }
        } catch (Exception ignored) {}

        return null;
    }

    public void unregister() {
        protocolManager.removePacketListeners(plugin);
    }
}
