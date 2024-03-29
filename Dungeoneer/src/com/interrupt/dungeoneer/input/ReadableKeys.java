package com.interrupt.dungeoneer.input;

import com.interrupt.managers.StringManager;

import java.util.Hashtable;

public class ReadableKeys {
    public static Hashtable<Integer, String> keyNames = new Hashtable<Integer, String>();

    static {
        keyNames.put(29, StringManager.get("input.ReadableKeys.keyNames.A"));
        keyNames.put(57, StringManager.get("input.ReadableKeys.keyNames.ALT_LEFT"));
        keyNames.put(58, StringManager.get("input.ReadableKeys.keyNames.ALT_RIGHT"));
        keyNames.put(-1, StringManager.get("input.ReadableKeys.keyNames.ANY_KEY"));
        keyNames.put(75, StringManager.get("input.ReadableKeys.keyNames.APOSTROPHE"));
        keyNames.put(77, StringManager.get("input.ReadableKeys.keyNames.AT"));
        keyNames.put(30, StringManager.get("input.ReadableKeys.keyNames.B"));
        keyNames.put(4, StringManager.get("input.ReadableKeys.keyNames.BACK"));
        keyNames.put(73, StringManager.get("input.ReadableKeys.keyNames.BACKSLASH"));
        keyNames.put(67, StringManager.get("input.ReadableKeys.keyNames.BACKSPACE"));
        keyNames.put(96, StringManager.get("input.ReadableKeys.keyNames.BUTTON_A"));
        keyNames.put(97, StringManager.get("input.ReadableKeys.keyNames.BUTTON_B"));
        keyNames.put(98, StringManager.get("input.ReadableKeys.keyNames.BUTTON_C"));
        keyNames.put(255, StringManager.get("input.ReadableKeys.keyNames.BUTTON_CIRCLE"));
        keyNames.put(102, StringManager.get("input.ReadableKeys.keyNames.BUTTON_L1"));
        keyNames.put(104, StringManager.get("input.ReadableKeys.keyNames.BUTTON_L2"));
        keyNames.put(110, StringManager.get("input.ReadableKeys.keyNames.BUTTON_MODE"));
        keyNames.put(103, StringManager.get("input.ReadableKeys.keyNames.BUTTON_R1"));
        keyNames.put(105, StringManager.get("input.ReadableKeys.keyNames.BUTTON_R2"));
        keyNames.put(109, StringManager.get("input.ReadableKeys.keyNames.BUTTON_SELECT"));
        keyNames.put(108, StringManager.get("input.ReadableKeys.keyNames.BUTTON_START"));
        keyNames.put(106, StringManager.get("input.ReadableKeys.keyNames.BUTTON_THUMBL"));
        keyNames.put(107, StringManager.get("input.ReadableKeys.keyNames.BUTTON_THUMBR"));
        keyNames.put(99, StringManager.get("input.ReadableKeys.keyNames.BUTTON_X"));
        keyNames.put(100, StringManager.get("input.ReadableKeys.keyNames.BUTTON_Y"));
        keyNames.put(101, StringManager.get("input.ReadableKeys.keyNames.BUTTON_Z"));
        keyNames.put(31, StringManager.get("input.ReadableKeys.keyNames.C"));
        keyNames.put(5, StringManager.get("input.ReadableKeys.keyNames.CALL"));
        keyNames.put(27, StringManager.get("input.ReadableKeys.keyNames.CAMERA"));
        keyNames.put(23, StringManager.get("input.ReadableKeys.keyNames.CENTER"));
        keyNames.put(28, StringManager.get("input.ReadableKeys.keyNames.CLEAR"));
        keyNames.put(243, StringManager.get("input.ReadableKeys.keyNames.COLON"));
        keyNames.put(55, StringManager.get("input.ReadableKeys.keyNames.COMMA"));
        keyNames.put(129, StringManager.get("input.ReadableKeys.keyNames.CONTROL_LEFT"));
        keyNames.put(130, StringManager.get("input.ReadableKeys.keyNames.CONTROL_RIGHT"));
        keyNames.put(32, StringManager.get("input.ReadableKeys.keyNames.D"));
        keyNames.put(67, StringManager.get("input.ReadableKeys.keyNames.DEL"));
        keyNames.put(20, StringManager.get("input.ReadableKeys.keyNames.DOWN"));
        keyNames.put(33, StringManager.get("input.ReadableKeys.keyNames.E"));
        keyNames.put(132, StringManager.get("input.ReadableKeys.keyNames.END"));
        keyNames.put(6, StringManager.get("input.ReadableKeys.keyNames.ENDCALL"));
        keyNames.put(66, StringManager.get("input.ReadableKeys.keyNames.ENTER"));
        keyNames.put(65, StringManager.get("input.ReadableKeys.keyNames.ENVELOPE"));
        keyNames.put(70, StringManager.get("input.ReadableKeys.keyNames.EQUALS"));
        keyNames.put(131, StringManager.get("input.ReadableKeys.keyNames.ESCAPE"));
        keyNames.put(111, StringManager.get("input.ReadableKeys.keyNames.ESCAPE"));
        keyNames.put(64, StringManager.get("input.ReadableKeys.keyNames.EXPLORER"));
        keyNames.put(34, StringManager.get("input.ReadableKeys.keyNames.F"));
        keyNames.put(244, StringManager.get("input.ReadableKeys.keyNames.F1"));
        keyNames.put(253, StringManager.get("input.ReadableKeys.keyNames.F10"));
        keyNames.put(254, StringManager.get("input.ReadableKeys.keyNames.F11"));
        keyNames.put(255, StringManager.get("input.ReadableKeys.keyNames.F12"));
        keyNames.put(245, StringManager.get("input.ReadableKeys.keyNames.F2"));
        keyNames.put(246, StringManager.get("input.ReadableKeys.keyNames.F3"));
        keyNames.put(247, StringManager.get("input.ReadableKeys.keyNames.F4"));
        keyNames.put(248, StringManager.get("input.ReadableKeys.keyNames.F5"));
        keyNames.put(249, StringManager.get("input.ReadableKeys.keyNames.F6"));
        keyNames.put(250, StringManager.get("input.ReadableKeys.keyNames.F7"));
        keyNames.put(251, StringManager.get("input.ReadableKeys.keyNames.F8"));
        keyNames.put(252, StringManager.get("input.ReadableKeys.keyNames.F9"));
        keyNames.put(80, StringManager.get("input.ReadableKeys.keyNames.FOCUS"));
        keyNames.put(112, StringManager.get("input.ReadableKeys.keyNames.FORWARD_DEL"));
        keyNames.put(35, StringManager.get("input.ReadableKeys.keyNames.G"));
        keyNames.put(68, StringManager.get("input.ReadableKeys.keyNames.GRAVE"));
        keyNames.put(36, StringManager.get("input.ReadableKeys.keyNames.H"));
        keyNames.put(79, StringManager.get("input.ReadableKeys.keyNames.HEADSETHOOK"));
        keyNames.put(3, StringManager.get("input.ReadableKeys.keyNames.HOME"));
        keyNames.put(37, StringManager.get("input.ReadableKeys.keyNames.I"));
        keyNames.put(133, StringManager.get("input.ReadableKeys.keyNames.INSERT"));
        keyNames.put(38, StringManager.get("input.ReadableKeys.keyNames.J"));
        keyNames.put(39, StringManager.get("input.ReadableKeys.keyNames.K"));
        keyNames.put(40, StringManager.get("input.ReadableKeys.keyNames.L"));
        keyNames.put(21, StringManager.get("input.ReadableKeys.keyNames.LEFT"));
        keyNames.put(71, StringManager.get("input.ReadableKeys.keyNames.LEFT_BRACKET"));
        keyNames.put(41, StringManager.get("input.ReadableKeys.keyNames.M"));
        keyNames.put(90, StringManager.get("input.ReadableKeys.keyNames.MEDIA_FAST_FORWARD"));
        keyNames.put(87, StringManager.get("input.ReadableKeys.keyNames.MEDIA_NEXT"));
        keyNames.put(85, StringManager.get("input.ReadableKeys.keyNames.MEDIA_PLAY_PAUSE"));
        keyNames.put(88, StringManager.get("input.ReadableKeys.keyNames.MEDIA_PREVIOUS"));
        keyNames.put(89, StringManager.get("input.ReadableKeys.keyNames.MEDIA_REWIND"));
        keyNames.put(86, StringManager.get("input.ReadableKeys.keyNames.MEDIA_STOP"));
        keyNames.put(82, StringManager.get("input.ReadableKeys.keyNames.MENU"));
        keyNames.put(69, StringManager.get("input.ReadableKeys.keyNames.MINUS"));
        keyNames.put(91, StringManager.get("input.ReadableKeys.keyNames.MUTE"));
        keyNames.put(42, StringManager.get("input.ReadableKeys.keyNames.N"));
        keyNames.put(83, StringManager.get("input.ReadableKeys.keyNames.NOTIFICATION"));
        keyNames.put(78, StringManager.get("input.ReadableKeys.keyNames.NUM"));
        keyNames.put(7, StringManager.get("input.ReadableKeys.keyNames.NUM_0"));
        keyNames.put(8, StringManager.get("input.ReadableKeys.keyNames.NUM_1"));
        keyNames.put(9, StringManager.get("input.ReadableKeys.keyNames.NUM_2"));
        keyNames.put(10, StringManager.get("input.ReadableKeys.keyNames.NUM_3"));
        keyNames.put(11, StringManager.get("input.ReadableKeys.keyNames.NUM_4"));
        keyNames.put(12, StringManager.get("input.ReadableKeys.keyNames.NUM_5"));
        keyNames.put(13, StringManager.get("input.ReadableKeys.keyNames.NUM_6"));
        keyNames.put(14, StringManager.get("input.ReadableKeys.keyNames.NUM_7"));
        keyNames.put(15, StringManager.get("input.ReadableKeys.keyNames.NUM_8"));
        keyNames.put(16, StringManager.get("input.ReadableKeys.keyNames.NUM_9"));
        keyNames.put(144, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_0"));
        keyNames.put(145, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_1"));
        keyNames.put(146, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_2"));
        keyNames.put(147, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_3"));
        keyNames.put(148, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_4"));
        keyNames.put(149, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_5"));
        keyNames.put(150, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_6"));
        keyNames.put(151, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_7"));
        keyNames.put(152, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_8"));
        keyNames.put(153, StringManager.get("input.ReadableKeys.keyNames.NUMPAD_9"));
        keyNames.put(43, StringManager.get("input.ReadableKeys.keyNames.O"));
        keyNames.put(44, StringManager.get("input.ReadableKeys.keyNames.P"));
        keyNames.put(93, StringManager.get("input.ReadableKeys.keyNames.PAGE_DOWN"));
        keyNames.put(92, StringManager.get("input.ReadableKeys.keyNames.PAGE_UP"));
        keyNames.put(56, StringManager.get("input.ReadableKeys.keyNames.PERIOD"));
        keyNames.put(94, StringManager.get("input.ReadableKeys.keyNames.PICTSYMBOLS"));
        keyNames.put(81, StringManager.get("input.ReadableKeys.keyNames.PLUS"));
        keyNames.put(18, StringManager.get("input.ReadableKeys.keyNames.POUND"));
        keyNames.put(26, StringManager.get("input.ReadableKeys.keyNames.POWER"));
        keyNames.put(45, StringManager.get("input.ReadableKeys.keyNames.Q"));
        keyNames.put(46, StringManager.get("input.ReadableKeys.keyNames.R"));
        keyNames.put(22, StringManager.get("input.ReadableKeys.keyNames.RIGHT"));
        keyNames.put(72, StringManager.get("input.ReadableKeys.keyNames.RIGHT_BRACKET"));
        keyNames.put(47, StringManager.get("input.ReadableKeys.keyNames.S"));
        keyNames.put(84, StringManager.get("input.ReadableKeys.keyNames.SEARCH"));
        keyNames.put(74, StringManager.get("input.ReadableKeys.keyNames.SEMICOLON"));
        keyNames.put(59, StringManager.get("input.ReadableKeys.keyNames.SHIFT_LEFT"));
        keyNames.put(60, StringManager.get("input.ReadableKeys.keyNames.SHIFT_RIGHT"));
        keyNames.put(76, StringManager.get("input.ReadableKeys.keyNames.SLASH"));
        keyNames.put(1, StringManager.get("input.ReadableKeys.keyNames.SOFT_LEFT"));
        keyNames.put(2, StringManager.get("input.ReadableKeys.keyNames.SOFT_RIGHT"));
        keyNames.put(62, StringManager.get("input.ReadableKeys.keyNames.SPACE"));
        keyNames.put(17, StringManager.get("input.ReadableKeys.keyNames.STAR"));
        keyNames.put(95, StringManager.get("input.ReadableKeys.keyNames.SWITCH_CHARSET"));
        keyNames.put(63, StringManager.get("input.ReadableKeys.keyNames.SYM"));
        keyNames.put(48, StringManager.get("input.ReadableKeys.keyNames.T"));
        keyNames.put(61, StringManager.get("input.ReadableKeys.keyNames.TAB"));
        keyNames.put(49, StringManager.get("input.ReadableKeys.keyNames.U"));
        keyNames.put(0, StringManager.get("input.ReadableKeys.keyNames.UNKNOWN"));
        keyNames.put(19, StringManager.get("input.ReadableKeys.keyNames.UP"));
        keyNames.put(50, StringManager.get("input.ReadableKeys.keyNames.V"));
        keyNames.put(25, StringManager.get("input.ReadableKeys.keyNames.VOLUME_DOWN"));
        keyNames.put(24, StringManager.get("input.ReadableKeys.keyNames.VOLUME_UP"));
        keyNames.put(51, StringManager.get("input.ReadableKeys.keyNames.W"));
        keyNames.put(52, StringManager.get("input.ReadableKeys.keyNames.X"));
        keyNames.put(53, StringManager.get("input.ReadableKeys.keyNames.Y"));
        keyNames.put(54, StringManager.get("input.ReadableKeys.keyNames.Z"));
    }
}
