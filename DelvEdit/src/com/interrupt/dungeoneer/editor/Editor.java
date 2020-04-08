package com.interrupt.dungeoneer.editor;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.interrupt.dungeoneer.editor.history.EditorHistory;
import com.interrupt.dungeoneer.editor.ui.EditorUi;
import com.interrupt.dungeoneer.editor.ui.FilePicker;
import com.interrupt.dungeoneer.editor.ui.FilePicker.ResultListener;
import com.interrupt.dungeoneer.editor.ui.menu.Scene2dMenuBar;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Level.Source;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.Timer;

public class Editor {
	
	private EditorFrame editorFrame;
	private final JFrame frame;
	
	private String currentFileName = null;
    private String currentDirectory = null;

    public enum EditorMode { Carve, Paint };

    public ActionListener saveAction;
    public ActionListener saveAsAction;
    public ActionListener openAction;
    public ActionListener exitAction;

    public ActionListener rotateLeftAction;
    public ActionListener rotateRightAction;
    public ActionListener playAction;
    public ActionListener carveAction;
    public ActionListener paintAction;
    public ActionListener deleteAction;
    public ActionListener planeHeightAction;
    public ActionListener vertexHeightAction;
    public ActionListener vertexToggleAction;
    public ActionListener undoAction;
    public ActionListener redoAction;
    public ActionListener toggleCollisionBoxesAction;
    public ActionListener toggleLightsAction;
    public ActionListener escapeAction;
    public ActionListener rotateCeilTexAction;
    public ActionListener rotateFloorTexAction;
    public ActionListener rotateWallAngle;
    public ActionListener copyAction;
    public ActionListener pasteAction;

    public ActionListener moveTileNorthAction;
    public ActionListener moveTileSouthAction;
    public ActionListener moveTileEastAction;
    public ActionListener moveTileWestAction;
    public ActionListener moveTileUpAction;
    public ActionListener moveTileDownAction;

    public ActionListener raiseFloorAction;
    public ActionListener lowerFloorAction;
    public ActionListener raiseCeilingAction;
    public ActionListener lowerCeilingAction;
    public ActionListener paintWallAction;
    public ActionListener pickWallAction;
    public ActionListener pickNewWallTexAction;
    public ActionListener fillTextureAction;

    public ActionListener xDragMode;
    public ActionListener yDragMode;
    public ActionListener zDragMode;
    public ActionListener rotateMode;

    public ActionListener flattenFloor;
    public ActionListener flattenCeiling;

    public ActionListener toggleSimulation;

    public EditorOptions editorOptions;

    private Timer saveMessageTimer = new Timer();
	
