package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;

public class GooglePlayAchievementDealer implements AchievementDealerInterface {
    @Override
    public void achieve(String identifier) {
        Gdx.app.log("Achievement", "(Google Play) " + identifier + " unlocked!");
    }
}
