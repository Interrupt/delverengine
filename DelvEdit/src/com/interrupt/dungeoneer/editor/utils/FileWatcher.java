package com.interrupt.dungeoneer.editor.utils;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.editor.Editor;

import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/** Subsystem for live reloading art assets. */
public class FileWatcher {
    private final Thread watcher;
    private static final boolean created = false;

    private long lastTimeCalled = 0;
    private final long MIN_TIME_BETWEEN_CALLS = 100;

    private final List<String> watchedExtensions = Arrays.asList("dat", "obj", "png", "frag", "vert");

    public FileWatcher() {
    	if (FileWatcher.created) {
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
								Gdx.app.log("Editor", "Live loading assets");
								lastTimeCalled = now;

								// Reload must happen on main render thread.
								Editor.app.needToReloadAssets.set(true);
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
    	if (watcher.isAlive()) {
    		return;
		}

    	watcher.setPriority(Thread.MIN_PRIORITY);
		watcher.start();
	}

    public void dispose() {
    	watcher.interrupt();
	}

	private String getFileExtension(String filename) {
    	int index = filename.lastIndexOf(".");
    	if (index != -1 && index != 0) {
    		return filename.substring(index + 1);
		}

    	return "";
	}
}
