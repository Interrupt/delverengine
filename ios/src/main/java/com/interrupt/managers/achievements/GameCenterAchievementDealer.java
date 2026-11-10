package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;

public class GameCenterAchievementDealer implements AchievementDealerInterface {
    @Override
    public void achieve(String identifier) {
        Gdx.app.log("Achievement", "(Game Center) " + identifier + " unlocked!");
    }
}
