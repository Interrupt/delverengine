package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.triggers.Trigger;

@GizmoFor(target = Trigger.class)
public class TriggerGizmo extends EntityGizmo {
    @Override
    public void draw(Entity entity) {
        super.draw(entity);

        Trigger trigger = (Trigger) entity;
        Vector3 boundingBoxCenter = new Vector3(trigger.x, trigger.z - 0.5f + (trigger.collision.z / 2), trigger.y);
        Vector3 boundingBoxSize = new Vector3(trigger.collision.x, trigger.collision.z / 2, trigger.collision.y);
        Handles.drawWireCube(boundingBoxCenter, boundingBoxSize);
    }
}
