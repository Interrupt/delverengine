package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.controllers.PovDirection;

public class SonyPad extends GamepadDefinition
{
   public static final String ID = "sony";
   public static final int BUTTON_SQUARE = 0;
   public static final int BUTTON_X = 1;
   public static final int BUTTON_O = 2;
   public static final int BUTTON_TRIANGLE = 3;
   public static final int BUTTON_BACK = 6;
   public static final int BUTTON_START = 7;
   public static final PovDirection BUTTON_DPAD_UP = PovDirection.north;
   public static final PovDirection BUTTON_DPAD_DOWN = PovDirection.south;
   public static final PovDirection BUTTON_DPAD_RIGHT = PovDirection.east;
   public static final PovDirection BUTTON_DPAD_LEFT = PovDirection.west;
   public static final int BUTTON_LB = 4;
   public static final int BUTTON_L3 = 10;
   public static final int BUTTON_RB = 5;
   public static final int BUTTON_R3 = 11;
   public static final int AXIS_LEFT_X = 0; //-1 is left | +1 is right
   public static final int AXIS_LEFT_Y = 1; //-1 is up | +1 is down
   public static final int AXIS_LEFT_TRIGGER = 4; //value 0 to 1f
   public static final int AXIS_RIGHT_X = 2; //-1 is left | +1 is right
   public static final int AXIS_RIGHT_Y = 3; //-1 is up | +1 is down
   public static final int AXIS_RIGHT_TRIGGER = 5; //value 0 to 1f
}
