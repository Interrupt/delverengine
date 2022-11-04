package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.gfx.Meshes;

public class RingHandle extends CameraAlignedHandle {
    private static final Mesh mesh;

    static {
        Quaternion offset = new Quaternion().setEulerAngles(0, 90, 0);
        mesh = Meshes.ring(1, 1 - (1 / 16f), 64);
        mesh.transform(new Matrix4().rotate(offset));
    }

    public RingHandle(Vector3 position, float size) {
        super(mesh, position);
        setScale(size, size, size);
    }
}
