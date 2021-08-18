package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.interrupt.dungeoneer.game.Game;

public class ImageElement extends Element {
    public String image;

    @Override
    public Actor getActor() {
        Image actor = new Image(new Texture(Game.getInternal(image)));
        actor.setPosition(x, y);

        return actor;
    }
}
