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
    private Array<String> themes = new Array<>();
    private Array<String> builders = new Array<>();

    private Array<SectionDefinition> sectionDefinitions = new Array<>();

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
            // Note: This prevents duplication in editor. Data is gathered by looking into the packaged files instead.
            if (mod.equals(".")) { continue; }

            refreshModGenerator(mod);
            refreshModData(mod);
        }

        refreshInternalGenerator();
        refreshInternalData();

        themes.sort();
        sectionDefinitions.sort(new Comparator<SectionDefinition>() {
            @Override
            public int compare(SectionDefinition o1, SectionDefinition o2) {
                return Integer.signum(o1.sortOrder - o2.sortOrder);
            }
        });
    }

    private void refreshModGenerator(String mod) {
        FileHandle parent = Game.getFile(mod + "/" + "generator");
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
            FileHandle info = Game.getFile(mod + "/" + "generator" + "/" + child.name() + "/" + "info.dat");
            if (info.exists()) {
                String theme = child.name().toUpperCase();

                if (!themes.contains(theme, false)) {
                    themes.add(theme);
                }
            }

            FileHandle section = Game.getFile(mod + "/" + "generator" + "/" + child.name() + "/" + "section.dat");
            if (section.exists()) {
                SectionDefinition sectionDefinition = JsonUtil.fromJson(SectionDefinition.class, section);

                if (!sectionDefinitions.contains(sectionDefinition, false)) {
                    sectionDefinitions.add(sectionDefinition);
                }
            }
        }
    }

    private void refreshInternalGenerator() {
        Array<FileHandle> packagedInfos = Game.findPackagedFiles("info.dat");

        for (FileHandle packagedInfo : packagedInfos) {
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
            SectionDefinition sectionDefinition = JsonUtil.fromJson(SectionDefinition.class, packagedSection);

            if (!sectionDefinitions.contains(sectionDefinition, false)) {
                sectionDefinitions.add(sectionDefinition);
            }
        }
    }

    private void refreshModData(String mod) {
        FileHandle parent = Game.getFile(mod + "/" + "data" + "/" + "room-builders");
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
            String builder = child.nameWithoutExtension().toUpperCase();

            if (!builders.contains(builder, false)) {
                builders.add(builder);
            }
        }
    }

    private void refreshInternalData() {
        Array<FileHandle> packagedBuilders = Game.findPackagedFiles("_rooms.dat");

        for (FileHandle packagedBuilder : packagedBuilders) {
            if (packagedBuilder.parent().name().equals("room-builders")) {
                String builder = packagedBuilder.nameWithoutExtension().toUpperCase();

                if (!builders.contains(builder, false)) {
                    builders.add(builder);
                }
            }
        }
    }

    public Array<String> getThemes() {
        return themes;
    }

    public Array<SectionDefinition> getSectionDefinitions() {
        return sectionDefinitions;
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
