package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
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

        Array<Entity> matches = Editor.app.level.getEntitiesById(trigger.triggersId);

        for (Entity m : matches) {
            Handles.setColor(getOutboundColor(entity, m));
            Handles.drawLine(trigger.x, trigger.z, trigger.y, m.x, m.z, m.y);
            Handles.setColor(Color.WHITE);
        }

        matches = Editor.app.level.getTriggersThatTargetId(trigger.id);
        for (Entity m : matches) {
            Handles.setColor(getInboundColor(entity, m));
            Handles.drawLine(trigger.x, trigger.z, trigger.y, m.x, m.z, m.y);
            Handles.setColor(Color.WHITE);
        }

    }

    private Color getOutboundColor(Entity self, Entity other) {
        if (Editor.selection.isPickedOrSelected(self)) {
            return Color.RED;
        }

        return Color.WHITE;
    }

    private Color getInboundColor(Entity self, Entity other) {
        Color result = Color.WHITE;
        if (Editor.selection.isPickedOrSelected(self)) {
            result = Color.GREEN;
        }
        if (Editor.selection.isPickedOrSelected(other)) {
            result = Color.RED;
        }

        return result;
    }
}
