package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class LevelUpOverlay extends WindowOverlay {

	String selectedAttribute = "";
	final Player player;

	protected ArrayMap<String, Integer> startingValues = new ArrayMap<String, Integer>();
	protected ArrayMap<String, Label> valueLabels = new ArrayMap<String, Label>();

	protected final Color selectedValue = new Color(0.6f, 1f, 0.6f, 1f);
	protected final Color unselectedValue = new Color(0.6f, 0.6f, 0.6f, 1f);

	protected Label hoverLabel;

	public LevelUpOverlay(Player player) {
		this.player = player;
		background = null;
	}

	@Override
	public void onShow() {
		super.onShow();
		Audio.setMusicVolume(0.3f);
		Audio.playSound("music/levelup.mp3", 0.4f);
	}

	@Override
	public void onHide() {
		super.onHide();
		Audio.setMusicVolume(1f);
		Audio.playSound("/ui/ui_dialogue_close.mp3", 0.35f);
	}

	int attributeNum = 0;
	protected void addAttribute(Table table, final String text, final Integer currentValue) {

		Color accentColor = new Color(Colors.PARALYZE);

		String translatedText = StringManager.get("overlays.LevelUpOverlay." + text.toUpperCase());
		final Label attributeName = new Label(translatedText, skin.get("input", LabelStyle.class));
		attributeName.setAlignment(Align.center);
		attributeName.setWrap(true);
		attributeName.setColor(new Color(0.298039215686275f, 0.12156862745098f, 0.12156862745098f, 1f));

		String currentStatValueLabel = StringManager.get("overlays.LevelUpOverlay.currentStatValueLabel");
		currentStatValueLabel = MessageFormat.format(currentStatValueLabel, currentValue);
		final Label value = new Label(currentStatValueLabel, skin.get(LabelStyle.class));
		value.setWrap(true);
		value.setFontScale(0.75f);
		value.setAlignment(Align.center);
		value.setColor(new Color(0.298039215686275f, 0.12156862745098f, 0.12156862745098f, 1f));
		
		valueLabels.put(text, value);
		startingValues.put(text, currentValue);

		final Table card = new Table(skin);
		buttonOrder.add(card);

		Drawable cardImageBackground = new TextureRegionDrawable(new TextureRegion(Art.loadTexture("ui/levelup/" + text.toLowerCase() + ".png")));
		Table imageTable = new Table(skin);
		imageTable.add("").height(139).align(Align.bottom).align(Align.center);
		imageTable.setBackground(cardImageBackground);

		card.add(attributeName).fillX().align(Align.center);
		card.row();
		card.add(value).fillX().align(Align.center).padTop(2);
		card.row();
		card.add(imageTable).align(Align.bottom);
		card.row();
		card.pack();

		table.add(card).pad(0);

		TextureRegion cardBackgroundRegion = new TextureRegion(Art.loadTexture("ui/levelup/window.png"));
		NinePatchDrawable cardBackground = new NinePatchDrawable(new NinePatch(cardBackgroundRegion, 11, 11, 11, 11));
		card.setBackground(cardBackground);
		card.setTouchable(Touchable.enabled);

		final int xLocation = (attributeNum * 120) - 8;
		card.addAction(Actions.sequence(Actions.hide(), Actions.moveTo(0, 0, 0.01f), Actions.show(), Actions.delay(0.15f), Actions.moveTo(xLocation, 0, 0.1f + attributeNum * 0.1f, Interpolation.exp5)));

		card.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				pickStat(text.toUpperCase());
				Audio.playSound("ui/ui_statincrease.mp3", 0.35f);
			}

			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				card.addAction(Actions.moveTo(xLocation, 8, 0.1f, Interpolation.pow2));
				hoverLabel.setText(StringManager.get("screens.CharacterScreen.tooltips." + text.toLowerCase()));
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				card.addAction(Actions.moveTo(xLocation, 0, 0.1f, Interpolation.pow2));
				hoverLabel.setText("");
			}
		});

		attributeNum++;
	}

	public void pickStat(String chosenAttribute) {
		if(!chosenAttribute.equals("")) {
			if(chosenAttribute.equals("ATTACK")) {
				player.stats.ATK++;
			}
			else if(chosenAttribute.equals("SPEED")) {
				player.stats.SPD++;
			}
			else if(chosenAttribute.equals("HEALTH")) {
				player.stats.END++;
			}
			else if(chosenAttribute.equals("MAGIC")) {
				player.stats.MAG++;
			}
			else if(chosenAttribute.equals("AGILITY")) {
				player.stats.DEX++;
			}
			else if(chosenAttribute.equals("DEFENSE")) {
				player.stats.DEF++;
			}

			applyStats();

			OverlayManager.instance.remove(this);
		}
	}
	
	protected void applyStats() {
		Player p = Game.instance.player;
		p.maxHp = (int)(p.stats.END * (p.stats.END / 3f)) + 4;
		p.maxHp += (p.level - 1) * 0.5f;
		p.hp = p.getMaxHp();
	}

	@Override
	public Table makeContent() {
		
		buttonOrder.clear();
		valueLabels.clear();
		startingValues.clear();
	    
		Table contentTable = new Table();
	    Label title = new Label(StringManager.get("overlays.LevelUpOverlay.levelUpLabel"), skin.get(LabelStyle.class));
	    title.setFontScale(1.25f);
	    contentTable.add(title).padBottom(4f).padTop(20);
	    contentTable.row();
	    
	    Label text = new Label(StringManager.get("overlays.LevelUpOverlay.chooseYourFateLabel"), skin.get(LabelStyle.class));
	    contentTable.add(text).padBottom(12f);
	    contentTable.row();
	    
	    Array<Stat> allStats = getAllStats();
	    
	    // randomly pick three stats to pick from
	    allStats.shuffle();

	    Table cardTable = new Table(skin);
	    for(int i = 0; i < 3; i++) {
	    	addAttribute(cardTable, allStats.get(i).name, allStats.get(i).stat);
	    }

	    contentTable.add(cardTable);
		contentTable.row();

		hoverLabel = new Label("", skin.get(LabelStyle.class));
		hoverLabel.setWrap(true);
		hoverLabel.setAlignment(Align.center);
		contentTable.add(hoverLabel).width(250).align(Align.center).align(Align.bottom).height(50);
	    
	    return contentTable;
	}
	
	public Array<Stat> getAllStats() {
		Array<Stat> stats = new Array<Stat>();
		
		stats.add(new Stat("Attack", player.stats.ATK));
		stats.add(new Stat("Speed", player.stats.SPD));
		stats.add(new Stat("Health", player.stats.END));
		stats.add(new Stat("Magic", player.stats.MAG));
		stats.add(new Stat("Agility", player.stats.DEX));
		stats.add(new Stat("Defense", player.stats.DEF));
		
		return stats;
	}
	
	protected class Stat {
		String name;
		int stat;
		
		public Stat(String name, int stat) {
			this.name = name;
			this.stat = stat;
		}
	}

	@Override
	public void resize(int width, int height) {
		if(ui != null && ui.getViewport() != null) {
			Viewport viewport = ui.getViewport();
			viewport.setWorldHeight(height * 0.325f / Options.instance.uiSize * 1.25f);
			viewport.setWorldWidth(width * 0.325f / Options.instance.uiSize * 1.25f);
			viewport.update(width, height, true);
		}
	}
}
