package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.editor.handles.Handle;
import com.interrupt.dungeoneer.editor.handles.PositionHandle;
import com.interrupt.dungeoneer.editor.handles.RotationHandle;
import com.interrupt.dungeoneer.editor.modes.EditorMode;
import com.interrupt.dungeoneer.entities.DirectionalEntity;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.math.MathUtils;

@GizmoFor(target = Entity.class)
public class EntityGizmo extends Gizmo {
    private final Handle translateHandle;
    private final Handle rotationHandle;

    public EntityGizmo(Entity entity) {
        super(entity);

        translateHandle = new PositionHandle(entity.getPosition()) {
            @Override
            public void change() {
                Vector3 p = getPosition();
                entity.setPosition(p.x, p.z, p.y + 0.5f);
            }
        };
        Editor.app.editorInput.addListener(translateHandle);

        rotationHandle = new RotationHandle(entity.getPosition()) {
            @Override
            public void change() {
                if (!(entity instanceof DirectionalEntity)) return;

                Quaternion rotation = getRotation();
                DirectionalEntity d = (DirectionalEntity)entity;
                Vector3 eulerAngles = d.getRotation();
                MathUtils.toEuler(rotation, eulerAngles);

                d.setRotation(
                    eulerAngles.x,
                    eulerAngles.z,
                    eulerAngles.y
                );
            }
        };
        Editor.app.editorInput.addListener(rotationHandle);
    }

    @Override
    public void draw() {
        Vector3 position = entity.getPosition();
        translateHandle.setPosition(position.x, position.z - 0.5f, position.y);
        rotationHandle.setPosition(position.x, position.z - 0.5f, position.y);

        if (entity instanceof DirectionalEntity) {
            DirectionalEntity d = (DirectionalEntity)entity;
            translateHandle.setRotation(d.rotation.z, d.rotation.x, d.rotation.y);
            rotationHandle.setRotation(d.rotation.z, d.rotation.x, d.rotation.y);
        }

        EditorMode.EditorModes current = Editor.app.currentEditorMode;

        if (current == EditorMode.EditorModes.ENTITY_TRANSLATE) {
            translateHandle.draw();
        }
        else if (current == EditorMode.EditorModes.ENTITY_ROTATE) {
            rotationHandle.draw();
        }

        if (entity.isSolid) {
            Vector3 boundingBoxCenter = new Vector3(entity.x, entity.z - 0.5f + (entity.collision.z / 2), entity.y);
            Vector3 boundingBoxSize = new Vector3(entity.collision.x, entity.collision.z / 2, entity.collision.y);
            Draw.color(EditorColors.COLLISION_GIZMO);
            Draw.wireCube(boundingBoxCenter, boundingBoxSize);
            Draw.color(Color.WHITE);
        }
    }
}
