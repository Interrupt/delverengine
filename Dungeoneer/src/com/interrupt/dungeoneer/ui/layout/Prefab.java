package com.interrupt.dungeoneer.ui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

public class Prefab extends Element {
    public String name;

    @Override
    protected Actor createActor() {
        Element element = Game.hudManager.getPrefab(name);

        if (element == null) {
            return null;
        }

        Element copy = (Element)KryoSerializer.copyObject(element);
        return copy.createActor();
    }
}
