package com.interrupt.dungeoneer.screens;

public class SplashScreenInfo {
    public SplashScreenInfo() { }

    /** Background level filepath. */
    public String backgroundLevel = "levels/temple-splash.bin";

    /** Background image filepath. */
    public String backgroundImage = "splash/Delver-Menu-BG.png";

    /** Splash screen logo filepath. */
    public String logoImage = "splash/Delver-Logo.png";

    /** Apply texture filtering to logo. */
    public boolean logoFilter = false;

    /** Splash screen music filepath. */
    public String music = "title.mp3";

    /** Fog ending distance for background level. */
    public float fogEnd = 15;

    /** Fog start distance for background level. */
    public float fogStart = 3;
}