	public Editor() {

		frame = new JFrame("DelvEdit");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                super.windowClosing(e);
            }
        });

        Graphics.DisplayMode defaultMode = LwjglApplicationConfiguration.getDesktopDisplayMode();
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "DelvEdit";
        config.fullscreen = false;
        config.width = defaultMode.width;
        config.height = defaultMode.height;
        config.vSyncEnabled = true;
        config.foregroundFPS = 120;
        config.backgroundFPS = 30;
        config.stencil = 8;

        config.addIcon("icon-128.png", Files.FileType.Internal); // 128x128 icon (mac OS)
        config.addIcon("icon-32.png", Files.FileType.Internal);  // 32x32 icon (Windows + Linux)
        config.addIcon("icon-16.png", Files.FileType.Internal);  // 16x16 icon (Windows)
		
		editorFrame = new EditorFrame(frame, this);
		new LwjglApplication(editorFrame, config);

		editorOptions = EditorOptions.fromLocalFiles();
		initActions();

		setTitle("New Level");
	}

    private void initActions() {
		
		final Editor editor = this;

        flattenFloor = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.flattenFloor();
            }
        };

        flattenCeiling = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.flattenCeiling();
            }
        };

        saveAction = new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                // invoke the save as dialog if we haven't saved anything yet
                if(currentFileName == null || currentDirectory == null) {
                    saveAsAction.actionPerformed(event);
                }
                else {
                    editorFrame.save(currentDirectory + currentFileName);
                    frame.setTitle("DelvEdit - " + currentFileName);
                }
            }
        };

        saveAsAction = new ActionListener() {
            public void actionPerformed (ActionEvent event) {

                //FileDialog dialog = new FileDialog(frame, "Save Level", FileDialog.SAVE);

                class WSFilter implements FileFilter {
                    @Override
                    public boolean accept(File pathname) {
                        return (pathname.getName().endsWith(".bin") || pathname.getName().endsWith(".dat"));
                    }
                };
                FileFilter wsFilter = new WSFilter();

                if(currentDirectory == null) {
                    currentDirectory = new FileHandle(".").file().getAbsolutePath();
                    currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 2);
                }

                FilePicker picker = FilePicker.createSaveDialog("Save Level", EditorUi.getSmallSkin(), new FileHandle(currentDirectory));
                picker.setFileNameEnabled(true);
                picker.setNewFolderEnabled(false);
                picker.setFilter(wsFilter);

                if(currentFileName == null) {
                    picker.setFileName("level.bin");
                }
                else {
                    picker.setFileName(currentFileName);
                }

                picker.setResultListener(new ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if(success) {
                            try {
                                editorFrame.save(result.file().getAbsolutePath());
                                currentDirectory = result.file().getParent() + "/";
                                currentFileName = result.name();

                                setTitle(currentFileName);
                            }
                            catch(Exception ex) {
                                Gdx.app.error("DelvEdit", ex.getMessage());
                            }
                        }
                        return true;
                    }
                });

                picker.show(editorFrame.editorUi.getStage());

                editor.editorFrame.editorUi.showingModal = picker;
                editorFrame.editorInput.resetKeys();
            }
        };

        openAction = new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                FileDialog dialog = new FileDialog(frame, "Open Level", FileDialog.LOAD);
                class WSFilter implements FileFilter {
                    @Override
                    public boolean accept(File path) {
                        String name = path.getName();
                        return (name.endsWith(".dat") || name.endsWith(".png") || name.endsWith(".bin"));
                    }
                }
                FileFilter wsFilter = new WSFilter();

                if(currentDirectory == null) {
                    currentDirectory = new FileHandle(".").file().getAbsolutePath();
                    currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 2);
                }

                FilePicker picker = FilePicker.createLoadDialog("Open Level", EditorUi.getSmallSkin(), new FileHandle(currentDirectory));
                picker.setFileNameEnabled(true);
                picker.setNewFolderEnabled(false);
                if(currentFileName != null) picker.setFileName(currentFileName);
                picker.setFilter(wsFilter);

                picker.setResultListener(new ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if(success) {
                            openLevel(result);
                        }

                        return true;
                    }
                });

                picker.show(editorFrame.editorUi.getStage());

                editor.editorFrame.editorUi.showingModal = picker;
                editorFrame.editorInput.resetKeys();
            }
        };

        exitAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        };

        rotateLeftAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editorFrame.level.rotate90();
                editorFrame.refresh();
            }
        };

        rotateRightAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editorFrame.level.rotate90();
                editorFrame.level.rotate90();
                editorFrame.level.rotate90();
                editorFrame.refresh();
            }
        };

        playAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                editorFrame.testLevel();
            }
        };

        carveAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doCarve();
            }
        };

        paintAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doPaint();
            }
        };

        deleteAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doDelete();
            }
        };

        planeHeightAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.setPlaneHeightMode();
            }
        };

        vertexHeightAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.setVertexHeightMode();
            }
        };

        vertexToggleAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.toggleVertexHeightMode();
            }
        };

        undoAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.undo();
            }
        };

        redoAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.redo();
            }
        };

        toggleCollisionBoxesAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.toggleCollisionBoxes();
            }
        };

        toggleLightsAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.toggleLights();
            }
        };

        escapeAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.clearSelection();
            }
        };

        rotateCeilTexAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.rotateCeilTex(1);
            }
        };

        rotateFloorTexAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.rotateFloorTex(1);
            }
        };

        raiseFloorAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doFloorMoveUp();
            }
        };

        lowerFloorAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doFloorMoveDown();
            }
        };

        raiseCeilingAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doCeilMoveUp();
            }
        };

        lowerCeilingAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.doCeilMoveDown();
            }
        };

        paintWallAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.paintSurfaceAtCursor();
            }
        };

        pickWallAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.pickTextureAtSurface();
            }
        };

        pickNewWallTexAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.pickNewSurfaceTexture();
            }
        };

        fillTextureAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.fillSurfaceTexture();
            }
        };

        rotateWallAngle = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.rotateAngle();
            }
        };

        copyAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.copy();
            }
        };

        pasteAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.paste();
            }
        };

        moveTileNorthAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.moveTiles(0, 1, 0);
            }
        };

        moveTileSouthAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.moveTiles(0, -1, 0);
            }
        };

        moveTileEastAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.moveTiles(1, 0, 0);
            }
        };

        moveTileWestAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.moveTiles(-1, 0, 0);
            }
        };

        moveTileUpAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.moveTiles(0, 0, 0.5f);
            }
        };

        moveTileDownAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.moveTiles(0, 0, -0.5f);
            }
        };

        toggleSimulation = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                editorFrame.toggleSimulation();
            }
        };

        xDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { editorFrame.setDragMode(EditorFrame.DragMode.X);
            }
        };
        yDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { editorFrame.setDragMode(EditorFrame.DragMode.Y);
            }
        };
        zDragMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { editorFrame.setDragMode(EditorFrame.DragMode.Z);
            }
        };
        rotateMode = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) { editorFrame.setMoveMode(EditorFrame.MoveMode.ROTATE);
            }
        };
	}

    public void createdNewLevel() {
	    currentDirectory = null;
	    currentFileName = null;
	    setTitle("New Level");
    }

    public void openLevel(FileHandle fileHandle) {
	    try {
            currentDirectory = fileHandle.file().getParent() + "/";
            currentFileName = fileHandle.name();
            saveMessageTimer.cancel();

            setTitle(currentFileName);

            String file = currentFileName;
            String dir = currentDirectory;

            FileHandle level = Gdx.files.getFileHandle(fileHandle.file().getAbsolutePath(), FileType.Absolute);
            if(level.exists()) {
                editorFrame.curFileName = level.path();

                setTitle(currentFileName);

                if(file.endsWith(".png")) {
                    String heightFile = dir + file.replace(".png", "-height.png");
                    if(!Gdx.files.getFileHandle(heightFile, FileType.Absolute).exists()) {
                        heightFile = dir + file.replace(".png", "_height.png");
                        if(!Gdx.files.getFileHandle(heightFile, FileType.Absolute).exists()) {
                            heightFile = null;
                        }
                    }

                    Level openLevel = new Level();
                    openLevel.loadForEditor(dir + file, heightFile);
                    editorFrame.level = openLevel;
                    editorFrame.refresh();

                    editorFrame.camX = openLevel.width / 2f;
                    editorFrame.camZ = 4.5f;
                    editorFrame.camY = openLevel.height / 2f;
                }
                else if(file.endsWith(".bin")) {
                    Level openLevel = KryoSerializer.loadLevel(level);

                    openLevel.init(Source.EDITOR);

                    editorFrame.level = openLevel;
                    editorFrame.refresh();

                    editorFrame.camX = openLevel.width / 2f;
                    editorFrame.camZ = 4.5f;
                    editorFrame.camY = openLevel.height / 2f;
                }
                else {
                    Level openLevel = Game.fromJson(Level.class, level);
                    openLevel.init(Source.EDITOR);

                    editorFrame.level = openLevel;
                    editorFrame.refresh();

                    editorFrame.camX = openLevel.width / 2f;
                    editorFrame.camZ = 4.5f;
                    editorFrame.camY = openLevel.height / 2f;
                }

                editorFrame.history = new EditorHistory();
                editorOptions.recentlyOpenedFiles.removeValue(level.path(), false);
                editorOptions.recentlyOpenedFiles.insert(0, level.path());
            }
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
    }

    public void dispose() {
	    EditorOptions.toLocalFiles(editorOptions);
    }

    public void setTitle(String title) {
        Gdx.graphics.setTitle(title + " — DelvEdit");
    }
}
