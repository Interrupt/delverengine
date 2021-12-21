package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.TravelInfo;
import com.interrupt.managers.StringManager;

import java.util.UUID;

public class TriggeredWarp extends Trigger {
    public TriggeredWarp() { hidden = true; spriteAtlas = "editor"; tex = 1; useVerb = "ENTER"; }

	@EditorProperty
	public boolean isExit = false;

	@EditorProperty
	public boolean unloadParentLevel = true;

	@EditorProperty
	public boolean unloadThisLevelOnExit = true;

	@EditorProperty
	public boolean fadetoBlack = true;

	@EditorProperty
	public boolean showLoadingScreen = true;

	@EditorProperty
	public String toWarpMarkerId = null;

	@EditorProperty(group = "Level Info")
	public boolean generated = false;

	@EditorProperty(group = "Level Info")
	public String levelToLoad = null;

	@EditorProperty(group = "Level Info")
	public String levelTheme = "DUNGEON";

	@EditorProperty(group = "Level Info")
	public String levelName = "UNKNOWN";

	@EditorProperty(group = "Level Info")
	public boolean spawnMonsters = false;

	@EditorProperty(group = "Level Info - Branch Path, usually will be blank")
	public String travelPath = null;

	@EditorProperty(group = "Level Appearance")
	public float fogStart = 5;

	@EditorProperty(group = "Level Appearance")
	public float fogEnd = 15;

	@EditorProperty(group = "Level Appearance")
	public Color fogColor = new Color(Color.BLACK);

	@EditorProperty(group = "Level Appearance")
	public Color ambientLightColor = new Color(Color.BLACK);

	@EditorProperty(group = "Level Appearance")
	public Color skyLightColor = new Color(0.2f, 0.2f, 0.3f, 1.0f);

	@EditorProperty(group = "Level Appearance")
	public String loadingScreenBackground = null;

	@EditorProperty( group = "Level Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	public String levelChangeSound = "door_beginning.mp3";

	@EditorProperty( group = "Level Audio", type = "FILE_PICKER", params = "audio/music", include_base = false )
	public String music = null;

	@EditorProperty( group = "Level Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	public String ambientSound = null;

	@EditorProperty(group = "Level Objective")
	public String objectivePrefabToSpawn = null;

	@Override
	public void doTriggerEvent(String value) {
		triggerStatus=TriggerStatus.WAITING;

		if(Game.inEditor) {
			if(isExit) {
				Game.instance.doLevelExit(this);
			}
			else {
				Game.instance.warpToLevel(travelPath, this);
			}
		}
		else {
			// don't show the loading screen if level is already loaded
			if (isExit) {
				TravelInfo info = Game.instance.player.getCurrentTravelPath();
				if (info.level != null) {
					Game.instance.doLevelExit(this);
					return;
				}
			}

			if(showLoadingScreen) {
				GameApplication.ShowLevelChangeScreen(this);
			}
			else {
				Game.instance.warpToLevel(travelPath, this);
			}
		}
	}

	public String getUseVerb() {
		String useVerbLocalized = StringManager.get("triggers.Trigger.useVerbs." + useVerb);
		if (useVerbLocalized.startsWith("triggers.")) {
			useVerbLocalized = useVerb;
		}

		return useVerbLocalized + " " + levelName;
	}

	@Override
	public void init(Level level, Level.Source source) {
		if(source != Level.Source.EDITOR) {
			// Ensure that a travel path is always set. If not, make one now.
			if (travelPath == null || travelPath.isEmpty())
				travelPath = UUID.randomUUID().toString();
		}

		super.init(level, source);
	}
}
