package com.interrupt.dungeoneer.editor.modes;

public class PaintMode extends DrawMode {
    public PaintMode() {
        super(EditorModes.PAINT);

        // Don't carve out new tiles or modify tile heights
        lockTiles = true;
    }
}
