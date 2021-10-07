package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.editor.handles.DragHandle;
import com.interrupt.dungeoneer.entities.Entity;

@GizmoFor(target = Entity.class)
public class EntityGizmo extends Gizmo {
    private final DragHandle xAxisHandle;
    private final DragHandle yAxisHandle;
    private final DragHandle zAxisHandle;
    private final DragHandle zPlaneHandle;

    private Quaternion rotation = new Quaternion();
    private Vector3 scale = new Vector3();
    public EntityGizmo(Entity entity) {
        super(entity);

        final float handleThickness = 1 / 32f;

        xAxisHandle = new DragHandle(entity.x + 1, entity.y, entity.z - 0.5f) {
            @Override
            public void draw() {
                super.draw();

                Draw.color(getDrawColor());
                rotation.setEulerAngles(0, 0, -90);
                Draw.cone(position, rotation, scale.set(0.25f, 0.25f, 0.25f));
                Draw.cube(new Vector3(entity.x, entity.y, entity.z - 0.5f), rotation, scale.set(handleThickness, handleThickness, 1));
                rotation.idt();
                Draw.color(Color.WHITE);
            }

            @Override
            public void change() {
                super.change();
                entity.x = position.x - 1;
            }
        };
        xAxisHandle.setColor(EditorColors.X_AXIS);
        xAxisHandle.setHighlightColor(EditorColors.X_AXIS_BRIGHT);

        yAxisHandle = new DragHandle(entity.x, entity.y + 1, entity.z - 0.5f) {
            @Override
            public void draw() {
                super.draw();

                Draw.color(getDrawColor());
                rotation.setEulerAngles(0, 90, 0);
                Draw.cone(position, rotation, scale.set(0.25f, 0.25f, 0.25f));
                Draw.cube(new Vector3(entity.x, entity.y, entity.z - 0.5f), rotation, scale.set(handleThickness, handleThickness, 1));
                Draw.color(Color.WHITE);
                rotation.idt();
            }

            @Override
            public void change() {
                super.change();
                entity.y = position.y - 1;
            }
        };
        yAxisHandle.setColor(EditorColors.Y_AXIS);
        yAxisHandle.setHighlightColor(EditorColors.Y_AXIS_BRIGHT);

        zAxisHandle = new DragHandle(entity.x, entity.y, entity.z - 0.5f) {
            @Override
            public void draw() {
                super.draw();

                Draw.color(getDrawColor());
                Draw.cone(position, rotation, scale.set(0.25f, 0.25f, 0.25f));
                Draw.cube(new Vector3(entity.x, entity.y, entity.z - 0.5f), rotation, scale.set(handleThickness, handleThickness, 1));
                Draw.color(Color.WHITE);
            }

            @Override
            public void change() {
                super.change();
                entity.z = position.z - 1 + 0.5f;
            }
        };
        zAxisHandle.setColor(EditorColors.Z_AXIS);
        zAxisHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        zPlaneHandle = new DragHandle(entity.x + 0.5f, entity.y, entity.z + 0.5f - 0.5f) {
            @Override
            public void draw() {
                super.draw();

                Draw.color(getDrawColor());
                Draw.quad(position, rotation, scale.set(0.25f, 0.25f, 0.25f));
                Draw.color(Color.WHITE);
            }

            @Override
            public void change() {
                super.change();
                entity.x = position.x - 0.5f;
                entity.y = position.y - 0.5f;
            }
        };
        zPlaneHandle.setColor(EditorColors.Z_AXIS);
        zPlaneHandle.setHighlightColor(EditorColors.Z_AXIS_BRIGHT);

        Editor.app.editorInput.addListener(xAxisHandle);
        Editor.app.editorInput.addListener(yAxisHandle);
        Editor.app.editorInput.addListener(zAxisHandle);
        Editor.app.editorInput.addListener(zPlaneHandle);
    }

    @Override
    public void draw() {
        xAxisHandle.position.set(entity.x + 1, entity.y, entity.z - 0.5f);
        yAxisHandle.position.set(entity.x, entity.y + 1, entity.z - 0.5f);
        zAxisHandle.position.set(entity.x, entity.y, entity.z + 1 - 0.5f);
        zPlaneHandle.position.set(entity.x + 0.5f, entity.y + 0.5f, entity.z -  0.5f);

        xAxisHandle.draw();
        yAxisHandle.draw();
        zAxisHandle.draw();
        zPlaneHandle.draw();

        if (entity.isSolid) {
            Vector3 boundingBoxCenter = new Vector3(entity.x, entity.z - 0.5f + (entity.collision.z / 2), entity.y);
            Vector3 boundingBoxSize = new Vector3(entity.collision.x, entity.collision.z / 2, entity.collision.y);
            Draw.color(EditorColors.COLLISION_GIZMO);
            Draw.wireCube(boundingBoxCenter, boundingBoxSize);
            Draw.color(Color.WHITE);
        }
    }
}
