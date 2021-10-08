package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;

public class Handle extends Handles.Handle {
    public final Vector3 position = new Vector3();

    private final Color color = Color.WHITE.cpy();
    private final Color highlightColor = Color.WHITE.cpy();

    public Handle(float x, float y, float z) {
        new Handles().super();
        position.set(x, y, z);
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

    private final Vector3 intersection = new Vector3();
    private final Plane plane = new Plane();
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!getSelected()) return false;

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

        position.set(intersection.x, intersection.z, intersection.y);
        change();

        return false;
    }
}
