package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.helpers.TileEdges;

public class TexPanMode extends DrawMode {
    public TexPanMode() {
        super(EditorModes.TEXPAN);

        // Don't carve out new tiles or modify tile heights
        lockTiles = true;
    }

    Vector3 clickHitLocation = new Vector3();
    Vector3 clickHitNormal = new Vector3();

    // Surface click properties
    Vector3 clickLocation = new Vector3();
    TileEdges clickedEdge = TileEdges.East;
    boolean clickedUpperWall = false;

    @Override
    public void applyTiles() {
        if(!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            return;
        }

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            clickHitLocation.set(intersectPoint);
            clickHitNormal.set(intersectNormal);

            if(!Editor.app.pickedSurface.isPicked)
                return;

            clickedEdge = Editor.app.pickedSurface.edge;
            clickedUpperWall = Editor.app.pickedSurface.tileSurface == EditorApplication.TileSurface.UpperWall;

            // keep track of this click position!
            int posX = (int)(clickHitLocation.x - clickHitNormal.x * 0.5f);
            int posY = (int)(clickHitLocation.z - clickHitNormal.z * 0.5f);
            clickLocation.set(posX, 0, posY);

            // reset drag!
            didStartDrag = false;
        }

        dragSurface(clickLocation);
    }

    boolean didStartDrag = false;
    Plane dragPlane = new Plane();
    Vector3 dragStart = new Vector3();
    Vector3 dragPlaneIntersectPos = new Vector3();
    Vector3 t_dragOffset = new Vector3();
    Vector3 t_dragVector = new Vector3();
    public void dragSurface(Vector3 tileLocation) {
        // Get a vertical drag plane
        Vector3 vertDir = t_dragVector.set(Editor.app.camera.direction);
        vertDir.y = 0;
        vertDir.nor();

        dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
        float len = dragPlane.distance(tileLocation);
        dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);

        boolean didIntersect = Intersector.intersectRayPlane(Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, dragPlaneIntersectPos);
        if(!didIntersect)
            return;

        // Clamp the dragging to a sub grid. Controls how much to divide a whole tile
        int clampHeightModifier = 16;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            clampHeightModifier = 8;

        // Round the intersect position a bit
        dragPlaneIntersectPos.y = (int)(dragPlaneIntersectPos.y * clampHeightModifier) / (float)clampHeightModifier;

        // Keep track of the initial drag location
        if(!didStartDrag) {
            didStartDrag = true;
            dragStart.set(dragPlaneIntersectPos);
        }

        // Now keep track of the drag offset
        Vector3 dragOffset = t_dragOffset.set(dragStart.x - dragPlaneIntersectPos.x,dragStart.y - dragPlaneIntersectPos.y,dragStart.z - dragPlaneIntersectPos.z);

        // Finally, pan this surface
        Editor.app.panSurfaceY(-dragOffset.y, (int)clickLocation.x, (int)clickLocation.z, clickedEdge, clickedUpperWall);

        // FIXME: Just do this once for the whole box, not per tile!
        Editor.app.markWorldAsDirty((int)tileLocation.x, (int)tileLocation.z, 1);

        // Now move the control point for next time
        tileLocation.y -= dragOffset.y;
        dragStart.set(dragPlaneIntersectPos);
    }
}
