package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.ModManager;
import com.interrupt.dungeoneer.input.Actions;
import com.interrupt.dungeoneer.screens.GameScreen;
import com.interrupt.dungeoneer.screens.LoadingScreen;
import com.interrupt.managers.StringManager;

public class LevelsOverlay extends WindowOverlay {

    public LevelsOverlay() { }
    
    int selectedLevel = -1;
    Table contentTable;

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

		TextButton backBtn = new TextButton(" " + StringManager.get("overlays.PauseOverlay.backButton") + " ", skin.get(TextButtonStyle.class));
		backBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Game.modManager.saveModsEnabledList();
				OverlayManager.instance.remove(thisOverlay);
			}
        });
        
        // TextButton playBtn = new TextButton(" " + StringManager.get("screens.MainMenuScreen.playButton") + " ", skin.get(TextButtonStyle.class));
        
        
        // playBtn.setColor(Colors.PLAY_BUTTON);

		Table contentTable = new Table();

	    Label headerText = new Label("Play Level",skin.get(LabelStyle.class));
	    headerText.setAlignment(Align.center);
	    contentTable.add(headerText).align(Align.center).width(200).padBottom(4).expand();
	    contentTable.row();

		Table innerTable = new Table();
		innerTable.align(Align.top);

		ScrollPane scrollPane = new ScrollPane(innerTable);

        Array<String> levels = new Array<String>();

        levels.add(".");

        FileHandle fh = Game.getInternal("levels");
        for(FileHandle h : fh.list()) {
            if (h.name().endsWith(".bin")) levels.add(h.name());
        }

        int i = 0;
        for(final String level : levels) {
            if(level.equals("."))
            	continue;

            String name = level.substring(0, level.indexOf("."));
            
            int maxNameLength = 40;
            if(name.length() > maxNameLength)
                name = name.substring(0, maxNameLength - 3) + "...";
                
            final String levelName = name;

            final TextButton lb = new TextButton(" " + levelName, skin);
            lb.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Level lvl = new Level();
                    lvl.theme = "TEST";
                    lvl.levelName = levelName;
                    lvl.fogStart = 3.0f; 
                    lvl.fogEnd = 30.0f;
                    lvl.generated = false;
                    lvl.levelFileName = "levels/"+level;

                    OverlayManager.instance.remove(thisOverlay);
					GameApplication.instance.createFromStartLevel(lvl);
                }
            });

            lb.align(Align.left);

            innerTable.add(lb).align(Align.top).align(Align.left).fillX().expand();

            buttonOrder.add(lb);
            innerTable.row();
            i++;
        }

		contentTable.add(scrollPane).fillX().expand().maxHeight(135).row();
	    
        contentTable.add(backBtn).align(Align.left).expand().padTop(8);
        // contentTable.add(playBtn).align(Align.right).expand().padTop(8);

	    ui.setScrollFocus(scrollPane);
	    
	    buttonOrder.clear();
	    buttonOrder.add(backBtn);
		
		return contentTable;
    }
    

}
