package com.interrupt.dungeoneer.entities.infos;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;

public class LevelInfo extends Entity {
    /** Ambient color. */
    @EditorProperty
    public static Color ambientColor = new Color(Color.BLACK);

    /** Starting distance of fog. */
    @EditorProperty
    public static float fogStart;

    /** Ending distance of fog. */
    @EditorProperty
    public static float fogEnd;

    /** Color of fog. */
    @EditorProperty
    public static Color fogColor = new Color(Color.BLACK);

    public LevelInfo() {
        hidden = true;
        spriteAtlas = "editor";
        tex = 12;
        isSolid = false;
    }

    @Override
    public void init(Level level, Level.Source source) {
        super.init(level, source);

        if (source != Level.Source.EDITOR) return;

        ambientColor.set(level.ambientColor);

        fogStart = level.fogStart;
        fogEnd = level.fogEnd;
        fogColor.set(level.fogColor);
    }

    @Override
    public void editorTick(Level level, float delta) {
        super.editorTick(level, delta);

        if (!levelNeedsUpdated(level))  return;

        // Ambient light
        level.ambientColor.set(ambientColor);

        // Fog
        level.fogStart = fogStart;
        level.fogEnd = fogEnd;
        level.fogColor.set(fogColor);

        level.isDirty = true;
    }

    private boolean levelNeedsUpdated(Level level) {
        return level.fogStart != fogStart
            || level.fogEnd != fogEnd
            || !level.fogColor.equals(fogColor)
            || !level.ambientColor.equals(ambientColor);
    }
}
