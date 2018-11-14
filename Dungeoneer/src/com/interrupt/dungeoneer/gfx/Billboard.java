package com.interrupt.dungeoneer.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

public class Billboard extends Sprite {
	private static final float ANIM_SPEED = 1f;
    public Vector3 wpos = new Vector3(0,0,0);       // WORLD position
    public Vector3 spos = new Vector3(0,0,0);       // SCREEN pos
    public Vector3 wmove = new Vector3(0,0,0);

    public static Billboard make(String imgPath) {
            Texture tex = new Texture(Gdx.files.internal(imgPath));
            Billboard sp = new Billboard(tex);
            return sp;
    }
    
    public Billboard(Texture tex) {
            super(tex);
    }

    public void setMove(float x, float y, float z) {
            wmove = new Vector3(x,y,z);     
    }

    public void wpos(float x, float y, float z) {
            wpos = new Vector3(x, y, z);
    }
    
    public void spos(Camera cam) {
            
    }

    public Vector3 project(Camera cam) {
            spos = new Vector3(wpos);       // dont overwrite worldpos!
            cam.project(spos);
            return spos;
    }
    
    public void update(float delta) {
            Vector3 moveAmt = wmove;
            moveAmt.scl(delta);
            // Log.out("moveamt:" + moveAmt);
            wpos.add(moveAmt);
            if (wpos.x > 10 || wpos.x<0 ) {
                    wmove.x = -wmove.x;
            }
    }
}
