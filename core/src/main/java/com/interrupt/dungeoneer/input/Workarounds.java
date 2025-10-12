package com.interrupt.dungeoneer.input;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;

public class Workarounds {
    public static boolean ignoreBrokenGdxControllersOniOS() {
        return Gdx.app.getType() != Application.ApplicationType.iOS;
    }
}
