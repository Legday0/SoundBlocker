package me.soundblocker.plugin;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private SoundManager soundManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        soundManager = new SoundManager(this);

        PacketEvents.getAPI().getEventManager()
            .registerListener(new SoundPacketListener(this));

        SoundCommand cmd = new SoundCommand(this);
        getCommand("sb").setExecutor(cmd);
        getCommand("sb").setTabCompleter(cmd);

        getLogger().info("SoundBlocker شغال! محجوب=" + soundManager.getBlocked().size()
            + " متبدل=" + soundManager.getReplaced().size());
    }

    @Override
    public void onDisable() {
        getLogger().info("SoundBlocker وقف.");
    }

    public SoundManager getSoundManager() { return soundManager; }
}
