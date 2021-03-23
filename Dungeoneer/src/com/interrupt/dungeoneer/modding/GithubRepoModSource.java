package com.interrupt.dungeoneer.modding;

import com.badlogic.gdx.utils.Array;

public class GithubRepoModSource implements ModSource {
    @Override
    public Array<String> getInstalledMods() {
        return new Array<>();
    }

    @Override
    public Array<String> getEnabledMods() {
        return new Array<>();
    }

    @Override
    public Array<ModData> getAvailableMods() {
        return new Array<>();
    }
}
