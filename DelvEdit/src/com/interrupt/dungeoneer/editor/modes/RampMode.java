package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.helpers.TileEdges;

public class RampMode extends CarveMode {
    public RampMode() {
        super(EditorModes.RAMP);
        canCarve = false;
        canExtrude = false;
        usePlanePicking = false;
        useCollisionTrianglePicking = true;
        tileSelectionSettings.boundsUseTileHeights = true;
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
        Array<Vector3> vertices = selection.getVertexLocations();
        for(int i = 0; i < vertices.size; i++) {
            Vector3 vert = vertices.get(i);

            float xMod = (vert.x - selection.x) / (float)selection.width;
            float yMod = (vert.y - selection.y) / (float)selection.height;

            float pickedMod = 0f;
            if(tileEdge == TileEdges.East) {
                pickedMod = xMod;
            } else if(tileEdge == TileEdges.West) {
                pickedMod = 1f - xMod;
            } else if(tileEdge == TileEdges.North) {
                pickedMod = 1f - yMod;
            } else if(tileEdge == TileEdges.South) {
                pickedMod = yMod;
            }

            // Save this new Z value to use in the next step
            vert.z = pickedMod * -dragOffset.y;
        }

        applyVertexHeightModifiers(selection, vertices, isCeiling, !isCeiling);
        packTileHeights(selection, isCeiling);
    }

    @Override
    protected void tryPickingControlPoint(TileSelection selection) {
        if(getPointerOverCeilingPlane(selection)) {
            pickedControlPoint = new ControlPoint(new Vector3(intersectPoint), ControlPoint.ControlPointType.ceiling);
            pickedSurfaceCeilingPoints.set(intersectPoint.y, intersectPoint.y);
        } else if(getPointerOverFloorPlane(selection)) {
            pickedControlPoint = new ControlPoint(new Vector3(intersectPoint), ControlPoint.ControlPointType.floor);
            pickedSurfaceFloorPoints.set(intersectPoint.y, intersectPoint.y);
        } else {
            return;
        }

        // Keep track of this hit being on the floor or ceiling
        boolean isHitOnFloor = pickedControlPoint.controlPointType == ControlPoint.ControlPointType.floor;

        float northDistance = Intersector.distanceLinePoint(
            selection.x, selection.y,
            selection.x + selection.width, selection.y,
            intersectPoint.x, intersectPoint.z);

        float eastDistance = Intersector.distanceLinePoint(
            selection.x + selection.width, selection.y,
            selection.x + selection.width, selection.y + selection.height,
            intersectPoint.x, intersectPoint.z);

        float southDistance = Intersector.distanceLinePoint(
            selection.x, selection.y + selection.height,
            selection.x + selection.width, selection.y + selection.height,
            intersectPoint.x, intersectPoint.z);

        float westDistance = Intersector.distanceLinePoint(
            selection.x, selection.y,
            selection.x, selection.y + selection.height,
            intersectPoint.x, intersectPoint.z);

        // Find smallest!
        float minDistance = Math.min(northDistance, eastDistance);
        minDistance = Math.min(minDistance, southDistance);
        minDistance = Math.min(minDistance, westDistance);

        if(minDistance == northDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.northFloor;
        }
        if(minDistance == eastDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.eastFloor;
        }
        if(minDistance == southDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.southFloor;
        }
        if(minDistance == westDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.westFloor;
        }

        if(!isHitOnFloor) {
            // Floor edge enums are all always one above the ceiling enums
            int idx = pickedControlPoint.controlPointType.ordinal();
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.values()[idx + 1];
        }

        // Switch to the moving control point state
        didStartDrag = false;
        didPickSurface = false;
        state = CarveModeState.SELECTED_CONTROL_POINT;
    }

    protected static boolean isControlPointOnCeiling(ControlPoint.ControlPointType c) {
        if(c == ControlPoint.ControlPointType.vertex)
            return false;

        // Even numbers are floors, odd are ceilings
        return c.ordinal() % 2 != 0;
    }

    protected static boolean controlPointTypeHasMatchingTileEdge(ControlPoint.ControlPointType c) {
        if(c == ControlPoint.ControlPointType.ceiling)
            return false;
        if(c == ControlPoint.ControlPointType.floor)
            return false;
        if(c == ControlPoint.ControlPointType.vertex)
            return false;

        return true;
    }

    protected static TileEdges getTileEdgeFromControlPointType(ControlPoint.ControlPointType c) {
        if(c == ControlPoint.ControlPointType.northCeil || c == ControlPoint.ControlPointType.northFloor)
            return TileEdges.North;
        if(c == ControlPoint.ControlPointType.eastCeil || c == ControlPoint.ControlPointType.eastFloor)
            return TileEdges.East;
        if(c == ControlPoint.ControlPointType.southCeil || c == ControlPoint.ControlPointType.southFloor)
            return TileEdges.South;

        // Only one left!
        return TileEdges.West;
    }
}
