package com.interrupt.dungeoneer.editor.file;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.TimeUtils;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.history.EditorHistory;
import com.interrupt.dungeoneer.editor.ui.*;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.ExitTile;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.utils.JsonUtil;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/** Class for working with the level file in editor. */
public class EditorFile {
    final String fileName;
    final String directory;
    final Date lastSaveDate;
    byte[] historyMarker;

    public static final String defaultName = "New Level.bin";
    public static final String defaultDirectory = ".";

    public EditorFile() {
        fileName = EditorFile.defaultName;
        directory = Gdx.files.local(EditorFile.defaultDirectory).path();
        lastSaveDate = new Date(TimeUtils.millis());
        historyMarker = Editor.app.history.top();
    }

    public EditorFile(FileHandle handle) {
        fileName = handle.name();
        directory = handle.file().getParent();
        lastSaveDate = new Date(TimeUtils.millis());
        historyMarker = Editor.app.history.top();
    }

    public String name() {
        return fileName;
    }

    public String directory() {
        return directory;
    }

    public boolean isDirty() {
        return !Arrays.equals(historyMarker, Editor.app.history.top());
    }

    public void markClean() {
        historyMarker = Editor.app.history.top();
        Editor.app.updateTitle();
    }

    /** Save file. Will prompt user for path if needed. */
    public void save() {
        save(new SaveAdapter());
    }

    /** Save file. Will prompt user for path if needed. Use the SaveListener for handling user choices. */
    public void save(final SaveListener listener) {
        if(Editor.app.file.name().equals(EditorFile.defaultName) && Editor.app.file.directory().equals(EditorFile.defaultDirectory)) {
            saveAs(listener);
        }
        else {
            saveInternal();
            listener.onSave();
        }
    }

