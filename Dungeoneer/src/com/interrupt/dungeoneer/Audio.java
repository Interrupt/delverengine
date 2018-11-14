package com.interrupt.dungeoneer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.interrupt.dungeoneer.audio.PlayingSound;
import com.interrupt.dungeoneer.entities.AmbientSound;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.PositionedSound;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.tiles.Tile;

public class Audio {
	
	public static Sound attack;
	public static Sound hit;
	public static Sound explode;
	public static Sound torch;
	public static Sound spell;
	public static Sound clang;
	public static Sound splash;
	public static Sound steps;
	
	public static Music music;
	
	private static String loadedMusic = "";
	
	private static ObjectMap<String, Sound> loadedSounds = new ObjectMap<String, Sound>();
	
	private static Array<PlayingSound> loopingSounds = new Array<PlayingSound>();
	private static Array<AmbientSound> ambientSounds = new Array<AmbientSound>();
	
	private static ArrayMap<String, String[]> soundFileCache = new ArrayMap<String, String[]>();

    private static Array<String> musicTracks = new Array<String>();
    private static int nextMusicTrackIndex = 0;

    // Used for fading music volume
    private static float musicMod = 1f;
    private static float musicTargetMod = 1f;
    private static float musicLerpSpeed = 0.016f;

    private static ArrayMap<String, PlayingSound> ambientTileSounds = new ArrayMap<String, PlayingSound>();
    private static ArrayMap<String, PlayingSound> ambientTileSoundsToStop = new ArrayMap<String, PlayingSound>();

    private static PlayingSound ambientSound = null;

    private static String currentAmbientSound = null;
    private static ArrayMap<String, Float> ambientSoundStack = new ArrayMap<String, Float>();
	private static ArrayMap<String, Float> ambientSoundStackPlayTime = new ArrayMap<String, Float>();
	private static float ambientSoundChangeSpeed = 0.1f;
	
	static public void init() {
		attack = loadSound("whoosh1.mp3");
		explode = loadSound("explode.mp3");
		spell = loadSound("spell-missile-2.mp3");
		clang = loadSound("clang.mp3");
		splash = loadSound("splash2.mp3");
		
		loadSound("hit.ogg");
		loadSound("inventory/drop_item.ogg");
		loadSound("pu_metal.ogg");
		loadSound("pu_gen.ogg");
		loadSound("pu_glass.ogg");
		loadSound("pu_gold.ogg");
		
		if(Gdx.app.getType() != ApplicationType.Android && Gdx.app.getType() != ApplicationType.iOS) {
			torch = loadSound("torch.mp3");
			steps = loadSound("steps.mp3");
		}
	}
	
	static public Sound loadSound(String filename)
	{
		try {
			// check if already loaded
			if(loadedSounds.containsKey(filename)) return loadedSounds.get(filename);
			
			// load if not already
			Sound loaded = Gdx.audio.newSound(Game.findInternalFileInMods("audio/" + filename.replaceAll(".ogg", ".mp3")));
			if(loaded != null) loadedSounds.put(filename, loaded);
			return loaded;
		} catch (Exception e) {
			Gdx.app.log("DelverAudio", "Error loading " + filename);
			return null;
		}
	}
	
	static public String[] getFileList(String filename) {
		String[] cached = soundFileCache.get(filename);
		if(cached == null) {
			cached = filename.split(",");
			soundFileCache.put(filename, cached);
		}
		return cached;
	}
	
	static public void preload(String filename) {
		if(filename == null) return;
		
		String[] files = getFileList(filename);
		if(!Game.isMobile) {
			for(int i = 0; i < files.length; i++) {
				loadSound(files[i]);
			}
		} else {
			loadSound(files[0]);
		}
	}
	
	static private Music loadMusic(String filename)
	{
		try {
			if(loadedMusic.equals(filename)) return music;
			loadedMusic = filename;
			return Gdx.audio.newMusic(Game.findInternalFileInMods("audio/music/" + filename));
		} catch (Exception e) {
			Gdx.app.log("DelverMusic", "Error loading " + filename);
			return null;
		}
	}

