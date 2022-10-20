package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.math.Vector3;
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
        tileSelectionSettings.boundsUseTileHeights = true;
    }

    private Vector3 t_adjustHeights = new Vector3();
    @Override
    public void adjustTileHeights(TileSelection selection, Vector3 dragStart, Vector3 dragOffset, ControlPoint.ControlPointType controlPointType) {
        boolean isCeiling = isControlPointOnCeiling(controlPointType);
        for (TileSelectionInfo info : selection) {
            Tile t = info.tile;
            if (t == null) {
                continue;
            }

            Vector3 dragAmount = t_adjustHeights.set(dragOffset);
            int selX = -1;
            int selY = -1;

            if(getTileEdgeFromControlPointType(controlPointType) == TileEdges.North) {
                selY = selection.y + selection.height;
            }
            else if(getTileEdgeFromControlPointType(controlPointType) == TileEdges.South) {
                selY = selection.y;
                dragAmount.y *= -1;
            }
            else if(getTileEdgeFromControlPointType(controlPointType) == TileEdges.West) {
                selX = selection.x + selection.width;
            }
            else if(getTileEdgeFromControlPointType(controlPointType) == TileEdges.East) {
                selX = selection.x;
                dragAmount.y *= -1;
            }

            // First set of verts
            float modX = ((float)info.x - (float)selX) / (float)selection.width;
            float modY = ((float)info.y - (float)selY) / (float)selection.height;

            // Second set of verts
            float modXNext = ((float)info.x - (float)selX + 1f) / (float)selection.width;
            float modYNext = ((float)info.y - (float)selY + 1f) / (float)selection.height;

            if(isCeiling) {
                if(selX != -1) {
                    t.ceilSlopeNE += dragAmount.y * modX;
                    t.ceilSlopeSE += dragAmount.y * modX;

                    t.ceilSlopeNW += dragAmount.y * modXNext;
                    t.ceilSlopeSW += dragAmount.y * modXNext;
                }
                if(selY != -1) {
                    t.ceilSlopeNE += dragAmount.y * modY;
                    t.ceilSlopeNW += dragAmount.y * modY;

                    t.ceilSlopeSE += dragAmount.y * modYNext;
                    t.ceilSlopeSW += dragAmount.y * modYNext;
                }
            } else {
                if(selX != -1) {
                    t.slopeNE += dragAmount.y * modX;
                    t.slopeSE += dragAmount.y * modX;

                    t.slopeNW += dragAmount.y * modXNext;
                    t.slopeSW += dragAmount.y * modXNext;
                }
                if(selY != -1) {
                    t.slopeNE += dragAmount.y * modY;
                    t.slopeNW += dragAmount.y * modY;

                    t.slopeSE += dragAmount.y * modYNext;
                    t.slopeSW += dragAmount.y * modYNext;
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

    protected static boolean isControlPointOnCeiling(ControlPoint.ControlPointType c) {
        if(c.ordinal() < ControlPoint.ControlPointType.ceiling.ordinal())
            return false;

        if(c == ControlPoint.ControlPointType.ceiling)
            return true;

        if(c == ControlPoint.ControlPointType.vertex)
            return false;

        // Even enums that are left are ceilings
        return c.ordinal() % 2 == 0;
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