    /** Save file as. Will prompt user for path. */
    public void saveAs(final SaveListener listener) {
        class WSFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                return hasValidSavableExtension(pathname.getName());
            }
        }

        FileFilter wsFilter = new WSFilter();

        FilePicker picker = FilePicker.createSaveDialog("Save Level", EditorUi.getSmallSkin(), new FileHandle(Editor.app.file.directory()));
        picker.setFileNameEnabled(true);
        picker.setNewFolderEnabled(false);
        picker.setFilter(wsFilter);
        picker.setFileName(Editor.app.file.name());

        picker.setResultListener(new FilePicker.ResultListener() {
            @Override
            public boolean result(boolean success, FileHandle result) {
                if(success) {
                    try {
                        result = addDefaultExtension(result);
                        Editor.app.file = new EditorFile(result);
                        Editor.app.file.saveInternal();
                    }
                    catch(Exception ex) {
                        Gdx.app.error("DelvEdit", ex.getMessage());
                    }

                    listener.onSave();
                }
                else {
                    listener.onCancel();
                }

                return true;
            }
        });

        picker.show(Editor.app.ui.getStage());

        Editor.app.ui.showingModal = picker;
        Editor.app.editorInput.resetKeys();
    }

    private void saveInternal() {
        saveInternal(Paths.get(directory, fileName).toString());
    }

    private void saveInternal(String fileName) {
        markClean();

        Level level = Editor.app.level;
        level.preSaveCleanup();

        // cleanup some of the tiles
        for(int x = 0; x < level.width; x++) {
            for(int y = 0; y < level.height; y++) {
                Tile cTile = level.getTileOrNull(x, y);
                if(cTile == null) {

                    // if any tiles around are not solid, make this a real tile
                    boolean makeRealTile = false;
                    for(int xx = x - 1; xx <= x + 1; xx += 2) {
                        for(int yy = y - 1; yy <= y + 1; yy += 2) {
                            Tile tile = level.getTile(xx, yy);
                            if(!tile.renderSolid) makeRealTile = true;
                        }
                    }

                    if(makeRealTile) {
                        Tile t = new Tile();
                        t.renderSolid = true;
                        t.blockMotion = true;
                        level.setTile(x, y, t);
                    }
                }
                else {
                    if(cTile.wallTex == 6 && !(cTile instanceof ExitTile) && cTile.IsSolid()) {
                        ExitTile exitTile = new ExitTile();
                        Tile.copy(cTile, exitTile);
                        level.setTile(x,  y, exitTile);
                    }
                }
            }
        }

        // write as json
        if(fileName.endsWith(".dat")) {
            JsonUtil.toJson(level, Gdx.files.absolute(fileName));
        }
        else {
            KryoSerializer.saveLevel(Gdx.files.absolute(fileName), level);
        }
    }

    /** Open the given file. */
    public void open(final FileHandle fileHandle) {
        Dialog savePrompt = new SaveChangesDialog() {
            @Override
            public void onSave() {
                Editor.app.file.save(new SaveAdapter() {
                    @Override
                    public void onSave() {
                        openInternal(fileHandle);
                    }
                });
            }

            @Override
            public void onDontSave() {
                openInternal(fileHandle);
            }
        };

        savePrompt.show(Editor.app.ui.getStage());
    }

    /** Prompt user then open file. */
    public void open() {
        Dialog savePrompt = new SaveChangesDialog() {
            @Override
            public void onSave() {
                Editor.app.file.save(new SaveAdapter() {
                    @Override
                    public void onSave() {
                        promptOpenFile();
                    }
                });
            }

            @Override
            public void onDontSave() {
                promptOpenFile();
            }
        };

        savePrompt.show(Editor.app.ui.getStage());
    }

    private void promptOpenFile() {
        class DefaultFileFilter implements FileFilter {
            @Override
            public boolean accept(File path) {
                return hasValidLoadableExtension(path.getName());
            }
        }
        FileFilter defaultFileFilter = new DefaultFileFilter();

        class LegacyFileFilter implements FileFilter {
            @Override
            public boolean accept(File path) {
                String name = path.getName();
                return (hasValidLoadableExtension(name) || hasValidLoadableLegacyExtension(name));
            }
        }
        FileFilter legacyFileFilter = new LegacyFileFilter();

        if(Editor.app.file.directory() == null) {
            Editor.app.file = new EditorFile(new FileHandle("."));
        }

        FilePicker picker = FilePicker.createLoadDialog("Open Level", EditorUi.getSmallSkin(), new FileHandle(Editor.app.file.directory()));
        picker.setFileNameEnabled(true);
        picker.setNewFolderEnabled(false);
        if(Editor.app.file.name() != null) picker.setFileName(Editor.app.file.name());
        picker.setFilter(defaultFileFilter);
        picker.enableLegacyFormatDisplayToggle(defaultFileFilter, legacyFileFilter);

        picker.setResultListener(new FilePicker.ResultListener() {
            @Override
            public boolean result(boolean success, FileHandle result) {
                if(success) {
                    openInternal(result);
                }

                return true;
            }
        });

        picker.show(Editor.app.ui.getStage());

        Editor.app.ui.showingModal = picker;
        Editor.app.editorInput.resetKeys();
    }

    private void openInternal(FileHandle file) {
        file = Gdx.files.getFileHandle(file.file().getAbsolutePath(), Files.FileType.Absolute);
        String name = file.name();

        if (file.exists()) {
            Level level;

            try {
                if (name.endsWith(".png")) {
                    level = loadPngFile(file);
                } else if (name.endsWith(".bin")) {
                    level = loadBinFile(file);
                } else if (name.endsWith(".dat")) {
                    level = loadDatFile(file);
                } else {
                    showWarningDialog("Unknown extension. Cannot load '" + name + "'.");
                    return;
                }
            } catch (SerializationException exception) {
                showWarningDialog("Corrupted file data. Cannot load '" + file.name() + "'.");
                return;
            } catch (Exception exception) {
                showWarningDialog(exception.getMessage() + " Cannot load '" + file.name() + "'.");
                return;
            }

            if (!validateLevel(level)) {
                showWarningDialog("Invalid level data. Cannot load '" + file.name() + "'.");
                return;
            }

            Editor.app.file = new EditorFile(file);

            Editor.app.level = level;
            Editor.app.refresh();
            Editor.app.cameraController.setPosition(level.width / 2f, 4.5f, level.height / 2f);

            Editor.app.history = new EditorHistory();
            Editor.app.history.saveState(Editor.app.level);
            Editor.app.file.markClean();

            Editor.options.addRecentlyOpenedFile(file.path());

            Editor.app.clearEntitySelection();
            Editor.app.viewSelected();
        } else {
            Editor.options.removeRecentlyOpenedFile(file.path());
            showWarningDialog("File does not exist. Cannot load '" + name + "'.");
            return;
        }
    }

    /** Loads a `.png` level file. */
    private Level loadPngFile(FileHandle file) {
        String name = file.name();
        String directory = file.file().getParent();

        String heightFile = directory + name.replace(".png", "-height.png");
        if (!Gdx.files.getFileHandle(heightFile, Files.FileType.Absolute).exists()) {
            heightFile = directory + name.replace(".png", "_height.png");
            if (!Gdx.files.getFileHandle(heightFile, Files.FileType.Absolute).exists()) {
                heightFile = null;
            }
        }

        Level level = new Level();
        level.loadForEditor(directory + name, heightFile);

        return level;
    }

    /** Loads a `.bin` level file. */
    private Level loadBinFile(FileHandle file) {
        Level level = KryoSerializer.loadLevel(file);
        level.init(Level.Source.EDITOR);

        return level;
    }

    /** Loads a `.dat` level file. */
    private Level loadDatFile(FileHandle file) {
        Level level = JsonUtil.fromJson(Level.class, file);
        level.postLoad();
        level.init(Level.Source.EDITOR);

        return level;
    }

    /** Validates that a given level is loaded correctly from storage. */
    private boolean validateLevel(Level level) {
        return level.entities != null;
    }

    /** Prompt user and create a new level. */
    public void create() {
        final Stage stage = Editor.app.ui.getStage();

        final NewLevelDialog newLevelDialog = new NewLevelDialog(EditorUi.getSmallSkin()) {
            @Override
            protected void result(Object object) {
                if((Boolean)object) {
                    createInternal(getLevelWidth(), getLevelHeight());
                }
            }
        };

        SaveChangesDialog savePrompt = new SaveChangesDialog() {
            @Override
            public void onSave() {
                Editor.app.file.save(new SaveAdapter() {
                    @Override
                    public void onSave() {
                        newLevelDialog.show(stage);
                        Editor.app.editorInput.resetKeys();
                    }
                });
            }

            @Override
            public void onDontSave() {
                newLevelDialog.show(stage);
                Editor.app.editorInput.resetKeys();
            }
        };

        savePrompt.show(stage);
    }

    private void createInternal(int width, int height) {
        Editor.app.createEmptyLevel(width, height);
    }

    private FileHandle addDefaultExtension(FileHandle fileHandle) {
        if (hasValidSavableExtension(fileHandle.name())) {
            return fileHandle;
        }

        return new FileHandle(fileHandle.path() + ".bin");
    }

    private boolean hasValidSavableExtension(String fileName) {
        return (fileName.endsWith(".dat") || fileName.endsWith(".bin"));
    }

    private boolean hasValidLoadableExtension(String fileName) {
        return (fileName.endsWith(".dat") || fileName.endsWith(".bin"));
    }

    private boolean hasValidLoadableLegacyExtension(String fileName) {
        return fileName.endsWith(".png");
    }

    public long getMillisSinceLastSave() {
        return Math.abs(TimeUtils.millis() - lastSaveDate.getTime());
    }

    public long getSecondsSinceLastSave() {
        return TimeUnit.SECONDS.convert(getMillisSinceLastSave(), TimeUnit.MILLISECONDS);
    }

    public long getMinutesSinceLastSave() {
        return TimeUnit.MINUTES.convert(getMillisSinceLastSave(), TimeUnit.MILLISECONDS);
    }

    public long getHoursSinceLastSave() {
        return TimeUnit.HOURS.convert(getMillisSinceLastSave(), TimeUnit.MILLISECONDS);
    }

    private void showWarningDialog(String warning) {
        Gdx.app.log("EditorFile", warning);

        WarningDialog warningDialog = new WarningDialog(EditorUi.smallSkin, warning);
        warningDialog.show(Editor.app.ui.getStage());
    }
}
