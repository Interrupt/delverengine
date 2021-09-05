package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.interrupt.dungeoneer.game.Game;

public class Image extends Element {
    public String image;

    @Override
    public Actor createActor() {
        FileHandle file = Game.getInternal(image);
        if (!file.exists()) {
            return null;
        }

        return new com.badlogic.gdx.scenes.scene2d.ui.Image(new Texture(file));
    }
}
