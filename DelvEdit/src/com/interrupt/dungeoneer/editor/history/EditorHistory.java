package com.interrupt.dungeoneer.editor.history;

import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.editor.Editor;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;

import java.util.Arrays;

public class EditorHistory {
    private final Array<byte[]> history = new Array<byte[]>();
    private int pos = 0;

	public void saveState(Level level) {
        byte[] levelBytes = KryoSerializer.toBytes(level);

        if(history.size > 0) {
            if(Arrays.equals(history.get(0), levelBytes)) return;
        }

        // we changed the timeline! remove newer stuff
        for(int i = 0; i < pos; i++)
            history.removeIndex(0);

        // make sure we're trying to save something new
        boolean needsToSave = true;
        if(history.size > 0) {
            byte[] previous = history.get(0);
            if(previous == levelBytes) needsToSave = false;
        }

        if(needsToSave) {
            history.insert(0, levelBytes);
            pos = 0;
        }

        // max
        int maxSize = 50;
        if(history.size > maxSize) {
            history.truncate(maxSize);
        }

        // Update title to reflect dirty status.
        Editor.app.updateTitle();
    }

	public Level undo() {
	    if(history.size == 0) return null;
        if(history.size > pos + 1) pos++;

        Editor.app.updateTitle();

        return KryoSerializer.loadLevel(history.get(pos));
    }

	public Level redo() {
        if(pos > 0) pos--;

        Editor.app.updateTitle();

        return KryoSerializer.loadLevel(history.get(pos));
    }

    private final byte[] nullHistoryState = new byte[]{};
    public byte[] top() {
	    if (history.isEmpty()) {
	        return nullHistoryState;
        }

	    return history.get(pos);
    }
}