package com.interrupt.dungeoneer.input;

/**
 * Single source of truth for which input options are shown for which device state.
 * <p>
 * All decisions here are pure static functions of the device state, which is sampled
 * when an options page is built: a page reflects the devices attached when it was
 * opened, and newly attached or detached devices take effect on re-entering the
 * options screens. No events, no caching, no state.
 * <p>
 * See docs/specs/mobile-input-options.md for the agreed design these rules implement.
 */
public class InputOptionsPolicy {

    /** The shape of the look sensitivity control on the Input page. */
    public enum LookControl {
        /** Separate Look X and Look Y sensitivity sliders. */
        PerAxis,
        /** A single Look Speed slider that sets both look axes to the same value. */
        Unified
    }

    /** Desktop always has Look X/Y sensitivity sliders. On Mobile, per-axis look
     * sensitivity is shown only when a gamepad is present (stick look benefits from
     * per-axis aim); otherwise touch-only players get a single unified Look Speed. */
    public static LookControl lookControl(boolean isMobile, boolean gamepadPresent) {
        if (!isMobile) return LookControl.PerAxis;
        return gamepadPresent ? LookControl.PerAxis : LookControl.Unified;
    }

    /** Key Bindings are always editable on Desktop, where a keyboard is always present.
     * On Mobile they are only offered when a hardware keyboard is attached. */
    public static boolean showKeysEntry(boolean isMobile, boolean hardwareKeyboardPresent) {
        return !isMobile || hardwareKeyboardPresent;
    }

    /** Gamepad Bindings are gated on Gamepad Present on every platform. */
    public static boolean showGamepadEntry(boolean gamepadPresent) {
        return gamepadPresent;
    }

    /** The value a unified Look Speed slider displays when the look axes diverged while
     * a gamepad was in use: it shows the X value, and any adjustment unifies both axes
     * onto the new value. (The player has switched to touch, for whom per-axis values
     * no longer matter; the alternative of showing X/Y whenever the axes differ would
     * reintroduce state-dependent UI.) */
    public static float lookSpeedValue(float mouseX, float mouseY) {
        return mouseX;
    }
}
