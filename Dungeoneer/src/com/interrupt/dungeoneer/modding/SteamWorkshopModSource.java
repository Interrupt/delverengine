package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.utils.Array;
import com.interrupt.api.steam.SteamApi;

public class SteamWorkshopModSource implements ModSource {
    @Override
    public void init() {

    }

    @Override
    public Array<String> getInstalledMods() {
        return SteamApi.api.getWorkshopFolders();
    }
}
