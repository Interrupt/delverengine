package com.interrupt.api.gamecenter;

public class NullGameCenterApi implements GameCenterApiInterface {
    @Override
    public void achieve(String identifier) {}

    @Override
    public boolean isAvailable() {
        return false;
    }
}
