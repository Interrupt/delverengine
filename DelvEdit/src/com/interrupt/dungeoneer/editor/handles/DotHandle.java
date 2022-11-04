package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.gfx.Meshes;
import com.interrupt.math.MathUtils;

public class DotHandle extends CameraAlignedHandle {
    private static final Mesh mesh;

    static {
        Quaternion offset = new Quaternion().setEulerAngles(0, 90, 0);
        mesh = Meshes.disc();
        mesh.transform(new Matrix4().rotate(offset));
    }

    public DotHandle(Vector3 position, float size) {
        super(mesh, position);
        setScale(size, size, size);
    }
}
