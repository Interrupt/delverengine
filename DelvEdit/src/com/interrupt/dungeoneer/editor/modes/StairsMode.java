package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;

public class StairsMode extends CarveMode {
    public StairsMode() {
        super(EditorModes.STAIRS);
        canCarve = false;
        canExtrude = false;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, boolean isCeiling) {
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // Get the arch heights
            float widthMod = (info.x + 0.5f - selection.x) / (float)selection.width;
            float heightMod = (info.y + 0.5f - selection.y) / (float)selection.height;

            // Pick our main direction based on selection differences
            boolean xMode = selection.width > selection.height;
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
