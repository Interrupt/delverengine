package com.interrupt.dungeoneer.editor.modes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorApplication;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.utils.JsonUtil;

public class EntityPickedMode extends EditorMode {
    // The different movement modes
    public enum DragMode { NONE, XY, X, Y, Z }
    public enum MoveMode { NONE, DRAG, ROTATE }

    // Current movement state
    MoveMode moveMode = MoveMode.DRAG;
    DragMode dragMode = DragMode.NONE;

    Vector3 rotateStart;

    Vector3 entityDragStart = null;
    Vector3 planeIntersectionStart = null;
    Vector3 dragOffset = null;

    Vector3 intPos = new Vector3();

    Plane dragPlane = new Plane();

    boolean isMoving = false;

    public EntityPickedMode() {
        super(EditorModes.ENTITY_PICKED);
    }

    // Sets to true on the tick where a mouse up was detected
    boolean leftClickIsDown = false;

    public MoveMode getMoveMode() {
        return moveMode;
    }

    public void setMoveMode(MoveMode newMoveMode) {
        moveMode = newMoveMode;
        if(moveMode == MoveMode.ROTATE) rotateStart = null;
    }

    public void setDragMode(DragMode newDragMode) {
        if(Editor.selection.picked == null) return;
        this.dragMode = newDragMode;

        // Update the drag plane when the drag mode changes
        if(dragMode == DragMode.Y) {
            Vector3 vertDir = new Vector3(Vector3.Y);
            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane = new Plane(vertDir, -len);
        }
        else if(dragMode == DragMode.X) {
            Vector3 vertDir = new Vector3(Vector3.Y);
            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane = new Plane(vertDir, -len);
        }
        else if(dragMode == DragMode.Z) {
            Vector3 vertDir = new Vector3(Editor.app.camera.direction);
            vertDir.y = 0;

            Plane vert = new Plane(vertDir, 0);
            float len = vert.distance(new Vector3(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane = new Plane(vertDir, -len);
        }
        else if(dragMode == DragMode.XY) {
            Vector3 vertDir = t_dragVector.set(Vector3.Y);

            t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
            Plane vert = t_dragPlane;

            float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
        } else {
            Vector3 vertDir = t_dragVector.set(Editor.app.camera.direction);

            t_dragPlane.set(vertDir.x, vertDir.y, vertDir.z, 0);
            Plane vert = t_dragPlane;

            float len = vert.distance(t_dragVector2.set(Editor.selection.picked.x, Editor.selection.picked.z, Editor.selection.picked.y));
            dragPlane.set(vertDir.x, vertDir.y, vertDir.z, -len);
        }

        entityDragStart = null;
        planeIntersectionStart = null;
        dragOffset = null;
    }

    // Override this to run logic when being switched to
    @Override
    public void start() {
        setMoveMode(MoveMode.DRAG);
        setDragMode(DragMode.XY);
    }

    @Override
    public void reset() {
        // Switch back to the carve mode when done here

        entityDragStart = null;
        planeIntersectionStart = null;
        dragOffset = null;

        // Save the movement state when we are done moving
        if (isMoving)
            Editor.app.history.saveState(Editor.app.level);
        isMoving = false;
    }

    @Override
    public void draw() {

    }

    Plane t_dragPlane = new Plane();
    Vector3 t_dragVector = new Vector3();
    Vector3 t_dragVector2 = new Vector3();
    Vector3 t_dragOffset = new Vector3();
    @Override
    public void tick() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            setDragMode(DragMode.X);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            setDragMode(DragMode.Y);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            setDragMode(DragMode.Z);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            setMoveMode(MoveMode.ROTATE);
        }

        // Keep track of the mouse click state
        boolean leftClickWasDown = leftClickIsDown;
        leftClickIsDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if(leftClickWasDown && !leftClickIsDown) {
            onMouseUp();
        }

        // In some drag modes, you don't need to hold the mouse button
        boolean shouldMoveEntity = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        shouldMoveEntity |= dragMode.ordinal() > DragMode.XY.ordinal();

        // Drag entities around
        if(!shouldMoveEntity) {
            dragOffset = null;
            entityDragStart = null;
            planeIntersectionStart = null;

            // Save the movement state when we are done moving
            if (isMoving)
                Editor.app.history.saveState(Editor.app.level);
            isMoving = false;

            return;
        }

        if(Editor.selection.picked == null) {
            return;
        }

        // Make a copy when Alt is pressed
        if(!isMoving && !leftClickWasDown && Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            // Make a copy
            Entity copy = JsonUtil.fromJson(Editor.selection.picked.getClass(), JsonUtil.toJson(Editor.selection.picked));
            Editor.app.level.entities.add(copy);

            Editor.app.pickEntity(copy);

            Array<Entity> copies = new Array<>();
            for(Entity selected : Editor.selection.selected) {
                Entity newCopy = JsonUtil.fromJson(selected.getClass(), JsonUtil.toJson(selected));
                Editor.app.level.entities.add(newCopy);
                copies.add(newCopy);
            }

            Editor.selection.selected.clear();
            Editor.selection.selected.addAll(copies);
            Editor.app.ui.showEntityPropertiesMenu(true);
        }

        // We're underway!
        if(!isMoving)
            isMoving = true;

        if(moveMode == MoveMode.DRAG && Intersector.intersectRayPlane(Editor.app.camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()), dragPlane, intPos)) {
            if(dragOffset == null) {
                dragOffset = t_dragOffset.set(Editor.selection.picked.x - intPos.x, Editor.selection.picked.y - intPos.z, Editor.selection.picked.z - intPos.y);
            }

            if(entityDragStart == null)
                entityDragStart = new Vector3(Editor.selection.picked.x, Editor.selection.picked.y, Editor.selection.picked.z);

            if(planeIntersectionStart == null)
                planeIntersectionStart = new Vector3(intPos);

            float startX = Editor.selection.picked.x;
            float startY = Editor.selection.picked.y;
            float startZ = Editor.selection.picked.z;

            // How far have we moved?
            t_dragVector.set(intPos).sub(planeIntersectionStart);

            Editor.selection.picked.x = entityDragStart.x + t_dragVector.x;
            Editor.selection.picked.y = entityDragStart.y + t_dragVector.z;
            Editor.selection.picked.z = entityDragStart.z + t_dragVector.y;

            if(dragMode == DragMode.XY) {
                Editor.selection.picked.z = entityDragStart.z;
            }
            if(dragMode == DragMode.Y) {
                Editor.selection.picked.x = entityDragStart.x;
            }
            else if(dragMode == DragMode.Z) {
                Editor.selection.picked.x = entityDragStart.x;
                Editor.selection.picked.y = entityDragStart.y;
            }
            else if(dragMode == DragMode.X) {
                Editor.selection.picked.y = entityDragStart.y;
            }

            if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                Editor.selection.picked.x = (int)(Editor.selection.picked.x * 8) / 8f;
                Editor.selection.picked.y = (int)(Editor.selection.picked.y * 8) / 8f;
                Editor.selection.picked.z = (int)(Editor.selection.picked.z * 8) / 8f;
            }

            float movedX = startX - Editor.selection.picked.x;
            float movedY = startY - Editor.selection.picked.y;
            float movedZ = startZ - Editor.selection.picked.z;

            for(Entity selected : Editor.selection.selected) {
                selected.x -= movedX;
                selected.y -= movedY;
                selected.z -= movedZ;
            }

            Editor.app.refreshEntity(Editor.selection.picked);
            for(Entity selected : Editor.selection.selected) {
                Editor.app.refreshEntity(selected);
            }
        }
    }

    public void onMouseUp() {
        if(Editor.selection.picked == null) {
            reset();
        }
    }
}
