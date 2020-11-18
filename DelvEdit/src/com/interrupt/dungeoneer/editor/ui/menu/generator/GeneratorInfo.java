package com.interrupt.dungeoneer.editor.ui.menu.generator;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.SectionDefinition;
import com.interrupt.utils.JsonUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class GeneratorInfo {
    private Array<String> themes = new Array<String>();
    private Array<String> builders = new Array<String>();

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
            refreshGenerator(mod);
            refreshData(mod);
        }

        themes.sort();
    }

    private void refreshGenerator(String mod) {
        FileHandle parent = Game.getInternal(mod + "/generator");
        if (!parent.exists() || !parent.isDirectory()) {
            return;
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
                SectionDefinition sectionDefinition = JsonUtil.fromJson(SectionDefinition.class, section);

                if (!sectionDefinitions.contains(sectionDefinition, false)) {
                    sectionDefinitions.add(sectionDefinition);
                }
            }
        }
    }

    private void refreshData(String mod) {
        FileHandle parent = Game.getInternal(mod + "/data/room-builders");
        if (!parent.exists() || !parent.isDirectory()) {
            return;
        }

        FileHandle[] children = parent.list(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        });

        for (FileHandle child : children) {
            // Check for room builders.
            String builder = child.nameWithoutExtension().toUpperCase();

            if (!builders.contains(builder, false)) {
                builders.add(builder);
            }
        }
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

    public boolean isLevelTemplateValid(Level template) {
        return template != null && themes.contains(template.theme, false)
                && builders.contains(template.roomGeneratorType, false);
    }

    public boolean isLastGeneratedLevelTemplateSelected(Level template) {
        return lastGeneratedLevelTemplate != null && lastGeneratedLevelTemplate.theme.equals(template.theme)
                && lastGeneratedLevelTemplate.roomGeneratorType.equals(template.roomGeneratorType);
    }

    public boolean isLastGeneratedRoomTemplateSelected(Level template) {
        return lastGeneratedRoomTemplate != null && lastGeneratedRoomTemplate.theme.equals(template.theme)
                && lastGeneratedRoomTemplate.roomGeneratorType.equals(template.roomGeneratorType);
    }
}
