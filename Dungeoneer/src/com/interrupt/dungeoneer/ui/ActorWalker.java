package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class ActorWalker {
    public <T extends Actor> void walk(Stage stage, ActorVisitor<T> visitor) {
        if (stage == null) return;

        for (Actor actor : stage.getActors()) {
            if (actor instanceof Group) {
                walk((Group)actor, visitor);
            }

            walk((T)actor, visitor);
        }
    }

    public <T extends Actor> void walk(Group group, ActorVisitor<T> visitor) {
        if (group == null) return;

        for (Actor actor : group.getChildren()) {
            if (actor instanceof Group) {
                walk((Group)actor, visitor);
            }

            walk((T)actor, visitor);
        }
    }

    public <T extends Actor> void walk(T actor, ActorVisitor<T> visitor) {
        if (actor == null) return;

        if (visitor.filter(actor)) {
            visitor.visit(actor);
        }
    }
}
