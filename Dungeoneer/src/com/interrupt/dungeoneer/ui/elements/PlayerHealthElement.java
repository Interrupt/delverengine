package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.ui.UiSkin;

public class PlayerHealthElement extends Element {
    @Override
    public Actor createActor() {
        Label label = new Label("HEALTH: 10/10", UiSkin.getSkin()) {
            @Override
            public void act(float delta) {
                super.act(delta);
                Player player = Game.instance.player;
                setText("HEALTH: " + player.hp + "/" + player.maxHp);
                setPosition(x, y);
            }
        };
        label.setPosition(x, y);
        label.setColor(Color.WHITE);

        return label;
    }
}
