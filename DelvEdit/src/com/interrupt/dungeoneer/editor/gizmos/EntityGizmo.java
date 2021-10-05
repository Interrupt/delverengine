package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.handles.ArrowHandle;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.Entity;

@GizmoFor(target = Entity.class)
public class EntityGizmo extends Gizmo {
    private final ArrowHandle xAxisHandle;
    private final ArrowHandle yAxisHandle;
    private final ArrowHandle zAxisHandle;

    public EntityGizmo(Entity entity) {
        super(entity);

        xAxisHandle = new ArrowHandle(entity.x + 1, entity.y, entity.z - 0.5f, 0, 0, 0) {
            @Override
            public void change() {
                super.change();
                entity.x = position.x - 1;
            }
        };
        xAxisHandle.setColor(EditorColors.X_AXIS);

        yAxisHandle = new ArrowHandle(entity.x, entity.y, entity.z + 1 - 0.5f, 0, 0, 0) {
            @Override
            public void change() {
                super.change();
                entity.z = position.z - 1 + 0.5f;
            }
        };
        yAxisHandle.setColor(EditorColors.Z_AXIS);

        zAxisHandle = new ArrowHandle(entity.x, entity.y + 1, entity.z - 0.5f, 0, 0, 0) {
            @Override
            public void change() {
                super.change();
                entity.y = position.y - 1;
            }
        };
        zAxisHandle.setColor(EditorColors.Y_AXIS);

        Editor.app.editorInput.addListener(xAxisHandle);
        Editor.app.editorInput.addListener(yAxisHandle);
        Editor.app.editorInput.addListener(zAxisHandle);
    }

    @Override
    public void draw() {
        xAxisHandle.position.set(entity.x + 1, entity.y, entity.z - 0.5f);
        yAxisHandle.position.set(entity.x, entity.y, entity.z + 1 - 0.5f);
        zAxisHandle.position.set(entity.x, entity.y + 1, entity.z - 0.5f);

        xAxisHandle.draw();
        yAxisHandle.draw();
        zAxisHandle.draw();

        if (entity.isSolid) {
            Vector3 boundingBoxCenter = new Vector3(entity.x, entity.z - 0.5f + (entity.collision.z / 2), entity.y);
            Vector3 boundingBoxSize = new Vector3(entity.collision.x, entity.collision.z / 2, entity.collision.y);
            Handles.setColor(EditorColors.COLLISION_GIZMO);
            Handles.drawWireCube(boundingBoxCenter, boundingBoxSize);
            Handles.setColor(Color.WHITE);
        }
    }
}
