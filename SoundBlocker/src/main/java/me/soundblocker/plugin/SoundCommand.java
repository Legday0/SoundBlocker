package me.soundblocker.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class SoundCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public SoundCommand(Main plugin) { this.plugin = plugin; }

    private String msg(String key) {
        String pre = plugin.getConfig().getString("messages.prefix", "&8[&bSoundBlocker&8] ").replace("&","§");
        String txt = plugin.getConfig().getString("messages." + key, key).replace("&","§");
        return pre + txt;
    }
    private String msg(String key, String... kv) {
        String m = msg(key);
        for (int i = 0; i < kv.length - 1; i += 2) m = m.replace("%" + kv[i] + "%", kv[i+1]);
        return m;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("soundblocker.admin")) { s.sendMessage(msg("no-perm")); return true; }
        SoundManager mgr = plugin.getSoundManager();

        if (a.length == 0) { sendHelp(s); return true; }

        switch (a[0].toLowerCase()) {

            case "reload" -> {
                plugin.reloadConfig();
                mgr.load();
                s.sendMessage(msg("reloaded"));
            }

            case "list" -> {
                s.sendMessage("§8§m────────────────────────────");
                s.sendMessage("§b§l محجوب (" + mgr.getBlocked().size() + "):");
                mgr.getBlocked().forEach(x -> s.sendMessage("  §7- §f" + x));
                s.sendMessage("§a§l متبدل (" + mgr.getReplaced().size() + "):");
                mgr.getReplaced().forEach((k, v) ->
                    s.sendMessage("  §7- §f" + k + " §7→ §a" + v.getSound()
                        + " §7(vol=" + v.getVolume() + " pitch=" + v.getPitch() + ")")
                );
                s.sendMessage("§8§m────────────────────────────");
            }

            case "block" -> {
                if (a.length < 2) { s.sendMessage("§cاستخدام: /sb block <صوت>"); return true; }
                if (mgr.block(a[1])) s.sendMessage(msg("blocked", "sound", a[1]));
                else                 s.sendMessage(msg("already-blocked", "sound", a[1]));
            }

            case "unblock" -> {
                if (a.length < 2) { s.sendMessage("§cاستخدام: /sb unblock <صوت>"); return true; }
                if (mgr.unblock(a[1])) s.sendMessage(msg("unblocked", "sound", a[1]));
                else                   s.sendMessage(msg("not-blocked", "sound", a[1]));
            }

            case "replace" -> {
                if (a.length < 3) { s.sendMessage("§cاستخدام: /sb replace <من> <إلى> [vol] [pitch]"); return true; }
                float vol = 1f, pitch = 1f;
                try { if (a.length > 3) vol   = Float.parseFloat(a[3]); } catch (Exception ignored) {}
                try { if (a.length > 4) pitch = Float.parseFloat(a[4]); } catch (Exception ignored) {}
                if (mgr.replace(a[1], a[2], vol, pitch))
                    s.sendMessage(msg("replaced", "from", a[1], "to", a[2]));
                else
                    s.sendMessage(msg("already-replaced", "sound", a[1]));
            }

            case "unreplace" -> {
                if (a.length < 2) { s.sendMessage("§cاستخدام: /sb unreplace <صوت>"); return true; }
                if (mgr.unreplace(a[1])) s.sendMessage(msg("unreplaced", "sound", a[1]));
                else                     s.sendMessage(msg("not-replaced", "sound", a[1]));
            }

            default -> sendHelp(s);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage("§8§m────────────────────────────");
        s.sendMessage("§b§l SoundBlocker - المساعدة");
        s.sendMessage("§8§m────────────────────────────");
        s.sendMessage("§e/sb block §f<صوت>");
        s.sendMessage("§e/sb unblock §f<صوت>");
        s.sendMessage("§a/sb replace §f<من> <إلى> [vol] [pitch]");
        s.sendMessage("§a/sb unreplace §f<صوت>");
        s.sendMessage("§b/sb list");
        s.sendMessage("§b/sb reload");
        s.sendMessage("§8§m────────────────────────────");
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String l, String[] a) {
        if (!s.hasPermission("soundblocker.admin")) return new ArrayList<>();
        SoundManager mgr = plugin.getSoundManager();
        if (a.length == 1)
            return Arrays.asList("block","unblock","replace","unreplace","list","reload");
        if (a.length == 2) {
            if (a[0].equalsIgnoreCase("unblock"))   return new ArrayList<>(mgr.getBlocked());
            if (a[0].equalsIgnoreCase("unreplace"))  return new ArrayList<>(mgr.getReplaced().keySet());
        }
        return new ArrayList<>();
    }
}
