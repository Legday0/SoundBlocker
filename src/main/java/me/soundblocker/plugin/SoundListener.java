package me.soundblocker.plugin;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySoundEvent;

public class SoundListener implements Listener {

    private final SoundBlocker plugin;

    public SoundListener(SoundBlocker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySound(EntitySoundEvent event) {
        String soundKey = event.getSound().getKey().getKey();

        // 1. Check if blocked
        if (plugin.isSoundBlocked(soundKey)) {
            event.setCancelled(true);
            return;
        }

        // 2. Check if replaced
        SoundReplacement rep = plugin.getReplacement(soundKey);
        if (rep != null) {
            event.setCancelled(true);

            // Play the replacement sound at same location
            Location loc = event.getEntity().getLocation();
            String newSound = rep.getSound();

            try {
                // Try to play as Bukkit Sound enum first
                Sound bukkitSound = Sound.valueOf(
                    newSound.toUpperCase().replace(".", "_").replace(":", "_")
                );
                loc.getWorld().playSound(loc, bukkitSound, rep.getVolume(), rep.getPitch());
            } catch (IllegalArgumentException e) {
                // Fallback: play as raw namespaced key string
                loc.getWorld().playSound(loc, newSound, rep.getVolume(), rep.getPitch());
            }
        }
    }
}
