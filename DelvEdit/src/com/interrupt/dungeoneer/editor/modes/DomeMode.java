package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;

public class DomeMode extends CarveMode {
    public DomeMode() {
        super(EditorModes.DOME);
        canCarve = false;
        canExtrude = false;
    }

    @Override
    public void adjustTileHeights(Vector3 dragStart, Vector3 dragOffset, boolean isCeiling) {
        for (TileSelectionInfo info : tileSelection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // Get the arch heights
            float widthMod = (info.x + 0.5f - tileSelection.x) / (float)tileSelection.width;
            float heightMod = (info.y + 0.5f - tileSelection.y) / (float)tileSelection.height;

            widthMod = (float)Math.sin(widthMod * Math.PI);
            heightMod = (float)Math.sin(heightMod * Math.PI);
            float calcedArchMod = (widthMod + heightMod) / 2f;

            if (isCeiling) {
                t.ceilHeight -= dragOffset.y * calcedArchMod;
            } else {
                t.floorHeight -= dragOffset.y * calcedArchMod;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }
}