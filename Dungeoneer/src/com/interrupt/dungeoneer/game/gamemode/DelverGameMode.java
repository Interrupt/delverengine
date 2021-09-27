package com.interrupt.dungeoneer.game.gamemode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.entities.projectiles.BeamProjectile;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.overlays.DebugOverlay;
import com.interrupt.dungeoneer.overlays.LevelUpOverlay;
import com.interrupt.dungeoneer.overlays.MapOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.screens.GameOverScreen;
import com.interrupt.dungeoneer.screens.WinScreen;
import com.interrupt.dungeoneer.tiles.Tile;

import static com.interrupt.dungeoneer.game.Game.rand;

/**
 * Game Mode that wraps all Delver-specific gameplay actions. Includes showing the win /
 * game over screens, deleting save games, leveling up, showing the various menu screens, etc.
 */
public class DelverGameMode implements GameModeInterface {

    @Override
    public void onGameStart(Game game) {
        Progression progression = game.progression;

        if (!progression.sawTutorial && game.gameData.tutorialLevel != null) {
            Player tutorialPlayer = new Player(game);
            tutorialPlayer.level = 2;
            tutorialPlayer.gold = progression.gold;
            tutorialPlayer.maxHp = 12;
            tutorialPlayer.hp = tutorialPlayer.maxHp;
            tutorialPlayer.randomSeed = rand.nextInt();

            game.player = tutorialPlayer;

            // Make sure the tutorial is never seen again
            game.progression.sawTutorial = true;

            // Show the tutorial level instead of the default level 0
            game.setLevel(game.gameData.tutorialLevel, -1);
        }
    }

    @Override
    public void onWin(Game game) {
        // hooray!
        Gdx.app.log("DelverGameMode", "You win!");
        deleteRunSave(game);

        // Show the win screen
        WinScreen winScreen = new WinScreen(GameManager.instance);
        GameApplication.instance.setScreen(winScreen);
    }

    @Override
    public void onGameOver(Game game) {
        // player died! Delete save
        Gdx.app.log("DelverGameMode", "Game over!");
        deleteRunSave(game);

        // Show the game over screen
        GameOverScreen gameOverScreen = new GameOverScreen(GameManager.instance);
        GameApplication.instance.setScreen(gameOverScreen);
    }

    // Delete the saved levels for the current run.
    private void deleteRunSave(Game game) {
        // Get the current save slot directory location
        String saveDir = game.getSaveDir();

        FileHandle dir = Game.getFile(saveDir);
        if(dir.exists()) {
            Gdx.app.log("DelverGameMode", "Sorry! Deleting current saves");
            dir.deleteDirectory();
        }
    }

    @Override
    public void onMonsterDeath(Actor died) {
        if(died == null)
            return;

        Player player = Game.instance.player;
        if(player == null)
            return;

        if(died instanceof Monster) {
            Monster deadMonster = (Monster)died;
            if (deadMonster.givesExp) {
                Game.instance.player.addExperience(3 + died.level);
            }
        }
    }

    @Override
    public void onPlayerTookDamage(Player player, int damage, Weapon.DamageType damageType, Entity instigator)
    {
        if(damage < 0)
            Game.flash(Colors.HEAL_FLASH, 20);
        else
            Game.flash(Colors.HURT_FLASH, 20);
    }

    @Override
    public void onPlayerDeath(Player player) {
        Gdx.app.log("DelverGameMode", "Player died!");
    }

