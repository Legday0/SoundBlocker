package me.soundblocker.plugin;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.bukkit.entity.Player;

public class SoundListener extends PacketListenerAbstract {

    private final SoundBlocker plugin;

    public SoundListener(SoundBlocker plugin) {
        super(PacketListenerPriority.HIGHEST);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        String soundKey = null;

        try {
            if (event.getPacketType() == PacketType.Play.Server.SOUND_EFFECT) {
                WrapperPlayServerSoundEffect wrapper = new WrapperPlayServerSoundEffect(event);
                soundKey = wrapper.getSoundId().getKey().getKey();

            } else if (event.getPacketType() == PacketType.Play.Server.ENTITY_SOUND_EFFECT) {
                WrapperPlayServerEntitySoundEffect wrapper = new WrapperPlayServerEntitySoundEffect(event);
                soundKey = wrapper.getSoundId().getKey().getKey();
            }
        } catch (Exception ignored) {}

        if (soundKey == null || soundKey.isEmpty()) return;

        // 1. حجب
        if (plugin.isSoundBlocked(soundKey)) {
            event.setCancelled(true);
            return;
        }

        // 2. تبديل
        SoundReplacement rep = plugin.getReplacement(soundKey);
        if (rep != null) {
            event.setCancelled(true);
            if (event.getPlayer() instanceof Player player) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    player.playSound(player.getLocation(), rep.getSound(), rep.getVolume(), rep.getPitch())
                );
            }
        }
    }
}
