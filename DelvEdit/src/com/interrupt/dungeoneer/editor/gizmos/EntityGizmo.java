package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.editor.handles.Handle;
import com.interrupt.dungeoneer.editor.handles.RotationHandle;
import com.interrupt.dungeoneer.entities.DirectionalEntity;
import com.interrupt.dungeoneer.entities.Entity;

@GizmoFor(target = Entity.class)
public class EntityGizmo extends Gizmo {
    private final Handle positionHandle;

    public EntityGizmo(Entity entity) {
        super(entity);

        positionHandle = new RotationHandle(entity.getPosition()) {
            @Override
            public void change() {
                if (!(entity instanceof DirectionalEntity)) return;

                Quaternion rotation = getRotation();
                DirectionalEntity d = (DirectionalEntity)entity;
                d.setRotation(rotation.getPitch(), rotation.getRoll(), rotation.getYaw());
            }
        };
        Editor.app.editorInput.addListener(positionHandle);
    }

    @Override
    public void draw() {
        Vector3 position = entity.getPosition();
        positionHandle.setPosition(position.x, position.z - 0.5f, position.y);

        if (entity instanceof DirectionalEntity) {
            DirectionalEntity d = (DirectionalEntity)entity;
            positionHandle.setRotation(d.rotation.z, d.rotation.x, d.rotation.y);
        }

        positionHandle.draw();

        if (entity.isSolid) {
            Vector3 boundingBoxCenter = new Vector3(entity.x, entity.z - 0.5f + (entity.collision.z / 2), entity.y);
            Vector3 boundingBoxSize = new Vector3(entity.collision.x, entity.collision.z / 2, entity.collision.y);
            Draw.color(EditorColors.COLLISION_GIZMO);
            Draw.wireCube(boundingBoxCenter, boundingBoxSize);
            Draw.color(Color.WHITE);
        }
    }
}
