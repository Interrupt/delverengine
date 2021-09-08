package com.interrupt.dungeoneer.ui.values;

import com.interrupt.dungeoneer.game.Game;

public class MenuMode extends DynamicValue {
    Game.MenuMode mode = Game.MenuMode.Hidden;

    public MenuMode() {}

    @Override
    public String stringValue() {
        return String.valueOf(Game.instance.menuMode.equals(mode));
    }

    @Override
    public int intValue() {
        return Game.instance.menuMode.equals(mode) ? 1 : 0;
    }

    @Override
    public float floatValue() {
        return Game.instance.menuMode.equals(mode) ? 1.0f : 0.0f;
    }

    @Override
    public boolean booleanValue() {
        return Game.instance.menuMode.equals(mode);
    }
}
