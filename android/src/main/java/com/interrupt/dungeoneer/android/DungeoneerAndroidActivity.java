package com.interrupt.dungeoneer.android;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.game.Game;

public class DungeoneerAndroidActivity extends AndroidApplication {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true; // Recommended, but not required.

        // Enable debug mode for debug builds
        Game.isDebugMode = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

        initialize(new GameApplication(), configuration);
    }
}
