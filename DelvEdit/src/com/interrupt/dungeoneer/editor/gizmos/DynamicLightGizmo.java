package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.entities.DynamicLight;

@GizmoFor(target = DynamicLight.class)
public class DynamicLightGizmo extends EntityGizmo {
    public DynamicLightGizmo(DynamicLight entity) {
        super(entity);
    }

    @Override
    public void draw() {
        super.draw();

        DynamicLight light = (DynamicLight)entity;
        Draw.color(EditorColors.LIGHT_GIZMO);
        Draw.wireSphere(new Vector3(light.x, light.z, light.y), light.range);
        Draw.color(Color.WHITE);
    }
}
