package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntArray;
import com.interrupt.dungeoneer.editor.ui.menu.MenuAccelerator;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;

public class EditorInput implements InputProcessor {

    private boolean[] buttonsDown = new boolean[100];
    private boolean[] keysDown = new boolean[256];
    public IntArray buttonEvents = new IntArray();

    public boolean isButtonPressed(int button) {
        return buttonsDown[button];
    }

    public boolean ignoreRightClick = false;

    public float scrollAmount = 0f;

    EditorFrame editor;
    public EditorInput(EditorFrame editor) {
        this.editor = editor;
    }

    @Override
    public boolean keyDown(int i) {
        for(final MenuItem item : MenuItem.acceleratorItems) {
            MenuAccelerator accelerator = item.getMenuAccelerator();
            if(i == accelerator.getKeystroke()) {
                boolean matched = false;

                boolean controlPressed = isKeyPressed(Input.Keys.CONTROL_LEFT) || isKeyPressed(Input.Keys.CONTROL_RIGHT);
                boolean shiftPressed = isKeyPressed(Input.Keys.SHIFT_LEFT) || isKeyPressed(Input.Keys.SHIFT_RIGHT);

                // check if this key matches
                if(accelerator.getControlRequired()) {
                    if(controlPressed) matched = true;
                }
                else if(accelerator.getShiftRequired()) {
                    if(shiftPressed) matched = true;
                }
                else {
                    if(!controlPressed && !shiftPressed) matched = true;
                }

                if(matched) {
                    item.actionListener.actionPerformed(null);
                    return true;
                }
            }
        }

        keysDown[i] = true;

        return false;
    }

    @Override
    public boolean keyUp(int i) {
        keysDown[i] = false;
        return false;
    }

    @Override
    public boolean keyTyped(char c) { return false; }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        buttonsDown[button] = true;
        if(!buttonEvents.contains(button)) buttonEvents.add(button);

        lastMouseLocation.set(x, y);

        ignoreRightClick = false;

        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        buttonsDown[button] = false;

        lastMouseLocation.set(x, y);

        if(Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(false);
        }

        editor.editorUi.touchUp(x, y, pointer, button);

        return false;
    }

    private Vector2 lastMouseLocation = new Vector2();

    @Override
    public boolean touchDragged(int x, int y, int pointer) {

        if(buttonsDown[Input.Buttons.RIGHT]) {
            ignoreRightClick = true;

            float moveX = (lastMouseLocation.x - x);
            float moveY = (lastMouseLocation.y - y);

            if(moveX >= 1 || moveY >= 1) {
                if (!Gdx.input.isCursorCatched()) {
                    Gdx.input.setCursorCatched(true);
                }
            }

            editor.rotX += moveX * 0.005f;
            editor.rotY -= moveY * 0.005f;
        }

        lastMouseLocation.set(x, y);

        return false;
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        scrollAmount = amount;

        return false;
    }

    public boolean isKeyPressed(int i) {
        return keysDown[i];
    }

    public void resetKeys() {
        for(int i = 0; i < keysDown.length; i++) keysDown[i] = false;
    }

    public void tick() {
        buttonEvents.clear();
        scrollAmount = 0f;
    }
}
