package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.TileEdges;

public class StairsMode extends RampMode {
    public StairsMode() {
        super();
        mode = EditorModes.STAIRS;
    }

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = isControlPointOnCeiling(controlPointType);

        boolean hasMatchingTileEdge = controlPointTypeHasMatchingTileEdge(controlPointType);
        if(!hasMatchingTileEdge)
            return;

        Vector3 dragAmount = t_adjustHeights.set(dragOffset);
        int selX = -1;
        int selY = -1;
        int offsetModX = 0;
        int offsetModY = 0;

        TileEdges tileEdge = getTileEdgeFromControlPointType(controlPointType);

        if(tileEdge == TileEdges.North) {
            selY = selection.y + selection.height;
        }
        else if(tileEdge == TileEdges.South) {
            selY = selection.y;
            offsetModY -= selection.height + 1;
            dragAmount.y *= -1;
        }
        else if(tileEdge == TileEdges.West) {
            selX = selection.x + selection.width;
        }
        else if(tileEdge == TileEdges.East) {
            selX = selection.x;
            offsetModX -= selection.width + 1;
            dragAmount.y *= -1;
        }

        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            float stepHeightX = 1f / (float)selection.width;
            float stepHeightY = 1f / (float)selection.height;

            float modX = ((float)selection.width + selection.x - info.x + offsetModX) * stepHeightX;
            float modY = ((float)selection.height + selection.y - info.y + offsetModY) * stepHeightY;

            if (isCeiling) {
                if(selX != -1)
                    t.ceilHeight -= dragAmount.y * modX;
                if(selY != -1)
                    t.ceilHeight -= dragAmount.y * modY;
            } else {
                if(selX != -1)
                    t.floorHeight -= dragAmount.y * modX;
                if(selY != -1)
                    t.floorHeight -= dragAmount.y * modY;
            }

            t.packHeights();
            if (t.getMinOpenHeight() < 0f) {
                t.compressFloorAndCeiling(!isCeiling);
            }
        }
    }
}
