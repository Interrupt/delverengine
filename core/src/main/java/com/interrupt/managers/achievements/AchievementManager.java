package com.interrupt.managers.achievements;

public class AchievementManager {
    public static AchievementDealerInterface achievementDealer = new NullAchievementDealer();

    public void achieve(String identifier) {
        achievementDealer.achieve(identifier);
    }
}
