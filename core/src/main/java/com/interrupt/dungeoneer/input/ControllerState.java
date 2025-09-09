package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class ControllerState {
	public enum MenuButtons { SELECT, CANCEL}
	public enum Axis { MOVE_X, MOVE_Y, LOOK_X, LOOK_Y, TRIGGER_LEFT, TRIGGER_RIGHT }
	public enum DPAD { LEFT, RIGHT, UP, DOWN }
	
	public Array<Actions.Action> buttonEvents = new Array<Actions.Action>();
	public Array<MenuButtons> menuButtonEvents = new Array<MenuButtons>();
	public Array<DPAD> dpadEvents = new Array<DPAD>();

	public Array<MenuButtons> lastMenuButtonEvents = new Array<MenuButtons>();
	
	public Vector2 rawMove = new Vector2();
	public Vector2 rawLook = new Vector2();
	
	public Vector2 controllerMove = new Vector2();
	public Vector2 controllerLook = new Vector2();
	
	public boolean use;
	public boolean attack;
	public boolean drop;
	public boolean map;
	public boolean inventory;
	public boolean pause;

	public void clearEvents() {
		this.buttonEvents.clear();
		this.menuButtonEvents.clear();
		this.dpadEvents.clear();
	}

	public void resetState() {
		this.use = false;
		this.attack = false;
		this.drop = false;
		this.map = false;
		this.inventory = false;
		this.pause = false;
	}
}
