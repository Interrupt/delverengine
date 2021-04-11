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
			isPickup = true;
		}

		tex = 88;
		artType = ArtType.item;
		name = StringManager.get("items.Gold.defaultNameText");
		collidesWith = CollidesWith.staticOnly;
		dropSound = "drops/drop_gold.mp3";
		pickupSound = "pu_gold.mp3";
		collision.x = 0.1f;
		collision.y = 0.1f;
	}

	public Gold(int amount) {
		this();
		goldAmount = amount;

		if (goldAmount <= 0) {
			goldAmount = 1;
		}

		if (goldAmount > 5) {
			tex = 89;
		}
	}

	@Override
	public String GetItemText() {
		return MessageFormat.format(StringManager.get("items.Gold.goldItemText"), goldAmount);
	}

	@Override
	public boolean canPickup(Player player) {
		return true;
	}

	@Override
	protected void pickup(Player player) {
		if (!isActive) {
			return;
		}

		player.changeGoldAmount(goldAmount);
		Audio.playSound(pickupSound, 0.3f, 1f);
		makeItemPickupAnimation(player);

		isActive = false;
	}
}
