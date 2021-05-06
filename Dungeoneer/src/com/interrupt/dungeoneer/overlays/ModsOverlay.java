package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class ModsOverlay extends WindowOverlay {

	public ModsOverlay() { }

	@Override
	public  void tick(float delta) {
		super.tick(delta);
	}

	@Override
	public void onShow() {
		super.onShow();

		final Overlay thisOverlay = this;
		InputListener listener = new InputListener() {
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
	}

	@Override
	public Table makeContent() {

		final Overlay thisOverlay = this;

		TextButton backBtn = new TextButton(" " + StringManager.get("overlays.ModsOverlay.backButton") + " ", skin.get(TextButtonStyle.class));
		backBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Game.modManager.saveModsEnabledList();
				OverlayManager.instance.remove(thisOverlay);
			}
		});

		Table contentTable = new Table();

	    Label headerText = new Label("Manage Mods",skin.get(LabelStyle.class));
	    headerText.setAlignment(Align.center);
	    contentTable.add(headerText).align(Align.center).width(200).padBottom(4).expand();
	    contentTable.row();

		Table innerTable = new Table();
		innerTable.align(Align.top);

		ScrollPane scrollPane = new ScrollPane(innerTable);

		if(Game.modManager != null) {
			Array<String> mods = Game.modManager.getAllMods();

			for(final String mod : mods) {

				if(mod.equals("."))
					continue;

				String modName = Game.modManager.getModName(mod);
				int maxModLength = 40;
				if(modName.length() > maxModLength)
					modName = modName.substring(0, maxModLength - 3) + "...";

				final CheckBox cb = new CheckBox(" " + modName, skin);
				cb.setChecked(Game.modManager.checkIfModIsEnabled(mod));

				cb.align(Align.left);

				cb.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						Game.modManager.setEnabled(mod, cb.isChecked());
						Game.modManager.refresh();
					}
				});

				innerTable.add(cb).align(Align.top).align(Align.left).fillX().expand();

				buttonOrder.add(cb);
				innerTable.row();
			}
		}

		contentTable.add(scrollPane).fillX().expand().maxHeight(135).row();

	    contentTable.add(backBtn).align(Align.left).expand().padTop(8);

	    ui.setScrollFocus(scrollPane);

	    buttonOrder.clear();
	    buttonOrder.add(backBtn);

		return contentTable;
	}
}
