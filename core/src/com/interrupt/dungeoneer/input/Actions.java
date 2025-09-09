package com.interrupt.dungeoneer.input;

import java.util.Hashtable;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;

public class Actions {
	public enum Action {
		USE,
		ATTACK,
		DROP,
		INVENTORY,
		ITEM_NEXT,
		ITEM_PREVIOUS,
		MAP,
		FORWARD,
		BACKWARD,
		STRAFE_LEFT,
		STRAFE_RIGHT,
		TURN_LEFT,
		TURN_RIGHT,
		LOOK_UP,
		LOOK_DOWN,
		MENU_SELECT,
		MENU_CANCEL,
		PAUSE,
		JUMP
	}

	public static Hashtable<Action, Integer> keyBindings = new Hashtable<Action, Integer>();
	public static Hashtable<Action, GamepadBinding> gamepadBindings = new Hashtable<Action, GamepadBinding>();
	public static Array<Action> keyOrder = new Array<Action>();
	
	static {
		keyBindings.put(Action.USE, Keys.E);
		keyBindings.put(Action.ATTACK, Keys.SPACE);
		keyBindings.put(Action.DROP, Keys.Q);
		keyBindings.put(Action.INVENTORY, Keys.I);
		keyBindings.put(Action.ITEM_NEXT, Keys.RIGHT_BRACKET);
		keyBindings.put(Action.ITEM_PREVIOUS, Keys.LEFT_BRACKET);
		keyBindings.put(Action.MAP, Keys.M);
		keyBindings.put(Action.PAUSE, Keys.ESCAPE);
		keyBindings.put(Action.FORWARD, Keys.W);
		keyBindings.put(Action.BACKWARD, Keys.S);
		keyBindings.put(Action.STRAFE_LEFT, Keys.A);
		keyBindings.put(Action.STRAFE_RIGHT, Keys.D);
		keyBindings.put(Action.TURN_LEFT, Keys.LEFT);
		keyBindings.put(Action.TURN_RIGHT, Keys.RIGHT);
		keyBindings.put(Action.LOOK_UP, Keys.UP);
		keyBindings.put(Action.LOOK_DOWN, Keys.DOWN);
		
		keyOrder.add(Action.USE);
		keyOrder.add(Action.ATTACK);
		keyOrder.add(Action.DROP);
		keyOrder.add(Action.INVENTORY);
		keyOrder.add(Action.ITEM_NEXT);
		keyOrder.add(Action.ITEM_PREVIOUS);
		keyOrder.add(Action.MAP);
		keyOrder.add(Action.PAUSE);
		keyOrder.add(Action.FORWARD);
		keyOrder.add(Action.BACKWARD);
		keyOrder.add(Action.STRAFE_LEFT);
		keyOrder.add(Action.STRAFE_RIGHT);
		keyOrder.add(Action.TURN_LEFT);
		keyOrder.add(Action.TURN_RIGHT);
		keyOrder.add(Action.LOOK_UP);
		keyOrder.add(Action.LOOK_DOWN);
	}
}
