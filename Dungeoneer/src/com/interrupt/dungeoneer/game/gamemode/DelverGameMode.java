package com.interrupt.dungeoneer.game.gamemode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameInput;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.editor.EditorMarker;
import com.interrupt.dungeoneer.entities.*;
import com.interrupt.dungeoneer.entities.items.Gold;
import com.interrupt.dungeoneer.entities.items.Key;
import com.interrupt.dungeoneer.entities.items.QuestItem;
import com.interrupt.dungeoneer.entities.items.Weapon;
import com.interrupt.dungeoneer.entities.projectiles.BeamProjectile;
import com.interrupt.dungeoneer.entities.triggers.Trigger;
import com.interrupt.dungeoneer.game.*;
import com.interrupt.dungeoneer.generator.GenInfo;
import com.interrupt.dungeoneer.generator.GenTheme;
import com.interrupt.dungeoneer.input.ControllerState;
import com.interrupt.dungeoneer.overlays.DebugOverlay;
import com.interrupt.dungeoneer.overlays.LevelUpOverlay;
import com.interrupt.dungeoneer.overlays.MapOverlay;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.screens.GameOverScreen;
import com.interrupt.dungeoneer.screens.WinScreen;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.tiles.Tile;
import com.interrupt.managers.EntityManager;

