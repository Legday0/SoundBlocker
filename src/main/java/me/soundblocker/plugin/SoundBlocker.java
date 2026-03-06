package me.soundblocker.plugin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundBlocker extends JavaPlugin {

    private static SoundBlocker instance;

    // Cache for fast lookup
    private List<String> blockedSounds;
    private Map<String, SoundReplacement> replacedSounds;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadSoundData();

        getServer().getPluginManager().registerEvents(new SoundListener(this), this);

        SoundCommand cmd = new SoundCommand(this);
        getCommand("sb").setExecutor(cmd);
        getCommand("sb").setTabCompleter(cmd);

        getLogger().info("====================================");
        getLogger().info("  SoundBlocker v2.0 شغال!");
        getLogger().info("  محجوب: " + blockedSounds.size() + " صوت");
        getLogger().info("  متبدل: " + replacedSounds.size() + " صوت");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("[SoundBlocker] تم إيقاف البلوجن.");
    }

    public void loadSoundData() {
        reloadConfig();
        blockedSounds = getConfig().getStringList("blocked-sounds");

        replacedSounds = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("replaced-sounds");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String cleanKey = key.toLowerCase();
                String sound = section.getString(key + ".sound", key);
                double volume = section.getDouble(key + ".volume", 1.0);
                double pitch = section.getDouble(key + ".pitch", 1.0);
                replacedSounds.put(cleanKey, new SoundReplacement(sound, (float) volume, (float) pitch));
            }
        }
    }

    // ---- Blocked ----

    public List<String> getBlockedSounds() {
        return blockedSounds;
    }

    public boolean isSoundBlocked(String sound) {
        String clean = clean(sound);
        for (String b : blockedSounds) {
            if (clean(b).equals(clean)) return true;
        }
        return false;
    }

    public boolean blockSound(String sound) {
        String clean = clean(sound);
        if (isSoundBlocked(clean)) return false;
        blockedSounds.add(clean);
        getConfig().set("blocked-sounds", blockedSounds);
        saveConfig();
        return true;
    }

    public boolean unblockSound(String sound) {
        String clean = clean(sound);
        boolean removed = blockedSounds.removeIf(s -> clean(s).equals(clean));
        if (removed) {
            getConfig().set("blocked-sounds", blockedSounds);
            saveConfig();
        }
        return removed;
    }

    // ---- Replaced ----

    public Map<String, SoundReplacement> getReplacedSounds() {
        return replacedSounds;
    }

    public SoundReplacement getReplacement(String sound) {
        return replacedSounds.get(clean(sound));
    }

    public boolean isSoundReplaced(String sound) {
        return replacedSounds.containsKey(clean(sound));
    }

    public boolean replaceSound(String from, String to, float volume, float pitch) {
        String cleanFrom = clean(from);
        if (isSoundReplaced(cleanFrom)) return false;
        SoundReplacement rep = new SoundReplacement(to, volume, pitch);
        replacedSounds.put(cleanFrom, rep);
        saveReplacedToConfig();
        return true;
    }

    public boolean unreplaceSound(String sound) {
        String clean = clean(sound);
        if (!replacedSounds.containsKey(clean)) return false;
        replacedSounds.remove(clean);
        saveReplacedToConfig();
        return true;
    }

    private void saveReplacedToConfig() {
        // Clear section then rewrite
        getConfig().set("replaced-sounds", null);
        for (Map.Entry<String, SoundReplacement> entry : replacedSounds.entrySet()) {
            String path = "replaced-sounds." + entry.getKey();
            getConfig().set(path + ".sound", entry.getValue().getSound());
            getConfig().set(path + ".volume", entry.getValue().getVolume());
            getConfig().set(path + ".pitch", entry.getValue().getPitch());
        }
        saveConfig();
    }

    // ---- Utils ----

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

    public static SoundBlocker getInstance() {
        return instance;
    }
}
