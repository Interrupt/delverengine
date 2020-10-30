package com.interrupt.dungeoneer.editor.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.entities.Entity;

public class EditorHierarchyWindow extends Window {
    ScrollPane scrollPane;
    List<Entity> entityList;
    String searchText = "";

    public EditorHierarchyWindow() {
        super("Hierarchy", EditorUi.smallSkin);

        TextButton closeButton = new TextButton("Close", EditorUi.smallSkin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
            }
        });

        getTitleTable().padLeft(6f).padRight(6f).add(closeButton);

        TextField searchTextField = new TextField("", EditorUi.smallSkin);
        searchTextField.setTextFieldListener(new TextField.TextFieldListener() {
            public void keyTyped(TextField textField, char key) {
                searchText = textField.getText().toLowerCase();
                updateInternal();
            }
        });
        add(searchTextField).align(Align.left).fillX().row();

        entityList = new List<>(EditorUi.smallSkin);
        entityList.getSelection().setRequired(false);
        entityList.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Entity entity = entityList.getSelected();
                if (entity != null) {
                    Editor.app.pickEntity(entity);
                }
            }
        });

        scrollPane = new ScrollPane(entityList, EditorUi.smallSkin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFlickScroll(false);
        add(scrollPane).growX();

        scrollPane.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    if (((InputEvent) event).getType() == InputEvent.Type.enter) {
                        event.getStage().setScrollFocus(scrollPane);
                    }
                }
                return false;
            }
        });

        scrollPane.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    if (((InputEvent) event).getType() == InputEvent.Type.exit) {
                        event.getStage().setScrollFocus(null);
                    }
                }
                return false;
            }
        });

        setVisible(false);
    }

    public void show() {
        if (isVisible()) {
            return;
        }

        updateInternal();
        performShowAction();

    }

    public void hide() {
        if (!isVisible()) {
            return;
        }

        performHideAction();
    }

    public void update() {
        if (!isVisible()) {
            return;
        }

        updateInternal();
    }

    public void select() {
        if (Editor.selection.picked != null) {
            entityList.setSelected(Editor.selection.picked);
            updateScrollBarPosition();
        }
    }

    public void deselect() {
        entityList.setSelected(null);
    }

    private void updateInternal() {
        Array<Entity> entities = Editor.app.level.entities;
        Array<Entity> filteredEntities = new Array<>();

        for (int i = 0; i < entities.size;i++) {
            if (entities.get(i).toString().toLowerCase().contains(searchText)) {
                filteredEntities.add(entities.get(i));
            }
        }

        if (filteredEntities.size > 0) {
            entityList.setItems(filteredEntities);
            scrollPane.setVisible(true);

            if (Editor.selection.picked != null) {
                entityList.setSelected(Editor.selection.picked);
                updateScrollBarPosition();
            } else {
                entityList.setSelected(null);
                resetScrollBarPosition();
            }
        } else {
            scrollPane.setVisible(false);
        }

        pack();
        setWidth(300f);
        setHeight(400f);
    }

    private void performShowAction() {
        addAction(Actions.sequence(Actions.alpha(0f), new Action() {
            @Override
            public boolean act(float delta) {
                setVisible(true);
                return true;
            }
        }, Actions.fadeIn(0.35f, Interpolation.fade)));
    }

    private void performHideAction() {
        addAction(Actions.sequence(Actions.alpha(1f), Actions.fadeOut(0.35f, Interpolation.fade), new Action() {
            @Override
            public boolean act(float delta) {
                setVisible(false);
                return true;
            }
        }));
    }

    private void updateScrollBarPosition() {
        float scrollPercent = entityList.getSelectedIndex() / (entityList.getItems().size - 1f);
        scrollPercent = Math.min(1f, Math.max(0f, scrollPercent));
        scrollPane.setScrollPercentY(scrollPercent);
    }

    private void resetScrollBarPosition() {
        scrollPane.setScrollPercentY(0f);
    }
}
