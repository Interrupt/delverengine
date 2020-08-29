package com.interrupt.dungeoneer.editor.file;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.TimeUtils;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.history.EditorHistory;
import com.interrupt.dungeoneer.editor.ui.EditorUi;
import com.interrupt.dungeoneer.editor.ui.FilePicker;
import com.interrupt.dungeoneer.editor.ui.NewLevelDialog;
import com.interrupt.dungeoneer.editor.ui.SaveChangesDialog;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.ExitTile;
import com.interrupt.dungeoneer.tiles.Tile;

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

    public static final String defaultName = "New Level";
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
                return (pathname.getName().endsWith(".bin") || pathname.getName().endsWith(".dat"));
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
            Game.toJson(level, Gdx.files.absolute(fileName));
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
        class WSFilter implements FileFilter {
            @Override
            public boolean accept(File path) {
                String name = path.getName();
                return (name.endsWith(".dat") || name.endsWith(".png") || name.endsWith(".bin"));
            }
        }
        FileFilter wsFilter = new WSFilter();

        if(Editor.app.file.directory() == null) {
            Editor.app.file = new EditorFile(new FileHandle("."));
        }

        FilePicker picker = FilePicker.createLoadDialog("Open Level", EditorUi.getSmallSkin(), new FileHandle(Editor.app.file.directory()));
        picker.setFileNameEnabled(true);
        picker.setNewFolderEnabled(false);
        if(Editor.app.file.name() != null) picker.setFileName(Editor.app.file.name());
        picker.setFilter(wsFilter);

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

    private void openInternal(FileHandle fileHandle) {
        try {
            // NOTE: You must access the new Editor file via Editor.app.file
            // for the rest of this method.
            Editor.app.file = new EditorFile(fileHandle);
            Editor.app.updateTitle();

            String fileName = Editor.app.file.name();
            String dir = Editor.app.file.directory();

            FileHandle levelFileHandle = Gdx.files.getFileHandle(fileHandle.file().getAbsolutePath(), Files.FileType.Absolute);
            if(levelFileHandle.exists()) {
                Level openLevel;

                if(fileName.endsWith(".png")) {
                    String heightFile = dir + fileName.replace(".png", "-height.png");
                    if(!Gdx.files.getFileHandle(heightFile, Files.FileType.Absolute).exists()) {
                        heightFile = dir + fileName.replace(".png", "_height.png");
                        if(!Gdx.files.getFileHandle(heightFile, Files.FileType.Absolute).exists()) {
                            heightFile = null;
                        }
                    }

                    openLevel = new Level();
                    openLevel.loadForEditor(dir + fileName, heightFile);
                }
                else if(fileName.endsWith(".bin")) {
                    openLevel = KryoSerializer.loadLevel(levelFileHandle);
                    openLevel.init(Level.Source.EDITOR);
                }
                else {
                    openLevel = Game.fromJson(Level.class, levelFileHandle);
                    openLevel.init(Level.Source.EDITOR);
                }

                Editor.app.level = openLevel;
                Editor.app.refresh();
                Editor.app.cameraController.setPosition(openLevel.width / 2f, 4.5f, openLevel.height / 2f);

                Editor.app.history = new EditorHistory();
                Editor.app.history.saveState(Editor.app.level);
                Editor.app.file.markClean();

                Editor.options.recentlyOpenedFiles.removeValue(levelFileHandle.path(), false);
                Editor.options.recentlyOpenedFiles.insert(0, levelFileHandle.path());

                Editor.app.viewSelected();
            }
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
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
        Editor.app.history = new EditorHistory();
        Editor.app.file = new EditorFile();

        Editor.app.level = new Level(width,height);
        Editor.app.refresh();

        Editor.app.history.saveState(Editor.app.level);
        Editor.app.file.markClean();

        Editor.app.cameraController.setPosition(
                Editor.app.level.width / 2f,
                Editor.app.level.height / 2f,
                4.5f
        );
        Editor.app.viewSelected();
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
}
