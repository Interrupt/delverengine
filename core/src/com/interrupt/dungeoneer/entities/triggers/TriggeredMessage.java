package com.interrupt.dungeoneer.entities.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Progression;
import com.interrupt.dungeoneer.overlays.MessageOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;

public class TriggeredMessage extends Trigger {
	
	@EditorProperty
	public String messageFile = "none.dat";

	@EditorProperty
	public String progressionKey = null;
	
	@EditorProperty
	public Float time = 5f;
	
	@EditorProperty
	public Float size = 1f;

	@EditorProperty
	public String triggerIdAfter = null;

	Array<String> messages = null;

	@EditorProperty
	public boolean pausesGame = true;

	@EditorProperty
	public Color textColor = null;

	@EditorProperty
	public String backgroundImage = null;

	private Integer messageProgression = null;

	public TriggeredMessage() { hidden = true; spriteAtlas = "editor"; tex = 16; }

	public void init(Level level, Level.Source source) {
		if(source == Level.Source.LEVEL_START) {
			if(messageFile.contains(",")) {
				String[] messages = messageFile.split(",");

				Integer lastShown = null;
				if(progressionKey != null) {
					lastShown = Game.instance.progression.messagesSeen.get(progressionKey);
				}

				if(lastShown == null) {
					lastShown = -1;
				}
				int toShow = lastShown + 1;

				toShow = Math.min(toShow, messages.length - 1);

				messageProgression = toShow;
				messageFile = messages[toShow];
			}
		}

		super.init(level, source);
	}
	
	@Override
	public void doTriggerEvent(String value) {

		// We saw this, do we need to update the progression?
		if(messageProgression != null && progressionKey != null) {
			Game.instance.progression.messagesSeen.put(progressionKey, messageProgression);
		}

		// change the message if a new one was given
		if(value != null && !value.equals("")) {
			messageFile = value;
		}

		if(messageFile != null && messageFile.equals("campfireguy5.dat")) {
			SteamApi.api.achieve("OLDGUY");
		}

		// Set a background, if one was given
		NinePatchDrawable background = null;
		if(backgroundImage != null && !backgroundImage.isEmpty()) {
			Texture bg = Art.loadTexture(backgroundImage);
			if(bg != null) {
				background = new NinePatchDrawable(new NinePatch(new TextureRegion(bg), 16, 16, 16, 16));
			}
		}

		if(messages != null) {
			MessageOverlay overlay = new MessageOverlay(messages,background, textColor);
			overlay.triggerOnClose = triggerIdAfter;
			overlay.pausesGame = pausesGame;
			OverlayManager.instance.push(overlay);
		}
		else {
			MessageOverlay overlay = new MessageOverlay(messageFile, Game.instance.player, background, textColor);
			overlay.triggerOnClose = triggerIdAfter;
			overlay.pausesGame = pausesGame;
			OverlayManager.instance.push(overlay);
		}
		
		super.doTriggerEvent(value);
	}
}
