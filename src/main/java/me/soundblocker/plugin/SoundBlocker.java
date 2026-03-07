package me.soundblocker.plugin;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundBlocker extends JavaPlugin {

    private static SoundBlocker instance;
    private List<String> blockedSounds = new ArrayList<>();
    private Map<String, SoundReplacement> replacedSounds = new HashMap<>();

    private File dataFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        setupDataFile();
        loadSoundData();

        // تسجيل PacketEvents listener
        PacketEvents.getAPI().getEventManager().registerListener(new SoundListener());
        PacketEvents.getAPI().init();

        SoundCommand cmd = new SoundCommand(this);
        getCommand("sb").setExecutor(cmd);
        getCommand("sb").setTabCompleter(cmd);

        getLogger().info("====================================");
        getLogger().info("  SoundBlocker v5.0 شغال! (PacketEvents)");
        getLogger().info("  محجوب: " + blockedSounds.size() + " صوت");
        getLogger().info("  متبدل: " + replacedSounds.size() + " صوت");
        getLogger().info("====================================");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    private void setupDataFile() {
        dataFile = new File(getDataFolder(), "sounds.yml");
        if (!dataFile.exists()) {
            getDataFolder().mkdirs();
            try { dataFile.createNewFile(); }
            catch (IOException e) { getLogger().severe("فشل في إنشاء sounds.yml!"); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadSoundData() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        blockedSounds = new ArrayList<>(dataConfig.getStringList("blocked"));
        replacedSounds = new HashMap<>();
        if (dataConfig.isConfigurationSection("replaced")) {
            for (String key : dataConfig.getConfigurationSection("replaced").getKeys(false)) {
                String realKey = key.replace("__", ".");
                String sound = dataConfig.getString("replaced." + key + ".sound", realKey);
                float volume = (float) dataConfig.getDouble("replaced." + key + ".volume", 1.0);
                float pitch  = (float) dataConfig.getDouble("replaced." + key + ".pitch",  1.0);
                replacedSounds.put(realKey, new SoundReplacement(sound, volume, pitch));
            }
        }
    }

    private void saveDataFile() {
        dataConfig.set("blocked", blockedSounds);
        dataConfig.set("replaced", null);
        for (Map.Entry<String, SoundReplacement> e : replacedSounds.entrySet()) {
            String safeKey = e.getKey().replace(".", "__");
            dataConfig.set("replaced." + safeKey + ".sound",  e.getValue().getSound());
            dataConfig.set("replaced." + safeKey + ".volume", e.getValue().getVolume());
            dataConfig.set("replaced." + safeKey + ".pitch",  e.getValue().getPitch());
        }
        try { dataConfig.save(dataFile); }
        catch (IOException e) { getLogger().severe("فشل في حفظ sounds.yml!"); }
    }

    // ---- Blocked ----
    public List<String> getBlockedSounds() { return blockedSounds; }

    public boolean isSoundBlocked(String sound) {
        String c = clean(sound);
        for (String b : blockedSounds) if (b.equals(c)) return true;
        return false;
    }

    public boolean blockSound(String sound) {
        String c = clean(sound);
        if (isSoundBlocked(c)) return false;
        blockedSounds.add(c);
        saveDataFile();
        return true;
    }

    public boolean unblockSound(String sound) {
        String c = clean(sound);
        boolean removed = blockedSounds.removeIf(s -> s.equals(c));
        if (removed) saveDataFile();
        return removed;
    }

    // ---- Replaced ----
    public Map<String, SoundReplacement> getReplacedSounds() { return replacedSounds; }

    public SoundReplacement getReplacement(String sound) {
        return replacedSounds.get(clean(sound));
    }

    public boolean isSoundReplaced(String sound) {
        return replacedSounds.containsKey(clean(sound));
    }

    public boolean replaceSound(String from, String to, float volume, float pitch) {
        String c = clean(from);
        if (isSoundReplaced(c)) return false;
        replacedSounds.put(c, new SoundReplacement(to, volume, pitch));
        saveDataFile();
        return true;
    }

    public boolean unreplaceSound(String sound) {
        String c = clean(sound);
        if (!replacedSounds.containsKey(c)) return false;
        replacedSounds.remove(c);
        saveDataFile();
        return true;
    }

    // ---- Utils ----
    public String clean(String sound) {
        return sound.toLowerCase().trim().replace("minecraft:", "");
    }

    public String getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "&8[&bSoundBlocker&8] ").replace("&", "§");
        String msg    = getConfig().getString("messages." + key, key).replace("&", "§");
        return prefix + msg;
    }

    public String getMessage(String key, String placeholder, String value) {
        return getMessage(key).replace("%" + placeholder + "%", value);
    }

    public static SoundBlocker getInstance() { return instance; }
}
