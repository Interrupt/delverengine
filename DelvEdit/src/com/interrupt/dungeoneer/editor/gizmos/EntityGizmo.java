package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.editor.handles.*;
import com.interrupt.dungeoneer.entities.DirectionalEntity;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.math.MathUtils;

@GizmoFor(target = Entity.class)
public class EntityGizmo extends Gizmo {
    private final Handle translateHandle;
    private final Handle rotationHandle;
    private final Handle scaleHandle;

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

        scaleHandle = new RingHandle(entity.getPosition(), 1f) {
            float distance = 0f;
            float startScale = 1f;
            @Override
            public void select() {
                super.select();
                distance = intersection.len();
                startScale = entity.scale;
                u.set(transform.getPosition());
            }
            private final Vector3 u = new Vector3();
            private final Vector3 v = new Vector3();

            @Override
            public void change() {
                v.set(transform.getPosition()).sub(u).sub(cursorDragOffset);
                distance = v.len();
                entity.scale = startScale * distance;
            }
        };
        Editor.app.editorInput.addListener(scaleHandle);
    }

    @Override
    public void draw() {
        Vector3 position = entity.getPosition();
        translateHandle.setPosition(position.x, position.z - 0.5f, position.y);
        rotationHandle.setPosition(position.x, position.z - 0.5f, position.y);
        scaleHandle.setPosition(position.x, position.z - 0.5f, position.y);

        if (entity instanceof DirectionalEntity) {
            DirectionalEntity d = (DirectionalEntity)entity;
            translateHandle.setRotation(d.rotation.z, d.rotation.x, d.rotation.y);
            rotationHandle.setRotation(d.rotation.z, d.rotation.x, d.rotation.y);
        }

        switch (Editor.app.currentEditorMode) {
            case ENTITY_TRANSLATE:
                translateHandle.draw();
                break;
            case ENTITY_ROTATE:
                rotationHandle.draw();
                break;
            case ENTITY_SCALE:
                scaleHandle.draw();
                break;
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
