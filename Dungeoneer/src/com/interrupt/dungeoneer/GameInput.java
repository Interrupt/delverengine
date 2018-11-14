package com.interrupt.dungeoneer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.IntArray;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.dungeoneer.input.GamepadManager;

public class GameInput implements InputProcessor {
	
	public boolean[] keysDown = new boolean[300];
	public IntArray keyEvents = new IntArray(10);
	
	public Integer leftPointer;
	public Integer rightPointer;
	public Integer uiTouchPointer;
	
	private Vector2 leftTouchPos = new Vector2();
	private Vector2 rightTouchPos = new Vector2();
	
	private Vector2 uiTouchPos = new Vector2();
	
	public boolean caughtCursor = true;
	
	private Stage menuUi = null;
	
	public Integer lastTouchedPointer = null;
	
	private boolean isPressedMouse1=false;
	private boolean isPressedMouse2=false;
	private boolean isPressedMouse3=false;
	private boolean wasPressedMouse1=false;
	private boolean wasPressedMouse2=false;
	private boolean wasPressedMouse3=false;
	private boolean newlyPressedMouse1=false;
	private boolean newlyPressedMouse2=false;
	private boolean newlyPressedMouse3=false;
	private boolean newlyMouseScrollUp=false;
	private boolean newlyMouseScrollDown=false;
	
	// gamepad stuff
	public GamepadManager gamepadManager = null;
	public boolean usingGamepad = false;
	public boolean showingGamepadCursor = false;
	public Vector2 gamepadCursorPosition = new Vector2();
	public final int gamepadPointerNum = 0;
	
	public void setGamepadManager(GamepadManager gamepadManager) {
		this.gamepadManager = gamepadManager;
		gamepadCursorPosition = new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
	}
	
	public void tick() {
		keyEvents.clear();
		//Store previous tick mouse buttons states
		wasPressedMouse1=isPressedMouse1;
		wasPressedMouse2=isPressedMouse2;
		wasPressedMouse3=isPressedMouse3;
		//Determine current tick mouse buttons states
		isPressedMouse1=Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		isPressedMouse2=Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
		isPressedMouse3=Gdx.input.isButtonPressed(Input.Buttons.MIDDLE);
		//Determine current tick mouse buttons changes
		newlyPressedMouse1=isPressedMouse1&!wasPressedMouse1;
		newlyPressedMouse2=isPressedMouse2&!wasPressedMouse2;
		newlyPressedMouse3=isPressedMouse3&!wasPressedMouse3;
		
		//reset mouse scroll wheel
		newlyMouseScrollUp=false;
		newlyMouseScrollDown=false;

		if(!Gdx.input.isCursorCatched()) {
			ignoreLastMouseLocation = true;
		}
	}

	@Override
	public boolean keyDown(int code) {
		usingGamepad = false;
		
		if (code > 0 && code < keysDown.length) {
			if(keysDown[code] == false) keyEvents.add(code);
			keysDown[code] = true;
		}
		
		return false;
	}

	@Override
	public boolean keyTyped(char c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int code) {
		usingGamepad = false;
		
		if (code > 0 && code < keysDown.length) {
			keysDown[code] = false;
		}
		
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		newlyMouseScrollDown=amount==-1;
		newlyMouseScrollUp=amount==1;
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		
		lastTouchedPointer = pointer;
		
		if(menuUi != null) menuUi.touchDown(x, y, pointer, button);
		
		// reset any pointers that match
		if(leftPointer != null && pointer == leftPointer) leftPointer = null;
		if(rightPointer != null && pointer == rightPointer) rightPointer = null;
		if(uiTouchPointer != null && pointer == uiTouchPointer) uiTouchPointer = null;
				
		if(Game.ui != null && !Game.ui.touchDown(x, y, pointer, button)) {
			if(x < Gdx.app.getGraphics().getWidth() / 2.0) {
				if(leftPointer == null) {
					leftPointer = pointer;
					leftTouchPos.x = x;
					leftTouchPos.y = y;
				}
			}
			else {
				if(rightPointer == null) {
					rightPointer = pointer;
					rightTouchPos.x = x;
					rightTouchPos.y = y;
				}
			}
		}
		else
		{
			uiTouchPos.x = x;
			uiTouchPos.y = y;
			uiTouchPointer = pointer;
		}
		
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		usingGamepad = false;
		
		if(menuUi != null)
			menuUi.touchDragged(x, y, pointer);
		
		if(Game.ui != null)
			Game.ui.touchDragged(x, y, pointer);

		return mouseMoved(x, y);
	}

