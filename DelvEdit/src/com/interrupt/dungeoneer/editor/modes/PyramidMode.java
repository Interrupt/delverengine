package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.selection.TileSelection;

public class PyramidMode extends CarveMode {
    public PyramidMode() {
        super(EditorModes.PYRAMID);
        canCarve = false;
        canExtrude = false;
        usePlanePicking = false;
        useCollisionTrianglePicking = true;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = isControlPointOnCeiling(controlPointType);

        // Apply a function across all of these vertices to set the height modifier
        // These steps are flattened to avoid cache misses, hopefully
        Array<Vector3> vertices = selection.getVertexLocations();
        for(int i = 0; i < vertices.size; i++) {
            Vector3 vert = vertices.get(i);

            float xMod = (vert.x - selection.x) / (float)selection.width;
            float yMod = (vert.y - selection.y) / (float)selection.height;

            if(xMod > 0.5f)
                xMod = 1f - xMod;
            if(yMod > 0.5f)
                yMod = 1f - yMod;

            xMod *= 2f;
            yMod *= 2f;

            float heightAtVertex = (xMod * yMod);

            // Save this new Z value to use in the next step
            vert.z = heightAtVertex * -dragOffset.y;
        }

        applyVertexHeightModifiers(selection, vertices, isCeiling, !isCeiling);
        packTileHeights(selection, isCeiling);
    }
}