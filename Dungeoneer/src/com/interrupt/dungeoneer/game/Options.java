package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.Actions.Action;
import com.interrupt.dungeoneer.input.GamepadBinding;
import com.interrupt.dungeoneer.input.GamepadDefinition;
import com.interrupt.utils.JsonUtil;
import com.interrupt.utils.OSUtils;

/** Game options container class. */
public class Options {
    transient public static Options instance;

    public boolean mouseInvert = false;
    public boolean enableMusic = true;
    public float mouseXSensitivity = 1;
    public float mouseYSensitivity = 1;
    public Action mouseButton1Action = Action.ATTACK;
    public Action mouseButton2Action = Action.USE;
    public Action mouseButton3Action = Action.INVENTORY;
    public float musicVolume = 0.5f;
    public float sfxVolume = 1f;
    public float gfxQuality = 1f;
    public boolean fullScreen = false;
    public boolean useMouseScroller = true;
    public boolean headBobEnabled = true;
    public boolean handLagEnabled = true;
    public boolean shadowsEnabled = true;
    public boolean fxaaEnabled = false;

    public float uiSize = 1;
    public boolean hideUI = false;
    public boolean alwaysShowCrosshair = false;

    public int key_use = Keys.E;
    public int key_attack = Keys.SPACE;
    public int key_forward = Keys.W;
    public int key_backward = Keys.S;
    public int key_strafe_left = Keys.A;
    public int key_strafe_right = Keys.D;
    public int key_map = Keys.M;
    public int key_pause = Keys.ESCAPE;
    public int key_inventory = Keys.I;
    public int key_next_item = Keys.RIGHT_BRACKET;
    public int key_previous_item = Keys.LEFT_BRACKET;
    public int key_drop = Keys.Q;
    public int key_look_up = Keys.UP;
    public int key_look_down = Keys.DOWN;
    public int key_turn_left = Keys.LEFT;
    public int key_turn_right = Keys.RIGHT;
    public int key_jump = -1;

    public GamepadBinding gamepad_use = null;
    public GamepadBinding gamepad_attack = null;
    public GamepadBinding gamepad_forward = null;
    public GamepadBinding gamepad_backward = null;
    public GamepadBinding gamepad_strafe_left = null;
    public GamepadBinding gamepad_strafe_right = null;
    public GamepadBinding gamepad_map = null;
    public GamepadBinding gamepad_pause = null;
    public GamepadBinding gamepad_inventory = null;
    public GamepadBinding gamepad_next_item = null;
    public GamepadBinding gamepad_previous_item = null;
    public GamepadBinding gamepad_drop = null;
    public GamepadBinding gamepad_look_up = null;
    public GamepadBinding gamepad_look_down = null;
    public GamepadBinding gamepad_turn_left = null;
    public GamepadBinding gamepad_turn_right = null;
    public GamepadBinding gamepad_jump = null;

    @Deprecated
    public int key_cursor = Keys.Z;

    public float fieldOfView = 67f;

    public int graphicsDetailLevel = 3;

    public int antiAliasingSamples = 0;
    public boolean vsyncEnabled = true;
    public int fpsLimit = 120;

    public boolean enablePostProcessing = false;
    public int postProcessingQuality = 3;
    public String postProcessFilter;

    public boolean tutorialEnabled = true;

    static {
        instance = new Options();
    }

    public Options() {
        if (OSUtils.isMobile()) {
            graphicsDetailLevel = 2;
            shadowsEnabled = false;
            postProcessingQuality = 0;
            fxaaEnabled = false;
        }
    }

    public static void SetKeyboardBindings() {
        Actions.keyBindings.put(Action.USE, Options.instance.key_use);
        Actions.keyBindings.put(Action.ATTACK, Options.instance.key_attack);
        Actions.keyBindings.put(Action.FORWARD, Options.instance.key_forward);
        Actions.keyBindings.put(Action.BACKWARD, Options.instance.key_backward);
        Actions.keyBindings.put(Action.STRAFE_LEFT, Options.instance.key_strafe_left);
        Actions.keyBindings.put(Action.STRAFE_RIGHT, Options.instance.key_strafe_right);
        Actions.keyBindings.put(Action.MAP, Options.instance.key_map);
        Actions.keyBindings.put(Action.PAUSE, Options.instance.key_pause);
        Actions.keyBindings.put(Action.INVENTORY, Options.instance.key_inventory);
        Actions.keyBindings.put(Action.DROP, Options.instance.key_drop);
        Actions.keyBindings.put(Action.LOOK_UP, Options.instance.key_look_up);
        Actions.keyBindings.put(Action.LOOK_DOWN, Options.instance.key_look_down);
        Actions.keyBindings.put(Action.TURN_LEFT, Options.instance.key_turn_left);
        Actions.keyBindings.put(Action.TURN_RIGHT, Options.instance.key_turn_right);
        Actions.keyBindings.put(Action.JUMP, Options.instance.key_jump);
    }

