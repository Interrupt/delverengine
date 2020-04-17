package com.interrupt.dungeoneer.editor.gizmos;

import com.interrupt.dungeoneer.editor.Selectable;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Group;

@GizmoFor(target = Group.class)
public class GroupGizmo extends EntityGizmo {
    @Override
    public void draw(Entity entity, Selectable selectable) {
        super.draw(entity, selectable);

        Group group = (Group)entity;
        for (Entity e : group.entities) {
            Gizmo gizmo = GizmoProvider.getGizmo(e.getClass());
            gizmo.draw(e, selectable);
        }
    }
}
