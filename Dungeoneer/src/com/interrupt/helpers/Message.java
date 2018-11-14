package com.interrupt.helpers;

import com.badlogic.gdx.utils.Array;

public class Message {
	public Message() { }
	public boolean repeats = true;
	public Array<Array<String>> messages = null;
	public String name = null;
	public boolean pickRandom = false;
}
