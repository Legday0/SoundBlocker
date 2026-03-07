package me.soundblocker.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List; // حل مشكلة cannot find symbol: class List
import java.util.Map;
import java.util.Set;

public class SoundBlocker extends JavaPlugin {

    private static SoundBlocker instance;
    private Set<String> blockedSounds; 
    private Map<String, SoundReplacement> replacedSounds;
    private SoundListener soundListener;
    private boolean debugMode;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadSoundData();

        soundListener = new SoundListener(this);

        SoundCommand cmd = new SoundCommand(this);
        getCommand("sb").setExecutor(cmd);
        getCommand("sb").setTabCompleter(cmd);

        getLogger().info("====================================");
        getLogger().info("  SoundBlocker v3.5 - نظام السيطرة الشاملة");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        if (soundListener != null) soundListener.unregister();
    }

    public void loadSoundData() {
        reloadConfig();
        debugMode = getConfig().getBoolean("debug", false);

        blockedSounds = new LinkedHashSet<>();
        List<String> rawList = getConfig().getStringList("blocked-sounds");
        if (rawList != null) {
            for (String s : rawList) {
                blockedSounds.add(clean(s));
            }
        }

        replacedSounds = new LinkedHashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("replaced-sounds");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String cleanKey = clean(key);
                String sound = section.getString(key + ".sound", key);
                double volume = section.getDouble(key + ".volume", 1.0);
                double pitch = section.getDouble(key + ".pitch", 1.0);
                replacedSounds.put(cleanKey, new SoundReplacement(sound, (float) volume, (float) pitch));
            }
        }
    }

    // حل مشكلة incompatible types: Set cannot be converted to List
    public List<String> getBlockedSounds() { 
        return new ArrayList<>(blockedSounds); 
    }

    public boolean isSoundBlocked(String sound) {
        return blockedSounds.contains(clean(sound));
    }

    public SoundReplacement getReplacement(String sound) {
        return replacedSounds.get(clean(sound));
    }

    public boolean isDebug() { return debugMode; }

    public boolean blockSound(String sound) {
        String c = clean(sound);
        if (blockedSounds.add(c)) {
            getConfig().set("blocked-sounds", new ArrayList<>(blockedSounds));
            saveConfig();
            return true;
        }
        return false;
    }

    public boolean unblockSound(String sound) {
        String c = clean(sound);
        if (blockedSounds.remove(c)) {
            getConfig().set("blocked-sounds", new ArrayList<>(blockedSounds));
            saveConfig();
            return true;
        }
        return false;
    }

    private String clean(String sound) {
        if (sound == null) return "";
        return sound.toLowerCase().trim().replace("minecraft:", "");
