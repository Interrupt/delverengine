package com.interrupt.dungeoneer.editor;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Group;
import com.interrupt.dungeoneer.entities.Prefab;
import com.interrupt.dungeoneer.game.Game;

public class JsonViewer extends JPanel {
	public JsonViewer(final Entity entity) {
		JTextArea textArea = new JTextArea(5, 20);

        Entity copy = Editor.app.entityManager.Copy(entity);

        if(copy instanceof Group) {
            Group g = (Group)copy;
            for(Entity e : g.entities) {
                e.x -= copy.x;
                e.y -= copy.y;
                e.z -= copy.z;
                e.drawable = null;
            }
        }

		copy.x = 0;
		copy.y = 0;
		copy.z = 0;
		copy.drawable = null;
		
		if(copy instanceof Prefab) {
			((Prefab) copy).entities.clear();
		}
		

		String jsonText = Game.toJson(copy, Entity.class);
		Json json = new Json();
		json.setOutputType(JsonWriter.OutputType.json);
		jsonText = json.prettyPrint(jsonText);
		textArea.setText(jsonText);
		
		add(textArea);
	}
}
