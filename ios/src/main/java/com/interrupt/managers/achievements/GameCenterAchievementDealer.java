package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;
import com.interrupt.api.gamecenter.GameCenterApi;

public class GameCenterAchievementDealer implements AchievementDealerInterface {
    @Override
    public void achieve(String identifier) {
        GameCenterApi.api.achieve(identifier);
        Gdx.app.log("Achievement", "(Game Center) " + identifier + " unlocked!");
    }
}
