package com.interrupt.dungeoneer.editor.ui.menu.generator;

public class RoomGeneratorSettings {
    public boolean northExit = true;
    public boolean eastExit = true;
    public boolean southExit = true;
    public boolean westExit = true;

    public static RoomGeneratorSettings copy(RoomGeneratorSettings settings) {
        RoomGeneratorSettings copiedSettings = new RoomGeneratorSettings();

        copiedSettings.northExit = settings.northExit;
        copiedSettings.eastExit = settings.eastExit;
        copiedSettings.southExit = settings.southExit;
        copiedSettings.westExit = settings.westExit;

        return copiedSettings;
    }
}
