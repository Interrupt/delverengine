package com.interrupt.dungeoneer.editor.gizmos;

import com.interrupt.dungeoneer.editor.Selectable;
import com.interrupt.dungeoneer.entities.Entity;

public interface Gizmo {
    void draw(Entity entity, Selectable selectable);
}