    @Override
    public void tickGame(Level level, float delta, GameInput input) {
        if(input == null)
            return;

        Player player = Game.instance.player;
        if(player == null)
            return;

        // Check input to see if there is anything that needs to happen. There is probably
        // a cleaner way to handle all of this per-game mode input stuff, but this is
        // better than what it was before.

        // ding?
        if(!player.isDead && player.exp >= player.getNextLevel() && player.canLevelUp)
        {
            // ding!
            player.level++;
            player.hp = player.getMaxHp();

            OverlayManager.instance.push(new LevelUpOverlay(player));
        }

        // inventory actions
        if(input.keyEvents.contains(Input.Keys.NUM_1)) doHotbarAction(player, 1);
        if(input.keyEvents.contains(Input.Keys.NUM_2)) doHotbarAction(player, 2);
        if(input.keyEvents.contains(Input.Keys.NUM_3)) doHotbarAction(player, 3);
        if(input.keyEvents.contains(Input.Keys.NUM_4)) doHotbarAction(player, 4);
        if(input.keyEvents.contains(Input.Keys.NUM_5)) doHotbarAction(player, 5);
        if(input.keyEvents.contains(Input.Keys.NUM_6)) doHotbarAction(player, 6);
        if(input.keyEvents.contains(Input.Keys.NUM_7)) doHotbarAction(player, 7);
        if(input.keyEvents.contains(Input.Keys.NUM_8)) doHotbarAction(player, 8);
        if(input.keyEvents.contains(Input.Keys.NUM_9)) doHotbarAction(player, 9);
        if(input.keyEvents.contains(Input.Keys.NUM_0)) doHotbarAction(player, 10);

        // Show various screens, when needed
        if(input.doInventoryAction()) {
            Game.instance.toggleInventory();
        }
        else if (Game.instance.menuMode != Game.MenuMode.Hidden && Game.gamepadManager.controllerState.menuButtonEvents.contains(ControllerState.MenuButtons.CANCEL, true)) {
            if (Game.instance.menuMode == Game.MenuMode.Inventory) {
                Game.instance.toggleInventory();
            }
            else {
                Game.instance.toggleCharacterScreen();
            }

            Game.gamepadManager.controllerState.clearEvents();
            Game.gamepadManager.controllerState.resetState();
        }

        if (input.keyEvents.contains(Input.Keys.C)) {
            Game.instance.toggleCharacterScreen();
        }

        if (input.doNextItemAction()) {
            player.wieldNextHotbarItem();
        }

        if (input.doPreviousItemAction()) {
            player.wieldPreviousHotbarItem();
        }

        if (input.doMapAction()) {

            // toggle map!
            if(OverlayManager.instance.current() == null)
                OverlayManager.instance.push(new MapOverlay());
            else
                OverlayManager.instance.clear();

            if (GameManager.renderer.showMap && Game.instance.getShowingMenu()) {
                Game.instance.toggleInventory();
            }

            if (GameManager.renderer.showMap) Audio.playSound("/ui/ui_map_open.mp3", 0.3f);
            else Audio.playSound("/ui/ui_map_close.mp3", 0.3f);
        }

        if (input.doBackAction()) {
            if (Game.instance.getShowingMenu())
                Game.instance.toggleInventory();
            else if (Game.instance.getInteractMode())
                Game.instance.toggleInteractMode();
        }

        // Debug stuff!
        if(Game.isDebugMode) {
            try {
                if (input.keyEvents.contains(Input.Keys.K))
                    OverlayManager.instance.push(new DebugOverlay(player));
                else if (input.keyEvents.contains(Input.Keys.L))
                    Game.instance.level.down.changeLevel(level);
                else if (input.keyEvents.contains(Input.Keys.J))
                    Game.instance.level.up.changeLevel(level);
            }
            catch(Exception ex) {
                Gdx.app.error("DelverDebug", ex.getMessage());
            }
        }

        // Escape sequence stuff
        if(player.isHoldingOrb && player.makeEscapeEffects) {
            tickEscapeEffects(level,player, delta);
        }
    }

    public void doHotbarAction(Player player, final int hotbarSlot) {
        if(player == null)
            return;

        int location = hotbarSlot - 1;
        if(location < 0 || location >= player.inventory.size || location + 1 > Game.hudManager.quickSlots.columns) return;
        player.UseInventoryItem(location);
    }