	private Vector2 lastMouseLocation = null;
	public boolean ignoreLastMouseLocation = false;

	@Override
	public boolean mouseMoved(int x, int y) {

		if(lastMouseLocation == null)
			lastMouseLocation = new Vector2(x, y);
		else if(ignoreLastMouseLocation) {
			lastMouseLocation.set(x, y);
			ignoreLastMouseLocation = false;
		}

		if(menuUi != null)
			menuUi.mouseMoved(x, y);
		
		if(Game.ui != null)
			Game.ui.mouseMoved(x, y);

		if(Game.instance != null && Game.instance.player != null) {
			Game.instance.player.rotateCamera(x - (int) lastMouseLocation.x, y - (int) lastMouseLocation.y, caughtCursor);
		}

		lastMouseLocation.set(x, y);

		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if(menuUi != null)
			menuUi.touchUp(x, y, pointer, button);
		
		if(leftPointer != null && pointer == leftPointer) leftPointer = null;
		if(rightPointer != null && pointer == rightPointer) rightPointer = null;
		if(uiTouchPointer != null && pointer == uiTouchPointer) uiTouchPointer = null;
		
		if(Game.ui != null)
			Game.ui.touchUp(x, y, pointer, button);
		
		return false;
	}
	
	public float getLeftDeltaX()
	{
		if(leftPointer == null) return 0;
		return Gdx.input.getDeltaX(leftPointer);
	}
	
	public float getLeftDeltaY()
	{
		if(leftPointer == null) return 0;
		return Gdx.input.getDeltaY(leftPointer);
	}
	
	public float getRightDeltaX()
	{
		if(rightPointer == null) return 0;
		return Gdx.input.getDeltaX(rightPointer);
	}
	
	public float getRightDeltaY()
	{
		if(rightPointer == null) return 0;
		return Gdx.input.getDeltaY(rightPointer);
	}
	
	public boolean isLeftTouched()
	{
		return leftPointer != null;
	}
	
	public boolean isRightTouched()
	{
		return rightPointer != null;
	}
	
	public Vector2 getLeftTouchPosition()
	{
		return leftTouchPos;
	}
	
	public Vector2 getRightTouchPosition()
	{
		return rightTouchPos;
	}
	
	public Vector2 getUiTouchPosition() {
		return uiTouchPos;
	}
	
	public void setMenuUI(Stage newUI) {
		menuUi = newUI;
	}

	public boolean checkKeyDown(Action action) {
		Integer binding = Actions.keyBindings.get(action);
		if(binding == null || binding < 0 || binding > keysDown.length) return false;
		return keysDown[binding];
	}
	
	public boolean isActionRequested(Action action){
		boolean isActionRequested = checkKeyDown(action);

		//Only trap caughtCursor for the primary button
		isActionRequested|=(caughtCursor && (Options.instance.mouseButton1Action == action) && isPressedMouse1);
		isActionRequested|=((Options.instance.mouseButton2Action == action) && isPressedMouse2);
		isActionRequested|=((Options.instance.mouseButton3Action == action) && isPressedMouse3);
		return isActionRequested;
	}
	
	public boolean isActionNewlyRequested(Action action) {
		if (action != null) {
			Integer keyBinding = Actions.keyBindings.get(action);
			if (keyBinding != null && keyEvents.contains(keyBinding)) return true;
			if (caughtCursor && Options.instance.mouseButton1Action == action && newlyPressedMouse1) return true;
			if (Options.instance.mouseButton2Action == action && newlyPressedMouse2) return true;
			if (Options.instance.mouseButton3Action == action && newlyPressedMouse3) return true;
		}
		if (gamepadManager != null && gamepadManager.controllerState.buttonEvents.contains(action, false)) return true;
		return false;
	}
	
	public boolean isMoveForwardPressed() {
		return checkKeyDown(Action.FORWARD);
	}
	
	public boolean isMoveBackwardsPressed() {
		return checkKeyDown(Action.BACKWARD);
	}
	
