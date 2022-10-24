package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Frustum;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.entities.ProjectedDecal;

@GizmoFor(target = ProjectedDecal.class)
public class ProjectedDecalGizmo extends EntityGizmo {
    public ProjectedDecalGizmo(ProjectedDecal entity) {
        super(entity);
    }

    @Override
    public void draw() {
        super.draw();

        ProjectedDecal decal = (ProjectedDecal) entity;
        Camera camera = decal.perspective;

        if (camera == null) {
            return;
        }

        Frustum frustum = camera.frustum;
        Draw.wireFrustum(frustum);
    }
}
