package me.soundblocker.plugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SoundCommand implements CommandExecutor, TabCompleter {

    private final SoundBlocker plugin;

    private static final List<String> SUB_COMMANDS = Arrays.asList(
        "block", "unblock", "replace", "unreplace", "list", "reload"
    );

    public SoundCommand(SoundBlocker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("soundblocker.admin")) {
            sender.sendMessage(plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // ==================
            //   /sb reload
            // ==================
            case "reload": {
                plugin.loadSoundData();
                sender.sendMessage(plugin.getMessage("reload-success"));
                break;
            }

            // ==================
            //   /sb list
            // ==================
            case "list": {
                List<String> blocked = plugin.getBlockedSounds();
                Map<String, SoundReplacement> replaced = plugin.getReplacedSounds();

                sender.sendMessage("§8§m──────────────────────────────");
                sender.sendMessage("§b  SoundBlocker §7- قائمة الأصوات");
                sender.sendMessage("§8§m──────────────────────────────");

                sender.sendMessage("§e§l المحجوبة (" + blocked.size() + "):");
                if (blocked.isEmpty()) {
                    sender.sendMessage("  §7مفيش أصوات محجوبة.");
                } else {
                    for (int i = 0; i < blocked.size(); i++) {
                        sender.sendMessage("  §7" + (i + 1) + ". §f" + blocked.get(i));
                    }
                }

                sender.sendMessage("§a§l المبدلة (" + replaced.size() + "):");
                if (replaced.isEmpty()) {
                    sender.sendMessage("  §7مفيش أصوات متبدلة.");
                } else {
                    int i = 1;
                    for (Map.Entry<String, SoundReplacement> entry : replaced.entrySet()) {
                        sender.sendMessage("  §7" + i + ". §f" + entry.getKey()
                            + " §7→ §a" + entry.getValue().getSound()
                            + " §7(vol=" + entry.getValue().getVolume()
                            + ", pitch=" + entry.getValue().getPitch() + ")");
                        i++;
                    }
                }
                sender.sendMessage("§8§m──────────────────────────────");
                break;
            }

            // ==================
            //   /sb block <sound>
            // ==================
            case "block": {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessage("usage-block"));
                    return true;
                }
                String sound = args[1].toLowerCase();
                if (plugin.blockSound(sound)) {
                    sender.sendMessage(plugin.getMessage("block-success", "sound", sound));
                } else {
                    sender.sendMessage(plugin.getMessage("already-blocked", "sound", sound));
                }
                break;
            }

            // ==================
            //   /sb unblock <sound>
            // ==================
            case "unblock": {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessage("usage-unblock"));
                    return true;
                }
                String sound = args[1].toLowerCase();
                if (plugin.unblockSound(sound)) {
                    sender.sendMessage(plugin.getMessage("unblock-success", "sound", sound));
                } else {
                    sender.sendMessage(plugin.getMessage("not-blocked", "sound", sound));
                }
                break;
            }

            // ==================
            //   /sb replace <from> <to> [volume] [pitch]
            // ==================
            case "replace": {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getMessage("usage-replace"));
                    return true;
                }
                String from = args[1].toLowerCase();
                String to = args[2].toLowerCase();
                float volume = 1.0f;
                float pitch = 1.0f;

                if (args.length >= 4) {
                    try { volume = Float.parseFloat(args[3]); }
                    catch (NumberFormatException e) {
                        sender.sendMessage("§cالفوليوم لازم يكون رقم مثل 1.0");
                        return true;
                    }
                }
                if (args.length >= 5) {
                    try { pitch = Float.parseFloat(args[4]); }
                    catch (NumberFormatException e) {
                        sender.sendMessage("§cالبيتش لازم يكون رقم مثل 1.0");
                        return true;
                    }
                }

                if (plugin.replaceSound(from, to, volume, pitch)) {
                    sender.sendMessage(plugin.getMessage("replace-success", "from", from).replace("%to%", to));
                } else {
                    sender.sendMessage(plugin.getMessage("already-replaced", "sound", from));
                }
                break;
            }

            // ==================
            //   /sb unreplace <sound>
            // ==================
            case "unreplace": {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessage("usage-unreplace"));
                    return true;
                }
                String sound = args[1].toLowerCase();
                if (plugin.unreplaceSound(sound)) {
                    sender.sendMessage(plugin.getMessage("unreplace-success", "sound", sound));
                } else {
                    sender.sendMessage(plugin.getMessage("not-replaced", "sound", sound));
                }
                break;
            }

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m──────────────────────────────");
        sender.sendMessage("§b  SoundBlocker §7- المساعدة");
        sender.sendMessage("§8§m──────────────────────────────");
        sender.sendMessage("§e/sb block §f<صوت>          §7- حجب صوت");
        sender.sendMessage("§e/sb unblock §f<صوت>        §7- فك حجب صوت");
        sender.sendMessage("§a/sb replace §f<من> <إلى> [vol] [pitch] §7- تبديل صوت");
        sender.sendMessage("§a/sb unreplace §f<صوت>      §7- فك تبديل صوت");
        sender.sendMessage("§b/sb list                   §7- عرض كل الأصوات");
        sender.sendMessage("§b/sb reload                 §7- إعادة تحميل الكونفيج");
        sender.sendMessage("§8§m──────────────────────────────");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("soundblocker.admin")) return new ArrayList<>();

        if (args.length == 1) {
            List<String> matches = new ArrayList<>();
            String typed = args[0].toLowerCase();
            for (String sub : SUB_COMMANDS) {
                if (sub.startsWith(typed)) matches.add(sub);
            }
            return matches;
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "unblock":
                    return new ArrayList<>(plugin.getBlockedSounds());
                case "unreplace":
                    return new ArrayList<>(plugin.getReplacedSounds().keySet());
                case "replace":
                case "block":
                    return Arrays.asList(
                        "entity.sheep.ambient",
                        "entity.cow.ambient",
                        "entity.pig.ambient",
                        "entity.chicken.ambient",
                        "entity.frog.ambient",
                        "entity.villager.ambient",
                        "entity.bat.ambient",
                        "ambient.cave.cave1"
                    );
            }
        }

        return new ArrayList<>();
    }
}
