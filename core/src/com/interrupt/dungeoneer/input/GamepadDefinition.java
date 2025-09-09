package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;

import java.util.Hashtable;

public class GamepadDefinition {
	public static Hashtable<Actions.Action, GamepadBinding> gamepadBindings = new Hashtable<>();

    public GamepadDefinition(Controller controller) {
        ControllerMapping mapping = controller.getMapping();

        gamepadBindings.put(Actions.Action.USE, new GamepadBinding(mapping.buttonA, GamepadBinding.GamepadInputType.BUTTON));
        gamepadBindings.put(Actions.Action.MAP, new GamepadBinding(mapping.buttonY, GamepadBinding.GamepadInputType.BUTTON));
        gamepadBindings.put(Actions.Action.PAUSE, new GamepadBinding(mapping.buttonStart, GamepadBinding.GamepadInputType.BUTTON));
        gamepadBindings.put(Actions.Action.INVENTORY, new GamepadBinding(mapping.buttonX, GamepadBinding.GamepadInputType.BUTTON));

        gamepadBindings.put(Actions.Action.MENU_SELECT, new GamepadBinding(mapping.buttonA, GamepadBinding.GamepadInputType.BUTTON));
        gamepadBindings.put(Actions.Action.MENU_CANCEL, new GamepadBinding(mapping.buttonB, GamepadBinding.GamepadInputType.BUTTON));

        gamepadBindings.put(Actions.Action.ITEM_PREVIOUS, new GamepadBinding(mapping.buttonL1, GamepadBinding.GamepadInputType.BUTTON));
        gamepadBindings.put(Actions.Action.ITEM_NEXT, new GamepadBinding(mapping.buttonR1, GamepadBinding.GamepadInputType.BUTTON));

        gamepadBindings.put(Actions.Action.STRAFE_LEFT, new GamepadBinding(mapping.axisLeftX, GamepadBinding.GamepadInputType.AXIS, -1));
        gamepadBindings.put(Actions.Action.STRAFE_RIGHT, new GamepadBinding(mapping.axisLeftX, GamepadBinding.GamepadInputType.AXIS, 1));

        gamepadBindings.put(Actions.Action.FORWARD, new GamepadBinding(mapping.axisLeftY, GamepadBinding.GamepadInputType.AXIS, -1));
        gamepadBindings.put(Actions.Action.BACKWARD, new GamepadBinding(mapping.axisLeftY, GamepadBinding.GamepadInputType.AXIS, 1));

        gamepadBindings.put(Actions.Action.TURN_LEFT, new GamepadBinding(mapping.axisRightX, GamepadBinding.GamepadInputType.AXIS, -1));
        gamepadBindings.put(Actions.Action.TURN_RIGHT, new GamepadBinding(mapping.axisRightX, GamepadBinding.GamepadInputType.AXIS, 1));

        gamepadBindings.put(Actions.Action.LOOK_UP, new GamepadBinding(mapping.axisRightY, GamepadBinding.GamepadInputType.AXIS, -1));
        gamepadBindings.put(Actions.Action.LOOK_DOWN, new GamepadBinding(mapping.axisRightY, GamepadBinding.GamepadInputType.AXIS, 1));

        gamepadBindings.put(Actions.Action.ATTACK, new GamepadBinding(mapping.buttonR2, GamepadBinding.GamepadInputType.AXIS, -1));
        gamepadBindings.put(Actions.Action.DROP, new GamepadBinding(mapping.buttonL2, GamepadBinding.GamepadInputType.AXIS, 1));
    }
}
