package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.math.MathUtils;

/** Handle that constrains translation to given axis. */
public class AxisAlignedHandle extends Handle {
    private Mesh mesh;

    public AxisAlignedHandle(Mesh mesh, Vector3 position, Vector3 axis) {
        super(
            position,
            new Quaternion(),
            new Vector3(1, 1, 1)
        );
        this.mesh = mesh;

        setAxis(axis);
    }

    public void setAxis(Vector3 axis) {
        AxisAlignedHandle.axis.set(axis).nor();
        Quaternion rotation = getRotation();
        MathUtils.lookRotation(AxisAlignedHandle.axis, rotation);
        setRotation(rotation);
    }

    @Override
    public void draw() {
        super.draw();

        Draw.color(getDrawColor());
        Draw.mesh(mesh, transform.getTransformation());
        Draw.color(Color.WHITE);
    }

    private static final Vector3 axis = new Vector3();
    private static final Vector3 projection = new Vector3();
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!getSelected()) return false;

        wasDragged = true;

        Vector3 position = getPosition();
        Camera camera = Editor.app.camera;
        plane.set(
            position.x,
            position.y,
            position.z,
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

        // Transform axis to world orientation
        axis.set(Vector3.Z);
        Quaternion rotation = transform.getRotation();
        rotation.transform(axis);

        // Handle translation
        projection.set(intersection).sub(position);
        MathUtils.project(projection, axis);
        position.add(projection);

        // Preserve selection offset
        projection.set(cursorDragOffset);
        MathUtils.project(projection, axis);
        position.add(projection);

        setPosition(position);
        change();

        return false;
    }
}
