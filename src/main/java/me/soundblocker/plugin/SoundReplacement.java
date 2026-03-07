package me.soundblocker.plugin;

public class SoundReplacement {

    private final String sound;
    private final float volume;
    private final float pitch;

    public SoundReplacement(String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public String getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }

    @Override
    public String toString() {
        return sound + " (volume=" + volume + ", pitch=" + pitch + ")";
    }
}
