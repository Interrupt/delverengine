package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class WarningDialog extends Dialog {
    public WarningDialog(Skin skin, String warning) {
        super("Warning", skin);

        text(warning);
        getContentTable().row();

        button("Close", true);
    }

    @Override
    protected void result(Object object) { }
}
