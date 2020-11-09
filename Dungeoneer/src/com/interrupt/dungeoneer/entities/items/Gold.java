package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class Gold extends Item {
	@EditorProperty
	public int goldAmount = 1;
	
	/** @deprecated v1.4.0: Use `Item::isPickup` instead. */
	@Deprecated
	public boolean autoPickup = false;
	
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
	public void tick(Level level, float delta) {
		super.tick(level, delta);
		
		if(this.isActive && (this.autoPickup || this.isPickup)) {
			Player player = Game.instance.player;

			if(this.isPlayerInReach(player)) {
				this.consumeGold(player);
			}
		}
	}
	
	@Override
	protected void pickup(Player player) {
		if(this.isActive) {
			this.consumeGold(player);
			this.makeItemPickupAnimation(player);
		}
	}

	/** Takes care of gold consumption by the player. */
	private void consumeGold(Player consumer) {
		consumer.changeGoldAmount(this.goldAmount);

		Audio.playSound(this.pickupSound, 0.3f, 1f);
		this.isActive = false;
	}

	/** Returns whether the player is in reach to pickup the gold. */
	private boolean isPlayerInReach(Player player) {
		return Math.abs(player.x + 0.5f - this.x) <= this.pickupDistance && Math.abs(player.y + 0.5f - this.y) <= this.pickupDistance;
	}
}
