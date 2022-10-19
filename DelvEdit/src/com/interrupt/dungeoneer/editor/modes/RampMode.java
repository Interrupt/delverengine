package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;

public class RampMode extends CarveMode {
    public RampMode() {
        super(EditorModes.RAMP);
        canCarve = false;
        canExtrude = false;
        tileSelection.boundsUseTileHeights = true;
    }

    @Override
    public void adjustTileHeights(Vector3 dragStart, Vector3 dragOffset, boolean isCeiling) {
        for (TileSelectionInfo info : tileSelection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // TODO: Pick direction based on closest edge to dragStart
            int selY = tileSelection.y + tileSelection.height;

            float mod = ((float)info.y - (float)selY) / (float)tileSelection.height;
            if(isCeiling) {
                t.ceilSlopeNE += dragOffset.y * mod;
                t.ceilSlopeNW += dragOffset.y * mod;
            } else {
                t.slopeNE += dragOffset.y * mod;
                t.slopeNW += dragOffset.y * mod;
            }

            if(tileSelection.height > 1) {
                mod = ((float)info.y - (float)selY + 1f) / (float)tileSelection.height;
                if(isCeiling) {
                    t.ceilSlopeSE += dragOffset.y * mod;
                    t.ceilSlopeSW += dragOffset.y * mod;
                } else {
                    t.slopeSE += dragOffset.y * mod;
                    t.slopeSW += dragOffset.y * mod;
                }
            }

            t.packHeights();
        }
    }
}
