package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.Entity;

@GizmoFor(target = DynamicLight.class)
public class DynamicLightGizmo extends EntityGizmo {
    @Override
    public void draw(Entity entity) {
        super.draw(entity);

        DynamicLight light = (DynamicLight)entity;
        Handles.setColor(EditorColors.LIGHT_GIZMO);
        Handles.drawWireSphere(new Vector3(light.x, light.z, light.y), light.range);
        Handles.setColor(Color.WHITE);
    }
}
