package com.interrupt.dungeoneer.editor.utils;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.entities.Entity;

import java.nio.file.*;
import java.util.Arrays;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/** Subsystem for live reloading art assets. */
public class FileWatcher {
    private final Thread watcher;
    private static final boolean initialized = false;

    private final List<String> watchedExtensions = Arrays.asList("dat", "obj", "png", "frag", "vert");

    public FileWatcher() {
    	if (FileWatcher.initialized) {
    		throw new RuntimeException("FileWatcher already initialized.");
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

							if (watchedExtensions.contains(extension)) {
								Art.refresh();
								for (Entity entity : Editor.app.getLevel().entities) {
									entity.drawable.refresh();
								}
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
