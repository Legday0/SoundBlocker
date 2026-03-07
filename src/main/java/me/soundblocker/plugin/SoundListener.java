package me.soundblocker.plugin;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.bukkit.entity.Player;

public class SoundListener implements PacketListener {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        SoundBlocker pl = SoundBlocker.getInstance();
        if (pl == null) return;

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
        if (pl.isSoundBlocked(soundKey)) {
            event.setCancelled(true);
            return;
        }

        // 2. تبديل
        SoundReplacement rep = pl.getReplacement(soundKey);
        if (rep != null) {
            event.setCancelled(true);
            if (event.getPlayer() instanceof Player player) {
                final String repSound = rep.getSound();
                final float repVol = rep.getVolume();
                final float repPitch = rep.getPitch();
                pl.getServer().getScheduler().runTask(pl, () ->
                    player.playSound(player.getLocation(), repSound, repVol, repPitch)
                );
            }
        }
    }
}
