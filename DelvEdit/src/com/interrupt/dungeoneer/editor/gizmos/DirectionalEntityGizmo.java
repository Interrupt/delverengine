package com.interrupt.dungeoneer.editor.gizmos;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.DirectionalEntity;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.math.OrientedBoundingBox;

@GizmoFor(target = DirectionalEntity.class)
public class DirectionalEntityGizmo implements Gizmo {
    private static final OrientedBoundingBox box = new OrientedBoundingBox();

    @Override
    public void draw(Entity entity) {
        DirectionalEntity directionalEntity = (DirectionalEntity)entity;
        if (directionalEntity.isSolid) {
            box.set(
                new Vector3(-directionalEntity.collision.x, 0, -directionalEntity.collision.y),
                new Vector3(directionalEntity.collision.x, directionalEntity.collision.z, directionalEntity.collision.y),
                directionalEntity.rotation.z,
                directionalEntity.rotation.x,
                directionalEntity.rotation.y
            );
            box.setOrigin(directionalEntity.x, directionalEntity.z -0.5f, directionalEntity.y);

            Handles.setColor(EditorColors.COLLISION_GIZMO);
            Handles.drawWireOBB(box);
            Handles.setColor(Color.WHITE);
        }
    }
}
