package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

public class Prefab extends Element {
    public String name;
    private Element element;

    @Override
    protected Actor createActor() {
        element = Game.hudManager.getPrefab(name);

        if (element == null) {
            return null;
        }

        element = (Element)KryoSerializer.copyObject(element);
        return element.createActor();
    }
}
