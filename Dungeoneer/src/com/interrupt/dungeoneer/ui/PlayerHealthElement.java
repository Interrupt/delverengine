package com.interrupt.dungeoneer.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;

public class PlayerHealthElement extends Element {
    @Override
    public Actor getActor() {
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
