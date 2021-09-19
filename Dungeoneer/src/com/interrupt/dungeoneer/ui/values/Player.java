package com.interrupt.dungeoneer.ui.values;

import com.interrupt.dungeoneer.game.Game;

public class Player extends ReflectedValue {
    @Override
    public Object getObject() {
        return Game.instance.player;
    }
}
