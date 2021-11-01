package com.interrupt.dungeoneer.ui.values;

import com.interrupt.dungeoneer.game.Game;

public class GameMessage extends ReflectedValue {
    @Override
    public Object getObject() {
        return Game.message2;
    }
}
