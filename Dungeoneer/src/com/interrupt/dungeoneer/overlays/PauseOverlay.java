package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.managers.StringManager;

public class PauseOverlay extends WindowOverlay {

	public PauseOverlay() { }

	@Override
	public  void tick(float delta) {
		// Check this before calling super.tick() because it will consume the buttonEvents.
		if (Game.gamepadManager.controllerState.buttonEvents.contains(Actions.Action.PAUSE, true)) {
			OverlayManager.instance.remove(this);
			Game.gamepadManager.controllerState.clearEvents();
			Game.gamepadManager.controllerState.resetState();
		}
		super.tick(delta);
	}

	@Override
	public void onShow() {
		super.onShow();
		Audio.setMusicVolume(0.3f);

		final Overlay thisOverlay = this;
		InputListener listener = new com.badlogic.gdx.scenes.scene2d.InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Keys.ESCAPE || keycode == Keys.BACK) {
					OverlayManager.instance.remove(thisOverlay);
				}
				return false;
			}
		};
		ui.addListener(listener);
	}

	@Override
	public void onHide() {
		super.onHide();
		Audio.setMusicVolume(1f);
	}

	@Override
	public Table makeContent() {

		final Overlay thisOverlay = this;

		TextButton backBtn = new TextButton(" " + StringManager.get("overlays.PauseOverlay.backButton") + " ", skin.get(TextButtonStyle.class));
		backBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		TextButton optionsBtn = new TextButton(" " + StringManager.get("overlays.PauseOverlay.optionsButton") + " ", skin.get(TextButtonStyle.class));
		optionsBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.remove(thisOverlay);
				OverlayManager.instance.push(new OptionsOverlay());
			}
		});

		TextButton controlsBtn = new TextButton(" " + StringManager.get("overlays.PauseOverlay.quitButton") + " ", skin.get(TextButtonStyle.class));
		controlsBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				OverlayManager.instance.clear();
				GameApplication.ShowMainMenuScreen();
			}
		});

		Table contentTable = new Table();
	    Label pauseText = new Label(StringManager.get("overlays.PauseOverlay.pauseHeader"),skin.get(LabelStyle.class));
	    contentTable.add(pauseText).padBottom(8f);
	    contentTable.row();

	    contentTable.add(backBtn).padBottom(1f).fillX();
	    contentTable.row();
		contentTable.add(optionsBtn).padBottom(1f).fillX();
	    contentTable.row();
	    contentTable.add(controlsBtn).fillX();

	    buttonOrder.clear();
	    buttonOrder.add(backBtn);
	    buttonOrder.add(optionsBtn);
	    buttonOrder.add(controlsBtn);

		return contentTable;
	}
}
