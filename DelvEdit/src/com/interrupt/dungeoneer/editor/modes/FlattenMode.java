package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.tiles.Tile;

public class FlattenMode extends CarveMode {
    public FlattenMode() {
        super(EditorModes.FLATTEN);
        canCarve = false;
        canExtrude = false;
        usePlanePicking = false;
        useCollisionTrianglePicking = true;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    // Override this for different behaviors when adjusting the tile ceiling heights
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = controlPointType == ControlPoint.ControlPointType.ceiling;
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // TODO: Flatten based on drag amount?
            // dragOffset.y;
            if (isCeiling) {
                t.ceilSlopeNE = 0;
                t.ceilSlopeSE = 0;
                t.ceilSlopeNW = 0;
                t.ceilSlopeSW = 0;
            } else {
                t.slopeNE = 0;
                t.slopeSE = 0;
                t.slopeNW = 0;
                t.slopeSW = 0;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }
}
