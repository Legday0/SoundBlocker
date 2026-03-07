package me.soundblocker.plugin;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.bukkit.entity.Player;

public class SoundPacketListener extends PacketListenerAbstract {

    private final Main plugin;

    public SoundPacketListener(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon type = event.getPacketType();
        String key = null;

        try {
            if (type == PacketType.Play.Server.SOUND_EFFECT) {
                key = new WrapperPlayServerSoundEffect(event)
                        .getSoundId().getKey().getKey();

            } else if (type == PacketType.Play.Server.ENTITY_SOUND_EFFECT) {
                key = new WrapperPlayServerEntitySoundEffect(event)
                        .getSoundId().getKey().getKey();
            }
        } catch (Exception ignored) { return; }

        if (key == null || key.isEmpty()) return;

        SoundManager mgr = plugin.getSoundManager();

        if (mgr.isBlocked(key)) {
            event.setCancelled(true);
            return;
        }

        SoundData rep = mgr.getReplacement(key);
        if (rep != null && event.getPlayer() instanceof Player player) {
            event.setCancelled(true);
            final String rs = rep.getSound();
            final float  rv = rep.getVolume();
            final float  rp = rep.getPitch();
            plugin.getServer().getScheduler().runTask(plugin, () ->
                player.playSound(player.getLocation(), rs, rv, rp)
            );
        }
    }
}
