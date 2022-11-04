package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.math.MathUtils;

public class CameraAlignedHandle extends Handle {
    private Mesh mesh;

    public CameraAlignedHandle(Mesh mesh, Vector3 position) {
        super(position);

        this.mesh = mesh;
    }

    @Override
    public void draw() {
        super.draw();

        // Look in the same direction as the camera.
        Camera camera = Editor.app.camera;
        Quaternion rotation = getRotation();
        MathUtils.lookRotation(camera.direction, rotation);
        setRotation(rotation);

        Draw.color(getDrawColor());
        Draw.mesh(mesh, transform.getTransformation());
        Draw.color(Color.WHITE);
    }
}
