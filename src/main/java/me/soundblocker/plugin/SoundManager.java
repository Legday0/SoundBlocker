package me.soundblocker.plugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoundManager {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration cfg;

    private final List<String>             blocked  = new ArrayList<>();
    private final Map<String, SoundData>   replaced = new HashMap<>();

    public SoundManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file   = new File(plugin.getDataFolder(), "sounds.yml");
        load();
    }

    // ── Load ──────────────────────────────────────────────
    public void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            try { file.createNewFile(); }
            catch (IOException e) { plugin.getLogger().severe("Cannot create sounds.yml"); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);

        blocked.clear();
        blocked.addAll(cfg.getStringList("blocked"));

        replaced.clear();
        if (cfg.isConfigurationSection("replaced")) {
            for (String safe : cfg.getConfigurationSection("replaced").getKeys(false)) {
                String key    = safe.replace("__", ".");
                String sound  = cfg.getString("replaced." + safe + ".sound",  key);
                float  volume = (float) cfg.getDouble("replaced." + safe + ".volume", 1.0);
                float  pitch  = (float) cfg.getDouble("replaced." + safe + ".pitch",  1.0);
                replaced.put(key, new SoundData(sound, volume, pitch));
            }
        }
    }

    // ── Save ──────────────────────────────────────────────
    private void save() {
        cfg.set("blocked", blocked);
        cfg.set("replaced", null);
        for (Map.Entry<String, SoundData> e : replaced.entrySet()) {
            String safe = e.getKey().replace(".", "__");
            cfg.set("replaced." + safe + ".sound",  e.getValue().getSound());
            cfg.set("replaced." + safe + ".volume", e.getValue().getVolume());
            cfg.set("replaced." + safe + ".pitch",  e.getValue().getPitch());
        }
        try { cfg.save(file); }
        catch (IOException e) { plugin.getLogger().severe("Cannot save sounds.yml"); }
    }

    // ── Blocked ───────────────────────────────────────────
    public List<String>           getBlocked()              { return blocked; }
    public boolean                isBlocked(String s)       { return blocked.contains(clean(s)); }

    public boolean block(String s) {
        String c = clean(s);
        if (isBlocked(c)) return false;
        blocked.add(c); save(); return true;
    }

    public boolean unblock(String s) {
        boolean r = blocked.remove(clean(s));
        if (r) save(); return r;
    }

    // ── Replaced ──────────────────────────────────────────
    public Map<String, SoundData> getReplaced()             { return replaced; }
    public boolean                isReplaced(String s)      { return replaced.containsKey(clean(s)); }
    public SoundData              getReplacement(String s)  { return replaced.get(clean(s)); }

    public boolean replace(String from, String to, float vol, float pitch) {
        String c = clean(from);
        if (isReplaced(c)) return false;
        replaced.put(c, new SoundData(to, vol, pitch)); save(); return true;
    }

    public boolean unreplace(String s) {
        boolean r = replaced.remove(clean(s)) != null;
        if (r) save(); return r;
    }

    // ── Util ──────────────────────────────────────────────
    public String clean(String s) {
        return s.toLowerCase().trim().replace("minecraft:", "");
    }
}
