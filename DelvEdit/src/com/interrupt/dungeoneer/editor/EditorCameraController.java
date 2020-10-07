package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.helpers.AnimationHelper;
import com.interrupt.helpers.InterpolationHelper;

/** Subsystem for controlling and positioning the editor camera. */
public class EditorCameraController extends InputAdapter implements EditorSubsystem {
    final Vector3 position = new Vector3(4, 4, 4);
    final Vector2 rotation = new Vector2((float) Math.PI / 4, 0.7f);

    float orbitDistance = 4.0f;

    double yawAcceleration = 0;
    double pitchAcceleration = 0;

    double walkSpeed = 0.15;
    double rotSpeed = 0.005;

    int scrollAmount;

    AnimationHelper animationHelper;

    public EditorCameraController() {
    }

    @Override
    public void init() {
        Editor.app.editorInput.addListener(this);
    }

    @Override
    public void dispose() {
        Editor.app.editorInput.removeListener(this);
    }

    @Override
    public void draw() {
        PerspectiveCamera camera = Editor.app.camera;

        camera.direction.set(0, 0, 1);
        camera.up.set(0, 1, 0);
        camera.rotate((float) Math.toDegrees(rotation.y), 1f, 0, 0);
        camera.rotate((float) Math.toDegrees(rotation.x), 0, 1f, 0);
        camera.update();
    }

