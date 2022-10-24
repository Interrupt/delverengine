package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class CompositeHandle extends Handle {
    private Array<Handle> children = new Array<>();

    public CompositeHandle(Vector3 position) {
        super(position);
    }

    public void add(Handle child) {
        children.add(child);
        transform.addChild(child.transform);
    }

    @Override
    public void draw() {
        super.draw();

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);

            if (Handles.isPicking()) {
                Handles.setPickColor(child);
            }

            child.draw();
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.keyDown(keycode);
        }

        return result;
    }

    @Override
    public boolean keyUp(int keycode) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.keyUp(keycode);
        }

        return result;
    }

    @Override
    public boolean keyTyped(char character) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.keyTyped(character);
        }

        return result;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.mouseMoved(screenX, screenY);
        }

        return result;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.scrolled(amountX, amountY);
        }

        return result;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.touchDown(screenX, screenY, pointer, button);
        }

        return result;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.touchUp(screenX, screenY, pointer, button);
        }

        return result;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.touchDragged(screenX, screenY, pointer);
        }

        return result;
    }
}
