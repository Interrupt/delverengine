package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.game.Level;

public class SetThemeDialog extends Dialog {

    SelectBox<String> themeSelect;

    public SetThemeDialog(Skin skin, Level level) {
        super("Set Level Theme for Testing", skin);

        themeSelect = new SelectBox<String>(skin);

        Array<String> themes = Editor.app.generatorInfo.getThemes();
        themeSelect.setItems(themes);

        if(level.theme != null)
            themeSelect.setSelected(level.theme);
        else
            themeSelect.setSelected("DUNGEON");

        getContentTable().add(new Label("Level Theme", skin));
        getContentTable().add(themeSelect);
        getContentTable().row();

        button("Set Theme", true);
        button("Cancel", false);
    }

    public String getSelectedTheme() {
        return themeSelect.getSelected();
    }
}