    static public void tick(float delta, Player player, Level level) {
        // Lerp music volume changes over time
        musicMod = Interpolation.linear.apply(musicMod, musicTargetMod, musicLerpSpeed * delta);
        setMusicVolume(Math.max(musicMod, 0f));

        int radius = 12;

        // Find nearby tiles with ambient sounds attached
        ambientTileSoundsToStop.putAll(ambientTileSounds);

        for(int i = 0; i < ambientTileSounds.size; i++) {
        	if(!ambientSoundStack.containsKey(ambientTileSounds.getKeyAt(i))) {
				ambientTileSounds.getValueAt(i).volume = 0f;
			}
        }

        Entity listening = player;
		GlRenderer renderer = GameManager.renderer;
		if(renderer.cutsceneCamera != null) {
			listening = renderer.cutsceneCamera;
		}

        for(int x = (int)listening.x - radius; x < listening.x + radius; x++) {
            for(int y = (int)listening.y - radius; y < listening.y + radius; y++) {
                Tile t = level.getTileOrNull(x, y);
                if(t != null && t.data != null && t.data.ambientSound != null) {
                    // Found a tile with an ambient sound attached
                    float volumeMod = getTileAmbientSoundVolume(t, x, y, listening, level);

                    // if close enough, play the sound
                    if(volumeMod > 0) {
                        PlayingSound playing = ambientTileSounds.get(t.data.ambientSound);

                        if (playing != null) {
                            if (volumeMod > playing.volume) playing.volume = volumeMod;
                        } else {
                            playing = playSound(t.data.ambientSound, volumeMod, 1f, true);
                            if (playing != null) {
                                //Gdx.app.log("DelverSound", "Started playing ambient sound");
                                ambientTileSounds.put(t.data.ambientSound, playing);
                            }
                        }

                        // This sound can keep playing for another tick
                        if (playing != null) ambientTileSoundsToStop.removeKey(t.data.ambientSound);
                    }
                }
            }
        }

        // Make sure our other ambient sounds play!
        for(int i = 0; i < ambientSoundStack.size; i++) {
        	String key = ambientSoundStack.getKeyAt(i);
        	Float value = ambientSoundStack.get(key);

			PlayingSound playing = ambientTileSounds.get(key);

			Float playingTime = ambientSoundStackPlayTime.get(key);
			if(playingTime == null)
				playingTime = 0f;

			boolean currentlyPlaying = currentAmbientSound != null && key.equals(currentAmbientSound);

			if(currentlyPlaying) {
				playingTime += delta * ambientSoundChangeSpeed;
			}
			else {
				playingTime -= delta * ambientSoundChangeSpeed;
			}

			playingTime = Math.max(playingTime, 0f);
			playingTime = Math.min(playingTime, 1f);

			ambientSoundStackPlayTime.put(key, playingTime);

			float calcVolume = Interpolation.linear.apply(0f, value, playingTime * playingTime);

			if(!currentlyPlaying && playingTime <= 0) {
				calcVolume = 0f;
			}

			if(calcVolume > 0) {
				if (playing != null) {
					playing.volume = calcVolume;
				} else {
					if(!key.equals("")) {
						playing = playSound(key, 0f, 1f, true);
						if (playing != null) {
							ambientTileSounds.put(key, playing);
						}
					}
				}

				ambientTileSoundsToStop.removeKey(ambientSoundStack.getKeyAt(i));
			}
		}

        // stop any sounds that are no longer playing
        for(int i = 0; i < ambientTileSoundsToStop.size; i++) {
            PlayingSound soundToStop = ambientTileSoundsToStop.getValueAt(i);
            if(soundToStop != null) {
                soundToStop.sound.stop(soundToStop.location);
                ambientTileSounds.removeValue(soundToStop, true);
				ambientSoundStack.removeKey(ambientTileSoundsToStop.getKeyAt(i));
                //Gdx.app.log("DelverSound", "Stopped playing ambient sound");
            }
        }

        // set the volume, finally
        for(int i = 0; i < ambientTileSounds.size; i++) {
            PlayingSound s = ambientTileSounds.getValueAt(i);
            s.setVolume(s.volume);
        }

        // reset for next tick
        ambientTileSoundsToStop.clear();
    }

    static public float getTileAmbientSoundVolume(Tile t, int x, int y, Entity listening, Level level) {
        if(t.data != null) {
            // how far from this tile are we?
            float xDist = Math.abs(x + 0.5f - listening.x);
            float yDist = Math.abs(y + 0.5f - listening.y);
            float zDist = Math.abs(t.floorHeight - listening.z + 0.5f);

            // how loud would this sound be, based on our position?
            float xMod = Math.max(0f, -xDist + t.data.ambientSoundRadius) / t.data.ambientSoundRadius;
            float yMod = Math.max(0f, -yDist + t.data.ambientSoundRadius) / t.data.ambientSoundRadius;
            float zMod = Math.max(0f, -zDist + t.data.ambientSoundRadius) / t.data.ambientSoundRadius;

            if(t.data.isWater && t.data.flowingSoundVolume != null) {
                float lowerWallHeight = level.maxLowerWallHeight(x, y);
                float waterfallHeight = Math.min(lowerWallHeight, 1.5f) / 1.5f;
                float flowingVolume = t.data.flowingSoundVolume * waterfallHeight;

                // ignore the height distance a bit for waterfalls
                zMod = Math.max(0f, -zDist + (t.data.ambientSoundRadius * lowerWallHeight)) / (t.data.ambientSoundRadius * lowerWallHeight);

                if(flowingVolume > t.data.ambientSoundVolume) {
                    return flowingVolume * xMod * yMod * zMod;
                }
            }

            return t.data.ambientSoundVolume * xMod * yMod * zMod;
        }

        return 0;
    }
	
