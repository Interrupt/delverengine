package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.ui.menu.AssetListItem;
import com.interrupt.dungeoneer.game.Game;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;

public class AssetPicker extends Dialog {

    public interface ResultListener {
        boolean result(boolean success, String result);
    }

    private final Skin skin;
    private boolean fileNameEnabled;
    private boolean newFolderEnabled;
    private final TextField fileNameInput;
    private final Label fileNameLabel;
    protected final FileHandle baseDir;
    private final Label fileListLabel;
    private final List fileList;

    private String parentDir;
    private String currentDir;
    protected String result;

    protected boolean includeBase = true;

    protected ResultListener resultListener;

    private Stage stage;

    private final TextButton ok;
    private final TextButton cancel;

    public Array<String> fullPathHistory = new Array<String>();

    private static final Comparator<AssetListItem> dirListComparator = new Comparator<AssetListItem>() {
        @Override
        public int compare(AssetListItem file1, AssetListItem file2) {
            if (file1.isDirectory && !file2.isDirectory) {
                return -1;
            }
            if (file1.isDirectory && file2.isDirectory) {
                return 0;
            }
            if (!file1.isDirectory && !file2.isDirectory) {
                return 0;
            }
            return 1;
        }
    };
    private FileFilter filter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };
    private boolean directoryBrowsingEnabled = true ;

    public AssetPicker(String title, final Skin skin, final String currentPath, final FileHandle baseDir, final boolean includeBase) {
        super(title, skin);
        this.skin = skin;
        this.baseDir = baseDir;
        this.includeBase = includeBase;
        currentDir = this.baseDir.path();

        Gdx.app.log("DelvEdit", "Asset picker showing with dir: " + baseDir.path());

        final Table content = getContentTable();
        content.top().left();

        fileListLabel = new Label("", skin);
        fileListLabel.setAlignment(Align.left);

        fileList = new List(skin);

        fileNameInput = new TextField("", skin);

        if(currentPath != null) {
            fileNameInput.setText(currentPath);
        }

        fileNameLabel = new Label("File name", skin);
        fileNameLabel.setColor(Color.GRAY);
        fileNameInput.setTextFieldListener(new TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                result = textField.getText();
            }
        });

        ok = new TextButton("Ok", skin);
        button(ok, true);

        cancel = new TextButton("Cancel", skin);
        button(cancel, false);
        key(Keys.ENTER, true);
        key(Keys.ESCAPE, false);

        fileList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AssetListItem selected = ((AssetListItem)fileList.getSelected());

                if (selected != null && !selected.isDirectory) {
                    result = selected.path;

                    if(result.startsWith("/") && result.length() > 1) {
                        result = result.substring(1);
                    }

                    if(!includeBase && baseDir.path().length() > 0) {
                        try {
                            result = result.substring(baseDir.path().length() + 1);
                        }
                        catch(Exception ex) { }
                    }

                    if(Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
                        fileNameInput.setText(fileNameInput.getText() + "," + result);
                    }
                    else {
                        fileNameInput.setText(result);
                    }
                }
            }
        });
    }

    private void changeDirectory(String directory) {

        Gdx.app.log("DelvEdit", "Changing directory to " + directory);

        parentDir = currentDir;
        currentDir = directory;

        String title = currentDir;

        if(title.equals("")) title = "/";
        if (title.length() > 38) title = "..." + title.substring(title.length() - 38, title.length());
        fileListLabel.setText(title);

        final Array<AssetListItem> items = new Array<AssetListItem>();

        final AssetListItem[] l = listDirectory(directory);
        Array<AssetListItem> list = new Array<AssetListItem>(l);

        for (final AssetListItem file : list) {
            FileHandle handle = Game.findInternalFileInMods(file.path);
            if (handle.file().isHidden()) continue;
            if (filter.accept(handle.file()) || handle.isDirectory()) {
                items.add(file);
            }
        }

        items.sort(dirListComparator);

        if(!directory.equals(baseDir.path())) {
            if(parentDir != null) {
                items.insert(0, new AssetListItem("..", "..", true, fullPathHistory.peek()));
            }
        }

        items.shrink();

        fileList.setItems(items.items);
        fileList.setSelected(fileNameInput.getText());

        // keep track of where we have been, for moving back through the tree
        fullPathHistory.add(directory);
    }

    public String getResult() {
        String path = fileNameInput.getText();
        Gdx.app.log("DelvEdit", "Returning result: " + path);
        return path;
    }

    public AssetPicker setFilter(FileFilter filter) {
        this.filter = filter;
        return this;
    }

    public AssetPicker setOkButtonText(String text) {
        this.ok.setText(text);
        return this;
    }


    public AssetPicker setCancelButtonText(String text) {
        this.cancel.setText(text);
        return this;
    }

    public AssetPicker setFileNameEnabled(boolean fileNameEnabled) {
        this.fileNameEnabled = fileNameEnabled;
        return this;
    }

    public AssetPicker setFileName(String fileName) {
        fileNameInput.setText(fileName);
        result = fileName;
        fileList.setSelected(fileName);
        return this;
    }

    public AssetPicker setResultListener(ResultListener result) {
        this.resultListener = result;
        return this;
    }


    public AssetPicker disableDirectoryBrowsing() {
        this.directoryBrowsingEnabled = false;
        return this;
    }


    @Override
    public Dialog show(Stage stage) {
        final Table content = getContentTable();
        content.add(fileListLabel).colspan(2).top().left().expandX().fillX().row();
        ScrollPane pane = new ScrollPane(fileList, skin);
        content.add(pane).size(300, 350).colspan(2).fill().expand().row();

        content.add(fileNameLabel);
        content.add(fileNameInput).fillX().expandX().row();
        stage.setKeyboardFocus(fileNameInput);

        Gdx.app.log("DelvEdit", "Showing AssetPicker dialog");

        fileList.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                Gdx.app.log("DelvEdit", "Got a click!");

                AssetListItem[] items = listDirectory(currentDir);
                FileHandle selected = null;

                AssetListItem selectedListItem = (AssetListItem)fileList.getSelected();
                if(selectedListItem == null)
                    return;

                String selection = selectedListItem.toString();

                Gdx.app.log("DelvEdit", "Clicked " + selection);

                if(selection.equals("../") || selection.equals("..")) {
                    fullPathHistory.pop();
                    changeDirectory(fullPathHistory.pop());
                }
                else {
                    String selectedName = selection.toString().replace("/", "");

                    for(int i = 0; i < items.length; i++) {
                        FileHandle file = Game.findInternalFileInMods(items[i].path);
                        if(file.name().equals(selectedName)) {
                            selected = file;
                        }
                    }

                    if(selected == null)
                        Gdx.app.log("DelvEdit", "Could not find selected file!");
                    else
                        Gdx.app.log("DelvEdit", "Found selected file: " + selected.path());

                    if (selected != null && (selected.isDirectory() || selected.extension().equals(""))) {
                        changeDirectory(selectedListItem.path);
                    }
                }
            }
        });

        this.stage = stage;
        changeDirectory(baseDir.path());

        // showing the dialog will set the focus
        stage.setScrollFocus(null);
        Dialog d = super.show(stage);

        // set the focuses back
        stage.setScrollFocus(pane);
        stage.setKeyboardFocus(fileNameInput);

        return d;
    }

    public AssetListItem[] listDirectory(String path) {
        Array<AssetListItem> files = new Array<AssetListItem>();

        Gdx.app.log("DelvEdit", "Listing directory: " + path);

        for(String folder : Game.modManager.modsFound) {
            String modFolderPath = folder + "/" + path;

            Gdx.app.log("DelvEdit", "Looking for files in: " + modFolderPath);

            FileHandle gf = Game.getInternal(modFolderPath);
            if(gf.exists()) {
                for(FileHandle entry : Game.listDirectory(gf)) {
                    AssetListItem item = new AssetListItem(path + "/" + entry.name(), entry.name(), entry.extension().equals(""), path);
                    if(!files.contains(item, false))
                        files.add(item);
                }
            }
        }

        // return as a bare array, like the directory list function
        files.shrink();
        AssetListItem[] r = new AssetListItem[files.size];
        for(int i = 0; i < files.size; i++) {
            r[i] = files.get(i);
        }
        return r;
    }
}