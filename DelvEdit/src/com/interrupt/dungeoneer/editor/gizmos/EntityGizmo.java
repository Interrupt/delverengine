package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.gfx.Draw;
import com.interrupt.dungeoneer.editor.handles.Handle;
import com.interrupt.dungeoneer.editor.handles.PositionHandle;
import com.interrupt.dungeoneer.entities.Entity;

@GizmoFor(target = Entity.class)
public class EntityGizmo extends Gizmo {
    private final Handle positionHandle;

    public EntityGizmo(Entity entity) {
        super(entity);

        positionHandle = new PositionHandle(entity.getPosition()) {
            @Override
            public void change() {
                entity.setPosition(getPosition());
            }
        };
        Editor.app.editorInput.addListener(positionHandle);
    }

    @Override
    public void draw() {
        positionHandle.setPosition(entity.getPosition());
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
