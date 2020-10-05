package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class DynamicMenuItem extends MenuItem {
    private DynamicMenuItemAction action = null;

    public DynamicMenuItem (CharSequence text) {
        super(text);
    }

    public DynamicMenuItem (CharSequence text, Skin skin) {
        super(text, skin);
    }

    public DynamicMenuItem (CharSequence text, Skin skin, DynamicMenuItemAction action) {
        super(text, skin);
        this.action = action;
        this.action.updateMenuItem(this);
    }

    @Override
    public void act(float delta) {
        if (action != null && action.isDirty()) {
            action.updateMenuItem(this);
        }

        super.act(delta);
    }
}
