package com.interrupt.api.steam;

import com.badlogic.gdx.utils.Array;

public interface SteamApiInterface {
    public boolean init();
    public Array<String> getWorkshopFolders();
    public void runCallbacks();
    public void achieve(String achievementName);
    public void achieve(String achievementName, int numProgress, int maxProgress);
    public void dispose();
    public void uploadToWorkshop(Long workshopId, String modImagePath, String modTitle, String modFolderPath);
    public boolean isAvailable();
}