package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.statuseffects.DrunkEffect;
import com.interrupt.dungeoneer.statuseffects.PoisonEffect;
import com.interrupt.dungeoneer.statuseffects.RestoreHealthEffect;
import com.interrupt.managers.StringManager;

public class Food extends Item {
	public enum FoodType { food, booze }

	/** Food type. */
	@EditorProperty
	public FoodType foodType = FoodType.food;

	/** Food modifier. Scales length of healing. */
	@EditorProperty
	public float foodMod = 1.0f;

	/** Sound played when Food is consumed. */
	@EditorProperty( group = "Audio", type = "FILE_PICKER", params = "audio", include_base = false )
	private String consumeSound = null;

	/** Description of Food item. */
	@EditorProperty
	public String infoText = "items.Food.infoText";

	public Food() { }

	public Food(float x, float y) {
		super(x, y, 0, ItemType.potion, StringManager.get("items.Food.defaultNameText"));
	}

	public void Eat(Player player) {
		player.history.ateFood(this);

		if(consumeSound == null || consumeSound.equals("")) {
			if (foodType == FoodType.food) {
				Audio.playSound("cons_food.mp3", 0.5f);
			} else {
				Audio.playSound("cons_drink.mp3", 0.5f);
			}
		}
		else {
			Audio.playSound(consumeSound, 0.5f);
		}

		if(foodMod != 0) {
			if (foodType == FoodType.food) {
				Game.ShowMessage(StringManager.get("items.Food.eatFoodText"), 1, 1f);
				player.hp += 1;
				if (player.hp > player.getMaxHp()) player.hp = player.getMaxHp();

				if(foodMod > 0)
					player.addStatusEffect(new RestoreHealthEffect((int) (1800 * Math.abs(foodMod)), 160, 1));
				else
					player.addStatusEffect(new PoisonEffect((int) (800 * Math.abs(foodMod)), 160, 1, false));

			} else if (foodType == FoodType.booze) {
				Game.ShowMessage(StringManager.get("items.Food.drinkBoozeText"), 1, 1f);
				player.hp += 1;
				if (player.hp > player.getMaxHp()) player.hp = player.getMaxHp();

				if(foodMod > 0)
					player.addStatusEffect(new RestoreHealthEffect((int) (3600 * Math.abs(foodMod)), 160, 1));
				else
					player.addStatusEffect(new PoisonEffect((int) (800 * Math.abs(foodMod)), 160, 1, false));

				player.addStatusEffect(new DrunkEffect((int) (3600 * foodMod * 0.75f)));
			}
		}

		int location = player.inventory.indexOf(this, true);
		player.inventory.set(location, null);
		Game.RefreshUI();
	}

	public boolean inventoryUse(Player player){
		Eat(player);
        return true;
	}

	@Override
	public void doPickup(Player player) {
		super.doPickup(player);
		Eat(player);
	}

	public String GetInfoText() {
		return StringManager.getOrDefaultTo(infoText, infoText);
	}
}
