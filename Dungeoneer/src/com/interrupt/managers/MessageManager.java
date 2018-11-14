package com.interrupt.managers;

import com.interrupt.dungeoneer.game.Game;
import com.interrupt.helpers.Message;

public class MessageManager {
	public MessageManager() { }
	
	public static Message getMessage(String file) {
		Message loadedMessage = Game.fromJson(Message.class, Game.findInternalFileInMods("data/messages/" + file));
		return loadedMessage;
	}
}
