package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class Gold extends Item {
	@EditorProperty
	public int goldAmount = 1;

	/** String translation keys. */
	private static final String TRANS_KEY_DEFAULT_NAME_TEXT = "items.Gold.defaultNameText";
	private static final String TRANS_KEY_GOLD_ITEM_TEXT = "items.Gold.goldItemText";

	public Gold() {
		this.tex = 88;
		this.artType = ArtType.item;
		this.name = StringManager.get(Gold.TRANS_KEY_DEFAULT_NAME_TEXT);
		this.collidesWith = CollidesWith.staticOnly;
		this.dropSound = "drops/drop_gold.mp3";
		this.collision.x = 0.1f;
		this.collision.y = 0.1f;
	}

	public Gold(float x, float y) {
		super(x, y, 0, ItemType.gold, StringManager.get(Gold.TRANS_KEY_DEFAULT_NAME_TEXT));
	}
	
	public Gold(int amount) {
		this();
		this.goldAmount = amount;
		this.name = StringManager.get(Gold.TRANS_KEY_DEFAULT_NAME_TEXT);

		if(this.goldAmount <= 0) this.goldAmount = 1;
		if(this.goldAmount > 5) tex = 89;
		
		this.pickupSound = "pu_gold.mp3";
	}

	@Override
	public String GetItemText() {
		return MessageFormat.format(StringManager.get(Gold.TRANS_KEY_GOLD_ITEM_TEXT), this.goldAmount);
	}

	@Override
	public boolean canPickup(Player player) {
		return true;
	}

	@Override
	protected void pickup(Player player) {
		if (!this.isActive) {
			return;
		}

		player.changeGoldAmount(this.goldAmount);
		Audio.playSound(this.pickupSound, 0.3f, 1f);
		this.makeItemPickupAnimation(player);

		this.isActive = false;
	}
}
