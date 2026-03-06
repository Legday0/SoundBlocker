package me.soundblocker.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.MinecraftKey;
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
        SoundBlocker pl = this.plugin;

        protocolManager.addPacketListener(new PacketAdapter(
                pl,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_SOUND_EFFECT,
                PacketType.Play.Server.ENTITY_SOUND
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    String soundKey = getSoundKey(event);
                    if (soundKey == null) return;

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
                        Location loc = player.getLocation();
                        player.playSound(loc, rep.getSound(), rep.getVolume(), rep.getPitch());
                    }

                } catch (Exception e) {
                    // تجاهل أخطاء الباكيت
                }
            }
        });
    }

    private String getSoundKey(PacketEvent event) {
        try {
            MinecraftKey key = event.getPacket().getMinecraftKeys().read(0);
            if (key != null) return key.getKey();
        } catch (Exception e) {
            try {
                return event.getPacket().getStrings().read(0);
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void unregister() {
        protocolManager.removePacketListeners(plugin);
    }
}
