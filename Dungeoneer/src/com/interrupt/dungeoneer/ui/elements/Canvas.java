package com.interrupt.dungeoneer.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Predicate;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.dungeoneer.ui.ActorWalker;
import com.interrupt.dungeoneer.ui.ActorVisitor;

public class Canvas implements InputProcessor, Disposable {
    private Viewport viewport;
    private Stage stage;

    private int minWidth = 640;
    private int minHeight = 360;

    private int width = minWidth;
    private int height = minHeight;

    public Array<Element> children = new Array<>();

    public Canvas() {}

    public void init() {
        if (viewport == null) {
            calculateScaling();
            viewport = new FitViewport(width, height);
        }

        if (stage == null) {
            stage = new Stage(viewport);

            for (Element child : children) {
                add(child);
            }
        }
    }

    public void add(Element child) {
        child.init();
        child.setCanvas(this);
        Actor actor = child.getActor();
        stage.addActor(actor);
        positionActor(actor, child);
    }

    private void reinit() {
        stage.dispose();
        stage = null;
        viewport = null;
        init();
    }

    public void draw() {
        viewport.apply();
        stage.draw();
    }

    public void act(float delta) {
        stage.act(delta);
    }

    public void resize () {
        reinit();
    }

    private void calculateScaling() {
        int displayWidth = Gdx.graphics.getWidth();
        int displayHeight = Gdx.graphics.getHeight();
        int maxPixelScale = Math.min(displayWidth / minWidth, displayHeight / minHeight);

        int pixelScale = 1;
        for (int i = maxPixelScale; i > 1; i--) {
            if (displayWidth / i >= minWidth && displayHeight / i >= minHeight) {
                pixelScale = i;
                break;
            }
        }

        width = displayWidth / pixelScale;
        height = displayHeight / pixelScale;

        if (width < minWidth || height < minHeight) {
            width = minWidth;
            height = minHeight;
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void positionActor(Actor actor, Element element) {
        Vector2 origin = new Vector2();
        Vector2 position = new Vector2(element.x, element.y);
        Vector2 offset = new Vector2();

        Align pivot = element.pivot;
        if (pivot.equals(Align.UNSET)) {
            pivot = element.anchor;
        }

        switch (pivot) {
            case BOTTOM_LEFT:
                break;

            case BOTTOM_CENTER:
                offset.x = -(int)(actor.getWidth() / 2);
                break;

            case BOTTOM_RIGHT:
                offset.x = -(int)(actor.getWidth());
                break;

            case CENTER_LEFT:
                offset.y = -(int)(actor.getHeight() / 2);
                break;

            case CENTER:
                offset.x = -(int)(actor.getWidth() / 2);
                offset.y = -(int)(actor.getHeight() / 2);
                break;

            case CENTER_RIGHT:
                offset.x = -(int)(actor.getWidth());
                offset.y = -(int)(actor.getHeight() / 2);
                break;

            case TOP_LEFT:
                offset.y = -(int)(actor.getHeight());
                break;

            case TOP_CENTER:
                offset.x = -(int)(actor.getWidth() / 2);
                offset.y = -(int)(actor.getHeight());
                break;

            case TOP_RIGHT:
                offset.x = -(int)(actor.getWidth());
                offset.y = -(int)(actor.getHeight());
                break;
        }

        switch (element.anchor) {
            case BOTTOM_LEFT:
                break;

            case BOTTOM_CENTER:
                origin.x = width / 2;
                break;

            case BOTTOM_RIGHT:
                origin.x = width;
                break;

            case CENTER_LEFT:
                origin.y = height / 2;
                break;

            case CENTER:
                origin.x = width / 2;
                origin.y = height / 2;
                break;

            case CENTER_RIGHT:
                origin.x = width;
                origin.y = height / 2;
                break;

            case TOP_LEFT:
                origin.y = height;
                break;

            case TOP_CENTER:
                origin.x = width / 2;
                origin.y = height;
                break;

            case TOP_RIGHT:
                origin.x += width;
                origin.y += height;
                break;
        }

        position.add(origin).add(offset);
        actor.setPosition(position.x, position.y);
    }

    private final ActorWalker walker = new ActorWalker();

    /**
     * Finds all children in stage hierarchy of the given type and that satisfy
     * the given predicate.
     * @param type Class of object to find
     * @param predicate Predicate to filter potential results against.
     * @return An Array of Actors
     */
    public <T extends Actor> Array<T> find(Class<T> type, Predicate<T> predicate) {
        Array<T> result = new Array<>();
        walker.walk(stage, new ActorVisitor<T>() {
            @Override
            public boolean filter(T actor) {
                return type.isAssignableFrom(actor.getClass()) && predicate.evaluate(actor);
            }

            @Override
            public void visit(T actor) {
                result.add(actor);
            }
        });

        return result;
    }

    public <T extends Actor> Array<T> find(Class<T> type) {
        return find(type, new Predicate<T>() {
            @Override
            public boolean evaluate(T arg0) {
                return true;
            }
        });
    }


    @Override
    public boolean keyDown(int keycode) {
        return stage.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return stage.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return stage.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return stage.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return stage.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return stage.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return stage.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return stage.scrolled(amount);
    }
}
