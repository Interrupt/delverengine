package com.interrupt.dungeoneer.android;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.interrupt.api.googleplay.GooglePlayApi;
import com.interrupt.api.googleplay.NullGooglePlayApi;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.achievements.AchievementManager;
import com.interrupt.managers.achievements.GooglePlayAchievementDealer;

public class DungeoneerAndroidActivity extends AndroidApplication {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.
        configuration.stencil = 8;

        // Enable debug mode for debug builds
        Game.isDebugMode = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        // Set target GooglePlayApi
        GooglePlayApi.api = new NullGooglePlayApi();
        AchievementManager.achievementDealer = new GooglePlayAchievementDealer();

        initialize(new GameApplication(), configuration);
    }
}
