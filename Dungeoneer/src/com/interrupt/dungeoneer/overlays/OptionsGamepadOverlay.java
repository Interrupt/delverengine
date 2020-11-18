package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.GameData;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.GamepadBinding;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.managers.StringManager;
import com.interrupt.utils.JsonUtil;

import java.text.MessageFormat;

public class OptionsGamepadOverlay extends WindowOverlay {

    private Table content = new Table();
    private Table mainTable;
    private Table changeKeyTable;

    Actions.Action currentlyEditing = null;
    ControllerListener controllerListener = null;

    public OptionsGamepadOverlay() {
        animateBackground = false;
    }

    boolean ignoreChange = false;

    public OptionsGamepadOverlay(boolean dimScreen, boolean showBackground)
    {
        animateBackground = false;
        this.dimScreen = dimScreen;
        this.showBackground = showBackground;
    }

    @Override
    public Table makeContent() {
        Options.loadOptions();
        Options options = Options.instance;

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

        controllerListener = new ControllerListener() {
            @Override
            public void connected(Controller controller) {

            }

            @Override
            public void disconnected(Controller controller) {

            }

            @Override
            public boolean buttonDown (Controller controller, int buttonIndex) {
                Actions.Action action = getCurrentlyEditing();
                if(action != null) {
                    Actions.gamepadBindings.put(action, new GamepadBinding(buttonIndex, GamepadBinding.GamepadInputType.BUTTON));
                    setupTable();
                }
                return true;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonCode) {
                return false;
            }

            @Override
            public boolean axisMoved(Controller controller, int axisCode, float value) {
                if(Math.abs(value) >= 0.5f) {
                    Actions.Action action = getCurrentlyEditing();
                    if(action != null) {
                        Actions.gamepadBindings.put(action, new GamepadBinding(axisCode, GamepadBinding.GamepadInputType.AXIS, value > 0 ? 1 : -1));
                        setupTable();
                    }
                }
                return false;
            }

            @Override
            public boolean povMoved(Controller controller, int povCode, PovDirection value) {
                Actions.Action action = getCurrentlyEditing();
                if(action != null) {
                    Actions.gamepadBindings.put(action, new GamepadBinding(povCode, GamepadBinding.GamepadInputType.POV, value.ordinal()));
                    setupTable();
                }
                return false;
            }

            @Override
            public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
                Actions.Action action = getCurrentlyEditing();
                if(action != null) {
                    Actions.gamepadBindings.put(action, new GamepadBinding(sliderCode, GamepadBinding.GamepadInputType.SLIDER, value ? 1 : 0));
                    setupTable();
                }
                return false;
            }

            @Override
            public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
                Actions.Action action = getCurrentlyEditing();
                if(action != null) {
                    Actions.gamepadBindings.put(action, new GamepadBinding(sliderCode, GamepadBinding.GamepadInputType.SLIDER, value ? 1 : 0));
                    setupTable();
                }
                return false;
            }