    public static void SetGamepadBindings(GamepadDefinition defaultGamepad) {
        SetGamepadAction(Action.USE, Options.instance.gamepad_use, defaultGamepad);
        SetGamepadAction(Action.ATTACK, Options.instance.gamepad_attack, defaultGamepad);
        SetGamepadAction(Action.FORWARD, Options.instance.gamepad_forward, defaultGamepad);
        SetGamepadAction(Action.BACKWARD, Options.instance.gamepad_backward, defaultGamepad);
        SetGamepadAction(Action.STRAFE_LEFT, Options.instance.gamepad_strafe_left, defaultGamepad);
        SetGamepadAction(Action.STRAFE_RIGHT, Options.instance.gamepad_strafe_right, defaultGamepad);
        SetGamepadAction(Action.MAP, Options.instance.gamepad_map, defaultGamepad);
        SetGamepadAction(Action.PAUSE, Options.instance.gamepad_pause, defaultGamepad);
        SetGamepadAction(Action.INVENTORY, Options.instance.gamepad_inventory, defaultGamepad);
        SetGamepadAction(Action.DROP, Options.instance.gamepad_drop, defaultGamepad);
        SetGamepadAction(Action.LOOK_UP, Options.instance.gamepad_look_up, defaultGamepad);
        SetGamepadAction(Action.LOOK_DOWN, Options.instance.gamepad_look_down, defaultGamepad);
        SetGamepadAction(Action.TURN_LEFT, Options.instance.gamepad_turn_left, defaultGamepad);
        SetGamepadAction(Action.TURN_RIGHT, Options.instance.gamepad_turn_right, defaultGamepad);
        SetGamepadAction(Action.ITEM_PREVIOUS, Options.instance.gamepad_previous_item, defaultGamepad);
        SetGamepadAction(Action.ITEM_NEXT, Options.instance.gamepad_next_item, defaultGamepad);
        SetGamepadAction(Action.MENU_SELECT, null, defaultGamepad);
        SetGamepadAction(Action.MENU_CANCEL, null, defaultGamepad);
        SetGamepadAction(Action.JUMP, null, defaultGamepad);
    }

    private static void SetGamepadAction(Action action, GamepadBinding binding, GamepadDefinition defaultGamepad) {
        if (binding != null) {
            Actions.gamepadBindings.put(action, binding);
        }
        else {
            Actions.gamepadBindings.remove(action);

            // Maybe fallback to the default for a gamepad
            if (GamepadDefinition.gamepadBindings != null) {
                GamepadBinding defaultBinding = GamepadDefinition.gamepadBindings.get(action);
                if (defaultBinding != null) {
                    Actions.gamepadBindings.put(action, defaultBinding);
                }
            }
        }
    }

    /** Used by the app to find the save directory. Probably should live somewhere else. */
    public static String getOptionsDir() {
        return "save/";
    }

    public static String getOptionsFilePath() {
        return "save/options.txt";
    }

    /** Load options from file and update instance. */
    public static void loadOptions() {
        FileHandle file = Game.getFile(getOptionsFilePath());

        instance = JsonUtil.fromJson(Options.class, file, () -> {
            Options o = new Options();
            JsonUtil.toJson(o, file);
            return o;
        });

        SetKeyboardBindings();
    }

    /** Save options to file. */
    public static void saveOptions() {
        if (Options.instance == null) {
            return;
        }

        try {
            FileHandle file = Game.getFile(getOptionsFilePath());
            JsonUtil.toJson(instance, file);
        }
        catch (Exception e) {
            Gdx.app.log("Delver", "Failed to save options file.");
        }
    }
}