	public boolean isStrafeLeftPressed() {
		return checkKeyDown(Action.STRAFE_LEFT);
	}
	
	public boolean isStrafeRightPressed() {
		return checkKeyDown(Action.STRAFE_RIGHT);
	}
	
	public boolean isTurnLeftPressed() {
		return checkKeyDown(Action.TURN_LEFT);
	}
	
	public boolean isTurnRightPressed() {
		return checkKeyDown(Action.TURN_RIGHT);
	}
	
	public boolean isLookUpPressed() {
		return checkKeyDown(Action.LOOK_UP);
	}
	
	public boolean isLookDownPressed() {
		return checkKeyDown(Action.LOOK_DOWN);
	}
	
	public boolean isAttackPressed() {
		return isActionRequested(Action.ATTACK);
	}

	public boolean isDropPressed() { return checkKeyDown(Action.DROP) || gamepadManager.controllerState.drop; }

	public boolean isJumpPressed() { return isActionNewlyRequested(Action.JUMP); }
	
	public boolean doUseAction() {
		return isActionNewlyRequested(Action.USE);
	}
	
	public boolean doDropAction() {
		return isActionNewlyRequested(Action.DROP);
	}
	
	public boolean doMapAction() {
		return isActionNewlyRequested(Action.MAP);
	}
	
	public boolean doInventoryAction() {
		return isActionNewlyRequested(Action.INVENTORY);
	}
	
	public boolean doNextItemAction(){
		return isActionNewlyRequested(Action.ITEM_NEXT) || newlyMouseScrollUp;
	}
	
	public boolean doPreviousItemAction(){
		return isActionNewlyRequested(Action.ITEM_PREVIOUS) || newlyMouseScrollDown;
	}
	
	public boolean doBackAction() {
		if (isActionNewlyRequested(Action.PAUSE)) {
			return true;
		}
		if (keyEvents.contains(Input.Keys.ESCAPE)) return true;
		if (caughtCursor) {
			if (newlyPressedMouse2 || newlyPressedMouse3) return true;
		}
		return false;
	}
	
	public int getPointerX(int cursor) {
		if(showingGamepadCursor && cursor == gamepadPointerNum) return (int)gamepadCursorPosition.x;
		return Gdx.input.getX(cursor);
	}
	
	public int getPointerY(int cursor) {
		if(showingGamepadCursor && cursor == gamepadPointerNum) return Gdx.graphics.getHeight() - (int)gamepadCursorPosition.y;
		return Gdx.input.getY(cursor);
	}
	
	public int getPointerX() {
		if(showingGamepadCursor) return (int)gamepadCursorPosition.x;
		return Gdx.input.getX();
	}
	
	public int getPointerY() {
		if(showingGamepadCursor) return Gdx.graphics.getHeight() - (int)gamepadCursorPosition.y;
		return Gdx.input.getY();
	}
	
	public Vector2 getGamepadCursorPosition() {
		if(!showingGamepadCursor) return null;
		return gamepadCursorPosition;
	}
	
	public boolean justTouched() {
		if(showingGamepadCursor) return (gamepadManager != null && gamepadManager.controllerState.buttonEvents.contains(Action.USE, false));
		return Gdx.input.justTouched();
	}
	
	public boolean isPointerTouched(int cursor) {
		if(showingGamepadCursor && cursor == gamepadPointerNum) return gamepadManager.controllerState.use;
		return Gdx.input.isTouched(cursor);
	}
	
	public boolean isTouched() {
		if(showingGamepadCursor && gamepadManager.controllerState.use) return true;
		return Gdx.input.isTouched();
	}
	
	public boolean isCursorCatched() {
		if(usingGamepad) return !showingGamepadCursor;
		return Gdx.input.isCursorCatched();
	}
	
	public void setCursorCatched(boolean isCatched) {
		if(isCursorCatched() != isCatched) {
			caughtCursor = isCatched;
			if (usingGamepad) showingGamepadCursor = !isCatched;
			else Gdx.input.setCursorCatched(isCatched);
			ignoreLastMouseLocation = true;
		}
	}
	
	public void clear() {
		isPressedMouse1 = false;
		isPressedMouse2 = false;
		isPressedMouse3 = false;

		for(int i = 0; i < keysDown.length; i++) {
			keysDown[i] = false;
		}
		
		keyEvents.clear();
	}
}
