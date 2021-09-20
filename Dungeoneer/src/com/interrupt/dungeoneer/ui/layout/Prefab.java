package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.game.Game;

public class Prefab extends Element {
    public String name;

    @Override
    protected Actor createActor() {
        Element element = Game.hudManager.getPrefab(name);

        if (element == null) {
            return null;
        }

        return element.createActor();
    }
}
