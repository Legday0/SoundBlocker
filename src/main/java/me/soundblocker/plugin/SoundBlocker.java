package me.soundblocker.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class SoundBlocker extends JavaPlugin {

    private static SoundBlocker instance;
    private List<String> blockedSounds;
    private Map<String, SoundReplacement> replacedSounds;
    private SoundListener soundListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadSoundData();

        // Register ProtocolLib packet listener
        soundListener = new SoundListener(this);

        // Register commands
        SoundCommand cmd = new SoundCommand(this);
        getCommand("sb").setExecutor(cmd);
        getCommand("sb").setTabCompleter(cmd);

        getLogger().info("====================================");
        getLogger().info("  SoundBlocker v3.0 شغال! (ProtocolLib)");
        getLogger().info("  محجوب: " + blockedSounds.size() + " صوت");
        getLogger().info("  متبدل: " + replacedSounds.size() + " صوت");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        if (soundListener != null) soundListener.unregister();
        getLogger().info("[SoundBlocker] تم إيقاف البلوجن.");
    }

    public void loadSoundData() {
        reloadConfig();

        blockedSounds = new ArrayList<>(getConfig().getStringList("blocked-sounds"));

        replacedSounds = new HashMap<>();
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

    public List<String> getBlockedSounds() { return blockedSounds; }
    public Map<String, SoundReplacement> getReplacedSounds() { return replacedSounds; }

    public boolean isSoundBlocked(String sound) {
        String c = clean(sound);
        for (String b : blockedSounds) {
            if (clean(b).equals(c)) return true;
        }
        return false;
    }

    public SoundReplacement getReplacement(String sound) {
        return replacedSounds.get(clean(sound));
    }

    public boolean isSoundReplaced(String sound) {
        return replacedSounds.containsKey(clean(sound));
    }

    public boolean blockSound(String sound) {
        String c = clean(sound);
        if (isSoundBlocked(c)) return false;
        blockedSounds.add(c);
        getConfig().set("blocked-sounds", blockedSounds);
        saveConfig();
        return true;
    }

    public boolean unblockSound(String sound) {
        String c = clean(sound);
        boolean removed = blockedSounds.removeIf(s -> clean(s).equals(c));
        if (removed) { getConfig().set("blocked-sounds", blockedSounds); saveConfig(); }
        return removed;
    }

    public boolean replaceSound(String from, String to, float volume, float pitch) {
        String c = clean(from);
        if (isSoundReplaced(c)) return false;
        replacedSounds.put(c, new SoundReplacement(to, volume, pitch));
        saveReplacedToConfig();
        return true;
    }

    public boolean unreplaceSound(String sound) {
        String c = clean(sound);
        if (!replacedSounds.containsKey(c)) return false;
        replacedSounds.remove(c);
        saveReplacedToConfig();
        return true;
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
        return sound.toLowerCase().replace("minecraft:", "");
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
