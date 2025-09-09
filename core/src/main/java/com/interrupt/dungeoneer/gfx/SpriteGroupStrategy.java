package com.interrupt.dungeoneer.gfx;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ShortArray;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.game.OverworldLevel;
import com.interrupt.dungeoneer.gfx.decals.DDecal;
import com.interrupt.dungeoneer.gfx.shaders.ShaderInfo;

public class SpriteGroupStrategy extends CameraGroupStrategy {
    private Camera camera;
    private ShaderInfo shader;
    private com.interrupt.dungeoneer.game.Game game;

    private ShortArray group_blend_mode = new ShortArray();
    private ArrayMap<Texture, Integer> opaque_groups = new ArrayMap<Texture, Integer>();
    private ArrayMap<Texture, Integer> blend_groups = new ArrayMap<Texture, Integer>();

    private int cur_group = 2;

    public SpriteGroupStrategy (final Camera camera, com.interrupt.dungeoneer.game.Game game, ShaderInfo shader, final int sortMod) {
    	super(camera, new Comparator<Decal>() {
			@Override
			public int compare (Decal o1, Decal o2) {
				float dist1 = camera.position.dst(o2.getPosition());
				float dist2 = camera.position.dst(o1.getPosition());
				return (int)Math.signum((dist2 - dist1) * sortMod);
			}
		});
    	
        this.camera = camera;
        this.game = game;
        this.shader = shader;
    }

    @Override
    public void beforeGroups () {
    	if (shader != null) {
	        shader.begin();
    	}
    }

    @Override
    public int decideGroup (Decal decal) {
        TextureRegion tr = decal.getTextureRegion();
        if(tr != null) {
            Texture texture = tr.getTexture();
            if(texture != null) {
                boolean isOpaque = decal.getMaterial().isOpaque();
                ArrayMap<Texture, Integer> groups = isOpaque ? opaque_groups : blend_groups;
                Integer group = groups.get(texture);

                if (group != null) {
                    return group;
                }
                else {
                    groups.put(texture, cur_group);
                    group_blend_mode.add(isOpaque ? 0 : 1);
                    return cur_group++;
                }
            }
        }

        return super.decideGroup(decal);
    }

    @Override
    public void beforeGroup (int group, Array<Decal> contents) {
        super.beforeGroup(group, contents);

        if(blend_groups.size > 0)
            GlRenderer.EnableBlending(blend_groups.containsValue(group, false));
        else
            GlRenderer.EnableBlending(false);

        if(shader != null && contents.size > 0) {
            Decal first = contents.first();
            Texture tex = first.getTextureRegion().getTexture();
            
            shader.begin();
            shader.setAttribute("u_tex_width", 1f / tex.getWidth());
            shader.setAttribute("u_tex_height", 1f / tex.getHeight());

            if(first instanceof DDecal) {
                TextureAtlas atlas = ((DDecal) first).getTextureAtlas();
                if(atlas != null) {
                    shader.setAttribute("u_sprite_columns", atlas.columns);
                    shader.setAttribute("u_sprite_rows", atlas.rows);
                }
            }
        }
    }

    @Override
    public void afterGroups () {
        if (shader != null) {
                shader.end();
        }
    }

    @Override
    public ShaderProgram getGroupShader (int group) {
        if(shader == null) return null;
        return shader.shader;
    }

}
