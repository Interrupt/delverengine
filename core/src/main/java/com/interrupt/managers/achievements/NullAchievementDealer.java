package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;

public class NullAchievementDealer implements AchievementDealerInterface {
    @Override
    public void achieve(String identifier) {
        Gdx.app.log("Achievement", identifier + " unlocked!");
    }
}
