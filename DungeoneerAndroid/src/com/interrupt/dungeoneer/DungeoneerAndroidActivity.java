package com.interrupt.dungeoneer;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.interrupt.dungeoneer.GameApplication;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class DungeoneerAndroidActivity extends AndroidApplication {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        initialize(new GameApplication(), config);
    	
        /* --- BELOW: LOWER RESOLUTION ON PHONES --- */
    	/*super.onCreate(savedInstanceState);

        // Create the layout
        RelativeLayout layout = new RelativeLayout(this);

        // Do the stuff that initialize() would do for you
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        // Create the libgdx View
        GLSurfaceView gameView = (GLSurfaceView) initializeForView(new GameApplication(), false);
        
        gameView.getHolder().setFixedSize(840 / 6, 480 / 6);

        // Add the libgdx view
        layout.addView(gameView);

        // Hook it all up
        setContentView(layout);*/
    }
}