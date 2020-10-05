package com.interrupt.dungeoneer.editor.ui.menu.generator;

import java.io.File;
import java.io.FileFilter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.generator.SectionDefinition;

/** Holds information about the theme, room and level generation. */
public class GeneratorInfo {
    /** List of available themes. */
    private Array<String> themes = new Array<String>();

    /** List of available room generators. */
    private Array<String> roomGenerators = new Array<String>();

    /** List of available level generators. */
    private Array<LevelGeneratorInfo> levelGenerators = new Array<LevelGeneratorInfo>();

    public String lastGeneratedRoomType;
    public String lastGeneratedLevelType;
    public String lastGeneratedLevelRoomType;

    public GeneratorInfo() {
        refresh();
    }
    
    /** Refreshes the generator information. */
    public void refresh() {
        themes.clear();
        roomGenerators.clear();
        levelGenerators.clear();

        Array<String> mods = Game.getModManager().modsFound;
        for (String mod : mods) {
            FileHandle parent = Game.getFile(mod + "/generator");
            if (!parent.exists() || !parent.isDirectory()) {
                continue;
            }

            FileHandle[] children = parent.list(new FileFilter(){
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });

            for (FileHandle child : children) {
                // Check for theme definition.
                FileHandle info = Game.getFile(child.path() + "/info.dat");
                if (info.exists()) {
                    String theme = child.nameWithoutExtension().toUpperCase();
                    
                    if (!themes.contains(theme, false)) {
                        themes.add(theme);
                    }
                }

                // Check for room/level definition.
                FileHandle section = Game.getFile(child.path() + "/section.dat");
                if (section.exists()) {
                    SectionDefinition sectionDefinition = Game.fromJson(SectionDefinition.class, section);

                    LevelGeneratorInfo levelGeneratorInfo = new LevelGeneratorInfo();
                    levelGeneratorInfo.name = sectionDefinition.name;

                    Array<LevelTemplateInfo> levelTemplateInfos = new Array<LevelTemplateInfo>();

                    for (Level level : sectionDefinition.levelTemplates) {
                        // TODO: Also display static levels.
                        if (!level.generated) {
                            continue;
                        }
                        
                        if (level.roomGeneratorType != null && !roomGenerators.contains(level.roomGeneratorType, false)) {
                            roomGenerators.add(level.roomGeneratorType);
                        }

                        LevelTemplateInfo levelTemplateInfo = new LevelTemplateInfo();
                        levelTemplateInfo.theme = level.theme;
                        levelTemplateInfo.roomGeneratorType = level.roomGeneratorType;

                        levelTemplateInfos.add(levelTemplateInfo);
                    }

                    levelGeneratorInfo.templates = levelTemplateInfos;
                    levelGenerators.add(levelGeneratorInfo);
                }
            }
        }

        themes.sort();
        roomGenerators.sort();
        levelGenerators.sort();

        // TODO: Make sure to adjust the lastGenerated* values when underlying data changed.
    }

    public Array<String> getThemes() {
        return themes;
    }

    public Array<String> getRoomGenerators() {
        return roomGenerators;
    }

    public Array<LevelGeneratorInfo> getLevelGenerators() {
        return levelGenerators;
    }
}
