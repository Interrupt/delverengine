package com.interrupt.dungeoneer;

import com.badlogic.gdx.graphics.glutils.HdpiMode;
import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.glkit.GLKViewDrawableStencilFormat;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.interrupt.api.gamecenter.GameCenterApi;
import com.interrupt.api.gamecenter.NullGameCenterApi;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.managers.achievements.AchievementManager;
import com.interrupt.managers.achievements.GameCenterAchievementDealer;

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends IOSApplication.Delegate {
    @Override
    protected IOSApplication createApplication() {
        IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
        configuration.orientationPortrait = false;
        configuration.hdpiMode = HdpiMode.Pixels;
        configuration.stencilFormat = GLKViewDrawableStencilFormat._8;

        // Set target GameCenterApi
        GameCenterApi.api = new NullGameCenterApi();
        AchievementManager.achievementDealer = new GameCenterAchievementDealer();

        return new IOSApplication(new GameApplication(), configuration);
    }

    public static void main(String[] argv) {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