	static public void playMusic(String filename, final boolean loop)
	{
		try {
            // reset the list of playing tracks
            musicTracks.clear();
            nextMusicTrackIndex = 0;

			if(Options.instance.musicVolume == 0 || filename == null) {
				if(music != null) {
					if(music.isPlaying()) music.stop();
					music.dispose();
				}
				music = null;
				loadedMusic = "";
				return;
			}

            // initialize the list of tracks to play
            musicTracks.addAll(filename.split(","));
            musicTracks.shuffle();

            playNextTrack(loop, false);

            // try one more time to play music, if it fails
            if(music == null) {
            	playNextTrack(loop, false);
			}
		}
		catch(Exception ex) {
			Gdx.app.log("DelverAudio", "Error playing music " + filename);
		}
	}

    static public void playNextTrack(final boolean loop, final boolean playMore) {
        try {
            String trackToPlay = musicTracks.get((nextMusicTrackIndex++) % musicTracks.size);

			if (!loadedMusic.equals(trackToPlay)) {
                if (music != null) {
                    if (music.isPlaying()) music.stop();
                    music.dispose();
                    music = null;
                }

                music = loadMusic(trackToPlay);

                if (music != null) {
                    music.play();
                    setMusicVolume(1f);
                    music.setLooping(loop);
                }
            }
            else {
				if(music != null && !music.isPlaying()) {
					music.play();
					setMusicVolume(1f);
					music.setLooping(loop);
				}
			}

            music.setOnCompletionListener(new Music.OnCompletionListener() {
                @Override
                public void onCompletion(Music music) {
                    if (playMore) playNextTrack(loop, playMore);
                }
            });
        }
        catch(Exception ex) {
            Gdx.app.log("DelverAudio", "Error switching music tracks");
            if(music != null) {
            	music.dispose();
            	music = null;
			}
			loadedMusic = "";
        }
    }

    static public void setMusicTargetVolume(float targetVolume) {
        if(targetVolume > 1.0f) targetVolume = 1f;
        if(targetVolume < 0f) targetVolume = 0f;
        musicTargetMod = targetVolume;
    }
	
	static public void setMusicVolume(float volume) {
		if(music != null && music.isPlaying()) {
            musicMod = volume;
			music.setVolume((Options.instance.musicVolume * volume) * 0.5f);
		}
	}
	
	static public Music getPlayingMusic() {
		return music;
	}
	
	static public void playSound(Sound sfx, float volume) {
		if(sfx != null) sfx.play(Options.instance.sfxVolume);
	}
	
	static public void playSound(String filename, float volume) {
		try {
			if(filename == null || filename.equals("")) return;
			String[] files = getFileList(filename);
			String theFile = null;
			
			if(!Game.isMobile)
				theFile = files[Game.rand.nextInt(files.length)];
			else
				theFile = files[0];
			
			Sound sfx = getSound(theFile);
			if(sfx != null) {
				sfx.play(volume * Options.instance.sfxVolume);
			}
		}
		catch(Exception ex) {
			Gdx.app.log("DelverAudio", "Couldn't play sound: " + filename);
		}
	}
	
	static public void playSound(String filename, float volume, float pitch) {
		try {
			if(filename == null || filename.equals("")) return;
			String[] files = getFileList(filename);
			String theFile = null;
			if(!Game.isMobile)
				theFile = files[Game.rand.nextInt(files.length)];
			else
				theFile = files[0];
			
			Sound sfx = getSound(theFile);
			if(sfx != null) {
				Long id = sfx.play(volume * Options.instance.sfxVolume);
				sfx.setPitch(id, pitch);
			}
		}
		catch(Exception ex) {
			Gdx.app.log("DelverAudio", "Couldn't play sound: " + filename);
		}
	}
	
	static public Sound getSound(String filename) {
		if(filename == null || filename.equals("")) return null;
		
		Sound sfx = loadedSounds.get(filename);
		if(sfx == null) sfx = loadSound(filename);
		return sfx;
	}
	
