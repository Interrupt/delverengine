package com.interrupt.dungeoneer.editor.handles;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class CompositeHandle extends Handle {
    protected Array<Handle> children = new Array<>();

    public CompositeHandle(Vector3 position) {
        super(position);
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
    public void setPosition(float x, float y, float z) {
        super.setPosition(x, y, z);

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            child.setPosition(x, y ,z);
        }
    }

    @Override
    public void setPosition(Vector3 position) {
        super.setPosition(position);

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            child.setPosition(position);
        }
    }

    @Override
    public void setRotation(float yaw, float pitch, float roll) {
        super.setRotation(yaw, pitch, roll);

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            child.setRotation(yaw, pitch, roll);
        }
    }

    @Override
    public void setRotation(Quaternion rotation) {
        super.setRotation(rotation);

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            child.setRotation(rotation);
        }
    }

    @Override
    public void setScale(float x, float y, float z) {
        super.setScale(x, y, z);

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            child.setScale(x, y, z);
        }
    }

    @Override
    public void setScale(Vector3 scale) {
        super.setScale(scale);

        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            child.setScale(scale);
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
    public boolean scrolled(int amount) {
        boolean result = false;
        for (int i = 0; i < children.size; i++) {
            Handle child = children.get(i);
            result |= child.scrolled(amount);
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
