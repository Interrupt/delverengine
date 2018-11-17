package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.input.ControllerState.DPAD;
import com.interrupt.dungeoneer.ui.ActionSpinnerButton;
import com.interrupt.dungeoneer.ui.SpinnerButton;
import com.interrupt.dungeoneer.ui.UiSkin;

public abstract class WindowOverlay extends Overlay {
	
	public float timer = 0;
	public Table stageTable;
	public Float uiScale = null;

	Skin skin;
	Interpolation lerp = Interpolation.sine;

	public boolean showBackground = true;
	public boolean dimScreen = true;

	protected float lerpValue = 0;
	protected ControllerState controllerState = null;
	protected Integer gamepadSelectionIndex = null;
	protected Integer lastGamepadSelectionIndex = null;

	protected Array<Actor> buttonOrder = new Array<Actor>();
	protected ArrayMap<Actor, Label> buttonLabels = new ArrayMap<Actor, Label>();

	public boolean animateBackground = true;
	public boolean animate = true;
	protected int align = Align.center;
	
	protected NinePatchDrawable background = new NinePatchDrawable(new NinePatch(UiSkin.getSkin().getRegion("window"), 11, 11, 11, 11));

	Vector2 offset = new Vector2();

	private Color animateColor = new Color();

	float keyRepeat = 0f;

	public boolean playSoundOnOpen = true;
	
	public WindowOverlay() {
	}

