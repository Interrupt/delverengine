package com.interrupt.dungeoneer.editor.gizmos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.editor.EditorColors;
import com.interrupt.dungeoneer.editor.ui.Handles;
import com.interrupt.dungeoneer.entities.AmbientSound;

@GizmoFor(target = AmbientSound.class)
public class AmbientSoundGizmo extends EntityGizmo {
    public AmbientSoundGizmo(AmbientSound entity) {
        super(entity);
    }

    @Override
    public void draw() {
        super.draw();

        AmbientSound sound = (AmbientSound)entity;
        Handles.setColor(EditorColors.AUDIO_GIZMO);
        Handles.drawWireSphere(new Vector3(sound.x, sound.z, sound.y), sound.radius);
        Handles.setColor(Color.WHITE);
    }
}
