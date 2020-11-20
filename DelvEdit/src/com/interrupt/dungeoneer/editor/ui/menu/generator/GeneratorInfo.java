package com.interrupt.dungeoneer.editor.ui.menu.generator;

import com.badlogic.gdx.Gdx;
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
            refreshModGenerator(mod);
            refreshModData(mod);
        }

        refreshGenerator();
        refreshData();

        themes.sort();
    }

    private void refreshModGenerator(String mod) {
        Gdx.app.log("DEBUG", "Refresh Generator Mod: " + mod);
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
            Gdx.app.log("DEBUG", "Packaged Info: " + info.path());
            if (info.exists()) {
                String theme = child.name().toUpperCase();

                if (!themes.contains(theme, false)) {
                    themes.add(theme);
                }
            }

            // Check for room/level definition.
            FileHandle section = Game.getInternal(mod + "/generator/" + child.name() + "/section.dat");
            Gdx.app.log("DEBUG", "Packaged Section: " + section.path());
            if (section.exists()) {
                SectionDefinition sectionDefinition = JsonUtil.fromJson(SectionDefinition.class, section);

                if (!sectionDefinitions.contains(sectionDefinition, false)) {
                    sectionDefinitions.add(sectionDefinition);
                }
            }
        }
    }

    private void refreshGenerator() {
        Gdx.app.log("DEBUG", "Refresh Generator");
        Array<FileHandle> packagedInfos = Game.findPackagedFiles("info.dat");
        for (FileHandle packagedInfo : packagedInfos) {
            Gdx.app.log("DEBUG", "Packaged Info: " + packagedInfo.path());
            FileHandle themeRoot = packagedInfo.parent();
            if (themeRoot.parent().name().equals("generator")) {
                String theme = themeRoot.name().toUpperCase();

                if (!themes.contains(theme, false)) {
                    themes.add(theme);
                }
            }
        }

        Array<FileHandle> packagedSections = Game.findPackagedFiles("section.dat");
        for (FileHandle packagedSection : packagedSections) {
            Gdx.app.log("DEBUG", "Packaged Section: " + packagedSection.path());
            SectionDefinition sectionDefinition = JsonUtil.fromJson(SectionDefinition.class, packagedSection);

            if (!sectionDefinitions.contains(sectionDefinition, false)) {
                sectionDefinitions.add(sectionDefinition);
            }
        }
    }

    private void refreshModData(String mod) {
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

    // I guess we need to use either info.dat or section.dat?
    private void refreshData() {}

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
