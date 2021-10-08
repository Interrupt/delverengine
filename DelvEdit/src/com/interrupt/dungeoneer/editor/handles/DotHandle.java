package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.gfx.Draw;

public class DotHandle extends Handle {
    public DotHandle(float x, float y, float z) {
        super(x, y, z);
    }

    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    private static final Quaternion rotation = new Quaternion();
    private static final Quaternion meshRotation = new Quaternion().setEulerAngles(0, 90, 0);
    @Override
    public void draw() {
        super.draw();

        Camera camera = Editor.app.camera;
        Vector3 up = Vector3.Y;
        Vector3 dir = camera.direction;

        // Look in the same direction as the camera.
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);

        // Account for disc facing the +z axis.
        rotation.mul(meshRotation);

        Draw.color(getDrawColor());
        Draw.disc(position, rotation, tmp.set(0.25f, 0.25f, 0.25f));
        Draw.color(Color.WHITE);
    }
}
