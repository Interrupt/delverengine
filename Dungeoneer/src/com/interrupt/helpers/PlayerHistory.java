package com.interrupt.helpers;

import com.badlogic.gdx.Gdx;
import com.interrupt.api.steam.SteamApi;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.game.Game;

public class PlayerHistory {
	
	public int monstersKilled = 0;
	public int foodEaten = 0;
	public int damageTaken = 0;
	public int potionsDrank = 0;
	public int scrollsUsed = 0;
	public int wandsUsed = 0;
	public int trapsActivated = 0;
	public int timesTeleported = 0;
	public int timesPoisoned = 0;
	public int thingsIdentified = 0;
	public int secretsFound = 0;
	
	public PlayerHistory() { }
	
	public void addMonsterKill(Monster m) {
		Gdx.app.log("PlayerHistory", "Killed a monster");
		monstersKilled++;

		if(monstersKilled > 150) {
			SteamApi.api.achieve("RUN_MONSTERS");
		}
	}
	
	public void ateFood(Item item) {
		Gdx.app.log("PlayerHistory", "Ate food");
		foodEaten++;

		if(foodEaten > 40) {
			SteamApi.api.achieve("RUN_FOOD");
		}
	}
	
	public void drankPotion(Item item) {
		Gdx.app.log("PlayerHistory", "Drank a potion");
		potionsDrank++;

		if(potionsDrank >= 15) {
			SteamApi.api.achieve("RUN_POTIONS");
		}
	}
	
	public void usedScroll(Item item) {
		Gdx.app.log("PlayerHistory", "Used a scroll");
		scrollsUsed++;

		if(scrollsUsed >= 10) {
			SteamApi.api.achieve("RUN_SCROLLS");
		}
	}
	
	public void tookDamage(int damage) {
		Gdx.app.log("PlayerHistory", "Took damage");
		damageTaken += damage;

		if(Game.instance != null && Game.instance.player != null && Game.instance.player.isAlive()) {
			if (damageTaken >= 400) {
				SteamApi.api.achieve("RUN_DAMAGE_TAKEN");
			}
		}
	}

	public void usedWand(Item item) {
		Gdx.app.log("PlayerHistory", "Used a wand");
		wandsUsed++;

		if(wandsUsed >= 300) {
			SteamApi.api.achieve("RUN_WANDS");
		}
	}
	
	public void activatedTrap(Entity trap) {
		Gdx.app.log("PlayerHistory", "Tripped a trap");
		trapsActivated++;

		if(trapsActivated >= 10) {
			SteamApi.api.achieve("RUN_TRAPS");
		}
	}
	
	public void teleported() {
		Gdx.app.log("PlayerHistory", "Was teleported");
		timesTeleported++;

		if(timesTeleported >= 10) {
			SteamApi.api.achieve("RUN_TELEPORTED");
		}
	}
	
	public void poisoned() {

		Gdx.app.log("PlayerHistory", "Was poisoned");
		timesPoisoned++;

		if(timesPoisoned >= 10) {
			SteamApi.api.achieve("RUN_POISONED");
		}
	}

	public void identified(Item item) {
		Gdx.app.log("PlayerHistory", "Identified an item");
		thingsIdentified++;

		if(thingsIdentified >= 5) {
			SteamApi.api.achieve("RUN_IDENTIFIED");
		}
	}

	public void foundSecret() {
		Gdx.app.log("PlayerHistory", "Found a secret");
		secretsFound++;

		if(secretsFound >= 5) {
			SteamApi.api.achieve("RUN_SECRETS");
		}
	}
}
