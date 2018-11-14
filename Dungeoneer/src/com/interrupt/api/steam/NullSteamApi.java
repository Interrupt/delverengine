package com.interrupt.api.steam;

import com.badlogic.gdx.utils.Array;

public class NullSteamApi implements SteamApiInterface {
    @Override
    public boolean init() {
        return false;
    }

    @Override
    public Array<String> getWorkshopFolders() {
        return new Array<String>();
    }

    @Override
    public void runCallbacks() { }

    @Override
    public void achieve(String achievementName) { }

    @Override
    public void achieve(String achievementName, int numProgress, int maxProgress) { }

    @Override
    public void dispose() { }

    @Override
    public void uploadToWorkshop(Long workshopId, String modImagePath, String modTitle, String modFolderPath) { }

    @Override
    public boolean isAvailable() {
        return false;
    }
}