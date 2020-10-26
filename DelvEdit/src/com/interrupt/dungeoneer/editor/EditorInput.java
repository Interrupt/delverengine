package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.interrupt.dungeoneer.editor.ui.menu.MenuAccelerator;
import com.interrupt.dungeoneer.editor.ui.menu.MenuItem;

public class EditorInput implements InputProcessor {
    private final boolean[] buttonsDown = new boolean[100];
    private final boolean[] keysDown = new boolean[256];
    public IntArray buttonEvents = new IntArray();

    private final Array<InputProcessor> listeners = new Array<InputProcessor>();

    public boolean isButtonPressed(int button) {
        return buttonsDown[button];
    }

    public EditorInput() {}

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

        for (InputProcessor listener : listeners) {
            boolean results = listener.keyDown(i);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyUp(int i) {
        keysDown[i] = false;

        for (InputProcessor listener : listeners) {
            boolean results = listener.keyUp(i);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        for (InputProcessor listener : listeners) {
            boolean results = listener.keyTyped(c);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        buttonsDown[button] = true;
        if(!buttonEvents.contains(button)) buttonEvents.add(button);

        Editor.app.ui.touchDown(x, y, pointer, button);

        for (InputProcessor listener : listeners) {
            boolean results = listener.touchDown(x, y, pointer, button);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        buttonsDown[button] = false;

        if(Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(false);
        }

        Editor.app.ui.touchUp(x, y, pointer, button);

        for (InputProcessor listener : listeners) {
            boolean results = listener.touchUp(x, y, pointer, button);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        for (InputProcessor listener : listeners) {
            boolean results = listener.touchDragged(x, y, pointer);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        for (InputProcessor listener : listeners) {
            boolean results = listener.mouseMoved(x, y);
            if (results) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        for (InputProcessor listener : listeners) {
            boolean results = listener.scrolled(amount);
            if (results) {
                return true;
            }
        }

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
    }

    public void addListener(InputProcessor listener) {
        listeners.add(listener);
    }

    public void removeListener(InputProcessor listener) {
        listeners.removeValue(listener, true);
    }
}
