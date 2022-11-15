package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.SpotLight;

import static com.badlogic.gdx.math.MathUtils.degreesToRadians;

@GizmoFor(target = SpotLight.class)
public class SpotLightGizmo extends EntityGizmo {
    @Override
    public void draw(Entity entity) {
        super.draw(entity);

        SpotLight light = (SpotLight)entity;
        Handles.setColor(EditorColors.LIGHT_GIZMO);

        float height = light.range;
        float radius = (float) Math.tan(degreesToRadians * light.spotLightWidth) * height;

        Vector3 direction = light.getDirection().nor();
        direction.set(direction.x, direction.z, direction.y);
        Vector3 position = new Vector3(light.x, light.z, light.y);
        position.add(direction.scl(height));
        direction.nor();
        direction.scl(-1f);

        Handles.drawWireCone(position, direction, radius, height);
        Handles.setColor(Color.WHITE);
    }
}
