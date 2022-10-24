package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.gfx.Draw;

public class PlaneAlignedHandle extends Handle {
    private final Mesh mesh;

    public PlaneAlignedHandle(Mesh mesh, Vector3 position, Quaternion rotation) {
        super(position, rotation, new Vector3(1, 1, 1));
        this.mesh = mesh;
    }

    @Override
    public void draw() {
        super.draw();

        Draw.color(getDrawColor());
        Draw.mesh(mesh, transform.getTransformation());
        Draw.color(Color.WHITE);
    }

    private static final Vector3 axis = new Vector3();

    @Override
    public void select() {
        // Calculate vector along axis
        axis.set(Vector3.Y);
        getRotation().transform(axis);

        // Capture offset of initial selection
        Camera camera = Editor.app.camera;
        Vector3 position = transform.getPosition();
        plane.set(
            position.x,
            position.y,
            position.z,
            axis.x,
            axis.y,
            axis.z
        );
        Intersector.intersectRayPlane(
            camera.getPickRay(
                Gdx.input.getX(),
                Gdx.input.getY()
            ),
            plane,
            intersection
        );

        cursorDragOffset.set(position).sub(intersection);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!getSelected()) return false;

        wasDragged = true;

        // Calculate vector along axis
        axis.set(Vector3.Y);
        getRotation().transform(axis);

        Vector3 position = getPosition();
        Camera camera = Editor.app.camera;
        plane.set(
            position.x,
            position.y,
            position.z,
            axis.x,
            axis.y,
            axis.z
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
        position.set(intersection).add(cursorDragOffset);
        setPosition(position);
        change();

        return false;
    }
}
