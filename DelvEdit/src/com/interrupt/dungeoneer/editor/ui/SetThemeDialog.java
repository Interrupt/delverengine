package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;

import java.io.File;
import java.io.FileFilter;

public class SetThemeDialog extends Dialog {

    SelectBox themeSelect;

    public SetThemeDialog(Skin skin, Level level) {
        super("Set Level Theme for Testing", skin);

        themeSelect = new SelectBox(skin);

        Array<String> mods = Game.getModManager().modsFound;
        Array<String> themes = new Array<String>();

        // Go find all the mod themes
        for(String mod : mods) {
            FileHandle dir = Game.getFile(mod + "/generator");
            if(dir.isDirectory()) {
                FileHandle[] dirs = dir.list(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });

                for(int i = 0; i < dirs.length; i++) {
                    String[] path = dirs[i].path().split("/");
                    String theme = path[path.length - 1].toUpperCase();
                    if(!themes.contains(theme, false)) themes.add(theme);
                }
            }
        }

        // Now add internally packaged themes
        Array<FileHandle> packagedThemes = Game.findPackagedFiles("info.dat");
        for(FileHandle f : packagedThemes) {
            FileHandle themeDir = f.parent();
            if(themeDir.parent().name().equals("generator")) {
                String theme = themeDir.name().toUpperCase();
                if(!themes.contains(theme, false)) themes.add(theme);
            }
        }

        themes.sort();

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
        return themeSelect.getSelected().toString();
    }
}
