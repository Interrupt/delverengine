package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
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

        TileEdges tileEdge = getTileEdgeFromControlPointType(controlPointType);

        // Apply a function across all of these vertices to set the height modifier
        // These steps are flattened to avoid cache misses, hopefully
        Array<Vector3> vertices = selection.getVertexLocations(0, 0);
        for(int i = 0; i < vertices.size; i++) {
            Vector3 vert = vertices.get(i);

            float xMod = (vert.x - selection.x) / (float)selection.width;
            float yMod = (vert.y - selection.y) / (float)selection.height;

            float pickedMod = 0f;
            if(tileEdge == TileEdges.East) {
                pickedMod = xMod + (1f / selection.width);
            } else if(tileEdge == TileEdges.West) {
                pickedMod = 1f - xMod;
            } else if(tileEdge == TileEdges.North) {
                pickedMod = 1f - yMod;
            } else if(tileEdge == TileEdges.South) {
                pickedMod = yMod + (1f / selection.height);
            }

            // Save this new Z value to use in the next step
            vert.z = pickedMod * -dragOffset.y;
        }

        applyTileHeightModifiers(selection, vertices, isCeiling, !isCeiling);
        packTileHeights(selection, isCeiling);
    }
}
