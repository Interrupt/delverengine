package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.InputAdapter;

public abstract class Handle extends InputAdapter {
    private final int id;

    private boolean hovered;
    private boolean selected;
    private boolean visible = false;


    public Handle() {
        id = Handles.getNewId();
        Handles.add(this);
    }

    public int getId() {
        return id;
    }

    public boolean getHovered() {
        return hovered;
    }

    public void setHovered(boolean hovered) {
        this.hovered = hovered;
    }

    public boolean getSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            if (!selected) deselect();
            else select();
        }

        this.selected = selected;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void draw() {
        setVisible(true);
    };

    /** Called when cursor is moved over handle. */
    public void enter() {
        setHovered(true);
    }

    /** Called when cursor is moved out of handle. */
    public void exit() {
        setHovered(false);
    }

    /** Called when handle becomes selected. */
    public void select() {}

    /** Called when handle become deselected. */
    public void deselect() {}

    /** Called when handle is manipulated. */
    public void change() {}

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != 0) {
            setHovered(false);
        }

        setSelected(getHovered());

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        setSelected(false);
        return false;
    }
}
