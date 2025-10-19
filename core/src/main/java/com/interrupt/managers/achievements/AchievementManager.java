package com.interrupt.managers.achievements;

import com.interrupt.api.gamecenter.GameCenterApi;
import com.interrupt.api.googleplay.GooglePlayApi;
import com.interrupt.api.steam.SteamApi;

public class AchievementManager {
    private AchievementDealerInterface achievementDealer = new NullAchievementDealer();

    public void init() {
        if (SteamApi.api.isAvailable()) {
            achievementDealer = new SteamAchievementDealer();
            return;
        }

        if (GooglePlayApi.api.isAvailable()) {
            achievementDealer = new GooglePlayAchievementDealer();
            return;
        }

        if (GameCenterApi.api.isAvailable()) {
            achievementDealer = new GameCenterAchievementDealer();
            return;
        }
    }

    public void achieve(String identifier) {
        achievementDealer.achieve(identifier);
    }
}
