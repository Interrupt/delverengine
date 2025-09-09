package com.interrupt.managers;

import com.interrupt.dungeoneer.game.Game;
import com.interrupt.helpers.Message;
import com.interrupt.utils.JsonUtil;

public class MessageManager {
	public MessageManager() { }
	
	public static Message getMessage(String file) {
		Message loadedMessage = JsonUtil.fromJson(Message.class, Game.findInternalFileInMods("data/messages/" + file));
		return loadedMessage;
	}
}
