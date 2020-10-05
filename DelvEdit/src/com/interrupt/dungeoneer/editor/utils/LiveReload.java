package com.interrupt.dungeoneer.editor.utils;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.editor.EditorArt;

import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/** Subsystem for live reloading art assets. */
public class LiveReload {
    private final Thread watcher;
    private static final boolean created = false;

    private long lastTimeCalled = 0;
    private final long MIN_TIME_BETWEEN_CALLS = 100;

    public AtomicBoolean needToReloadAssets = new AtomicBoolean(false);

    private final List<String> watchedExtensions = Arrays.asList("dat", "obj", "png", "frag", "vert");

    public LiveReload() {
    	if (LiveReload.created) {
    		throw new RuntimeException("FileWatcher instance already exists.");
		}

        watcher = new Thread() {
			public void run() {
				try {
					WatchService watchService = FileSystems.getDefault().newWatchService();
					java.nio.file.Path path = Paths.get(Gdx.files.getLocalStoragePath());
					path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

					boolean running = true;
					while(running) {
						WatchKey key = watchService.take();
						for (WatchEvent<?> event : key.pollEvents()) {
							@SuppressWarnings("unchecked")
							WatchEvent<Path> watchEvent = (WatchEvent<Path>)event;
							String filename = watchEvent.context().toString();
							String extension = getFileExtension(filename).toLowerCase();

							long now = System.currentTimeMillis();

							if (watchedExtensions.contains(extension) && now - lastTimeCalled > MIN_TIME_BETWEEN_CALLS) {
								lastTimeCalled = now;

								// Reload must happen on main render thread.
								needToReloadAssets.set(true);
								break;
							}
						}

						running = key.reset();
					}
				}
				catch (Exception ignore) {}
			}
		};
    }

    public void init() {
		Gdx.app.log("LiveReload", "Initializing");
    	if (watcher.isAlive()) {
    		return;
		}

    	watcher.setPriority(Thread.MIN_PRIORITY);
		watcher.start();
	}

    public void dispose() {
    	Gdx.app.log("LiveReload", "Disposing");
    	watcher.interrupt();
	}

	public void tick() {
        if (needToReloadAssets.compareAndSet(true, false)) {
			Gdx.app.log("LiveReload", "Reloading assets");
			// TODO: Use ActionListener event handling instead.
			EditorArt.refresh();
			Editor.app.generatorInfo.refresh();
            Editor.app.initTextures();
		}
    }

	private String getFileExtension(String filename) {
    	int index = filename.lastIndexOf(".");
    	if (index != -1 && index != 0) {
    		return filename.substring(index + 1);
		}

    	return "";
	}
}