	@Override
	public void tick(float delta) {

		if(running) {
			timer += delta;	
		}

		controllerState = Game.gamepadManager.controllerState;

		// Allow all overlays to be exited via the menu cancel button
		if (Game.gamepadManager.controllerState.menuButtonEvents.contains(ControllerState.MenuButtons.CANCEL, true)) {
			OverlayManager.instance.remove(this);
			Game.gamepadManager.controllerState.clearEvents();
			Game.gamepadManager.controllerState.resetState();
		}

		// Translate keyboard directions into gamepad directions
		if(keyRepeat > 0) keyRepeat -= delta * 400f;
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			if(keyRepeat <= 0) {
				Game.gamepadManager.controllerState.dpadEvents.add(DPAD.UP);
				keyRepeat = 50;
			}
		} else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			if(keyRepeat <= 0) {
				Game.gamepadManager.controllerState.dpadEvents.add(DPAD.DOWN);
				keyRepeat = 50;
			}
		} else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			if(keyRepeat <= 0) {
				Game.gamepadManager.controllerState.dpadEvents.add(DPAD.LEFT);
				keyRepeat = 50;
			}
		} else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			if(keyRepeat <= 0) {
				Game.gamepadManager.controllerState.dpadEvents.add(DPAD.RIGHT);
				keyRepeat = 50;
			}
		}
		else {
			keyRepeat = 0;
		}

		Actor selected = null;
		if(gamepadSelectionIndex != null && gamepadSelectionIndex < buttonOrder.size) selected = buttonOrder.get(gamepadSelectionIndex);

		// basic gamepad support
		if(Game.gamepadManager.controllerState.dpadEvents.size > 0 && buttonOrder.size > 0) {
			DPAD direction = Game.gamepadManager.controllerState.dpadEvents.get(0);

			// Slider support
			if((direction == DPAD.LEFT || direction == DPAD.RIGHT) && doLeftRightAction(selected, direction)) {
				// already did the action!
			}
			else if(direction == DPAD.DOWN || direction == DPAD.RIGHT) {
				if(gamepadSelectionIndex == null) gamepadSelectionIndex = 0;
				else gamepadSelectionIndex = (gamepadSelectionIndex + 1) % buttonOrder.size;
			}
			else if(direction == DPAD.UP || direction == DPAD.LEFT) {
				if(gamepadSelectionIndex == null) gamepadSelectionIndex = 0;
				gamepadSelectionIndex--;
				if(gamepadSelectionIndex < 0) gamepadSelectionIndex = buttonOrder.size - 1;
			}
		}
		
		if(gamepadSelectionIndex != null && buttonOrder.size > 0 && 
				(Game.gamepadManager != null && Game.gamepadManager.controllerState.buttonEvents.contains(Actions.Action.USE, true) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Game.gamepadManager.controllerState.menuButtonEvents.contains(ControllerState.MenuButtons.SELECT, true))) {
			Actor button = buttonOrder.get(gamepadSelectionIndex);
			for(EventListener listener : button.getListeners()) {
				if(listener instanceof ClickListener) {
					((ClickListener)listener).clicked(new InputEvent(), button.getX(), button.getY());
				}
			}
		}

		for(Actor actor : buttonOrder) {
			if(actor != selected) {
				if(actor instanceof CheckBox) {
					// special style?
				}
				else if(actor instanceof TextButton) {
					((TextButton) actor).setStyle(UiSkin.getSkin().get("default", TextButtonStyle.class));
				}

				if(buttonLabels.containsKey(actor)) {
					Label notSelected = buttonLabels.get(actor);
					notSelected.setColor(Color.WHITE);
				}
			}
			else {
				if(actor instanceof CheckBox) {
					// special style?
				}
				else if(actor instanceof TextButton) {
					((TextButton) actor).setStyle(UiSkin.getSkin().get("gamepad-selected", TextButtonStyle.class));
				}

				if(buttonLabels.containsKey(actor)) {
					Label notSelected = buttonLabels.get(actor);
					notSelected.setColor(Colors.PARALYZE);
				}
			}
		}
		
		// do hover events
		if(lastGamepadSelectionIndex != gamepadSelectionIndex) {
			try {
				if (lastGamepadSelectionIndex != null) {
					Actor actor = buttonOrder.get(lastGamepadSelectionIndex);
					for (EventListener listener : actor.getListeners()) {
						if (listener instanceof ClickListener) {
							((ClickListener) listener).exit(new InputEvent(), actor.getX(), actor.getY(), 0, null);
						}
					}
				}
				if (gamepadSelectionIndex != null) {
					Actor actor = buttonOrder.get(gamepadSelectionIndex);
					for (EventListener listener : actor.getListeners()) {
						if (listener instanceof ClickListener) {
							((ClickListener) listener).enter(new InputEvent(), actor.getX(), actor.getY(), 0, null);
						}
					}
				}
			}
			catch (Exception ex) { lastGamepadSelectionIndex = null; gamepadSelectionIndex = null; }
		}
		
		Game.gamepadManager.menuMode = true;
		Game.gamepadManager.tick(delta);
		
		lastGamepadSelectionIndex = gamepadSelectionIndex;
	}

	private boolean doLeftRightAction(Actor selected, DPAD direction) {
		if(selected instanceof Slider) {
			Slider s = (Slider) selected;
			float v = s.getValue();
			float stepSize = s.getStepSize();

			if (direction == DPAD.LEFT) {
				s.setValue(v - stepSize);
			} else {
				s.setValue(v + stepSize);
			}
			return true;
		}
		else if(selected instanceof SpinnerButton) {
			SpinnerButton b = (SpinnerButton) selected;
			b.setValueNext();
			return true;
		}
		else if(selected instanceof ActionSpinnerButton) {
			ActionSpinnerButton b = (ActionSpinnerButton) selected;
			b.setValueNext();
			return true;
		}
		else if(selected instanceof CheckBox) {
			return true;
		}
		return false;
	}

	@Override
	public void onShow() {
		skin = UiSkin.getSkin();

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        int scaleFactor = width < height * 1.5 ? width : height;
        float scaleMod = width < height * 1.5 ? 480f * 1.5f : 480f;
        uiScale = (scaleFactor / scaleMod) * 3f;

        uiScale *= Math.min(1f, Options.instance.uiSize);

		Gdx.app.log("Delver", Gdx.graphics.getWidth() + " x " + Gdx.graphics.getHeight());

		//FillViewport viewport = new FillViewport(Gdx.graphics.getWidth() / uiScale, Gdx.graphics.getHeight() / uiScale);
		ScalingViewport viewport = new ScalingViewport(Scaling.fill, 850f, 480f);
		ui = new Stage(viewport);

		resize(width, height);
		
		makeLayout();

		if(playSoundOnOpen) {
			Audio.playSound("/ui/ui_button_click.mp3", 0.1f);
		}

		if(catchInput)
			Gdx.input.setInputProcessor(ui);

		controllerState = Game.gamepadManager.controllerState;
	}
	
	protected void makeLayout() {
		if(stageTable == null) {
			stageTable = new Table();
			ui.addActor(stageTable);
		}
		else {
			stageTable.clear();
		}
		
		stageTable.setFillParent(true);
		stageTable.align(align);
		
		Table mainTable = new Table();
		if(background != null && showBackground) mainTable.setBackground(background);
		stageTable.add(mainTable).fill();
	    
		Table contentTable = makeContent();
	    mainTable.add(contentTable).pad(4f);
	}
	
	protected void makeLayout(Table contentTable) {
		if(stageTable == null) {
			stageTable = new Table();
			ui.addActor(stageTable);
		}
		else {
			stageTable.clear();
		}
		
		stageTable.setFillParent(true);
		stageTable.align(align);
		
		Table mainTable = new Table();
		if(background != null && showBackground) mainTable.setBackground(background);
		stageTable.add(mainTable).fill();
	    
	    mainTable.add(contentTable).pad(4f);
	    
	    ui.addActor(stageTable);
	}

	@Override
	protected void draw(float delta) {
		
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		
		int scaleFactor = width < height * 1.5 ? width : height;
		float scaleMod = width < height * 1.5 ? 480f * 1.5f : 480f;
		uiScale = (scaleFactor / scaleMod) * 3f;
		
		uiScale *= Math.min(1f, Options.instance.uiSize);
		
		if(!Game.isMobile) uiScale *= 0.75f;
		
		renderer = GameManager.renderer;
		gl = renderer.getGL();
		
		lerpValue = animate ? lerp.apply(0, 1f, Math.min(timer * 4, 1f)) : 1f;
		float moveLerpValue = animate ? lerp.apply(-10f, 0f, Math.min(timer * 4, 1f)) : 0f;

		if(catchInput && dimScreen) {
			renderer.uiBatch.begin();
			renderer.uiBatch.enableBlending();

			if(animateBackground)
				renderer.uiBatch.setColor(0, 0, 0, 0.5f * lerpValue);
			else
				renderer.uiBatch.setColor(0, 0, 0, 0.5f);

			renderer.uiBatch.draw(renderer.flashRegion, -renderer.camera2D.viewportWidth / 2, -renderer.camera2D.viewportHeight / 2, renderer.camera2D.viewportWidth, renderer.camera2D.viewportHeight);
			renderer.uiBatch.end();
			renderer.uiBatch.disableBlending();
		}

		stageTable.setPosition(0 + offset.x, moveLerpValue + offset.y);

		animateColor.set(1, 1, 1, 1f * lerpValue);
		stageTable.setColor(animateColor);
		
		super.draw(delta);
	}

	@Override
	public void onHide() {

	}

	public void addGamepadButtonOrder(Actor actor, Label label) {
		buttonOrder.add(actor);
		if(label != null) {
			buttonLabels.put(actor, label);
		}
	}

	// Override this to make table content
	public abstract Table makeContent();
}
