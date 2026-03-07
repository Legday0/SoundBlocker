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

        protocolManager.addPacketListener(new PacketAdapter(
                pl,
                ListenerPriority.HIGHEST,
                PacketType.Play.Server.NAMED_SOUND_EFFECT,
                PacketType.Play.Server.ENTITY_SOUND
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    // مررنا نوع الباكت للميثود عشان نعرف نقرأها صح
                    String soundKey = getSoundKey(event.getPacket(), event.getPacketType());
                    
                    if (soundKey == null || soundKey.isEmpty()) return;

                    // 1. حجب (Block)
                    if (pl.isSoundBlocked(soundKey)) {
                        event.setCancelled(true);
                        return;
                    }

                    // 2. تبديل (Replace)
                    SoundReplacement rep = pl.getReplacement(soundKey);
                    if (rep != null) {
                        event.setCancelled(true);
                        Player player = event.getPlayer();
                        // نلعب الصوت الجديد بمكانه الحالي
                        player.playSound(player.getLocation(), rep.getSound(), rep.getVolume(), rep.getPitch());
                    }
                } catch (Exception e) {
                    // شيلنا ignored عشان لو حصل error في الـ Reflection نعرف سببه
                    // pl.getLogger().warning("Error processing sound packet: " + e.getMessage());
                }
            }
        });
    }

    private String getSoundKey(PacketContainer packet, PacketType type) {
        Object soundObject = null;

        try {
            if (type == PacketType.Play.Server.ENTITY_SOUND) {
                // في ENTITY_SOUND الصوت غالباً بيكون في الـ Structures (Index 0)
                soundObject = packet.getStructures().readSafely(0);
            } 
            
            // لو منفعش أو كان النوع التاني، نستخدم الـ Modifier العادي (Index 0)
            if (soundObject == null) {
                soundObject = packet.getModifier().readSafely(0);
            }

            if (soundObject != null) {
                return parseMinecraftKey(soundObject.toString());
            }

            // Fallback الأخير: MinecraftKeys
            var key = packet.getMinecraftKeys().readSafely(0);
            if (key != null) return key.getKey();

        } catch (Exception ignored) {}

        return null;
    }

    // ميثود موحدة عشان تنضف النص وتطلع الـ ID بتاع الصوت
    private String parseMinecraftKey(String raw) {
        if (raw == null || !raw.contains("minecraft:")) return null;

        try {
            // بنبدأ من بعد كلمة minecraft:
            int start = raw.lastIndexOf("minecraft:") + "minecraft:".length();
            
            // بنمشي في النص لحد ما نلاقي أي علامة قفل (] } , space)
            StringBuilder keyBuilder = new StringBuilder();
            for (int i = start; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (c == ']' || c == '}' || c == ',' || c == ' ' || c == '/' || c == ')') {
                    break;
                }
                keyBuilder.append(c);
            }
            return keyBuilder.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public void unregister() {
        protocolManager.removePacketListeners(plugin);
    }
}
