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
            .translate(0, 1, 0)
            .scale(0.25f, 0.25f, 0.25f);

        Mesh point = Meshes.cone();
        point.transform(matrix);

        Mesh stem = Meshes.cube(1 / 32f, 1 / 32f, 1f);

        mesh = Meshes.combine(point, stem);
    }

    public ArrowHandle(Vector3 position, Quaternion rotation) {
        super(mesh, position, rotation);
    }
}