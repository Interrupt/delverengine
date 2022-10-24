package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.gfx.Meshes;

public class ArrowHandle extends AxisAlignedHandle {
    private static final Mesh mesh;

    static {
        // Construct arrow mesh
        Matrix4 matrix = new Matrix4()
            .rotate(new Quaternion().setEulerAngles(0, 90, 0))
            .translate(0, 1, 0)
            .scale(0.25f, 0.25f, 0.25f);

        Mesh point = Meshes.cone();
        point.transform(matrix);

        Mesh stem = Meshes.cube(1 / 32f, 1 / 32f, 1f);
        stem.transform(
            new Matrix4().rotate(new Quaternion().setEulerAngles(0, 90, 0))
        );

        mesh = Meshes.combine(point, stem);
    }

    public ArrowHandle(Vector3 position, Vector3 axis) {
        super(mesh, position, axis);
    }
}
