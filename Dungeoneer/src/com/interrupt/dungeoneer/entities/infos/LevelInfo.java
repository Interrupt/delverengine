package com.interrupt.dungeoneer.entities.infos;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

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

    /** Path to mesh file of skybox. */
    @EditorProperty(type = "FILE_PICKER", params = "meshes")
    public static String skyboxMesh;

    /** Path to texture file for skybox. */
    @EditorProperty(type = "FILE_PICKER", params = "")
    public static String skyboxTexture;

    /** Comma separated list of mp3 filepaths. */
    @EditorProperty
    public static String music;

    /** Comma separated list of mp3 filepaths. */
    @EditorProperty
    public static String actionMusic;

    /** Play music on a loop. */
    @EditorProperty
    public static boolean loopMusic = true;

    /** Ambient sound filepath. */
    @EditorProperty(type = "FILE_PICKER", params = "audio", include_base = false)
    public static String ambientSound = null;

    /** Ambient sound volume. */
    @EditorProperty
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

        if (source != Level.Source.EDITOR) {
            // Set to inactive so it will get deleted
            isActive = false;
            return;
        }

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

        if (level.skybox != null) {
            skyboxMesh = level.skybox.meshFile;
            skyboxTexture = level.skybox.textureFile;
        }
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

        if (level.skybox == null) {
            level.skybox = new DrawableMesh();
        }

        level.skybox.meshFile = skyboxMesh;
        level.skybox.textureFile = skyboxTexture;
        level.skybox.isDirty = true;

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
            || skyboxNeedsUpdated(level)
            || !Objects.equals(level.music, music)
            || !Objects.equals(level.actionMusic, actionMusic)
            || !Objects.equals(level.loopMusic, loopMusic)
            || !Objects.equals(level.ambientSound, ambientSound)
            || !Objects.equals(level.ambientSoundVolume, ambientSoundVolume)
            || !level.ambientColor.equals(ambientColor);
    }

    private boolean skyboxNeedsUpdated(Level level) {
        // If the mesh + texture is not set, no update is needed.
        if (skyboxMesh == null || skyboxMesh.isEmpty()) return false;
        if (skyboxTexture == null || skyboxTexture.isEmpty()) return false;

        // If we get this far and the skybox is null, we need an update
        if (level.skybox == null) return true;


        // Verify drawable fields are equal
        if (!Objects.equals(level.skybox.meshFile, skyboxMesh)) return true;
        if (!Objects.equals(level.skybox.textureFile, skyboxTexture)) return true;

        return false;
    }
}
