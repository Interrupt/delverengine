package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ControlPoint;
import com.interrupt.dungeoneer.editor.selection.TileSelection;
import com.interrupt.dungeoneer.editor.selection.TileSelectionInfo;
import com.interrupt.dungeoneer.tiles.Tile;

public class RampMode extends CarveMode {
    public RampMode() {
        super(EditorModes.RAMP);
        canCarve = false;
        canExtrude = false;
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = controlPointType == ControlPoint.ControlPointType.northCeil;
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            if(controlPointType != ControlPoint.ControlPointType.northFloor && controlPointType != ControlPoint.ControlPointType.northCeil)
                continue;

            int selY = selection.y + selection.height;

            float mod = ((float)info.y - (float)selY) / (float)selection.height;
            if(isCeiling) {
                t.ceilSlopeNE += dragOffset.y * mod;
                t.ceilSlopeNW += dragOffset.y * mod;
            } else {
                t.slopeNE += dragOffset.y * mod;
                t.slopeNW += dragOffset.y * mod;
            }

            if(selection.height > 1) {
                mod = ((float)info.y - (float)selY + 1f) / (float)selection.height;
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

    @Override
    protected void tryPickingControlPoint(TileSelection selection) {
        float edgeHitDistance = 0.25f;

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
        boolean hitEdge = false;

        // Pick an edge!
        if(intersectPoint.x < selection.x + edgeHitDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.westCeil;
            hitEdge = true;
        }
        if(intersectPoint.x > selection.x + selection.width - edgeHitDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.eastCeil;
            hitEdge = true;
        }
        if(intersectPoint.z < selection.y + edgeHitDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.northCeil;
            hitEdge = true;
        }
        if(intersectPoint.z > selection.y + selection.height - edgeHitDistance) {
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.southCeil;
            hitEdge = true;
        }

        if(isHitOnFloor && hitEdge) {
            // Floor edge enums are all always one above the ceiling enums
            int idx = pickedControlPoint.controlPointType.ordinal();
            pickedControlPoint.controlPointType = ControlPoint.ControlPointType.values()[idx + 1];
        }

        // Switch to the moving control point state
        didStartDrag = false;
        didPickSurface = false;
        state = CarveModeState.SELECTED_CONTROL_POINT;
    }
}
