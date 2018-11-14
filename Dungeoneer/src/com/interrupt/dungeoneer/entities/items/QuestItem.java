package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.utils.Array;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.AmbientSound;
import com.interrupt.dungeoneer.entities.DynamicLight;
import com.interrupt.dungeoneer.entities.DynamicLight.LightType;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class QuestItem extends Item {
	
	public QuestItem() {
		super(0, 0, 59, ItemType.quest, StringManager.get("items.QuestItem.defaultNameText"));
	}

	public QuestItem(float x, float y) {
		super(x, y, 59, ItemType.quest, StringManager.get("items.QuestItem.defaultNameText"));
	}
	
	@EditorProperty
	private String ambientSoundFile = "ambient/orb_glow_loop.mp3";

	@EditorProperty
	private boolean showPickupMessage = true;

	@EditorProperty
	private boolean giveAchievement = false;
	
	private AmbientSound ambientSound = null;
	
	float hoverTimer = 0;
	
	@Override
	public void tick(Level level, float delta) {
		super.tick(level, delta);
		
		if(ambientSoundFile != null) {
			try {
				if(ambientSound == null) {
					ambientSound = new AmbientSound(x,y,z,ambientSoundFile,1f,1f,6f);
				}
				else {
					ambientSound.x = x;
					ambientSound.y = y;
					ambientSound.z = z;
					
					ambientSound.tick(level, delta);
				}
			}
			catch(Exception ex) { }
		}
		
		if(isActive) {
			// animate some
			hoverTimer += delta;
			yOffset = (float)Math.sin(hoverTimer * 0.08f) * 0.01f;
			
			// make a light, if not one already
			if(attached == null) {
				attached = new Array<Entity>();
				
				DynamicLight l = new DynamicLight();
				l.lightColor.set(0.6f, 0, 0);
				l.lightType = LightType.sin_slight;
				l.haloMode = HaloMode.CORONA_ONLY;
				attached.add(l);
			}
		}
	}
	
	protected void pickup(Player player)
	{	
		if(!isOnFloor) return;
		
		if(Game.instance.player.addToInventory(this))
		{
			isActive = false;
			Audio.playSound(pickupSound, 0.3f, 1f);

			if(ambientSound != null) ambientSound.onDispose();

			if(triggersOnPickup != null && !triggersOnPickup.isEmpty()) {
				Game.instance.level.trigger(this, triggersOnPickup, name);
			}
		}
		else {
			Game.ShowMessage(StringManager.get("items.QuestItem.noRoomText"), 1);
		}
	}
	
	public void doQuestThing() {

		if(Game.instance.player.isHoldingOrb) return;

		Game.ShowMessage(MessageFormat.format(StringManager.get("items.QuestItem.gotItemText"), GetName()), 8, 1f);
		
		Game.message.clear();

		if(showPickupMessage) {
			String escapeMessage = StringManager.get("items.QuestItem.escapeMessageText");
			for (String s : escapeMessage.split("\n")) {
				Game.message.add(s);
			}
		}

		// reset some variables that aren't needed when in the inventory (save file space)
		x = 0;
		y = 0;
		isOnFloor = false;
		wasOnFloorLast = false;
		resetTickCount();
		
		// make life difficult
		Game.instance.player.isHoldingOrb = true;
		
		// quit ambient sound
		if(ambientSound != null) ambientSound.onDispose();
		
		// shit just got real son
		Audio.playMusic(Game.instance.level.actionMusic, Game.instance.level.loopMusic);

		if(giveAchievement)
			SteamApi.api.achieve("ORB");
	}
	
	public void onDispose() {
		super.onDispose();
		if(ambientSound != null) ambientSound.onDispose();
	}

	@Override
	public void onPickup() {
		doQuestThing();
	}
}
