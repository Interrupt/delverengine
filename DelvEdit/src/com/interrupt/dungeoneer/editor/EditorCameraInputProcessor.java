package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;

public class EditorCameraInputProcessor implements InputProcessor {
    private final Camera camera;

    private float camX;
    private float camY;
    private float camZ;

    private float rotX = (float)Math.PI;
    private float rotY = 1.4f;

    private float rota = 0;
    private float rotya = 0;
    private final float rotYClamp = 1.571f;

    private final float walkSpeed = 0.15f;
    private final float rotSpeed = 0.009f;
    private final float maxRot = 0.8f;

    private float orbitDistance = 4.0f;

    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean moveForward = false;
    private boolean moveBackward = false;

    private boolean turnLeft = false;
    private boolean turnRight = false;
    private boolean turnUp = false;
    private boolean turnDown = false;

    private boolean moveFast = false;
    private final float moveMultiplier = 2.0f;

    public EditorCameraInputProcessor(Camera camera) {
        this.camera = camera;
        camX = camera.position.x;
        camY = camera.position.z;
        camZ = camera.position.y;
    }

    public void tick() {
        /*
        boolean turnLeft = (Gdx.input.getDeltaX() < 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
        boolean turnRight = (Gdx.input.getDeltaX() > 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
        boolean turnUp = (Gdx.input.getDeltaY() > 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
        boolean turnDown = (Gdx.input.getDeltaY() < 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
        */

        /*
        turnLeft |= Gdx.input.isKeyPressed(Input.Keys.LEFT) && !moveFast;
        turnRight |= Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !moveFast;
        turnUp |= Gdx.input.isKeyPressed(Input.Keys.DOWN) && !moveFast;
        turnDown |= Gdx.input.isKeyPressed(Input.Keys.UP) && !moveFast;
        */

        if(turnLeft) {
            rota += rotSpeed;
            if(rota > maxRot) rota = maxRot;
        }
        else if(turnRight) {
            rota -= rotSpeed;
            if(rota < -maxRot) rota = -maxRot;
        }

        rotX += rota;
        rota *= 0.8;

        if(turnUp) {
            rotya += rotSpeed * 0.6f;
            if(rotya > maxRot) rotya = maxRot;
        }
        else if(turnDown) {
            rotya -= rotSpeed * 0.6f;
            if(rotya < -maxRot) rotya = -maxRot;
        }

        rotY += rotya;

        if (rotY < -rotYClamp) rotY = -rotYClamp;
        if (rotY > rotYClamp) rotY = rotYClamp;

        rotya *= 0.8;

        float xm = 0f;
        float zm = 0f;

        if(moveLeft) {
            xm = -1f;
        }
        if(moveRight) {
            xm = 1f;
        }

        if(moveForward) {
            zm = -1f;
        }
        if(moveBackward) {
            zm = 1f;
        }

        if(moveDown && !moveFast) {
            camZ -= 0.1f;
        }
        if(moveUp && !moveFast) {
            camZ += 0.1f;
        }

        if (moveFast) {
            xm *= moveMultiplier;
            zm *= moveMultiplier;
        }

        camZ += (zm * Math.sin(rotY)) * walkSpeed;
        zm *= Math.cos(rotY);
        camX += (xm * Math.cos(rotX) + zm * Math.sin(rotX)) * walkSpeed;
        camY += (zm * Math.cos(rotX) - xm * Math.sin(rotX)) * walkSpeed;

        /*
        if(player != null) {
            player.rot = rotX;
            player.yrot = rotY;

            player.xa += (xm * Math.cos(rotX) + zm * Math.sin(rotX)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
            player.ya += (zm * Math.cos(rotX) - xm * Math.sin(rotX)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
        }
        */

        /*
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && (editorInput.isButtonPressed(Input.Buttons.RIGHT) || turnLeft || turnRight || turnUp || turnDown)) {
            // Calculate the next camera direction vector;
            Vector3 cameraNewDirection = new Vector3(0, 0, 1);
            cameraNewDirection.rotate(rotY * 57.2957795f, 1f, 0, 0);
            cameraNewDirection.rotate((float)(rotX + 3.14) * 57.2957795f, 0, 1f, 0);
            cameraNewDirection.nor();

            // Calculate the orbit pivot.
            Vector3 pivotPosition = new Vector3(camera.direction).scl(orbitDistance).add(camera.position);

            // Calculate new camera position.
            cameraNewDirection.scl(-orbitDistance);
            cameraNewDirection.add(pivotPosition);

            camX = cameraNewDirection.x;
            camY = cameraNewDirection.z;
            camZ = cameraNewDirection.y;
        }
        */

        camera.position.set(camX, camZ, camY);
        camera.direction.set(0, 0, 1);
        camera.up.set(0, 1, 0);
        camera.rotate(rotY * 57.2957795f, 1f, 0, 0);
        camera.rotate((float)(rotX + 3.14) * 57.2957795f, 0, 1f, 0);
        camera.update();
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean wasHandled = false;
        switch (keycode) {
            case Input.Keys.W:
                moveForward = true;
                wasHandled = true;
                break;

            case Input.Keys.S:
                moveBackward = true;
                wasHandled = true;
                break;

            case Input.Keys.A:
                moveLeft = true;
                wasHandled = true;
                break;

            case Input.Keys.D:
                moveRight = true;
                wasHandled = true;
                break;

            case Input.Keys.Q:
                moveDown = true;
                wasHandled = true;
                break;

            case Input.Keys.E:
                moveUp = true;
                wasHandled = true;
                break;

            case Input.Keys.LEFT:
                turnLeft = true;
                wasHandled = true;
                break;

            case Input.Keys.RIGHT:
                turnRight = true;
                wasHandled = true;
                break;

            case Input.Keys.UP:
                turnDown = true;
                wasHandled = true;
                break;

            case Input.Keys.DOWN:
                turnUp = true;
                wasHandled = true;
                break;

            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                moveFast = true;
                break;

            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
                break;
        }

        return wasHandled;
    }

    @Override
    public boolean keyUp(int keycode) {
        boolean wasHandled = false;
        switch (keycode) {
            case Input.Keys.W:
                moveForward = false;
                wasHandled = true;
                break;

            case Input.Keys.S:
                moveBackward = false;
                wasHandled = true;
                break;

            case Input.Keys.A:
                moveLeft = false;
                wasHandled = true;
                break;

            case Input.Keys.D:
                moveRight = false;
                wasHandled = true;
                break;

            case Input.Keys.Q:
                moveDown = false;
                wasHandled = true;
                break;

            case Input.Keys.E:
                moveUp = false;
                wasHandled = true;
                break;

            case Input.Keys.LEFT:
                turnLeft = true;
                wasHandled = true;
                break;

            case Input.Keys.RIGHT:
                turnRight = false;
                wasHandled = true;
                break;

            case Input.Keys.UP:
                turnDown = false;
                wasHandled = true;
                break;

            case Input.Keys.DOWN:
                turnUp = false;
                wasHandled = true;
                break;

            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                moveFast = false;
                break;

            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
                break;
        }

        return wasHandled;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