    private transient float t_timeSinceEscapeEffect = 0;
    private transient Color escapeFlashColor = new Color();
    private void tickEscapeEffects(Level level, Player player, float delta) {
        t_timeSinceEscapeEffect += delta;

        // Will spawn some stuff around the player, hold onto the player location
        float x = player.x;
        float y = player.y;

        if(t_timeSinceEscapeEffect > 300) {
            t_timeSinceEscapeEffect = 0 - rand.nextInt(150);

            float mod = rand.nextFloat();

            boolean bigShake = rand.nextBoolean();

            // flash
            if(bigShake) {
                mod += 0.25f;
                escapeFlashColor.set(Color.PURPLE);
                escapeFlashColor.a = 0.3f + mod * 0.5f;
                if(escapeFlashColor.a > 0.75f)
                    escapeFlashColor.a = 0.75f;
                Game.flash(escapeFlashColor, 20 + (int) (50 * mod));
            }
            else {
                mod *= 0.5f;
                t_timeSinceEscapeEffect *= 0.5f;
            }

            // play a sound
            Audio.playSound("break/earthquake1.mp3,break/earthquake2.mp3", mod * 0.2f + 0.1f);

            // shakey shake
            player.shake(2 + 4 * mod);

            // make dust!
            int dustRadius = 9;
            for(int dustX = (int)x - dustRadius; dustX < (int)x + dustRadius; dustX++ ) {
                for(int dustY = (int)y - dustRadius; dustY < (int)y + dustRadius; dustY++ ) {
                    float particleX = dustX + rand.nextFloat();
                    float particleY = dustY + rand.nextFloat();

                    Tile t = level.getTileOrNull((int)particleX, (int)particleY);
                    if(t != null && !t.blockMotion) {
                        float particleZ = t.getCeilHeight(particleX, particleY);

                        // only make particles that are in places we can see
                        if(bigShake || rand.nextFloat() < mod) {
                            if (GameManager.renderer.camera.frustum.pointInFrustum(particleX, particleZ - 0.8f, particleY)) {
                                Particle p = CachePools.getParticle(particleX, particleY, particleZ, 0, 0, 0, rand.nextInt(3), Color.WHITE, false);

                                p.checkCollision = false;
                                p.floating = true;
                                p.lifetime = (int) (200 * rand.nextFloat()) + 40;
                                p.shader = "dust";
                                p.spriteAtlas = "dust_puffs";
                                p.startScale = 1f + (0.5f * rand.nextFloat() - 0.25f);
                                p.endScale = 1f + (0.5f * rand.nextFloat() - 0.25f);
                                p.endColor = new Color(1f, 1f, 1f, 0f);
                                p.scale = 0.5f;

                                p.xa = 0;
                                p.ya = 0;
                                p.za = -0.005f + rand.nextFloat() * -0.02f;

                                Game.GetLevel().SpawnNonCollidingEntity(p);
                            }
                        }

                        if(rand.nextFloat() < 0.035f * mod) {
                            BeamProjectile beam = new BeamProjectile(particleX, particleY, particleZ, 0, 0.0001f, -0.2f - rand.nextFloat() * 0.1f, 4, Weapon.DamageType.MAGIC, Color.PURPLE, player);
                            beam.hitSound = "magic/mg_fwoosh.mp3";

                            if(rand.nextBoolean()) {
                                Explosion e = new Explosion();

                                e.spawns = new Array<Entity>();
                                Fire f = new Fire();
                                f.color = new Color(Color.PURPLE);
                                f.scaleMod = 0;
                                f.particleEffect = "Magic Fire Effect";
                                f.lifeTime = 100 + rand.nextInt(400);
                                f.z = 0.3f;

                                e.spawns.add(f);
                                e.color = null;

                                e.explodeSound = "magic/mg_fwoosh.mp3";

                                beam.explosion = e;
                            }

                            Game.GetLevel().SpawnEntity(beam);
                        }
                    }
                }
            }
        }
    }
}
