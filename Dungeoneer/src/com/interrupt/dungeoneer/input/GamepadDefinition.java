package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.controllers.mappings.Ouya;

import java.util.Hashtable;

public class GamepadDefinition {

	public static Hashtable<Actions.Action, GamepadBinding> gamepadBindings = new Hashtable<Actions.Action, GamepadBinding>();
	public GamepadDefinition() { }

	public GamepadDefinition(Ouya ouya) {
		gamepadBindings.put(Actions.Action.USE, new GamepadBinding(Ouya.BUTTON_O, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.INVENTORY, new GamepadBinding(Ouya.BUTTON_U, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MAP, new GamepadBinding(Ouya.BUTTON_Y, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.MENU_SELECT, new GamepadBinding(Ouya.BUTTON_O, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MENU_CANCEL, new GamepadBinding(Ouya.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.ITEM_PREVIOUS, new GamepadBinding(Ouya.BUTTON_L2, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.ITEM_NEXT, new GamepadBinding(Ouya.BUTTON_R2, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.STRAFE_LEFT, new GamepadBinding(Ouya.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.STRAFE_RIGHT, new GamepadBinding(Ouya.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.FORWARD, new GamepadBinding(Ouya.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.BACKWARD, new GamepadBinding(Ouya.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.TURN_LEFT, new GamepadBinding(Ouya.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.TURN_RIGHT, new GamepadBinding(Ouya.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.LOOK_UP, new GamepadBinding(Ouya.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.LOOK_DOWN, new GamepadBinding(Ouya.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.DROP, new GamepadBinding(Ouya.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
		gamepadBindings.put(Actions.Action.ATTACK, new GamepadBinding(Ouya.AXIS_LEFT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
	}
	
	public GamepadDefinition(Xbox360Pad pad) {
		gamepadBindings.put(Actions.Action.USE, new GamepadBinding(Xbox360Pad.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MAP, new GamepadBinding(Xbox360Pad.BUTTON_Y, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.PAUSE, new GamepadBinding(Xbox360Pad.BUTTON_START, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.INVENTORY, new GamepadBinding(Xbox360Pad.BUTTON_X, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.MENU_SELECT, new GamepadBinding(Xbox360Pad.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MENU_CANCEL, new GamepadBinding(Xbox360Pad.BUTTON_B, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.ITEM_PREVIOUS, new GamepadBinding(Xbox360Pad.BUTTON_LB, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.ITEM_NEXT, new GamepadBinding(Xbox360Pad.BUTTON_RB, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.STRAFE_LEFT, new GamepadBinding(Xbox360Pad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.STRAFE_RIGHT, new GamepadBinding(Xbox360Pad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.FORWARD, new GamepadBinding(Xbox360Pad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.BACKWARD, new GamepadBinding(Xbox360Pad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.TURN_LEFT, new GamepadBinding(Xbox360Pad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.TURN_RIGHT, new GamepadBinding(Xbox360Pad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.LOOK_UP, new GamepadBinding(Xbox360Pad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.LOOK_DOWN, new GamepadBinding(Xbox360Pad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.ATTACK, new GamepadBinding(Xbox360Pad.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.DROP, new GamepadBinding(Xbox360Pad.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
	}
	
	public GamepadDefinition(Xbox360OSXPad pad) {
		gamepadBindings.put(Actions.Action.USE, new GamepadBinding(Xbox360OSXPad.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MAP, new GamepadBinding(Xbox360OSXPad.BUTTON_Y, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.PAUSE, new GamepadBinding(Xbox360OSXPad.BUTTON_START, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.INVENTORY, new GamepadBinding(Xbox360OSXPad.BUTTON_X, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.MENU_SELECT, new GamepadBinding(Xbox360OSXPad.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MENU_CANCEL, new GamepadBinding(Xbox360OSXPad.BUTTON_B, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.ITEM_PREVIOUS, new GamepadBinding(Xbox360OSXPad.BUTTON_LB, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.ITEM_NEXT, new GamepadBinding(Xbox360OSXPad.BUTTON_RB, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.STRAFE_LEFT, new GamepadBinding(Xbox360OSXPad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.STRAFE_RIGHT, new GamepadBinding(Xbox360OSXPad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.FORWARD, new GamepadBinding(Xbox360OSXPad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.BACKWARD, new GamepadBinding(Xbox360OSXPad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.TURN_LEFT, new GamepadBinding(Xbox360OSXPad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.TURN_RIGHT, new GamepadBinding(Xbox360OSXPad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.LOOK_UP, new GamepadBinding(Xbox360OSXPad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.LOOK_DOWN, new GamepadBinding(Xbox360OSXPad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.ATTACK, new GamepadBinding(Xbox360OSXPad.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.DROP, new GamepadBinding(Xbox360OSXPad.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
	}
	
	public GamepadDefinition(SonyPad pad) {
		gamepadBindings.put(Actions.Action.USE, new GamepadBinding(SonyPad.BUTTON_X, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.INVENTORY, new GamepadBinding(SonyPad.BUTTON_SQUARE, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MAP, new GamepadBinding(SonyPad.BUTTON_TRIANGLE, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.PAUSE, new GamepadBinding(SonyPad.BUTTON_START, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.MENU_SELECT, new GamepadBinding(SonyPad.BUTTON_X, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MENU_CANCEL, new GamepadBinding(SonyPad.BUTTON_O, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.ITEM_PREVIOUS, new GamepadBinding(SonyPad.BUTTON_LB, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.ITEM_NEXT, new GamepadBinding(SonyPad.BUTTON_RB, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.STRAFE_LEFT, new GamepadBinding(SonyPad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.STRAFE_RIGHT, new GamepadBinding(SonyPad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.FORWARD, new GamepadBinding(SonyPad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.BACKWARD, new GamepadBinding(SonyPad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.TURN_LEFT, new GamepadBinding(SonyPad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.TURN_RIGHT, new GamepadBinding(SonyPad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.LOOK_UP, new GamepadBinding(SonyPad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.LOOK_DOWN, new GamepadBinding(SonyPad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.ATTACK, new GamepadBinding(SonyPad.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
		gamepadBindings.put(Actions.Action.DROP, new GamepadBinding(SonyPad.AXIS_LEFT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
	}
	
	public GamepadDefinition(MogaProPad pad) {
		gamepadBindings.put(Actions.Action.USE, new GamepadBinding(MogaProPad.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.INVENTORY, new GamepadBinding(MogaProPad.BUTTON_X, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MAP, new GamepadBinding(MogaProPad.BUTTON_Y, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.PAUSE, new GamepadBinding(MogaProPad.BUTTON_START, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.MENU_SELECT, new GamepadBinding(MogaProPad.BUTTON_A, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.MENU_CANCEL, new GamepadBinding(MogaProPad.BUTTON_B, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.ITEM_PREVIOUS, new GamepadBinding(MogaProPad.BUTTON_LB, GamepadBinding.GamepadInputType.BUTTON));
		gamepadBindings.put(Actions.Action.ITEM_NEXT, new GamepadBinding(MogaProPad.BUTTON_RB, GamepadBinding.GamepadInputType.BUTTON));

		gamepadBindings.put(Actions.Action.STRAFE_LEFT, new GamepadBinding(MogaProPad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.STRAFE_RIGHT, new GamepadBinding(MogaProPad.AXIS_LEFT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.FORWARD, new GamepadBinding(MogaProPad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.BACKWARD, new GamepadBinding(MogaProPad.AXIS_LEFT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.TURN_LEFT, new GamepadBinding(MogaProPad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.TURN_RIGHT, new GamepadBinding(MogaProPad.AXIS_RIGHT_X, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.LOOK_UP, new GamepadBinding(MogaProPad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.LOOK_DOWN, new GamepadBinding(MogaProPad.AXIS_RIGHT_Y, GamepadBinding.GamepadInputType.AXIS, 1));

		gamepadBindings.put(Actions.Action.ATTACK, new GamepadBinding(MogaProPad.AXIS_RIGHT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, -1));
		gamepadBindings.put(Actions.Action.DROP, new GamepadBinding(MogaProPad.AXIS_LEFT_TRIGGER, GamepadBinding.GamepadInputType.AXIS, 1));
	}
}