    private boolean inRotateMode() {
        return Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) || (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT));
    }

    private boolean inOrbitMode() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && inRotateMode();
    }

    private boolean inPanMode() {
        return !Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
    }

    private boolean canRotateWithArrowKeys() {
        return !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
    }

    private boolean inFastMode() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
    }

    @Override
    public void tick() {
        PerspectiveCamera camera = Editor.app.camera;
        EditorInput input = Editor.app.editorInput;

        if (canRotateWithArrowKeys()) {
            yawAcceleration += Gdx.input.isKeyPressed(Input.Keys.LEFT) ? rotSpeed : 0;
            yawAcceleration -= Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? rotSpeed : 0;

            pitchAcceleration += Gdx.input.isKeyPressed(Input.Keys.DOWN) ? rotSpeed : 0;
            pitchAcceleration -= Gdx.input.isKeyPressed(Input.Keys.UP) ? rotSpeed : 0;
        }

        rotation.x += yawAcceleration;
        yawAcceleration *= 0.8;

        rotation.y += pitchAcceleration;
        pitchAcceleration *= 0.8;

        rotation.y = (float)Math.min(Math.PI / 2, Math.max(-Math.PI / 2, rotation.y));

        if (animationHelper != null && !animationHelper.isDonePlaying()) { // Animation first, do not allow movement if animating.
            animationHelper.tickAnimation(Gdx.graphics.getDeltaTime());
            position.set(animationHelper.getCurrentPosition());
        } else if (inOrbitMode()) {
            // Calculate the next camera direction vector.
            Vector3 cameraNewDirection = new Vector3(0, 0, 1);

            cameraNewDirection.rotate((float) Math.toDegrees(rotation.y), 1f, 0, 0);
            cameraNewDirection.rotate((float) Math.toDegrees(rotation.x), 0, 1f, 0);

            // Calculate the orbit pivot.
            orbitDistance = Math.max(0, orbitDistance);

            Vector3 pivotPosition = new Vector3(camera.direction).scl(orbitDistance).add(camera.position);

            // Calculate new camera position.
            cameraNewDirection.scl(-orbitDistance);
            cameraNewDirection.add(pivotPosition);

            position.set(cameraNewDirection.x, cameraNewDirection.z, cameraNewDirection.y);
        } else if (inPanMode()) {
            float panSpeed = (float) walkSpeed * 0.1f;

            // Compute upward and sideways movement vectors, scale them by scroll amount.
            Vector3 vMove = camera.up.cpy().scl(Gdx.input.getDeltaY() * panSpeed);
            Vector3 hMove = camera.up.cpy().crs(camera.direction).scl(Gdx.input.getDeltaX() * panSpeed);

            position.x += vMove.x + hMove.x;
            position.y += vMove.z + hMove.z;
            position.z += vMove.y + hMove.y;
        } else {
            float xMovement = 0f;
            float zMovement = 0f;

            if (input.isKeyPressed(Input.Keys.A)) {
                xMovement += 1f;
            }
            if (input.isKeyPressed(Input.Keys.D)) {
                xMovement -= 1f;
            }

            if (input.isKeyPressed(Input.Keys.W) || scrollAmount < 0) {
                zMovement += 1f + -scrollAmount;
            }
            if (input.isKeyPressed(Input.Keys.S) || scrollAmount > 0) {
                zMovement -= 1f + scrollAmount;
            }

            scrollAmount = 0;

            if (inFastMode()) {
                xMovement *= 2.0f;
                zMovement *= 2.0f;
            } else {
                if (input.isKeyPressed(Input.Keys.Q)) {
                    position.z -= 0.1f;
                }
                if (input.isKeyPressed(Input.Keys.E)) {
                    position.z += 0.1f;
                }
            }

            orbitDistance -= zMovement * walkSpeed;

            position.z -= zMovement * Math.sin(rotation.y) * walkSpeed;

            zMovement *= Math.cos(rotation.y); // Project onto grid plane for lateral movement adjustment.

            position.x += (zMovement * Math.sin(rotation.x) + xMovement * Math.cos(-rotation.x)) * walkSpeed;
            position.y += (zMovement * Math.cos(rotation.x) + xMovement * Math.sin(-rotation.x)) * walkSpeed;

            updatePlayer(xMovement, zMovement); // Adjust player rotation and movement.
        }

        camera.position.set(position.x, position.z, position.y);
    }

    private void updatePlayer(float xMovement, float zMovement) {
        Player player = Editor.app.player;

        if (player != null) {
            player.rot = rotation.x;
            player.yrot = rotation.y;

            player.xa += (xMovement * Math.cos(rotation.x) - zMovement * Math.sin(rotation.x)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
            player.rota += (zMovement * Math.cos(rotation.x) + xMovement * Math.sin(rotation.x)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
        }
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Vector2 getRotation() {
        return rotation;
    }

    public void setRotation(float x, float y) {
        rotation.set(x, y);
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (inRotateMode()) { // Normal rotation, pan mode disabled (SHIFT_LEFT released)
            if (!Gdx.input.isCursorCatched()) {
                Gdx.input.setCursorCatched(true);
            }

            rotation.x -= Gdx.input.getDeltaX() * rotSpeed;
            rotation.y += Gdx.input.getDeltaY() * rotSpeed;
        }

        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        scrollAmount += amount;

        return false;
    }

    /** Position the camera to view the current selection. */
    public void viewSelected() {
        PerspectiveCamera camera = Editor.app.camera;
        Level level = Editor.app.level;

        float minDistance = 3.0f;

        // Default to framing up level grid.
        Vector3 selectedPosition = new Vector3(level.width / 2f, level.height / 2f, 0);
        orbitDistance = selectedPosition.len();

        // Focus on picked entity.
        if (Editor.selection.picked != null) {
            orbitDistance = Editor.app.getEntityBoundingSphereRadius(Editor.selection.picked) * 1.5f / (float) Math.tan(Math.toRadians(camera.fieldOfView) / 2);
            orbitDistance = Math.max(minDistance, orbitDistance);
            selectedPosition.set(Editor.selection.picked.x, Editor.selection.picked.y, Editor.selection.picked.z);
        }
        // Focus on tile selection.
        else if (Editor.app.selected) {
            BoundingBox bounds = Editor.selection.tiles.getBounds();

            Vector3 size = new Vector3();
            bounds.getDimensions(size);
            bounds.getCenter(selectedPosition);

            orbitDistance = size.len() * 2;
        }

        Vector3 cameraOffset = new Vector3(camera.direction.x, camera.direction.z, camera.direction.y).scl(orbitDistance);
        Vector3 finalPosition = new Vector3(selectedPosition).sub(cameraOffset);
        moveTo(finalPosition);
    }

    /** Smoothly move to given destination. */
    public void moveTo(Vector3 destination) {
        animationHelper = new AnimationHelper(
                position,
                Vector3.Zero,
                destination,
                Vector3.Zero,
                0.5f,
                InterpolationHelper.InterpolationMode.exp10Out
        );
    }

    public void setDefaultPositionAndRotation() {
        Level level = Editor.app.level;
        int width = level.width;
        int height = level.height;

        float altitude = 6f;
        float distance = 10f;

        float lookAngle = (float)Math.asin((float)altitude/distance);
        float projectedDistance = distance * (float)Math.cos((float)lookAngle);
        float position = projectedDistance / 1.4142f;

        setPosition(position + width / 2, position + height / 2, altitude);
        setRotation((float)Math.PI + (float)Math.PI / 4f, lookAngle);
    }
}
