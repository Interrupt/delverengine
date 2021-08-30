package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.interrupt.dungeoneer.game.Game;

public class ModalGroupElement extends ElementGroup {
    Game.MenuMode mode = Game.MenuMode.Hidden;
    @Override
    protected Actor createActor() {
        WidgetGroup group = new WidgetGroup() {
            @Override
            public void act(float delta) {
                super.act(delta);
                setVisible(Game.instance.menuMode.equals(mode));
            }
        };
        group.setWidth(width);
        group.setHeight(height);

        for (Element child : children) {
            child.init();
            child.setCanvas(canvas);
            Actor actor = child.getActor();
            positionActor(actor, child);
            group.addActor(actor);
        }

        return group;
    }
}
