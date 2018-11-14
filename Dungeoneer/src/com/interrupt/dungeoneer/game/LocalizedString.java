package com.interrupt.dungeoneer.game;

import java.util.HashMap;

public class LocalizedString {
    public String localizedName;
    public String comment;
    public String form;
    public HashMap<String, String> forms;

    public LocalizedString() {
        this.forms = new HashMap<String, String>();
    }

    public LocalizedString(String localizedName, String comment) {
        this.localizedName = localizedName;
        this.comment = comment;
    }
}
