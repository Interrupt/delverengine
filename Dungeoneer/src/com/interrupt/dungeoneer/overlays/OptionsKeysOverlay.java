package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.GameData;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.ReadableKeys;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;

import java.text.MessageFormat;

public class OptionsKeysOverlay extends WindowOverlay {
    private Table content = new Table();
    private Table mainTable;
    private Table changeKeyTable;
    private ArrayMap<Integer, Actions.Action> existingActions = new ArrayMap<Integer, Actions.Action>();

    boolean ignoreEscape = false;

    public OptionsKeysOverlay() {
        animateBackground = false;
    }

    public OptionsKeysOverlay(boolean dimScreen, boolean showBackground)
    {
        animateBackground = false;
        this.dimScreen = dimScreen;
        this.showBackground = showBackground;
    }

    @Override
    public Table makeContent() {

        // check if the Jump option should be enabled
        try {
            GameData gameData = JsonUtil.fromJson(GameData.class, Game.findInternalFileInMods("data/game.dat"));
            if (gameData != null && gameData.playerJumpEnabled) {
                if (!Actions.keyOrder.contains(Actions.Action.JUMP, false)) {
                    Actions.keyOrder.add(Actions.Action.JUMP);
                }
            }
        }
        catch(Exception ex) {
            Gdx.app.log("KeysOverlay", ex.getMessage());
        }

        setupTable();
        return content;
    }

    public void setupTable() {
        changeKeyTable = null;

        buttonOrder.clear();
        buttonLabels.clear();
        existingActions.clear();
        lastGamepadSelectionIndex = null;

        mainTable = new Table();
        mainTable.setFillParent(true);

        Label header = new Label(StringManager.get("screens.OptionsKeysScreen.keyboardControlsLabel"),skin.get(Label.LabelStyle.class));
        header.setFontScale(1.1f);
        mainTable.add(header);
        mainTable.row();

        for(final Actions.Action action : Actions.keyOrder) {
            Integer keyBinding = Actions.keyBindings.get(action);

            if(keyBinding != null)
                existingActions.put(keyBinding, action);

            final String readableAction = "screens.OptionsScreen.readableActions." + action.toString().replace('_', ' ');
            final Table t = new Table(UiSkin.getSkin());
            t.setBackground("table-no-hover");

            t.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    changeKey(readableAction, action);
                    Audio.playSound("/ui/ui_button_click.mp3", 0.1f);
                }

                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    t.setBackground("table-hover");
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    t.setBackground("table-no-hover");
                }
            });

            float rowHeight = 11;

            Label label = new Label(StringManager.get(readableAction) + " ", skin.get(Label.LabelStyle.class));
            label.setFontScale(0.8f);
            t.add(label).height(rowHeight).width(100);

            String labelText = null;
            if(keyBinding != null && keyBinding != -1) {
                labelText = ReadableKeys.keyNames.get(keyBinding);
            }

            if(labelText == null)
                labelText = "";

            final Label setKeyButton = new Label(labelText, skin.get(Label.LabelStyle.class));
            setKeyButton.setColor(new Color(Colors.PARALYZE));
            setKeyButton.setFontScale(0.8f);

            t.add(setKeyButton).height(rowHeight).width(100);
            t.pack();

            addGamepadButtonOrder(t, null);

            mainTable.add(t).fillX().height(rowHeight);
            mainTable.row();
        }

        TextButton backBtn = new TextButton(StringManager.get("screens.OptionsKeysScreen.backButton"), skin.get(TextButton.TextButtonStyle.class));
        backBtn.setWidth(200);
        backBtn.setHeight(50);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveAndClose();
            }
        });

        mainTable.add(backBtn);

        // Left align everything
        Array<Cell> cells = mainTable.getCells();
        for(Cell c : cells) {
            c.align(Align.left);
        }

        mainTable.getCell(backBtn).padTop(4);
        mainTable.getCell(header).align(Align.center).padBottom(4);

        content.clear();
        content.add(mainTable);
        mainTable.pack();
        content.pack();

        buttonOrder.add(backBtn);
    }

    public void tick(float delta) {
        // back
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if(!ignoreEscape) {
                if (changeKeyTable == null) {
                    saveAndClose();
                } else {
                    setupTable();
                }
            }
        }

        super.tick(delta);

        ignoreEscape = false;
    }

    public int getKeyBinding(Actions.Action action) {
        Integer f = Actions.keyBindings.get(action);
        return f != null ? f : -1;
    }

    public void saveAndClose() {
        Options.instance.key_use = getKeyBinding(Actions.Action.USE);
        Options.instance.key_attack = getKeyBinding(Actions.Action.ATTACK);
        Options.instance.key_forward = getKeyBinding(Actions.Action.FORWARD);
        Options.instance.key_backward = getKeyBinding(Actions.Action.BACKWARD);
        Options.instance.key_strafe_left = getKeyBinding(Actions.Action.STRAFE_LEFT);
        Options.instance.key_strafe_right = getKeyBinding(Actions.Action.STRAFE_RIGHT);
        Options.instance.key_map = getKeyBinding(Actions.Action.MAP);
        Options.instance.key_inventory = getKeyBinding(Actions.Action.INVENTORY);
        Options.instance.key_next_item = getKeyBinding(Actions.Action.ITEM_NEXT);
        Options.instance.key_previous_item = getKeyBinding(Actions.Action.ITEM_PREVIOUS);
        Options.instance.key_drop = getKeyBinding(Actions.Action.DROP);
        Options.instance.key_look_up = getKeyBinding(Actions.Action.LOOK_UP);
        Options.instance.key_look_down = getKeyBinding(Actions.Action.LOOK_DOWN);
        Options.instance.key_turn_left = getKeyBinding(Actions.Action.TURN_LEFT);
        Options.instance.key_turn_right = getKeyBinding(Actions.Action.TURN_RIGHT);
        Options.instance.key_jump = getKeyBinding(Actions.Action.JUMP);

        Options.saveOptions();
        OverlayManager.instance.replaceCurrent(new OptionsInputOverlay(dimScreen, showBackground));
    }

    public void changeKey(final String readableAction, final Actions.Action action) {
        content.clear();
        changeKeyTable = new Table();

        String localizedReadableAction = StringManager.get(readableAction);
        String localizedKeyPrompt = MessageFormat.format(StringManager.get("screens.OptionsKeysScreen.pressKeyLabel"), localizedReadableAction);
        changeKeyTable.add(new Label(localizedKeyPrompt, skin.get(Label.LabelStyle.class))).padBottom(4f);
        changeKeyTable.row();

        changeKeyTable.add(new Label(StringManager.get("screens.OptionsKeysScreen.pressEscToCancelLabel"),skin.get(Label.LabelStyle.class)));
        changeKeyTable.row();

        content.add(changeKeyTable);
        content.pack();

        ui.addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode) {

                if (keycode != Input.Keys.BACK && keycode != Input.Keys.ESCAPE) {
                    if(existingActions.containsKey(keycode)) {
                        Actions.Action existing = existingActions.get(keycode);
                        Actions.keyBindings.remove(existing);
                    }

                    Actions.keyBindings.put(action, keycode);
                }

                setupTable();

                ui.removeListener(this);

                ignoreEscape = true;

                return true;
            }
        });
    }
}