	static public void playPositionedSound(String filename, Vector3 pos, float volume, float range) {
		if(filename == null || filename.equals("")) return;
		if(volume < 0.005) return; // why bother?
		
		try {
			String[] files = getFileList(filename);
			String theFile = null;
				
			if(!Game.isMobile)
				theFile = files[Game.rand.nextInt(files.length)];
			else
				theFile = files[0];
			
	 		Sound sfx = Audio.getSound(theFile);
			if(sfx != null && Game.instance.level != null) {
				PositionedSound p = new PositionedSound((float)pos.x, (float)pos.y, (float)pos.z, sfx, volume, range, 200);
				Game.instance.level.non_collidable_entities.add(p);
			}
		} catch (Exception ex) {
			Gdx.app.log("DelverAudio", "Couldn't play positioned sound: " + filename);
		}
	}
	
	static public void playPositionedSound(String filename, Vector3 pos, float volume, float pitch, float range) {
		if(filename == null || filename.equals("")) return;
		if(volume < 0.005) return; // why bother?
		
		try {
			String[] files = getFileList(filename);
			String theFile = null;
				
			if(!Game.isMobile)
				theFile = files[Game.rand.nextInt(files.length)];
			else
				theFile = files[0];
			
	 		Sound sfx = Audio.getSound(theFile);
			if(sfx != null && Game.instance.level != null) {
				PositionedSound p = new PositionedSound((float)pos.x, (float)pos.y, (float)pos.z, sfx, volume, pitch, range, 200);
				Game.instance.level.non_collidable_entities.add(p);
			}
		} catch (Exception ex) {
			Gdx.app.log("DelverAudio", "Couldn't play positioned sound: " + filename);
		}
	}
	
	static public void disposeAudio(String ignore) {
		try {
			if(music != null) {
				music.stop();
				music.dispose();
				music = null;
			}
			loadedMusic = "";
		}
		catch(Exception ex) {
			Gdx.app.log("DelverAudio", "Couldn't unload music");
		}
		
		if(!Game.isMobile) {
			for(Entry<String, Sound> sound : loadedSounds.entries()) {
				try {
					// don't unload level change sounds
					if(ignore != null && ignore.contains(sound.key)) continue;
					
					Sound s = sound.value;
					s.stop();
					s.dispose();
					s = null;
				}
				catch(Exception ex) {
					Gdx.app.log("DelverAudio", "Couldn't unload sound");
				}
			}
			loadedSounds.clear();
			loopingSounds.clear();
			ambientSounds.clear();
			soundFileCache.clear();
			
			attack = null;
			hit = null;
			explode = null;
			torch = null;
			spell = null;
			clang = null;
			splash = null;
			steps = null;
		}
	}

	public static PlayingSound playSound(String filename, float volume, float pitch, boolean loops) {
		
		// looping sounds aren't supported on mobile
		if(loops && Game.isMobile) return null;
		
		if(filename == null || filename.equals("")) return null;
		String[] files = getFileList(filename);
		String theFile = null;
			
		if(!Game.isMobile)
			theFile = files[Game.rand.nextInt(files.length)];
		else
			theFile = files[0];
		
		Sound sfx = getSound(theFile);
		if(sfx != null) {
			long id = sfx.play(volume * Options.instance.sfxVolume);
			sfx.setPitch(id, pitch);
			sfx.setLooping(id, loops);

			PlayingSound s = new PlayingSound(sfx, id, volume);

			if(loops) {
				loopingSounds.add(s);
			}

            return s;
		}
		
		return null;
	}

	public static void playAmbientSound(String filename, float volume, float changeSpeed) {
		ambientSoundStack.put(filename, volume);
		currentAmbientSound = filename;
		ambientSoundChangeSpeed = changeSpeed;
	}

	public static PlayingSound getAmbientSound() {
		return ambientSound;
	}
	
	public static void stopLoopingSounds() {
		for(PlayingSound s : loopingSounds) {
			if(s.sound != null)
				s.sound.stop();
		}
		loopingSounds.clear();

        ambientTileSounds.clear();
        ambientSoundStack.clear();
		ambientSoundStackPlayTime.clear();
		
		if(Audio.torch != null) Audio.torch.stop();
	}

	public static void updateLoopingSoundVolumes() {
		for(PlayingSound s : loopingSounds) {
			s.updateVolume();
		}
		for(AmbientSound s : ambientSounds) {
			s.refreshVolume();
		}
	}

	public static void addAmbientSound(AmbientSound ambientSound) {
		ambientSounds.add(ambientSound);
	}

	public static void removeAmbientSound(AmbientSound ambientSound) {
		ambientSounds.removeValue(ambientSound, true);
	}
}
