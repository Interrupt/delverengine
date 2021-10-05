package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.math.Frustum;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.Camera;

@GizmoFor(target = Camera.class)
public class CameraGizmo extends EntityGizmo {
    public CameraGizmo(Camera entity) {
        super(entity);
    }

    @Override
    public void draw() {
        super.draw();
        Camera camera = (Camera) entity;
        Frustum frustum = camera.getCamera().frustum;
        Handles.drawWireFrustum(frustum);
    }
}
