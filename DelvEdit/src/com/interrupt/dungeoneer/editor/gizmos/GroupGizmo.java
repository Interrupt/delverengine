package com.interrupt.dungeoneer.editor.gizmos;

import com.interrupt.dungeoneer.editor.SelectionState;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Group;

@GizmoFor(target = Group.class)
public class GroupGizmo extends EntityGizmo {
    @Override
    public void draw(Entity entity, SelectionState selectionState) {
        super.draw(entity, selectionState);

        Group group = (Group)entity;
        for (Entity e : group.entities) {
            Gizmo gizmo = GizmoProvider.getGizmo(e.getClass());
            gizmo.draw(e, selectionState);
        }
    }
}
