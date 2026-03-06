package me.soundblocker.plugin;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.List;
import java.util.Map;

public class SoundListener implements Listener {

    private final SoundBlocker plugin;

    public SoundListener(SoundBlocker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        applyEntitySoundRules(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        applyEntitySoundRules(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            applyEntitySoundRules(entity);
        }
    }

    private void applyEntitySoundRules(Entity entity) {
        if (!(entity instanceof LivingEntity)) return;

        String entityType = entity.getType().getKey().getKey();

        List<String> blocked = plugin.getBlockedSounds();
        Map<String, SoundReplacement> replaced = plugin.getReplacedSounds();

        for (String sound : blocked) {
            if (sound.contains(entityType)) {
                entity.setSilent(true);
                return;
            }
        }

        for (Map.Entry<String, SoundReplacement> entry : replaced.entrySet()) {
            if (entry.getKey().contains(entityType)) {
                entity.setSilent(true);
                scheduleReplacementSound(entity, entry.getValue());
                return;
            }
        }
    }

    private void scheduleReplacementSound(Entity entity, SoundReplacement rep) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!entity.isValid() || entity.isDead()) return;
            if (Math.random() < 0.01) {
                Location loc = entity.getLocation();
                try {
                    Sound sound = Sound.valueOf(
                        rep.getSound().toUpperCase().replace(".", "_").replace(":", "_")
                    );
                    loc.getWorld().playSound(loc, sound, rep.getVolume(), rep.getPitch());
                } catch (IllegalArgumentException e) {
                    loc.getWorld().playSound(loc, rep.getSound(), rep.getVolume(), rep.getPitch());
                }
            }
        }, 20L, 1L);
    }
}
