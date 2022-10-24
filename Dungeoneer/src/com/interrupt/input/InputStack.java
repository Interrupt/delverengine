package com.interrupt.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.SnapshotArray;

import java.util.EmptyStackException;

public class InputStack implements InputProcessor {
    private final SnapshotArray<InputProcessor> processors = new SnapshotArray(4);

    public InputStack() {
        // Add a nop processor
        processors.add(new InputAdapter());
    }

    public void push(InputProcessor processor) {
		if (processor == null) throw new NullPointerException("processor cannot be null");
		processors.add(processor);
	}

	public InputProcessor pop() {
        if (empty()) throw new EmptyStackException();
        InputProcessor result = peek();
        processors.removeIndex(processors.size - 1);

        return result;
    }

	public InputProcessor peek() {
        if (empty()) throw new EmptyStackException();
        return processors.get(processors.size - 1);
    }

    public boolean empty() {
        return processors.size < 2;
    }

    @Override
    public boolean keyDown(int keycode) {
        return peek().keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return peek().keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return peek().keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return peek().touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return peek().touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return peek().touchCancelled(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return peek().touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return peek().mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return peek().scrolled(amountX, amountY);
    }
}
