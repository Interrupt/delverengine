package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public interface ActorVisitor <T extends Actor> {
    boolean filter(T actor);
    void visit(T actor);
}
