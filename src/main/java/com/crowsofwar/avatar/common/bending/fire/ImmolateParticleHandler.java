package com.crowsofwar.avatar.common.bending.fire;

import com.crowsofwar.avatar.common.AvatarParticles;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.particle.NetworkParticleSpawner;
import com.crowsofwar.avatar.common.particle.ParticleSpawner;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.Random;


public class ImmolateParticleHandler extends TickHandler {
	private final ParticleSpawner particles;

	public ImmolateParticleHandler(int id) {
		super(id);
		particles = new NetworkParticleSpawner();
	}

	@Override
	public boolean tick(BendingContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData data = ctx.getData();
		AbilityData aD = data.getAbilityData("immolate");
		World world = ctx.getWorld();
		int duration = data.getTickHandlerDuration(this);
		int immolateDuration = aD.getLevel() > 0 ? 60 + 40 * aD.getLevel() : 40;
		//The particles take a while to disappear after the ability finishes- so you decrease the time the particles can spawn
		Random rand = new Random();
		double r = rand.nextDouble();
		if (!world.isRemote) {
			for (int i = 0; i < 12; i++) {
				int random = rand.nextInt(2) + 1;
				r = random == 1 ? r : r * -1;
				Vector location = Vector.toRectangular(Math.toRadians(entity.rotationYaw + (i * 30) + (r * 2)), 0).times(0.5).withY(entity.getEyeHeight() - 0.7);
				particles.spawnParticles(world, AvatarParticles.getParticleFlames(), 1, 1, location.plus(Vector.getEntityPos(entity)),
						new Vector(0.8, 4, 0.8));
			}
		}
		return duration >= immolateDuration;
	}
}
