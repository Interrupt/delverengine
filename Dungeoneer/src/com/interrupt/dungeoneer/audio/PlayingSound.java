package com.interrupt.dungeoneer.audio;

import com.badlogic.gdx.audio.Sound;
import com.interrupt.dungeoneer.game.Options;

public class PlayingSound {
    final public Sound sound;
    final public long location;
    public float volume = 1f;

    public PlayingSound(Sound sound, long location) {
        this.sound = sound;
        this.location = location;
    }

    public PlayingSound(Sound sound, long location, float volume) {
        this.sound = sound;
        this.location = location;
        this.volume = volume;
    }

    public void setVolume(float newVolume) {
        this.volume = newVolume;
        if(sound != null) {
            sound.setVolume(location, newVolume * Options.instance.sfxVolume);
        }
    }

    public void updateVolume() {
        sound.setVolume(location, volume * Options.instance.sfxVolume);
    }
}