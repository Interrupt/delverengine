package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.interrupt.dungeoneer.collision.Collision;
import com.interrupt.dungeoneer.collision.Collision.CollisionType;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Entity.ArtType;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.gfx.drawables.DrawableMesh;

public class CachePools {
	
	// cache for numbers
	protected static Array<String> commonNumbers = new Array<String>();
	
	public static String toString(int number) {
		if(number < commonNumbers.size) {
			return commonNumbers.get(number);
		}
		return number + "";
	}
	
	static {
		for(int i = 0; i < 200; i++) {
			commonNumbers.add(i + "");
		}
	}
	
	// pool for Vector3s
    public static Pool<Vector3> vector3Pool = new Pool<Vector3>(32) {
    	@Override
        protected Vector3 newObject () {
                return new Vector3();
        }
    };
    protected static Array<Vector3> usedVectors = new Array<Vector3>();
    
    // pool for Particles
    protected static Pool<Particle> particles = new Pool<Particle>(64, 1024) {
    	@Override
        protected Particle newObject () {
                return new Particle();
        }
    };
    protected static Array<Particle> usedParticles = new Array<Particle>();
    
    protected static Pool<Color> colorPool = new Pool<Color>(32) {
    	@Override
        protected Color newObject () {
                return new Color();
        }
    };
    protected static Array<Color> usedColors = new Array<Color>();
    
    protected static Pool<String> stringPool = new Pool<String>(32) {
    	@Override
        protected String newObject () {
                return new String();
        }
    };
    protected static Array<String> usedStrings = new Array<String>();
    
    protected static Pool<Collision> collisionPool = new Pool<Collision>(32) {
    	@Override
        protected Collision newObject () {
                return new Collision();
        }
    };
    protected static Array<Collision> usedCollisions = new Array<Collision>();
    
    public static Pool<BoundingBox> aabbPool = new Pool<BoundingBox>(16) {
    	@Override
        protected BoundingBox newObject () {
                return new BoundingBox();
        }
    };
    protected static Array<BoundingBox> usedAABB = new Array<BoundingBox>();

	public static Pool<DrawableMesh> meshPool = new Pool<DrawableMesh>(16) {
		@Override
		protected DrawableMesh newObject () {
			return new DrawableMesh();
		}
	};
	protected static Array<DrawableMesh> usedMeshes = new Array<DrawableMesh>();
    
    // Grab a particle
    public static Particle getParticle() {
    	Particle p = particles.obtain();
    	p.isActive = true;
    	p.tex = 0;
    	p.artType = ArtType.particle;
    	p.isSolid = false;
    	p.xa = 0;
    	p.ya = 0;
    	p.za = 0;
    	p.isDynamic = true;
    	p.collision.set(0.03f,0.03f,0.035f);
    	p.floating = false;
    	p.x = 0;
    	p.y = 0;
    	p.z = 0;
    	p.lifetime = 20 + Game.rand.nextInt(160);
    	p.checkCollision = true;
    	p.color.set(1f, 1f, 1f, 1f);
    	p.scale = 1;
    	p.startScale = 1;
    	p.endScale = 1;
    	p.yOffset = 0f;
    	p.stopAnimation();
    	p.physicsSleeping = false;
    	p.initialized = false;
		p.roll = 0;
		p.rotateAmount = 0;
		p.movementRotateAmount = 0f;
		p.haloMode = Entity.HaloMode.NONE;
		p.shadowType = Entity.ShadowType.NONE;
		p.drawDistance = Entity.DrawDistance.FAR;
		p.turbulenceMoveModifier = 0f;
		p.turbulenceAmount = 0f;
		p.blendMode = Entity.BlendMode.OPAQUE;
		p.shader = null;
		p.endColor = null;

		if(p.drawable != null) {
			p.spriteAtlas = "particle";
			p.drawable.refresh();
		}
    	
    	usedParticles.add(p);
    	
    	return p;
    }

	public static Particle getParticle(float x, float y, float z, String spriteAtlas, int tex) {
		Particle p = getParticle(x, y, z, tex);
		p.setSpriteAtlas(spriteAtlas);
		return p;
	}
    
    public static Particle getParticle(float x, float y, float z, int tex) {
    	Particle p = getParticle();
    	p.x = x;
    	p.y = y;
    	p.z = z;
    	p.tex = tex;
    	
    	return p;
    }
    
    public static Particle getParticle(float x, float y, float z, float xv, float yv, float zv, int tex, Color c, boolean fullBright) {
    	Particle p = getParticle();
    	p.x = x;
    	p.y = y;
    	p.z = z;
    	
    	p.xa = xv * 2;
		p.ya = yv * 2;
		p.za = zv * 2;
		
		p.tex = tex;
		p.color.set(c);
		p.fullbrite = fullBright;
		
    	return p;
    }
    
