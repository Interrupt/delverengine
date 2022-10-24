package com.interrupt.dungeoneer.editor.gizmos;

import com.interrupt.dungeoneer.entities.Entity;

public abstract class Gizmo {
    public static int gizmoIds = 1;
    private final int id;
    protected Entity entity;

    public Gizmo(Entity entity) {
        id = gizmoIds++;
        this.entity = entity;
    }

    public int getId() {
        return id;
    }

    public abstract void draw();
}
