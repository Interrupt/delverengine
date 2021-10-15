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
import com.interrupt.math.MathUtils;

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
        Draw.mesh(mesh, getPosition(), getRotation(), getScale());
        Draw.color(Color.WHITE);
    }

    private static final Vector3 axis = new Vector3();
    private static final Vector3 projection = new Vector3();

    @Override
    public void select() {
        // Calculate vector along axis
        axis.set(Vector3.Y);
        getRotation().transform(axis);
        MathUtils.swizzleXZY(axis);

        // Capture offset of initial selection
        Camera camera = Editor.app.camera;
        Vector3 position = transform.getPosition();
        plane.set(
            position.x,
            position.z,
            position.y,
            axis.x,
            axis.z,
            axis.y
        );
        Intersector.intersectRayPlane(
            camera.getPickRay(
                Gdx.input.getX(),
                Gdx.input.getY()
            ),
            plane,
            intersection
        );
        MathUtils.swizzleXZY(intersection);

        cursorDragOffset.set(position).sub(intersection);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!getSelected()) return false;

        wasDragged = true;

        // Calculate vector along axis
        axis.set(Vector3.Y);
        getRotation().transform(axis);
        MathUtils.swizzleXZY(axis);

        Vector3 position = getPosition();
        Camera camera = Editor.app.camera;
        plane.set(
            position.x,
            position.z,
            position.y,
            axis.x,
            axis.z,
            axis.y
        );
        Intersector.intersectRayPlane(
            camera.getPickRay(
                screenX,
                screenY
            ),
            plane,
            intersection
        );
        MathUtils.swizzleXZY(intersection);

        // Preserve selection offset
        position.set(intersection).add(cursorDragOffset);
        setPosition(position);
        change();

        return false;
    }
}
