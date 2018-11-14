package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Json;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.api.steam.workshop.WorkshopModData;
import com.interrupt.dungeoneer.steamapi.SteamEditorApi;

import java.io.File;
import java.io.FileFilter;

public class UploadModDialog extends Window {

    TextField imageField;
    TextField titleField;
    TextField idField;
    TextField modFolderField;
    Label statusLabel;

    final Skin skin;

    TextButton cancelButton;
    TextButton pickFolderButton;
    TextButton pickImageButton;
    TextButton uploadButton;

    public UploadModDialog(String title, final Skin skin) {
        super(title, skin);
        this.skin = skin;
        this.setModal(true);
        final UploadModDialog window = this;

        imageField = new TextField("", skin);
        titleField = new TextField("", skin);
        idField = new TextField("", skin);
        modFolderField = new TextField("", skin);
        statusLabel = new Label("", skin);
        statusLabel.setColor(51f / 255f, 181f / 255f, 229f / 255f, 1f);

        String currentDirectoryPath = new FileHandle(".").file().getAbsolutePath();
        final String currentDirectory = currentDirectoryPath.substring(0, currentDirectoryPath.length() - 2);

        pickFolderButton = new TextButton("Choose Folder", skin);
        pickFolderButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FilePicker picker = FilePicker.createPickDialog("Select Mod Folder", skin, new FileHandle(currentDirectory));
                picker.setOkButtonText("Use This Folder");

                picker.setFilter(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });

                picker.setResultListener(new FilePicker.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if(success) {
                            makeStepTwoContent(result);
                        }
                        return true;
                    }
                });

                picker.show(window.getStage());
            }
        });

        pickImageButton = new TextButton("Choose Image", skin);
        pickImageButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                FilePicker picker = FilePicker.createPickDialog("Select Preview Image", skin, new FileHandle(currentDirectory));
                picker.setFilter(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".png") || pathname.getName().endsWith(".jpg");
                    }
                });

                picker.setResultListener(new FilePicker.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if(success) {
                            imageField.setText(result.toString());
                        }
                        return true;
                    }
                });

                picker.show(window.getStage());
            }
        });

        cancelButton = new TextButton("Close", skin);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                window.hide();
            }
        });

        uploadButton = new TextButton("Upload Mod", skin);
        uploadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SteamEditorApi.uploadModDialog = window;
                SteamApi.api.uploadToWorkshop(getWorkshopId(), getModImagePath(), getModTitle(), getModFolderPath());
            }
        });

        makeStepOneContent();

        /*add(new Label("Preview Image", skin)).align(Align.left);
        add(imageField).align(Align.left);
        row();

        add(new Label("Title", skin)).align(Align.left);
        add(titleField).width(300f);
        row();

        add(new Label("Workshop Id", skin)).align(Align.left);
        add(idField).align(Align.left);
        row();

        add(new Label("Mod Folder", skin)).align(Align.left);
        add(modFolderField).align(Align.left);
        row();

        statusLabel = new Label("", skin);
        add(statusLabel).width(300f).height(30).colspan(2).align(Align.left);
        row();

        TextButton uploadButton = new TextButton("Upload Mod", skin);
        //TextButton cancelButton = new TextButton("Cancel", skin);

        final UploadModDialog parentWindow = this;

        uploadButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                SteamEditorApi.uploadModDialog = parentWindow;
                SteamApi.api.uploadToWorkshop(getWorkshopId(), getModImagePath(), getModTitle(), getModFolderPath());
            }
        });

        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                parentWindow.hide();
            }
        });

        add(uploadButton).pad(4f);
        add(cancelButton).pad(4f).align(Align.right);*/
    }

    public void makeStepOneContent() {
        clear();

        add(new Label("Pick Mod Folder", skin)).align(Align.left);
        add(modFolderField).width(300f).align(Align.left);
        add(pickFolderButton).align(Align.left);
        row();

        add(cancelButton).colspan(3).pad(4f).align(Align.right);

        pack();
    }

    public void makeStepTwoContent(FileHandle folder) {
        clear();

        modFolderField.setText(folder.toString());

        loadModMetaData();

        add(new Label("Pick Mod Folder", skin)).align(Align.left);
        add(modFolderField).width(300f).align(Align.left);
        add(pickFolderButton).align(Align.left);
        row();

        add(new Label("Preview Image", skin)).align(Align.left);
        add(imageField).width(300f).align(Align.left);
        add(pickImageButton).align(Align.left);
        row();

        add(new Label("Title", skin)).align(Align.left);
        add(titleField).colspan(2).fillX();
        row();

        add(statusLabel).colspan(2).fillX().height(36).colspan(2).align(Align.left);
        row();

        add(uploadButton).align(Align.left);
        add(cancelButton).colspan(2).pad(4f).align(Align.right);

        if(getWorkshopId() == null) {
            uploadButton.setText("Create New Mod");
        }
        else {
            uploadButton.setText("Update Mod");
        }

        pack();
    }

    public void makeDoneContent() {
        uploadButton.setText("Update Mod");
        pack();
    }

    public Window show (Stage stage) {
        clearActions();

        pack();
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        stage.addActor(this);
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);

        return this;
    }

    public void setUploadStatus(String status) {
        statusLabel.setText(status);
    }

    public void hide () {
        remove();
    }

    public String getModImagePath() {
        return imageField.getText();
    }

    public String getModTitle() {
        return titleField.getText();
    }

    public Long getWorkshopId() {
        if(idField.getText().equals("")) return null;
        return Long.parseLong(idField.getText());
    }

    public void setWorkshopId(Long workshopId) {
        idField.setText(workshopId.toString());
    }

    public String getModFolderPath() {
        return modFolderField.getText();
    }

    private void loadModMetaData() {
        try {
            FileHandle modInfoFile = new FileHandle(getModFolderPath()).child("modInfo.json");
            if(modInfoFile.exists()) {
                Json json = new Json();
                WorkshopModData data = json.fromJson(WorkshopModData.class, new FileHandle(getModFolderPath()).child("modInfo.json"));
                setWorkshopId(data.workshopId);
                titleField.setText(data.title);
                imageField.setText(new FileHandle(getModFolderPath()).child(data.image).file().getAbsolutePath());
            }
        }
        catch(Exception ex) {
            Gdx.app.error("DelvEdit", ex.getMessage());
        }
    }
}
