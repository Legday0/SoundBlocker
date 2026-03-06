package me.soundblocker.plugin;

import com.destroystokyo.paper.event.entity.EntitySoundEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
            Location loc = event.getEntity().getLocation();
            loc.getWorld().playSound(loc, rep.getSound(), rep.getVolume(), rep.getPitch());
        }
    }
}
