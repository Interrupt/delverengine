package com.interrupt.managers.achievements;

import com.interrupt.api.googleplay.GooglePlayApi;
import com.interrupt.api.steam.SteamApi;

public class AchievementManager {
    public AchievementDealerInterface achievementDealer = new NullAchievementDealer();

    public void init() {
        if (SteamApi.api.isAvailable()) {
            achievementDealer = new SteamAchievementDealer();
        }

        if (GooglePlayApi.api.isAvailable()) {
            achievementDealer = new GooglePlayAchievementDealer();
        }
    }
}
