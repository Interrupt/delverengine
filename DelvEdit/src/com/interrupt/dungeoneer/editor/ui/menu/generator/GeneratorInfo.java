package com.interrupt.dungeoneer.editor.ui.menu.generator;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.SectionDefinition;

public class GeneratorInfo {
    private Array<String> themes = new Array<String>();

    private Array<SectionDefinition> sectionDefinitions = new Array<SectionDefinition>();

    public Level lastGeneratedLevelTemplate;
    public Level lastGeneratedRoomTemplate;

    public GeneratorInfo() {
        refresh();
    }

    public void refresh() {
        themes.clear();
        sectionDefinitions.clear();

        Array<String> mods = Game.getModManager().modsFound;
        for (String mod : mods) {
            FileHandle parent = Game.getInternal(mod + "/generator");
            if (!parent.exists() || !parent.isDirectory()) {
                continue;
            }

            FileHandle[] children = parent.list(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (FileHandle child : children) {
                // Check for theme definition.
                FileHandle info = Game.getInternal(mod + "/generator/" + child.name() + "/info.dat");
                if (info.exists()) {
                    String theme = child.name().toUpperCase();

                    if (!themes.contains(theme, false)) {
                        themes.add(theme);
                    }
                }

                // Check for room/level definition.
                FileHandle section = Game.getInternal(mod + "/generator/" + child.name() + "/section.dat");
                if (section.exists()) {
                    SectionDefinition sectionDefinition = Game.fromJson(SectionDefinition.class, section);

                    if (!sectionDefinitions.contains(sectionDefinition, false)) {
                        sectionDefinitions.add(sectionDefinition);
                    }
                }
            }
        }

        themes.sort();
    }

    public Array<String> getThemes() {
        return themes;
    }

    public Array<SectionDefinition> getSectionDefinitions() {
        Array<SectionDefinition> sortedSectionDefinitions = sectionDefinitions;
        sortedSectionDefinitions.sort(new Comparator<SectionDefinition>() {
            @Override
            public int compare(SectionDefinition o1, SectionDefinition o2) {
                return Integer.signum(o1.sortOrder - o2.sortOrder);
            }
        });

        return sortedSectionDefinitions;
    }
}
