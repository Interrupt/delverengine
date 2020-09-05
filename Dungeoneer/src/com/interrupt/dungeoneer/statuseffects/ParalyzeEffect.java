package com.interrupt.dungeoneer.statuseffects;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.managers.StringManager;

public class ParalyzeEffect extends StatusEffect {
    public Particle effectParticle;
    public Vector3 effectOffset = new Vector3(0.0f, 0.0f, 0.55f);
    public boolean wasOwnerFloating = false;

    public ParalyzeEffect() {
        this(500);
    }

    public ParalyzeEffect(int time) {
        this.name = StringManager.get("statuseffects.ParalyzeEffect.defaultNameText");
        this.speedMod = 0.0f;
        this.timer = time;
        this.statusEffectType = StatusEffectType.PARALYZE;

        this.effectParticle = CachePools.getParticle();
        this.effectParticle.floating = true;
        this.effectParticle.lifetime = time;
        this.effectParticle.fullbrite = true;
        this.effectParticle.startScale = 1.0f;
        this.effectParticle.endScale = 1.0f;
        this.effectParticle.checkCollision = false;
        this.effectParticle.playAnimation(72, 79, 20.0f, true);
        Game.GetLevel().SpawnNonCollidingEntity(this.effectParticle);
    }

    @Override
    public void doTick(Actor owner, float delta) {
        this.effectParticle.x = owner.x + this.effectOffset.x;
        this.effectParticle.y = owner.y + this.effectOffset.y;
        this.effectParticle.z = owner.z + this.effectOffset.z;

        if(owner instanceof Player) {
            this.effectParticle.x += Game.camera.direction.x * 0.25f;
            this.effectParticle.y += Game.camera.direction.z * 0.25f;
        }
    }

    @Override
    public void onStatusBegin(Actor owner) {
        this.wasOwnerFloating = owner.floating;
        owner.floating = false;

        int impactParticleCount = Game.rand.nextInt(3) + 2;
        Vector3 cameraRight = Game.camera.direction.crs(new Vector3(0,1,0)).nor();

        if (!this.showParticleEffect) {
            return;
        }

        Vector3 particleDirection = CachePools.getVector3();

        for (int i = 0; i < impactParticleCount; i++) {
            Particle p = CachePools.getParticle();
            p.tex = 71;
            p.lifetime = 45;
            p.scale = 0.75f;
            p.startScale = 1.0f;
            p.endScale = 0.125f;
            p.fullbrite = true;
            p.rotateAmount = -4.5f;
            p.checkCollision = false;

            p.x = owner.x + this.effectOffset.x;
            p.y = owner.y + this.effectOffset.y;
            p.z = owner.z + this.effectOffset.z;

            particleDirection.set(cameraRight);
            particleDirection.scl(Game.rand.nextFloat() * 0.0125f + 0.0125f);

            if (i % 2 == 0) {
                particleDirection.scl(-1.0f);
                p.rotateAmount = 4.5f;
            }

            p.xa = particleDirection.x;
            p.ya = particleDirection.z;
            p.za = Game.rand.nextFloat() * 0.05f + 0.025f;

            Game.GetLevel().SpawnNonCollidingEntity(p);
        }

        CachePools.freeVector3(particleDirection);
    }

    @Override
    public void forPlayer(Player player) {
        timer *= 0.5;
    }

    @Override
    public void onStatusEnd(Actor owner) {
        this.active = false;
        this.effectParticle.lifetime = 0;
        CachePools.freeParticle(this.effectParticle);

        owner.floating = this.wasOwnerFloating;
    }
}
