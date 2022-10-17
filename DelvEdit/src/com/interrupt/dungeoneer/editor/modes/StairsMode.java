package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;

public class StairsMode extends CarveMode {
    public StairsMode() {
        super(EditorModes.STAIRS);
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

            // Pick our main direction based on selection differences
            boolean xMode = tileSelection.width > tileSelection.height;
            float pickedArchMod = xMode ? widthMod : heightMod;

            if (isCeiling) {
                t.ceilHeight -= dragOffset.y * pickedArchMod;
            } else {
                t.floorHeight -= dragOffset.y * pickedArchMod;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }
}
