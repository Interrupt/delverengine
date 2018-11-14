package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.controllers.PovDirection;

public class Xbox360OSXPad extends GamepadDefinition
{
   /*
    * The Tattieboogie osx 360 pad driver is pretty jacked...
    */

   public static final int BUTTON_X = 13;
   public static final int BUTTON_Y = 14;
   public static final int BUTTON_A = 11;
   public static final int BUTTON_B = 12;
   public static final int BUTTON_BACK = 6;
   public static final int BUTTON_START = 7;
   public static final PovDirection BUTTON_DPAD_UP = PovDirection.north;
   public static final PovDirection BUTTON_DPAD_DOWN = PovDirection.south;
   public static final PovDirection BUTTON_DPAD_RIGHT = PovDirection.east;
   public static final PovDirection BUTTON_DPAD_LEFT = PovDirection.west;
   public static final int BUTTON_LB = 8;
   public static final int BUTTON_L3 = 4;
   public static final int BUTTON_RB = 9;
   public static final int BUTTON_R3 = 5;
   public static final int AXIS_LEFT_X = 3; //-1 is left | +1 is right
   public static final int AXIS_LEFT_Y = 2; //-1 is up | +1 is down
   public static final int AXIS_LEFT_TRIGGER = 1; //value 0 to 1f
   public static final int AXIS_RIGHT_X = 4; //-1 is left | +1 is right
   public static final int AXIS_RIGHT_Y = 5; //-1 is up | +1 is down
   public static final int AXIS_RIGHT_TRIGGER = 0; //value 0 to -1f
}