            @Override
            public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
                return false;
            }
        };

        content = new Table();
        setupTable();
        return content;
    }

    public void tick(float delta) {
        // back
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if(changeKeyTable == null) {
                saveAndClose();
            }
            else {
                setupTable();
            }
        }

        super.tick(delta);

        ignoreChange = false;
    }

    public Table setupTable() {
        currentlyEditing = null;
        changeKeyTable = null;

        buttonOrder.clear();
        buttonLabels.clear();
        lastGamepadSelectionIndex = null;

        mainTable = new Table();
        mainTable.setFillParent(true);

        Label header = new Label(StringManager.get("screens.OptionsGamepadScreen.title"),skin.get(Label.LabelStyle.class));
        header.setFontScale(1.1f);
        mainTable.add(header);
        mainTable.row();

        for(final Actions.Action action : Actions.keyOrder) {
            GamepadBinding gamepadBinding = Actions.gamepadBindings.get(action);

            final String readableAction = "screens.OptionsScreen.readableActions." + action.toString().replace('_', ' ');

            String valueLabelText = "NONE";
            if(gamepadBinding != null) valueLabelText = gamepadBinding.toString();

            final Table t = new Table(UiSkin.getSkin());
            t.setBackground("table-no-hover");

            t.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    changeButton(readableAction, action);
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

            final Label setButtonLabel = new Label(valueLabelText, skin.get(Label.LabelStyle.class));
            setButtonLabel.setColor(new Color(Colors.PARALYZE));
            setButtonLabel.setFontScale(0.8f);

            t.add(setButtonLabel).height(rowHeight).width(100);
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
        content.pack();
        mainTable.pack();

        buttonOrder.add(backBtn);

        currentlyEditing = null;

        Game.gamepadManager.controllerState.clearEvents();

        return mainTable;
    }

    public void saveAndClose() {
        Options.instance.gamepad_use = Actions.gamepadBindings.get(Actions.Action.USE);
        Options.instance.gamepad_attack = Actions.gamepadBindings.get(Actions.Action.ATTACK);
        Options.instance.gamepad_forward = Actions.gamepadBindings.get(Actions.Action.FORWARD);
        Options.instance.gamepad_backward = Actions.gamepadBindings.get(Actions.Action.BACKWARD);
        Options.instance.gamepad_strafe_left = Actions.gamepadBindings.get(Actions.Action.STRAFE_LEFT);
        Options.instance.gamepad_strafe_right = Actions.gamepadBindings.get(Actions.Action.STRAFE_RIGHT);
        Options.instance.gamepad_map = Actions.gamepadBindings.get(Actions.Action.MAP);
        Options.instance.gamepad_pause = Actions.gamepadBindings.get(Actions.Action.PAUSE);
        Options.instance.gamepad_inventory = Actions.gamepadBindings.get(Actions.Action.INVENTORY);
        Options.instance.gamepad_next_item = Actions.gamepadBindings.get(Actions.Action.ITEM_NEXT);
        Options.instance.gamepad_previous_item = Actions.gamepadBindings.get(Actions.Action.ITEM_PREVIOUS);
        Options.instance.gamepad_drop = Actions.gamepadBindings.get(Actions.Action.DROP);
        Options.instance.gamepad_look_up = Actions.gamepadBindings.get(Actions.Action.LOOK_UP);
        Options.instance.gamepad_look_down = Actions.gamepadBindings.get(Actions.Action.LOOK_DOWN);
        Options.instance.gamepad_turn_left = Actions.gamepadBindings.get(Actions.Action.TURN_LEFT);
        Options.instance.gamepad_turn_right = Actions.gamepadBindings.get(Actions.Action.TURN_RIGHT);

        Options.saveOptions();

        Controllers.removeListener(controllerListener);
        OverlayManager.instance.replaceCurrent(new OptionsInputOverlay(dimScreen, showBackground));
    }

    public void changeButton(final String readableAction, final Actions.Action action) {

        if(currentlyEditing != null) {
            return;
        }

        ignoreChange = true;

        currentlyEditing = action;

        mainTable.clear();
        changeKeyTable = mainTable;

        String localizedReadableAction = StringManager.get(readableAction);
        String localizedKeyPrompt = MessageFormat.format(StringManager.get("screens.OptionsGamepadScreen.entry"), localizedReadableAction);
        changeKeyTable.add(new Label(localizedKeyPrompt, skin.get(Label.LabelStyle.class))).padBottom(4f);
        changeKeyTable.row();

        changeKeyTable.add(new Label(StringManager.get("screens.OptionsKeysScreen.pressEscToCancelLabel"),skin.get(Label.LabelStyle.class)));
        changeKeyTable.row();

        Controllers.addListener(controllerListener);
    }

    public Actions.Action getCurrentlyEditing() {
        if(ignoreChange) return null;
        return currentlyEditing;
    }
}
