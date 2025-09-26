package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.overlays.MessageOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.ui.UiSkin;
import com.interrupt.helpers.Message;
import com.interrupt.managers.MessageManager;
import com.interrupt.managers.StringManager;

public class Note extends Item {
	public Note() { isSolid = true; yOffset = -0.1f; }
	
	@EditorProperty(group = "Note Message")
	public String messageFile = null;

	@EditorProperty(group = "Note Message")
	public String noteText = null;

	@EditorProperty(group = "Note Message")
	Color textColor = new Color(0.4f, 0.2f, 0.2f, 1f);
	
	@Override
	public String GetInfoText() {
		return "";
	}
	
	public Array<String> messages = null;

	@EditorProperty(group = "Note Message")
	public String backgroundImage = null;
	
	private Array<String> getReadMessages(Player player) {
		String seenMessages = player.seenMessages.get(messageFile);
		if(seenMessages == null) return new Array<String>();
		return new Array<String>(seenMessages.split(","));
	}
	
	private void updateReadMessages(Player player, Array<String> messagesToUpdate) {
		messagesToUpdate.shrink();
		player.seenMessages.put(messageFile, StringJoiner(messagesToUpdate, ","));
	}
	
	public void Read(Player player) {
		
		// grab a random message if we need to
		if(messageFile != null && !messageFile.equals("")) {
			Message m = MessageManager.getMessage(messageFile);
			if(m.pickRandom && messages == null && m.messages.size > 0) {
				// build a list of all the numbers to pick from
				Array<String> availableIndices = new Array<String>();
				for(int i = 0; i < m.messages.size; i++) { availableIndices.add(Integer.valueOf(i).toString()); }
				
				// grab the list of the ones seen before
				Array<String> seenMessages = getReadMessages(player);
				
				// remove the seen ones
				if(seenMessages != null) availableIndices.removeAll(seenMessages, true);
				
				// grab one of the remaining ones to display
				Integer saw = null;
				if(availableIndices.size > 0) {
					saw = Integer.parseInt(availableIndices.get(Game.rand.nextInt(availableIndices.size)));
				}
				else {
					// grab one, and reset
					player.seenMessages.remove(messageFile);
					saw = Game.rand.nextInt(m.messages.size);
				}
				
				// get one
				messages = m.messages.get(saw);
				
				// update the seen list
				if(seenMessages == null) seenMessages = new Array<String>();
				seenMessages.add(saw.toString());
				
				updateReadMessages(player, seenMessages);
			}
		}
		
		NinePatchDrawable background = new NinePatchDrawable(new NinePatch(UiSkin.getSkin().getRegion("note-window"), 16, 16, 16, 16));

		if(backgroundImage != null && !backgroundImage.isEmpty()) {
			Texture bg = Art.loadTexture(backgroundImage);
			if(bg != null) {
				background = new NinePatchDrawable(new NinePatch(new TextureRegion(bg), 16, 16, 16, 16));
			}
		}
		
		if(messages != null) {
			OverlayManager.instance.push(new MessageOverlay(messages, background, textColor));
		}
		else if(messageFile != null && !messageFile.equals("")) {
			OverlayManager.instance.push(new MessageOverlay(messageFile, Game.instance.player, background, textColor));
		}
		else if(noteText != null && !noteText.equals("")) {
			Array<String> message = new Array<String>();
			message.add(noteText);
			OverlayManager.instance.push(new MessageOverlay(message, background, textColor));
		}
		else {
			Array<String> message = new Array<String>();
			message.add(StringManager.get("items.Note.ellipsisText"));
			OverlayManager.instance.push(new MessageOverlay(message, background, textColor));
		}
	}
	
	public boolean inventoryUse(Player player){
		Read(player);
        return true;
	}
	
	private static String StringJoiner(Array<String> aArr, String sSep) {
	    StringBuilder sbStr = new StringBuilder();
	    for (int i = 0, il = aArr.size; i < il; i++) {
	        if (i > 0)
	            sbStr.append(sSep);
	        sbStr.append(aArr.get(i));
	    }
	    return sbStr.toString();
	}
}
