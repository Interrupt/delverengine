package com.interrupt.api.steam.workshop;

public class WorkshopModData {
    // Basic mod info
    public long workshopId;
    public String title;
    public String image;

    // Campaigns filter which mods are loaded, only matching campaigns will be used
    public String campaignId;

    public WorkshopModData() { }

    public WorkshopModData(long workshopId, String title, String image) {
        this.workshopId = workshopId;
        this.title = title;
        this.image = image;
    }
}
