package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.input.ControllerState.DPAD;

public class GamepadManager implements ControllerListener {
	public ControllerState controllerState;
	public Interpolation moveLerp = Interpolation.fade;
	public Interpolation lookLerp = Interpolation.fade;
	public boolean menuMode = false;

	private float menuRepeatTimer = 0f;
	private boolean menuRepeated = false;
	public Float deadzoneMagnitude = 0.15f;

	private Vector2 result = new Vector2();
	private Vector2 direction = new Vector2();
	private Vector2 offset = new Vector2();

	public ArrayMap<Controller, GamepadDefinition> controllerMapping = new ArrayMap<>();

	public GamepadManager(ControllerState state) {
		controllerState = state;
	}

	public void tick(float delta) {
		if(controllerState != null) {
			controllerState.buttonEvents.clear();
			controllerState.menuButtonEvents.clear();
			controllerState.dpadEvents.clear();

			// Keep a record of some of the buttons that were pressed last time
			controllerState.lastMenuButtonEvents.clear();
			controllerState.lastMenuButtonEvents.addAll(controllerState.menuButtonEvents);

			// lerp move
			{
				int moveXSign = controllerState.rawMove.x < 0 ? -1 : 1;
				int moveYSign = controllerState.rawMove.y < 0 ? -1 : 1;

				Vector2 input = applyDeadzone(controllerState.rawMove);

				controllerState.controllerMove.x = moveLerp.apply(Math.abs(input.x)) * moveXSign;
				controllerState.controllerMove.y = moveLerp.apply(Math.abs(input.y)) * moveYSign;
			}

			// lerp look
			{
				int lookXSign = controllerState.rawLook.x < 0 ? -1 : 1;
				int lookYSign = controllerState.rawLook.y < 0 ? -1 : 1;

				Vector2 input = applyDeadzone(controllerState.rawLook);

				controllerState.controllerLook.x = lookLerp.apply(Math.abs(input.x)) * lookXSign;
				controllerState.controllerLook.y = lookLerp.apply(Math.abs(input.y)) * lookYSign;
			}

			// in menu mode, let the move stick fake dpad inputs for menu movement
			if(menuMode) {
				if(menuRepeatTimer <= 0) {
					if(controllerState.controllerMove.x > 0.3f)
						controllerState.dpadEvents.add(DPAD.RIGHT);
					else if(controllerState.controllerMove.x < -0.3f)
						controllerState.dpadEvents.add(DPAD.LEFT);
					else if(controllerState.controllerMove.y > 0.3f)
						controllerState.dpadEvents.add(DPAD.DOWN);
					else if(controllerState.controllerMove.y < -0.3f)
						controllerState.dpadEvents.add(DPAD.UP);
				}

				if(controllerState.dpadEvents.size > 0) {
					menuRepeatTimer = menuRepeated ? 0.15f : 0.3f;
					menuRepeated = true;
					Gdx.app.log("Delver", controllerState.dpadEvents.get(0).name());
				}

				if(menuRepeated) {
					if(Math.abs(controllerState.controllerMove.x) < 0.1f
							&& Math.abs(controllerState.controllerMove.y) < 0.1f) {
						menuRepeated = false;
						menuRepeatTimer = 0;
					}
				}


				if(menuRepeatTimer > 0) menuRepeatTimer -= delta;
			}
		}
	}

	public boolean isAnalogEvent(Actions.Action action) {
		// Return true for analog events (like look and turn)
		if(action == Actions.Action.FORWARD) {
			return true;
		}
		else if(action == Actions.Action.BACKWARD) {
			return true;
		}
		else if(action == Actions.Action.STRAFE_LEFT) {
			return true;
		}
		else if(action == Actions.Action.STRAFE_RIGHT) {
			return true;
		}
		else if(action == Actions.Action.TURN_LEFT) {
			return true;
		}
		else if(action == Actions.Action.TURN_RIGHT) {
			return true;
		}
		else if(action == Actions.Action.LOOK_UP) {
			return true;
		}
		else if(action == Actions.Action.LOOK_DOWN) {
			return true;
		}
		return false;
	}

