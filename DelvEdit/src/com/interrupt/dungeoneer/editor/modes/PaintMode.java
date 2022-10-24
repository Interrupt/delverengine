package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.interrupt.dungeoneer.editor.Editor;

public class PaintMode extends DrawMode {
    public PaintMode() {
        super(EditorModes.PAINT);

        // Don't carve out new tiles or modify tile heights
        lockTiles = true;
    }

    @Override
    public void tick() {
        super.tick();

        if(Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            Editor.app.pickTextureAtSurface();
        }
    }

    @Override
    public void applyTiles() {
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            super.applyTiles();
            return;
        }

        Editor.app.paintSurfaceAtCursor();
    }
}
