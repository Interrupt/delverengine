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

	/**
	 * @deprecated <code>autoPickup</code> has been deprecated in favor of
	 *             <code>Item::isPickup</code> and will get removed in the next
	 *             major release.
	 */
	@Deprecated
	public boolean autoPickup = false;

	/**
	 * @deprecated <code>playedDropSound</code> has been deprecated and will get
	 *             removed in the next major release.
	 */
	@Deprecated
	public boolean playedDropSound = false;

	public Gold() {
		// Make sure legacy code is supported.
		if (autoPickup) {
			this.isPickup = true;
		}

		this.tex = 88;
		this.artType = ArtType.item;
		this.name = StringManager.get("items.Gold.defaultNameText");
		this.collidesWith = CollidesWith.staticOnly;
		this.dropSound = "drops/drop_gold.mp3";
		this.pickupSound = "pu_gold.mp3";
		this.collision.x = 0.1f;
		this.collision.y = 0.1f;
	}

	public Gold(int amount) {
		this();
		this.goldAmount = amount;

		if (this.goldAmount <= 0) {
			this.goldAmount = 1;
		}

		if (this.goldAmount > 5) {
			tex = 89;
		}
	}

	@Override
	public String GetItemText() {
		return MessageFormat.format(StringManager.get("items.Gold.goldItemText"), this.goldAmount);
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
