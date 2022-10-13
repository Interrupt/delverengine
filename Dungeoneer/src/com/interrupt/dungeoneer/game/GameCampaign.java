package com.interrupt.dungeoneer.game;

public class GameCampaign {
    /** The campaigns ID. EG: "base_game" */
    public String campaignId;

    /** The display name of the campaign to show in the UI */
    public String displayName;

    // Filled in when the mod is found
    private transient String modPath;

    // Public constructor needed for Kryo serialization
    public GameCampaign() { }

    public String getModPath() {
        return modPath;
    }

    public void setModPath(String inModPath) {
        modPath = inModPath;
    }
}
