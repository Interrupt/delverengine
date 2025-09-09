package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.math.Frustum;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.Camera;
import com.interrupt.dungeoneer.entities.Entity;

@GizmoFor(target = Camera.class)
public class CameraGizmo extends EntityGizmo {
    @Override
    public void draw(Entity entity) {
        super.draw(entity);
        Camera camera = (Camera) entity;
        Frustum frustum = camera.getCamera().frustum;
        Handles.drawWireFrustum(frustum);
    }
}
