package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.api.steam.workshop.WorkshopModData;
import com.interrupt.dungeoneer.entities.Item;

import java.util.HashMap;

public class Progression {
	public int gold = 40;
	public int lowestFloor = 0;
	public int experienceGained = 0;
	public int messagesFound = 0;
	public boolean won = false;

	public int goldAtStartOfRun = 0;

    public int wins = 0;
    public int deaths = 0;

    public boolean sawTutorial = false;

    // only show some things once in a run
	public Array<String> uniqueTilesSeen = new Array<String>();
	public Array<String> uniqueItemsSpawned = new Array<String>();
    public Array<String> dungeonAreasSeen = new Array<String>();

    public HashMap<String, Integer> messagesSeen = new HashMap<String, Integer>();

    public int inventoryUpgrades = 0;
	public int hotbarUpgrades = 0;

    public ArrayMap<String, String> progressionTriggers = new ArrayMap<String, String>();
	public ArrayMap<String, String> untilDeathProgressionTriggers = new ArrayMap<String, String>();

	public double playtime = 0;

	public Array<String> modsUsed = new Array<String>();

    public Progression() { }

	public void newRunStarted() {
	    // don't keep the unique lists between runs
		uniqueTilesSeen.clear();
		dungeonAreasSeen.clear();
		uniqueItemsSpawned.clear();
		untilDeathProgressionTriggers.clear();
		goldAtStartOfRun = gold;
	}

	public void trackMods() {
		if(Game.modManager != null) {
			modsUsed.clear();
			modsUsed.addAll(Game.modManager.modsFound);
		}
	}

	public void addInventorySlot() {
		inventoryUpgrades++;
	}

	public void addHotbarSlot() {
		hotbarUpgrades++;
	}

	public boolean hasSeenDungeonArea(String area) {
	    return dungeonAreasSeen.contains(area, false);
    }

    public void markDungeonAreaAsSeen(String area) {
	    if(!hasSeenDungeonArea(area)) dungeonAreasSeen.add(area);
    }

	public void spawnedUniqueItem(Item item) {
	    uniqueItemsSpawned.add(item.name);
    }

    public boolean hasSpawnedUniqueItem(Item item) {
	    return uniqueItemsSpawned.contains(item.name, false);
    }

    public void updatePlaytime(float deltaTime) {
    	playtime += deltaTime;
	}

	public String getPlaytime() {
		if(playtime < 0)
			return "??:??";

		double seconds = playtime;
		int minutes = (int)Math.floor(seconds / 60);
		int hours = (int)Math.floor(minutes / 60);

		minutes %= 60;
		seconds %= 60;

		String playtime = "";
		if(hours != 0) {
			playtime += String.format("%02d", hours) + ":";
		}

		if(hours == 0) {
			// "0:45"
			playtime += String.format("%01d", minutes);
		}
		else {
			// "1:04:45"
			playtime += String.format("%02d", minutes);
		}

		playtime += ":" + String.format("%02d", (int)seconds);

		return playtime;
	}

	// Make sure the mods list hasn't changed since we played this save last
	public Array<String> checkForMissingMods() {
    	Array<String> missingMods = new Array<String>();

    	if(Game.modManager == null)
    		return missingMods;

		Array<String> allMods = Game.modManager.getAllMods();
    	for(String mod : modsUsed) {
    		if(!allMods.contains(mod, false)) {
				missingMods.add(Game.modManager.getModName(mod));
			}
    		else if(!Game.modManager.checkIfModIsEnabled(mod)) {
				missingMods.add(Game.modManager.getModName(mod));
			}
		}

		return missingMods;
	}
}
