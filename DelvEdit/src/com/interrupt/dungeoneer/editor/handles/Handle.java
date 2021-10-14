package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.math.Transform;

public class Handle extends Handles.Handle {
    public final Transform transform = new Transform();

    private final Color color = Color.WHITE.cpy();
    private final Color highlightColor = Color.WHITE.cpy();

    public Handle(Vector3 position) {
        this(position, new Quaternion(), new Vector3(1, 1, 1));
    }

    public Handle(Vector3 position, Quaternion rotation, Vector3 scale) {
        new Handles().super();

        transform.set(position, rotation, scale);
    }

    public void setPosition(float x, float y, float z) {
        transform.setPosition(x, y, z);
    }

    public void setPosition(Vector3 position) {
        transform.setPosition(position);
    }

    public Vector3 getPosition() {
        return transform.getPosition();
    }

    public void setRotation(float yaw, float pitch, float roll) {
        transform.setRotation(yaw, pitch, roll);
    }

    public void setRotation(Quaternion rotation) {
        transform.setRotation(rotation);
    }

    public Quaternion getRotation() {
        return transform.getRotation();
    }

    public void setScale(float x, float y, float z) {
        transform.setScale(x, y, z);
    }

    public void setScale(Vector3 scale) {
        transform.setScale(scale);
    }

    public Vector3 getScale() {
        return transform.getScale();
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
        Vector3 position = transform.getPosition();
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
        Vector3 position = transform.getPosition();
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
        setPosition(position);
        change();

        return false;
    }
}
