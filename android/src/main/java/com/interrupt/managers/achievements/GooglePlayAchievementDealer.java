package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;
import com.interrupt.api.googleplay.GooglePlayApi;

public class GooglePlayAchievementDealer implements AchievementDealerInterface {
    @Override
    public void achieve(String identifier) {
        GooglePlayApi.api.achieve(identifier);
        Gdx.app.log("Achievement", "(Google Play) " + identifier + " unlocked!");
    }
}
