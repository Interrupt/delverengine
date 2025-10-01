package com.interrupt.api.googleplay;

public class NullGooglePlayApi implements GooglePlayApiInterface {
    @Override
    public void achieve(String identifier) {}

    @Override
    public boolean isAvailable() {
        return false;
    }
}
