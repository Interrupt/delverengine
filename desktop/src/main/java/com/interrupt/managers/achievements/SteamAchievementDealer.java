package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;
import com.interrupt.api.steam.SteamApi;

public class SteamAchievementDealer implements AchievementDealerInterface {
    @Override
    public void achieve(String identifier) {
        SteamApi.api.achieve(identifier);
        Gdx.app.log("Achievement", "(Steam) " + identifier + " unlocked!");
    }
}
