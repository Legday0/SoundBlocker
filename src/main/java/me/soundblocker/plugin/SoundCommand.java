package me.soundblocker.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

// استيراد المكتبات اللازمة لحل مشكلة "cannot find symbol"
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoundBlocker extends JavaPlugin {

    private static SoundBlocker instance;
    private Set<String> blockedSounds; 
    private Map<String, SoundReplacement> replacedSounds;
    private SoundListener soundListener;

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
        getLogger().info("  SoundBlocker v3.0 شغال!");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        if (soundListener != null) soundListener.unregister();
    }

    public void loadSoundData() {
        reloadConfig();

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

    // التعديل المهم هنا: بنحول الـ Set لـ List عشان ملف SoundCommand ميضربش ايرور
    public List<String> getBlockedSounds() { 
        return new ArrayList<>(blockedSounds); 
    }

    public Map<String, SoundReplacement> getReplacedSounds() { 
        return replacedSounds; 
    }

    public boolean isSoundBlocked(String sound) {
        return blockedSounds.contains(clean(sound));
    }

    public SoundReplacement getReplacement(String sound) {
        return replacedSounds.get(clean(sound));
    }

    public boolean isSoundReplaced(String sound) {
        return replacedSounds.containsKey(clean(sound));
    }

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

    public boolean replaceSound(String from, String to, float volume, float pitch) {
        String c = clean(from);
        replacedSounds.put(c, new SoundReplacement(to, volume, pitch));
        saveReplacedToConfig();
        return true;
    }

    public boolean unreplaceSound(String sound) {
        String c = clean(sound);
        if (replacedSounds.remove(c) != null) {
            saveReplacedToConfig();
            return true;
        }
        return false;
    }

    private void saveReplacedToConfig() {
        getConfig().set("replaced-sounds", null);
        for (Map.Entry<String, SoundReplacement> e : replacedSounds.entrySet()) {
            String path = "replaced-sounds." + e.getKey();
            getConfig().set(path + ".sound", e.getValue().getSound());
            getConfig().set(path + ".volume", e.getValue().getVolume());
            getConfig().set(path + ".pitch", e.getValue().getPitch());
        }
        saveConfig();
    }

    private String clean(String sound) {
        if (sound == null) return "";
        return sound.toLowerCase().trim().replace("minecraft:", "");
    }

    public String getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "&8[&bSoundBlocker&8] ").replace("&", "§");
        String msg = getConfig().getString("messages." + key, key).replace("&", "§");
        return prefix + msg;
    }

    public String getMessage(String key, String placeholder, String value) {
        return getMessage(key).replace("%" + placeholder + "%", value);
    }

    public static SoundBlocker getInstance() { return instance; }
}
