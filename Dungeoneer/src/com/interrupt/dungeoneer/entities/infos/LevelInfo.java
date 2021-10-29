package com.interrupt.dungeoneer.entities.infos;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;

import java.util.Objects;

public class LevelInfo extends Entity {
    /** Name of level. */
    @EditorProperty
    public static String levelName = "";

    /** Ambient color. */
    @EditorProperty
    public static Color ambientColor = new Color(Color.BLACK);

    /** Camera far draw distance. */
    @EditorProperty
    public static float viewDistance = 15f;

    /** Starting distance of fog. */
    @EditorProperty
    public static float fogStart;

    /** Ending distance of fog. */
    @EditorProperty
    public static float fogEnd;

    /** Color of fog. */
    @EditorProperty
    public static Color fogColor = new Color(Color.BLACK);

    /** Color from skybox. */
    @EditorProperty
    public static Color skyLightColor = new Color(0.5f,0.5f,0.5f,0);

    /** Color of shadows. */
    @EditorProperty
    public static Color shadowColor = new Color(0.5f, 0.4f, 0.85f, 1f);

    @EditorProperty
    /** Comma separated list of mp3 filepaths. */
    public static String music;

    @EditorProperty
    /** Comma separated list of mp3 filepaths. */
    public static String actionMusic;

    @EditorProperty
    /** Play music on a loop. */
    public static boolean loopMusic = true;

    @EditorProperty(type = "FILE_PICKER", params = "audio", include_base = false)
    /** Ambient sound filepath. */
    public static String ambientSound = null;

    @EditorProperty
    /** Ambient sound volume. */
    public static float ambientSoundVolume = 0.5f;

    public LevelInfo() {
        hidden = true;
        spriteAtlas = "editor";
        tex = 21;
        isSolid = false;
    }

    @Override
    public void init(Level level, Level.Source source) {
        super.init(level, source);

        if (source != Level.Source.EDITOR) return;

        levelName = level.levelName;
        ambientColor.set(level.ambientColor);
        viewDistance = level.viewDistance;
        fogStart = level.fogStart;
        fogEnd = level.fogEnd;
        fogColor.set(level.fogColor);
        skyLightColor.set(level.skyLightColor);
        shadowColor.set(level.shadowColor);
        music = level.music;
        actionMusic = level.actionMusic;
        loopMusic = level.loopMusic;
        ambientSound = level.ambientSound;
        ambientSoundVolume = level.ambientSoundVolume;
    }

    @Override
    public void editorTick(Level level, float delta) {
        super.editorTick(level, delta);

        if (!levelNeedsUpdated(level))  return;

        level.levelName = levelName;
        level.ambientColor.set(ambientColor);
        level.viewDistance = viewDistance;
        level.fogStart = fogStart;
        level.fogEnd = fogEnd;
        level.fogColor.set(fogColor);
        level.skyLightColor.set(skyLightColor);
        level.shadowColor.set(shadowColor);
        level.music = music;
        level.actionMusic = actionMusic;
        level.loopMusic = loopMusic;
        level.ambientSound = ambientSound;
        level.ambientSoundVolume = ambientSoundVolume;

        level.isDirty = true;
    }

    private boolean levelNeedsUpdated(Level level) {
        return level.fogStart != fogStart
            || level.fogEnd != fogEnd
            || !level.fogColor.equals(fogColor)
            || level.viewDistance != viewDistance
            || !Objects.equals(level.levelName, levelName)
            || !level.skyLightColor.equals(skyLightColor)
            || !level.shadowColor.equals(shadowColor)
            || !Objects.equals(level.music, music)
            || !Objects.equals(level.actionMusic, actionMusic)
            || !Objects.equals(level.loopMusic, loopMusic)
            || !Objects.equals(level.ambientSound, ambientSound)
            || !Objects.equals(level.ambientSoundVolume, ambientSoundVolume)
            || !level.ambientColor.equals(ambientColor);
    }
}