import java.util.Random;

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

    public void preLevelInit(Level.Source source, Level level) {
        if(source != Level.Source.LEVEL_START)
            return;

        placeStairsDown(level, source);
        generateTraps(level, source);
    }

    private void placeStairsDown(Level level, Level.Source source) {
        if(!level.generated)
            return;

        // Find the places where the stairs down could possibly go
        Array<Vector2> stairLocations = new Array<>();
        if(level.editorMarkers != null && level.editorMarkers.size > 0) {
            for (EditorMarker marker : level.editorMarkers) {
                if(marker.type == GenInfo.Markers.exitLocation) {
                    stairLocations.add(new Vector2(marker.x, marker.y));
                }
            }
        }

        if(stairLocations.size == 0)
            return;

        Random levelRand = new Random();

        // place stairs if needed, need to know their locations before generating entities
        if(stairLocations.size > 0) {
            if(level.makeStairsDown) {
                // Pick one of the possible stair locations to place the stairs down at
                Vector2 downLoc = stairLocations.get(levelRand.nextInt(stairLocations.size));
                stairLocations.removeValue(downLoc, true);

                // Make the stairs down
                Tile downTile = level.getTile((int) downLoc.x, (int) downLoc.y);
                level.down = level.spawnStairs(Stairs.StairDirection.down, (int) downLoc.x, (int) downLoc.y, downTile.floorHeight);
            }

            // If there are leftover stair locations, spawn any given objectives or just make some good loot
            for(int i = 0; i< stairLocations.size; i++) {
                if(level.objectivePrefab != null && !level.objectivePrefab.isEmpty()) {
                    // We have an objective to try to spawn on this level!
                    try {
                        String[] prefabInfo = level.objectivePrefab.split("/+");
                        Entity objective = EntityManager.instance.getEntity(prefabInfo[0], prefabInfo[1]);
                        if(objective != null) {
                            objective.x = stairLocations.get(i).x + 0.5f;
                            objective.y = stairLocations.get(i).y + 0.5f;
                            level.entities.add(objective);
                        }
                    }
                    catch(Exception ex) {
                        Gdx.app.error("Delver", "Could not spawn objective item: " + level.objectivePrefab);
                    }

                    level.objectivePrefab = null;
                }
                else {
                    // Make good loot! Can be higher level than normal to be special
                    int num = levelRand.nextInt(5);
                    Item itm = null;

                    if (num == 0) {
                        itm = Game.GetItemManager().GetRandomArmor(Game.instance.player.level + levelRand.nextInt(2));
                    } else if (num == 1) {
                        itm = Game.GetItemManager().GetRandomWeapon(Game.instance.player.level + levelRand.nextInt(2));
                    } else if (num == 2) {
                        itm = Game.GetItemManager().GetRandomWand();
                    } else if (num == 3) {
                        itm = Game.GetItemManager().GetRandomPotion();
                    } else if (num == 4) {
                        itm = Game.GetItemManager().GetRandomRangedWeapon(Game.instance.player.level + levelRand.nextInt(2));
                    }

                    if (itm != null) {
                        itm.x = stairLocations.get(i).x + 0.5f;
                        itm.y = stairLocations.get(i).y + 0.5f;
                        level.entities.add(itm);
                    }
                }
            }
        }
    }

    private void generateTraps(Level level, Level.Source source) {
        // Generate some traps the first time into this level
        if(level.traps == null || level.traps.length == 0)
            return;

        // Some markers should have a wide trap-free area around them
        Array<Vector2> trapAvoidLocs = new Array<>();

        // Some markers should just not generate traps under them
        Boolean canMakeTrap[] = new Boolean[level.width * level.height];
        for(int i = 0; i < level.width * level.height; i++)
            canMakeTrap[i] = false;

        if(level.editorMarkers != null && level.editorMarkers.size > 0) {
            for (EditorMarker marker : level.editorMarkers) {
                // Never make a trap under any marker
                if (marker.x >= 0 && marker.x < level.width && marker.y >= 0 && marker.y < level.height)
                    canMakeTrap[marker.x + marker.y * level.width] = false;

                // Handle some generic marker logic
                if(marker.type == GenInfo.Markers.playerStart) {
                    trapAvoidLocs.add(new Vector2(marker.x, marker.y));
                }
                else if(marker.type == GenInfo.Markers.stairDown) {
                    trapAvoidLocs.add(new Vector2(marker.x, marker.y));
                }
                else if(marker.type == GenInfo.Markers.stairUp) {
                    trapAvoidLocs.add(new Vector2(marker.x, marker.y));
                }
                else if(marker.type == GenInfo.Markers.boss) {
                    trapAvoidLocs.add(new Vector2(marker.x, marker.y));
                }
                else if(marker.type == GenInfo.Markers.exitLocation)
                {
                    trapAvoidLocs.add(new Vector2(marker.x, marker.y));
                }
            }
        }

        // Can generate traps now that we know where not to put them
        Random levelRand = new Random();
        int trapAvoidRadius = 6;

        for(int x = 0; x < level.width; x++) {
            for(int y = 0; y < level.height; y++) {
                // Only generate so many traps
                if(levelRand.nextFloat() > 0.012f)
                    continue;

                // And only generate traps on free and clear tiles
                Tile c = level.getTileOrNull(x,y);
                if(c == null)
                    continue;

                if(!canMakeTrap[x + y * level.width])
                    continue;

                if(!c.CanSpawnHere())
                    continue;

                if(!c.isFlat())
                    continue;

                // Check to see if we are far enough away from any locations to avoid
                boolean closeToAvoidLoc = false;
                for(Vector2 avoidLoc : trapAvoidLocs) {
                    if( Math.abs(x - avoidLoc.x) < trapAvoidRadius && Math.abs(y - avoidLoc.y) < trapAvoidRadius ) {
                        closeToAvoidLoc = true;
                    }
                }

                if(closeToAvoidLoc)
                    continue;

                // We're able to place a trap here, pick a random trap for this location
                Prefab p = new Prefab("Traps", level.traps[levelRand.nextInt(level.traps.length)]);
                p.x = x + 0.5f;
                p.y = y + 0.5f;
                p.z = c.getFloorHeight(x, y) + 0.5f;
                level.entities.add(p);
                p.init(level, source);
            }
        }
    }

    public void handleEditorMarker(Level level, EditorMarker marker, Vector2 levelOffset, Vector2 tileOffset)
    {
        GenTheme genTheme = level.genTheme;
        int x = marker.x + (int)levelOffset.x;
        int y = marker.y + (int)levelOffset.y;

        // Get some tile information
        Tile atTile = level.getTileOrNull(x, y);
        float floorPos = atTile != null ? atTile.getFloorHeight(marker.x + levelOffset.x, marker.y + levelOffset.y) : 0;
        float ceilPos = atTile != null ? atTile.ceilHeight : 0;

        // Keep track of where stairs are spawned
        Array<Vector2> stairLocations = new Array<Vector2>();

        if(marker.type == GenInfo.Markers.torch) {
            if(level.spawnRates != null && Game.rand.nextFloat() > level.spawnRates.lights)
                return;

            Entity t;
            if(genTheme != null && genTheme.spawnLights != null && genTheme.spawnLights.size > 0) {
                Entity light = genTheme.spawnLights.get(Game.rand.nextInt(genTheme.spawnLights.size));
                t = (Light) KryoSerializer.copyObject(light);
            }
            else {
                Entity torch = EntityManager.instance.getEntity("Lights", "Torch");
                if(torch != null) {
                    t = torch;
                }
                else {
                    // orange default torch
                    t = new Torch(x + 0.5f + tileOffset.x, y + 0.5f + tileOffset.y, 4, new Color(1f, 0.8f, 0.4f, 1f));
                }
            }

            if(t != null) {
                t.x = x + 0.5f + tileOffset.x;
                t.y = y + 0.5f + tileOffset.y;
                t.z = floorPos + 0.5f;

                Entity light = level.decorateWallWith(t, false, true);
                if(light != null && light.isActive) {
                    level.SpawnNonCollidingEntity(light);
                }
            }
        }
        else if(marker.type == GenInfo.Markers.stairDown) {
            level.down = level.spawnStairs(Stairs.StairDirection.down, x, y, floorPos);
        }
        else if(marker.type == GenInfo.Markers.stairUp) {
            level.up = level.spawnStairs(Stairs.StairDirection.up, x, y, ceilPos);
        }
        else if(marker.type == GenInfo.Markers.boss) {
            // make the boss and the orb
            Item orb = new QuestItem(x + 0.5f + tileOffset.x, y + 0.5f + tileOffset.y);
            orb.z = floorPos + 0.5f;
            level.entities.add(orb);

            // grab a monster from the BOSS category
            Monster m = Game.GetMonsterManager().GetRandomMonster("BOSS");
            if(m != null) {
                m.x = x + 0.5f;
                m.y = y + 0.5f;
                m.z = floorPos + 0.5f;
                m.Init(level, 20);
                level.SpawnEntity(m);
            }
        }
        else if(marker.type == GenInfo.Markers.door) {
            Door door = null;

            if(genTheme != null && genTheme.doors != null && genTheme.doors.size > 0) {
                door = new Door(genTheme.doors.get(Game.rand.nextInt(genTheme.doors.size)));
            }

            if(door == null) {
                door = new Door(x + 0.5f, y + 0.5f, 10);
            }

            door.x = x + 0.5f;
            door.y = y + 0.5f;
            door.z = floorPos + 0.5f;

            level.SpawnEntity(door);
            door.placeFromPrefab(level);
        }
        else if(marker.type == GenInfo.Markers.decor || (marker.type == GenInfo.Markers.decorPile && genTheme != null && genTheme.decorations != null)) {
            if(level.spawnRates != null && Game.rand.nextFloat() > level.spawnRates.decor)
                return;

            // try to pull a decoration from the genTheme, or just make one from the item list
            Entity d = null;

            if(genTheme != null && genTheme.decorations != null) {
                if(genTheme.decorations.size > 0)
                    d = EntityManager.instance.Copy(genTheme.decorations.get(Game.rand.nextInt(genTheme.decorations.size)));
            }
            else {
                d = Game.GetItemManager().GetRandomDecoration();
            }

            if( d != null ) {
                float rx = (Game.rand.nextFloat() * (1 - d.collision.x * 2f)) - (1 - d.collision.x * 2f) / 2f;
                float ry = (Game.rand.nextFloat() * (1 - d.collision.y * 2f)) - (1 - d.collision.y * 2f) / 2f;
                d.x = x + rx + 0.5f + tileOffset.x;
                d.y = y + ry + 0.5f + tileOffset.y;
                d.z = atTile.getFloorHeight(d.x, d.y) + 0.5f;

                level.SpawnEntity(d);
            }
        }
        else if(marker.type == GenInfo.Markers.decorPile) {
            if(level.spawnRates != null && Game.rand.nextFloat() > level.spawnRates.decor)
                return;

            int num = Game.rand.nextInt(3) + 1;
            for(int i = 0; i < num; i++)
            {
                Entity d = Game.GetItemManager().GetRandomDecoration();
                if( d != null ) {
                    float rx = (Game.rand.nextFloat() * (1 - d.collision.x * 2f)) - (1 - d.collision.x * 2f) / 2f;
                    float ry = (Game.rand.nextFloat() * (1 - d.collision.y * 2f)) - (1 - d.collision.y * 2f) / 2f;
                    d.x = x + rx + 0.5f + tileOffset.x;
                    d.y = y + ry + 0.5f + tileOffset.y;
                    d.z = atTile.getFloorHeight(d.x, d.y) + 0.5f;

                    level.SpawnEntity(d);
                }
            }
        }
        else if(marker.type == GenInfo.Markers.monster) {
            if(level.spawnRates != null && Game.rand.nextFloat() > level.spawnRates.monsters)
                return;

            String levelTheme = level.theme;

            // Rarely, grab monsters from another theme if set
            if(level.alternateMonsterThemes != null && level.alternateMonsterThemes.size > 0) {
                if(Game.rand.nextFloat() < 0.1f) {
                    levelTheme = level.alternateMonsterThemes.random();
                }
            }

            Monster m = Game.GetMonsterManager().GetRandomMonster(levelTheme);

            if(m != null)
            {
                m.x = x + 0.5f + tileOffset.x;
                m.y = y + 0.5f + tileOffset.y;
                m.z = floorPos + 0.5f;
                m.Init(level, Game.instance.player.level);
                level.SpawnEntity(m);
            }
        }
        else if(marker.type == GenInfo.Markers.key)
        {
            // make a key
            Key key = new Key(x + 0.5f + tileOffset.x, y + 0.5f + tileOffset.y);
            key.z = floorPos + 0.5f;
            level.SpawnEntity(key);
        }
        else if(marker.type == GenInfo.Markers.loot)
        {
            if(level.spawnRates != null && Game.rand.nextFloat() > level.spawnRates.loot)
                return;

            // loot!
            Item itm = Game.GetItemManager().GetLevelLoot(Game.instance.player.level);

            if(itm != null) {
                itm.x = x + 0.5f + tileOffset.x;
                itm.y = y + 0.5f + tileOffset.y;
                itm.z = floorPos + 0.5f;

                level.SpawnEntity(itm);
            }
        }
        else if(marker.type == GenInfo.Markers.exitLocation)
        {
            if(level.generated) stairLocations.add(new Vector2(x,y));
        }
        else if(marker.type == GenInfo.Markers.secret)
        {
            Item itm = null;
            if(Game.rand.nextBoolean()) {
                itm = new Gold(Game.rand.nextInt((level.dungeonLevel * 3) + 1) + 10);
            }
            else {
                // Make good loot!
                int num = Game.rand.nextInt(5);

                if (num == 0) {
                    itm = Game.GetItemManager().GetRandomArmor(Game.instance.player.level + Game.rand.nextInt(2));
                } else if (num == 1) {
                    itm = Game.GetItemManager().GetRandomWeapon(Game.instance.player.level + Game.rand.nextInt(2));
                } else if (num == 2) {
                    itm = Game.GetItemManager().GetRandomWand();
                } else if (num == 3) {
                    itm = Game.GetItemManager().GetRandomPotion();
                } else if (num == 4) {
                    itm = Game.GetItemManager().GetRandomRangedWeapon(Game.instance.player.level + Game.rand.nextInt(2));
                }
            }

            if(itm != null) {
                itm.x = x + 0.5f + tileOffset.x;
                itm.y = y + 0.5f + tileOffset.y;
                itm.z = floorPos + 0.5f;
                level.SpawnEntity(itm);
            }

            // Add the secret trigger
            Trigger secretTrigger = new Trigger();
            secretTrigger.message = "A secret has been revealed!";
            secretTrigger.x = x + 0.5f + tileOffset.x;
            secretTrigger.y = y + 0.5f + tileOffset.y;
            secretTrigger.z = floorPos + 0.5f;
            secretTrigger.collision.x = 0.65f;
            secretTrigger.collision.y = 0.65f;
            secretTrigger.collision.z = 1f;
            secretTrigger.triggerType = Trigger.TriggerType.PLAYER_TOUCHED;
            secretTrigger.triggerResets = false;
            secretTrigger.messageTime = 2f;
            secretTrigger.triggerSound = "ui/ui_secret_found.mp3";
            secretTrigger.isSecret = true;
            level.SpawnEntity(secretTrigger);

            // Add a monster, rarely
            if(Game.rand.nextFloat() > 0.85f) {
                Monster m = Game.GetMonsterManager().GetRandomMonster(level.theme);

                if(m != null)
                {
                    m.x = x + 0.5f + tileOffset.x;
                    m.y = y + 0.5f + tileOffset.y;
                    m.z = floorPos + 0.5f;
                    m.Init(level, Game.instance.player.level);
                    level.SpawnEntity(m);
                }
            }
        }
    }
}
