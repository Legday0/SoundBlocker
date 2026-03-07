package me.soundblocker.plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

public class SoundListener {

    private final SoundBlocker plugin;
    private final ProtocolManager protocolManager;

    public SoundListener(SoundBlocker plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();
    }

    private void registerPacketListener() {
        final SoundBlocker pl = this.plugin;

        // ضفنا CUSTOM_SOUND_EFFECT عشان نضمن إن أي صوت خارجي يتم صيده
        protocolManager.addPacketListener(new PacketAdapter(
                pl,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_SOUND_EFFECT,
                PacketType.Play.Server.ENTITY_SOUND,
                PacketType.Play.Server.CUSTOM_SOUND_EFFECT 
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    String soundKey = getSoundKey(event.getPacket(), event.getPacketType());
                    
                    if (soundKey == null || soundKey.isEmpty()) return;

                    // 1. نظام الحجب الشامل
                    if (pl.isSoundBlocked(soundKey)) {
                        event.setCancelled(true);
                        return;
                    }

                    // 2. نظام التبديل الشامل
                    SoundReplacement rep = pl.getReplacement(soundKey);
                    if (rep != null) {
                        event.setCancelled(true);
                        Player player = event.getPlayer();
                        // نلعب الصوت البديل في مكان اللاعب أو مصدر الصوت
                        player.playSound(player.getLocation(), rep.getSound(), rep.getVolume(), rep.getPitch());
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    private String getSoundKey(PacketContainer packet, PacketType type) {
        try {
            // في حالة الـ ENTITY_SOUND (زي الـ XP والـ Mobs)
            if (type == PacketType.Play.Server.ENTITY_SOUND) {
                Object soundEffect = packet.getStructures().readSafely(0);
                if (soundEffect != null) return parseMinecraftKey(soundEffect.toString());
            }
            
            // في حالة الـ CUSTOM_SOUND_EFFECT (أصوات الريسورس باكس)
            if (type == PacketType.Play.Server.CUSTOM_SOUND_EFFECT) {
                return parseMinecraftKey(packet.getMinecraftKeys().readSafely(0).toString());
            }

            // الحالة العامة للـ NAMED_SOUND_EFFECT
            Object soundField = packet.getModifier().readSafely(0);
            if (soundField != null) return parseMinecraftKey(soundField.toString());

        } catch (Exception ignored) {}
        return null;
    }

    private String parseMinecraftKey(String raw) {
        if (raw == null) return null;
        
        // لو النص فيه "minecraft:" بناخده من بعدها، لو لا بناخد النص بالكامل
        String key = raw.toLowerCase();
        if (key.contains("minecraft:")) {
            int start = key.lastIndexOf("minecraft:") + "minecraft:".length();
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < key.length(); i++) {
                char c = key.charAt(i);
                // بنوقف عند أي علامة تقفل الـ String بتاع الـ NMS
                if (c == ']' || c == '}' || c == ',' || c == ' ' || c == '/' || c == ')' || c == '\'') break;
                sb.append(c);
            }
            return sb.toString().trim();
        }
        
        // تنظيف النص من أي علامات غريبة لو مكنش فيه "minecraft:"
        return key.replaceAll("[^a-z0-9_.]", ""); 
    }

    public void unregister() {
        protocolManager.removePacketListeners(plugin);
    }
}
