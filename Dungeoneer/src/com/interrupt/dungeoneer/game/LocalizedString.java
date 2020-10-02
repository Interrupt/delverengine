package com.interrupt.dungeoneer.game;

import java.util.HashMap;

public class LocalizedString {
    /** Translated string that will display in game. */
    public String localizedName;

    /** Comment to provide useful context to translator. */
    public String comment;

    /** A string used to describe the gender and count of a noun. Only used for items. */
    public String form;

    /** A table of form to forms strings used to ensure adjectives and nouns agree. */
    public HashMap<String, String> forms;

    public LocalizedString() {
        this.forms = new HashMap<String, String>();
    }

    public LocalizedString(String localizedName, String comment) {
        this.localizedName = localizedName;
        this.comment = comment;
    }
}
