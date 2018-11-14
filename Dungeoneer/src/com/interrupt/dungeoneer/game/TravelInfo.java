package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.math.Vector3;

// holds information of how a player warped
public class TravelInfo {
    public String locationUuid;
    public Vector3 fromLocation;
    public int fromLevelNum;

    // some warps might not want to unload their level
    public transient Level level = null;

    public TravelInfo() { }

    public TravelInfo(String locationUuid, Vector3 fromLocation, int fromLevelNum) {
        this.locationUuid = locationUuid;
        this.fromLocation = fromLocation;
        this.fromLevelNum = fromLevelNum;
    }
}
