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
    final Vector3 position = new Vector3(7.5f, 8f, 6.5f);
    final Vector2 rotation = new Vector2(3.14159f, 1.4f);

    float orbitDistance = 4.0f;
    
    double rota = 0;
	double ya = 0;
	float yClamp = 1.571f;
	
    float scrollSpeed = 0.4f;
	float za = 0f;

	double walkSpeed = 0.15;
	double rotSpeed = 0.009;
	double maxRot = 0.8;

	int scrollAmount;

	AnimationHelper animationHelper;

    public EditorCameraController() {}
    
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
		camera.rotate(rotation.y * 57.2957795f, 1f, 0, 0);
		camera.rotate((float)(rotation.x + 3.14) * 57.2957795f, 0, 1f, 0);
		camera.update();
    }

    @Override
    public void tick() {
        PerspectiveCamera camera = Editor.app.camera;
        EditorInput input = Editor.app.editorInput;

        boolean turnLeft = (Gdx.input.getDeltaX() < 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
		boolean turnRight = (Gdx.input.getDeltaX() > 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
		boolean turnUp = (Gdx.input.getDeltaY() > 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));
		boolean turnDown = (Gdx.input.getDeltaY() < 0 && Gdx.input.isButtonPressed(Input.Buttons.MIDDLE));

		turnLeft |= Gdx.input.isKeyPressed(Input.Keys.LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
		turnRight |= Gdx.input.isKeyPressed(Input.Keys.RIGHT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
		turnUp |= Gdx.input.isKeyPressed(Input.Keys.DOWN) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
		turnDown |= Gdx.input.isKeyPressed(Input.Keys.UP) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

		if(turnLeft) {
			rota += rotSpeed;
			if(rota > maxRot) rota = maxRot;
		}
		else if(turnRight) {
			rota -= rotSpeed;
			if(rota < -maxRot) rota = -maxRot;
		}

		rotation.x += rota;
		rota *= 0.8;

		if(turnUp) {
			ya += rotSpeed * 0.6f;
			if(ya > maxRot) ya = maxRot;
		}
		else if(turnDown) {
			ya -= rotSpeed * 0.6f;
			if(ya < -maxRot) ya = -maxRot;
		}

		rotation.y += ya;

		if (rotation.y < -yClamp) rotation.y = -yClamp;
		if (rotation.y > yClamp) rotation.y = yClamp;

		ya *= 0.8;

		float xm = 0f;
		float zm = 0f;

		if(input.isKeyPressed(Input.Keys.A)) {
			xm = -1f;
		}
		if(input.isKeyPressed(Input.Keys.D)) {
			xm = 1f;
		}

		if(input.isKeyPressed(Input.Keys.W) || scrollAmount < 0) {
			zm = -1f;
		}
		if(input.isKeyPressed(Input.Keys.S) || scrollAmount > 0) {
			zm = 1f;
		}

		if (scrollAmount < 0) {
			za -= scrollSpeed;
		}
		else if (scrollAmount > 0) {
			za += scrollSpeed;
		}

		zm += za;

		za *= 0.8f;

		if(input.isKeyPressed(Input.Keys.Q) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			position.z -= 0.1f;
		}
		if(input.isKeyPressed(Input.Keys.E) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			position.z += 0.1f;
		}

		if (input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			xm *= 2.0f;
			zm *= 2.0f;
		}

		orbitDistance += zm * walkSpeed;

		position.z += (zm * Math.sin(rotation.y)) * walkSpeed;
		zm *= Math.cos(rotation.y);
		position.x += (xm * Math.cos(rotation.x) + zm * Math.sin(rotation.x)) * walkSpeed;
		position.y += (zm * Math.cos(rotation.x) - xm * Math.sin(rotation.x)) * walkSpeed;

		Player player = Editor.app.player;
		if(player != null) {
			player.rot = rotation.x;
			player.yrot = rotation.y;

			player.xa += (xm * Math.cos(rotation.x) + zm * Math.sin(rotation.x)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
			player.ya += (zm * Math.cos(rotation.x) - xm * Math.sin(rotation.x)) * 0.025f * Math.min(player.friction * 1.4f, 1f);
		}

		if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && (input.isButtonPressed(Input.Buttons.RIGHT) || turnLeft || turnRight || turnUp || turnDown)) {
			// Calculate the next camera direction vector;
			Vector3 cameraNewDirection = new Vector3(0, 0, 1);
			cameraNewDirection.rotate(rotation.y * 57.2957795f, 1f, 0, 0);
			cameraNewDirection.rotate((float)(rotation.x + 3.14) * 57.2957795f, 0, 1f, 0);
			cameraNewDirection.nor();

			// Calculate the orbit pivot.
			if (orbitDistance < 0) {
				orbitDistance = 3.0f;
			}

			Vector3 pivotPosition = new Vector3(camera.direction).scl(orbitDistance).add(camera.position);

			// Calculate new camera position.
			cameraNewDirection.scl(-orbitDistance);
			cameraNewDirection.add(pivotPosition);

			position.set(cameraNewDirection.x, cameraNewDirection.z, cameraNewDirection.y);
		}

		if (animationHelper != null && !animationHelper.isDonePlaying()) {
			animationHelper.tickAnimation(Gdx.graphics.getDeltaTime());
			position.set(animationHelper.getCurrentPosition());
		}

        camera.position.set(position.x, position.z, position.y);

        scrollAmount = 0;
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
    public boolean touchDown(int x, int y, int pointer, int button) {
        lastMouseLocation.set(x, y);

        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        lastMouseLocation.set(x, y);

        return false;
    }

    private final Vector2 lastMouseLocation = new Vector2();
    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            Editor.app.editorInput.ignoreRightClick = true;

            float moveX = (lastMouseLocation.x - x);
            float moveY = (lastMouseLocation.y - y);

            if(moveX >= 1 || moveY >= 1) {
                if (!Gdx.input.isCursorCatched()) {
                    Gdx.input.setCursorCatched(true);
                }
            }

            rotation.x += moveX * 0.005f;
            rotation.y -= moveY * 0.005f;
        }

        lastMouseLocation.set(x, y);

        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        scrollAmount = amount;

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

		// Focus on picked entity
		if (Editor.selection.picked != null) {
			orbitDistance = Editor.app.getEntityBoundingSphereRadius(Editor.selection.picked) * 1.5f / (float)Math.tan(Math.toRadians(camera.fieldOfView) / 2);
			orbitDistance = Math.max(minDistance, orbitDistance);
			selectedPosition.set(Editor.selection.picked.x, Editor.selection.picked.y, Editor.selection.picked.z);
		}
		// Focus on tile selection
		else if (Editor.app.selected) {
			BoundingBox bounds = Editor.selection.tiles.getBounds();

			Vector3 size = new Vector3();
			bounds.getDimensions(size);
			orbitDistance = size.len();

			bounds.getCenter(selectedPosition);
		}

		Vector3 cameraOffset = new Vector3(camera.direction.x,camera.direction.z,camera.direction.y).scl(orbitDistance);
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
}