    public static Particle getParticle(float x, float y, float z, float xv, float yv, float zv, float lifetime, int tex, Color c, boolean fullBright) {
    	Particle p = getParticle(x, y, z, xv, yv, zv, tex, c, fullBright);
		p.lifetime = lifetime;
    	return p;
    }
    
    public static Particle getParticle(float x, float y, float z, float xv, float yv, float zv, float lifetime, float startScale, float endScale, int tex, Color c, boolean fullBright) {
    	Particle p = getParticle(x, y, z, xv, yv, zv, tex, c, fullBright);
		p.lifetime = lifetime;
		p.startScale = startScale;
		p.endScale = endScale;
    	return p;
    }
    
    public static void freeParticle(Particle p) {
    	if(usedParticles.contains(p, true))
    		particles.free(p);
    }
    
    public static Vector3 getVector3() {
    	synchronized (vector3Pool) {
			Vector3 v = vector3Pool.obtain();
			v.set(0, 0, 0);
			usedVectors.add(v);
			return v;
		}
    }
    
    public static Vector3 getVector3(float x, float y, float z) {
    	synchronized (vector3Pool) {
			Vector3 v = vector3Pool.obtain();
			v.set(x, y, z);
			usedVectors.add(v);
			return v;
		}
    }
    
    public static Vector3 getVector3(Vector3 toCopy) {
    	return getVector3(toCopy.x,toCopy.y,toCopy.z);
    }
    
    public static void freeVector3(Vector3 v) {
    	synchronized (vector3Pool) {
			if (usedVectors.contains(v, true))
				vector3Pool.free(v);
		}
    }
    
    public static Color getColor() {
    	Color c = colorPool.obtain();
    	c.set(0,0,0,1);
    	usedColors.add(c);
    	return c;
    }
    
    public static Color getColor(float r, float g, float b, float a) {
    	Color c = colorPool.obtain();
    	c.set(r,g,b,a);
    	usedColors.add(c);
    	return c;
    }
    
    public static Color getColor(Color c) {
    	return getColor(c.r,c.g,c.b,c.a);
    }
    
    public static void freeColor(Color c) {
    	colorPool.free(c);
    	usedColors.removeValue(c, true);
    }
    
    public static String getString() {
    	String str = stringPool.obtain();
    	usedStrings.add(str);
    	return str;
    }
    
    public static Collision getCollision(float x, float y, CollisionType colType) {
    	Collision c = collisionPool.obtain();
    	c.set(x, y, colType);
    	usedCollisions.add(c);
    	return c;
    }
    
    public static BoundingBox getAABB(Vector3 min, Vector3 max) {
    	BoundingBox b = aabbPool.obtain();
    	b.max.set(max);
    	b.min.set(min);
    	usedAABB.add(b);
    	return b;
    }
    
    public static BoundingBox getAABB(Entity e) {
    	if(e instanceof Player) {
			return getAABB(e, 0.5f, 0.5f, -0.5f);
		}
		else {
			return getAABB(e, 0f, 0f, -0.5f);
		}
    }

    private static Vector3 bbTempMin = new Vector3();
    private static Vector3 bbTempMax = new Vector3();
	public static BoundingBox getAABB(Entity e, float xOffset, float yOffset, float zOffset) {
		BoundingBox b = aabbPool.obtain();
		b.set(bbTempMin.set(e.x - e.collision.x + xOffset, e.z + zOffset, e.y - e.collision.y + yOffset), bbTempMax.set(e.x + e.collision.x + xOffset, e.z + e.collision.z + zOffset, e.y + e.collision.y + yOffset));
		usedAABB.add(b);
		return b;
	}

	public static DrawableMesh getMesh(String meshFile, float x, float y, float z, Vector3 scale, float rot) {
		DrawableMesh mesh = meshPool.obtain();
		usedMeshes.add(mesh);

		if(mesh.meshFile == null || !meshFile.equals(mesh.meshFile)) {
			mesh.meshFile = meshFile;
			mesh.loadedMesh = null;
		}

		mesh.x = x;
		mesh.y = y;
		mesh.z = z;
		mesh.setScaleVector(scale);
		mesh.rotX = 0f;
		mesh.rotY = 0f;
		mesh.rotZ = 0f;
		mesh.rotZ = rot;
		mesh.update();

		return mesh;
	}
    
    public static void clearOnTick() {
    	vector3Pool.freeAll(usedVectors);
    	usedVectors.clear();
    	
    	colorPool.freeAll(usedColors);
    	usedColors.clear();
    	
    	stringPool.freeAll(usedStrings);
    	usedStrings.clear();
    	
    	collisionPool.freeAll(usedCollisions);
    	usedCollisions.clear();
    	
    	aabbPool.freeAll(usedAABB);
    	usedAABB.clear();

		meshPool.freeAll(usedMeshes);
		usedMeshes.clear();
    }

	public static void freeMeshes() {
		meshPool.freeAll(usedMeshes);
		usedMeshes.clear();
	}

	public static void clearMeshPool() {
		meshPool.clear();
		usedMeshes.clear();
	}
}
