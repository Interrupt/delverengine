package com.interrupt.dungeoneer.editor.gizmos;

import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Group;

@GizmoFor(target = Group.class)
public class GroupGizmo extends EntityGizmo {
    public GroupGizmo(Group entity) {
        super(entity);
    }

    @Override
    public void draw() {
        super.draw();

        Group group = (Group)entity;
        for (Entity e : group.entities) {
            Gizmo gizmo = GizmoProvider.get(e);
            if (gizmo != null) gizmo.draw();
        }
    }
}