	public boolean matchButton(int index, GamepadBinding.GamepadInputType type, int mod, float value) {
        if(Game.instance != null) Game.instance.input.usingGamepad = true;

        for(Actions.Action action : Actions.gamepadBindings.keySet()) {
            GamepadBinding binding = Actions.gamepadBindings.get(action);

            if(binding == null) continue;

            if(binding.number == index && binding.type == type && binding.modifier == mod) {
                value = Math.abs(value);
                boolean pressed = value > 0.25f;

                if(pressed && !isAnalogEvent(action)) {
                    // Add button events for all other digital actions
                    controllerState.buttonEvents.add(action);
                }

                if(action == Actions.Action.ATTACK) {
                    controllerState.attack = pressed;
                }
                else if(action == Actions.Action.DROP) {
                    controllerState.drop = pressed;
                }
                else if(action == Actions.Action.USE) {
                    controllerState.use = pressed;

                    if(value > 0.25f) {
                        controllerState.menuButtonEvents.add(ControllerState.MenuButtons.SELECT);
                    }
                }
                else if(action == Actions.Action.MAP) {
                    controllerState.map = pressed;
                }
                else if(action == Actions.Action.PAUSE) {
                    controllerState.pause = pressed;
                }
                else if(action == Actions.Action.INVENTORY) {
                    controllerState.inventory = pressed;

						if(value > 0.25f) {
							controllerState.menuButtonEvents.add(ControllerState.MenuButtons.CANCEL);
						}
					}
					else if(action == Actions.Action.FORWARD) {
						controllerState.rawMove.y = value * mod;
					}
					else if(action == Actions.Action.BACKWARD) {
						controllerState.rawMove.y = value * mod;
					}
					else if(action == Actions.Action.STRAFE_LEFT) {
						controllerState.rawMove.x = value * mod;
					}
					else if(action == Actions.Action.STRAFE_RIGHT) {
						controllerState.rawMove.x = value * mod;
					}
					else if(action == Actions.Action.TURN_LEFT) {
						controllerState.rawLook.x = -value * mod;
					}
					else if(action == Actions.Action.TURN_RIGHT) {
						controllerState.rawLook.x = -value * mod;
					}
					else if(action == Actions.Action.LOOK_UP) {
						controllerState.rawLook.y = -value * mod;
					}
					else if(action == Actions.Action.LOOK_DOWN) {
						controllerState.rawLook.y = -value * mod;
					}
                    else if(action == Actions.Action.MENU_SELECT) {
                        if (value > 0.25f) controllerState.menuButtonEvents.add(ControllerState.MenuButtons.SELECT);
                    }
                    else if(action == Actions.Action.MENU_CANCEL) {
                        if (value > 0.25f) controllerState.menuButtonEvents.add(ControllerState.MenuButtons.CANCEL);
                    }
				}
            else if(binding.number == index && binding.type == type) {
                if(action == Actions.Action.ATTACK) {
                    controllerState.attack = false;
                }
                else if(action == Actions.Action.DROP) {
                    controllerState.drop = false;
                }
                else if(action == Actions.Action.USE) {
                    controllerState.use = false;
                }
                else if(action == Actions.Action.MAP) {
                    controllerState.map = false;
                }
                else if(action == Actions.Action.PAUSE) {
                    controllerState.pause = false;
                }
                else if(action == Actions.Action.INVENTORY) {
                    controllerState.inventory = false;
                }
            }
        }

        return false;
    }

	@Override
	public boolean buttonDown (Controller controller, int buttonIndex) {
        dpadMenuNavigation(controller, buttonIndex);
		return matchButton(buttonIndex, GamepadBinding.GamepadInputType.BUTTON, 0, 1);
	}

	@Override
	public boolean buttonUp (Controller controller, int buttonIndex) {
		return matchButton(buttonIndex, GamepadBinding.GamepadInputType.BUTTON, 0, 0);
	}

	@Override
	public boolean axisMoved (Controller controller, int axisIndex, float value) {
		return matchButton(axisIndex, GamepadBinding.GamepadInputType.AXIS, (value > 0) ? 1 : -1, value);
	}

    public void dpadMenuNavigation(Controller controller, int buttonIndex) {
        if (!this.menuMode) return;

        ControllerMapping mapping = controller.getMapping();

        if (buttonIndex == mapping.buttonDpadUp) {
            this.controllerState.dpadEvents.add(DPAD.UP);
            return;
        }

        if (buttonIndex == mapping.buttonDpadDown) {
            this.controllerState.dpadEvents.add(DPAD.DOWN);
            return;
        }

        if (buttonIndex == mapping.buttonDpadRight) {
            this.controllerState.dpadEvents.add(DPAD.RIGHT);
            return;
        }

        if (buttonIndex == mapping.buttonDpadLeft) {
            this.controllerState.dpadEvents.add(DPAD.LEFT);
            return;
        }
    }

    @Override
    public void connected (Controller controller) {
    	mapController(controller);
    }

    @Override
    public void disconnected (Controller controller) {
    	controllerMapping.removeKey(controller);
    }

    public void init() {
    	controllerMapping.clear();
    	Controller first = Controllers.getControllers().first();
    	if(first != null) mapController(first);
    }

    public void mapController(Controller controller) {
        controllerMapping.put(controller, new GamepadDefinition(controller));
        Options.SetGamepadBindings(controllerMapping.get(controller));
    }

    public Vector2 applyDeadzone(Vector2 rawInput) {
    	// If the input is below the threshold, return a zero vector.
    	result.set(rawInput);
    	if (result.len() <= deadzoneMagnitude) {
    		return Vector2.Zero;
    	}

    	// Normalize the input from the threshold to 1.0f
    	direction.set(rawInput).nor();
    	offset.set(direction).scl(deadzoneMagnitude);

    	result.sub(offset).scl( 1f / direction.sub(offset).len());

    	return result;
    }

    public int controllerCount() {
        return controllerMapping.size;
    }
}
