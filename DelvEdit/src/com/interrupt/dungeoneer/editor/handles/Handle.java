package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;

public class Handle extends Handles.Handle {
    public final Vector3 position = new Vector3();
    public final Quaternion rotation = new Quaternion();
    public final Vector3 scale = new Vector3().set(1, 1, 1);

    private final Color color = Color.WHITE.cpy();
    private final Color highlightColor = Color.WHITE.cpy();

    public Handle(Vector3 position) {
        new Handles().super();
        this.position.set(position);
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public Color getColor() {
        return this.color;
    }

    public void setHighlightColor(Color color) {
        this.highlightColor.set(color);
    }

    public Color getHighlightColor() {
        return this.highlightColor;
    }

    protected Color getDrawColor() {
        if (getSelected() || getHovered()) return highlightColor;

        return color;
    }

    protected static final Vector3 cursorDragOffset = new Vector3();
    @Override
    public void select() {
        // Capture offset of initial selection
        Camera camera = Editor.app.camera;
        plane.set(
            position.x,
            position.z,
            position.y,
            camera.direction.x,
            camera.direction.y,
            camera.direction.z
        );
        Intersector.intersectRayPlane(
            camera.getPickRay(
                Gdx.input.getX(),
                Gdx.input.getY()
            ),
            plane,
            intersection
        );

        cursorDragOffset.set(position).sub(intersection.x, intersection.z, intersection.y);
    }

    @Override
    public void deselect() {
        if (wasDragged) {
            Editor.app.history.save();
            wasDragged = false;
        }
    }

    protected final Vector3 intersection = new Vector3();
    protected final Plane plane = new Plane();
    protected boolean wasDragged = false;
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!getSelected()) return false;

        wasDragged = true;

        Camera camera = Editor.app.camera;
        plane.set(
            position.x,
            position.z,
            position.y,
            camera.direction.x,
            camera.direction.y,
            camera.direction.z
        );
        Intersector.intersectRayPlane(
            camera.getPickRay(
                screenX,
                screenY
            ),
            plane,
            intersection
        );

        // Preserve selection offset
        position.set(intersection.x, intersection.z, intersection.y).add(cursorDragOffset);
        change();

        return false;
    }
}
