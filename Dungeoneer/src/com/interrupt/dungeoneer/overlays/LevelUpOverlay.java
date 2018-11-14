package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ArrayMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.rpg.Stats;
import com.interrupt.managers.StringManager;

public class LevelUpOverlay extends WindowOverlay {
	
	String selectedAttribute = "";
	final Player player;
	protected TextButton doneBtn;
	protected ArrayMap<String, Integer> startingValues = new ArrayMap<String, Integer>();
	protected ArrayMap<String, Label> valueLabels = new ArrayMap<String, Label>();
	
	protected final Color selectedValue = new Color(0.6f, 1f, 0.6f, 1f);
	protected final Color unselectedValue = new Color(0.6f, 0.6f, 0.6f, 1f);
	
	public LevelUpOverlay(Player player) {
		this.player = player;
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
	
	protected void addAttribute(Table table, final String text, final Integer currentValue) {
		
		boolean selected = selectedAttribute.equals(text);

		String translatedText = StringManager.get("overlays.LevelUpOverlay." + text.toUpperCase());
		final Label attributeName = new Label(translatedText, skin.get("input", LabelStyle.class));
		final Label value = new Label(selected ? ((Integer)(currentValue + 1)).toString() : currentValue.toString(), skin.get(LabelStyle.class));
		
		valueLabels.put(text, value);
		startingValues.put(text, currentValue);
		
		if(!selected)
			value.setColor(unselectedValue);
		else
			value.setColor(selectedValue);
		
		attributeName.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				
				for(Entry<String, Label> entry : valueLabels.entries()) {
					entry.value.setColor(unselectedValue);
					entry.value.setText(startingValues.get(entry.key).toString());
				}
				
				selectedAttribute = text;
				doneBtn.setColor(1f, 1f, 1f, 1f);
				
				Integer startingValue = startingValues.get(selectedAttribute) + 1;
				
				value.setColor(selectedValue);
				value.setText(startingValue.toString());
				
				Audio.playSound("ui/ui_statincrease.mp3", 0.35f);
			}
			
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				attributeName.setStyle(skin.get("inputover", LabelStyle.class));
				
				if(!selectedAttribute.equals(text))
					value.setColor(0.7f, 0.7f, 0.7f, 1f);
			}
			
			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				attributeName.setStyle(skin.get("input", LabelStyle.class));
				
				if(!selectedAttribute.equals(text))
					value.setColor(0.6f, 0.6f, 0.6f, 1f);
			}
		});
		
		buttonOrder.add(attributeName);
		
    	table.add(attributeName).align(Align.left).padBottom(2);
		table.add(value).align(Align.right).padLeft(20).padBottom(2);
		
		table.row();
	}
	
	protected void applyStats() {
		Player p = Game.instance.player;
		
		p.maxHp = (int)(p.stats.END * (p.stats.END / 3f)) + 4;
		p.hp = p.getMaxHp();
	}

	@Override
	public Table makeContent() {
		
		buttonOrder.clear();
		valueLabels.clear();
		startingValues.clear();
		
		final Overlay thisOverlay = this;
		
		doneBtn = new TextButton("Done", skin.get(TextButtonStyle.class));
		doneBtn.setWidth(200);
		doneBtn.setHeight(50);
		
		if(selectedAttribute.equals("")) {
			doneBtn.setColor(1f, 1f, 1f, 0.5f);
		}
		
		doneBtn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String chosenAttribute = selectedAttribute.toUpperCase();
				
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
						
					OverlayManager.instance.remove(thisOverlay);
				}
			}
		});
	    
		Table contentTable = new Table();
	    Label title = new Label(StringManager.get("overlays.LevelUpOverlay.levelUpLabel"), skin.get(LabelStyle.class));
	    contentTable.add(title).colspan(2).padBottom(4f);
	    contentTable.row();
	    
	    Label text = new Label(StringManager.get("overlays.LevelUpOverlay.chooseYourFateLabel"), skin.get(LabelStyle.class));
	    text.setFontScale(0.6f);
	    contentTable.add(text).colspan(2).padBottom(8f);
	    contentTable.row();
	    
	    Array<Stat> allStats = getAllStats();
	    
	    // randomly pick three stats to pick from
	    allStats.shuffle();
	    for(int i = 0; i < 3; i++) {
	    	addAttribute(contentTable, allStats.get(i).name, allStats.get(i).stat);
	    }
	    
	    contentTable.add(doneBtn).padTop(6).align(Align.center).colspan(2);
	    
	    buttonOrder.add(doneBtn);
	    
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
}
