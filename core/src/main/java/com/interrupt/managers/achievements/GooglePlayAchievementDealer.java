package com.interrupt.managers.achievements;

import com.badlogic.gdx.Gdx;

// import com.google.android.gms.games.PlayGames;

public class GooglePlayAchievementDealer implements AchievementDealerInterface {

    @Override
    public void achieve(String identifier) {
        // PlayGames.getAchievementClient().unlock(identifier);
        Gdx.app.log("Achievement", "(Google Play) " + identifier + " unlocked!");
    }

}
