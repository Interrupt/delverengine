package com.interrupt.api.steam.workshop;

public class WorkshopModData {
    public long workshopId;
    public String title;
    public String image;

    public WorkshopModData() { }

    public WorkshopModData(long workshopId, String title, String image) {
        this.workshopId = workshopId;
        this.title = title;
        this.image = image;
    }
}