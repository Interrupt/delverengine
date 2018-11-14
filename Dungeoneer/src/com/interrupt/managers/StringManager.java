package com.interrupt.managers;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.LocalizedString;

import java.util.HashMap;

public class StringManager {
    public static HashMap<String, LocalizedString> localizedStrings = null;

    public static void init() {
        if (StringManager.localizedStrings == null) {
            StringManager.localizedStrings = Game.getModManager().loadLocalizedStrings();
        }
    }

    public static String get(String key) {
        LocalizedString localizedName = StringManager.localizedStrings.get(key);

        if (localizedName == null) {
            return key;
        }

        return localizedName.localizedName;
    }

    public static String getOrDefaultTo(String key, String defaultValue) {
        LocalizedString localizedName = StringManager.localizedStrings.get(key);

        if (localizedName == null) {
            return defaultValue;
        }

        return localizedName.localizedName;
    }

    public static String get(String key, String form) {
        LocalizedString localizedName = StringManager.localizedStrings.get(key);

        if (localizedName == null) {
            return key;
        }

        String localizedForm = localizedName.forms.get(form);
        if (localizedForm == null) {
            return localizedName.localizedName;
        }

        return localizedForm;
    }

    public static String form(String key) {
        LocalizedString localizedName = StringManager.localizedStrings.get(key);

        if (localizedName == null) {
            return null;
        }

        return localizedName.form;
    }
}

