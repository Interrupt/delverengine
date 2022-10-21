package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.TileEdges;

public class ArchMode extends RampMode {
    public ArchMode() {
        super();
        mode = EditorModes.ARCH;
    }

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = isControlPointOnCeiling(controlPointType);

        boolean hasMatchingTileEdge = controlPointTypeHasMatchingTileEdge(controlPointType);
        if(!hasMatchingTileEdge)
            return;

        TileEdges tileEdge = getTileEdgeFromControlPointType(controlPointType);

        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            // Get the arch heights
            float widthMod = (info.x + 0.5f - selection.x) / (float)selection.width;
            float heightMod = (info.y + 0.5f - selection.y) / (float)selection.height;

            widthMod = (float)Math.sin(widthMod * Math.PI);
            heightMod = (float)Math.sin(heightMod * Math.PI);

            // Pick our arch direction based on selection differences
            boolean xMode = tileEdge == TileEdges.East || tileEdge == TileEdges.West;
            float pickedArchMod = xMode ? heightMod : widthMod;

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
