/* 
  This file is part of AvatarMod.
    
  AvatarMod is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  
  AvatarMod is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with AvatarMod. If not, see <http://www.gnu.org/licenses/>.
*/
package com.crowsofwar.avatar.common.bending.fire;

import com.crowsofwar.avatar.common.bending.BattlePerformanceScore;
import com.crowsofwar.avatar.common.bending.StatusControl;
import com.crowsofwar.avatar.common.damageutils.AvatarDamageSource;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.BendingContext;
import com.crowsofwar.avatar.common.entity.EntityShield;
import com.crowsofwar.avatar.common.particle.ParticleBuilder;
import com.crowsofwar.avatar.common.util.AvatarEntityUtils;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import com.crowsofwar.avatar.common.util.Raytrace;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;

import static com.crowsofwar.avatar.common.config.ConfigClient.CLIENT_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;
import static com.crowsofwar.gorecore.util.Vector.getEyePos;
import static java.lang.Math.toRadians;

/**
 * @author CrowsOfWar
 */
public class FireblastUpdateTick extends TickHandler {

	public FireblastUpdateTick(int id) {
		super(id);
	}

	@Override
	public boolean tick(BendingContext ctx) {
		BendingData data = ctx.getData();
		EntityLivingBase entity = ctx.getBenderEntity();
		Bender bender = ctx.getBender();
		World world = ctx.getWorld();
		AbilityData abilityData = data.getAbilityData("fire_blast");
		int duration = ctx.getData().getTickHandlerDuration(this);


		AbilityTreePath path = abilityData.getPath();

		int level = abilityData.getLevel();
		int flamesPerSecond;

		//TODO: Movement modifier


		flamesPerSecond = level <= 0 ? 6 : 10;
		if (level == 3 && path == AbilityTreePath.FIRST)
			flamesPerSecond = 10;
		else if (level == 3 && path == AbilityTreePath.SECOND)
			flamesPerSecond = 8;


		double powerRating = bender.calcPowerRating(Firebending.ID);

		float requiredChi = STATS_CONFIG.chiFlamethrowerSecond / flamesPerSecond;
		if (level == 3 && path == AbilityTreePath.FIRST) {
			requiredChi = STATS_CONFIG.chiFlamethrowerSecondLvl4_1 / flamesPerSecond;
		}
		if (level == 3 && path == AbilityTreePath.SECOND) {
			requiredChi = STATS_CONFIG.chiFlamethrowerSecondLvl4_2 / flamesPerSecond;
		}

		// Adjust chi to power rating
		// Multiply chi by a number (from 0..2) based on the power rating - powerFactor
		//  Numbers 0..1 would reduce the chi, while numbers 1..2 would increase the chi
		// maxPowerFactor: maximum amount that the chi can be multiplied by
		// e.g. 0.1 -> chi can be changed by 10%; powerFactor in between 0.9..1.1
		double maxPowerFactor = 0.4;
		double powerFactor = (powerRating + 100) / 100 * maxPowerFactor + 1 - maxPowerFactor;
		requiredChi *= powerFactor;

		if (bender.consumeChi(requiredChi)) {

			Vector eye = getEyePos(entity);
			boolean inWaterBlock = world.getBlockState(entity.getPosition()) instanceof BlockLiquid || world.getBlockState(entity.getPosition()).getBlock() == Blocks.WATER
					|| world.getBlockState(entity.getPosition()).getBlock() == Blocks.FLOWING_WATER;
			boolean headInLiquid = world.getBlockState(entity.getPosition().up()) instanceof BlockLiquid || world.getBlockState(entity.getPosition().up()).getBlock() == Blocks.WATER
					|| world.getBlockState(entity.getPosition().up()).getBlock() == Blocks.FLOWING_WATER;

			if (!world.isRaining() && !(headInLiquid || inWaterBlock)) {

				double speedMult = 15 + 5 * abilityData.getXpModifier();
				double randomness = 3.0 - 0.5 * (abilityData.getXpModifier() + Math.max(abilityData.getLevel(), 0));
				float range = 4;
				int fireTime = 0;
				float size = 1;
				float damage = 0.5F;
				float performanceAmount = 2;
				float xp = SKILLS_CONFIG.flamethrowerHit;
				float knockBack = 1;
				boolean lightsFires = false;

				switch (abilityData.getLevel()) {
					case 1:
						size = 1.5F;
						damage = 1F;
						fireTime = 2;
						range = 5;
						performanceAmount = 3;
						knockBack += 0.5;
						break;
					case 2:
						size = 2;
						fireTime = 4;
						damage = 3F;
						range = 7;
						performanceAmount = 5;
						knockBack += 1;
						break;
				}
				if (level == 3 && path == AbilityTreePath.FIRST) {
					speedMult = 38;
					randomness = 0;
					fireTime = 5;
					size = 1.25F;
					damage = 7F;
					range = 11;
					performanceAmount = 6;
					knockBack += 2;
				}
				if (level == 3 && path == AbilityTreePath.SECOND) {
					speedMult = 12;
					randomness = 9;
					fireTime = 20;
					size = 3.0F;
					damage = 2.5F;
					range = 6.5F;
					performanceAmount = 2;
					lightsFires = true;
					knockBack += 1;
				}

				// Affect stats by power rating
				range += powerFactor / 100F;
				size += powerRating / 100F;
				damage += powerRating / 100F;
				fireTime += (int) (powerRating / 50F);
				speedMult += powerRating / 100f * 2.5f;
				randomness = randomness >= powerRating / 100f * 2.5f ? randomness - powerRating / 100F * 2.5 : 0;
				randomness = randomness < 0 ? 0 : randomness;

				double yawRandom = entity.rotationYaw + (Math.random() * 2 - 1) * randomness;
				double pitchRandom = entity.rotationPitch + (Math.random() * 2 - 1) * randomness;
				Vector look = randomness == 0 ? Vector.getLookRectangular(entity) : Vector.toRectangular(toRadians(yawRandom), toRadians(pitchRandom));


				Vector start = look.plus(eye.minusY(0.5));

				List<Entity> hit = Raytrace.entityRaytrace(world, start, look, range + (int) speedMult / 10F, size / 2.2F, entity1 -> entity1 != entity);
				hit.remove(entity);
				if (!hit.isEmpty()) {
					for (Entity target : hit) {
						if (!world.isRemote) {
							if (target.canBeCollidedWith() && (target.getTeam() == null || target.getTeam() != null && target.getTeam() != entity.getTeam()) || target instanceof EntityShield) {
								boolean attack = target.attackEntityFrom(AvatarDamageSource.causeFlamethrowerDamage(target, entity), damage);
								if (attack) {
									target.setFire(fireTime + 2);
									target.setEntityInvulnerable(false);
									Vector knockback = look.times(speedMult / 100).times(knockBack);
									if (target.canBePushed())
										target.addVelocity(knockback.x(), knockback.y(), knockback.z());
									AvatarUtils.afterVelocityAdded(target);
									BattlePerformanceScore.addScore(entity, (int) performanceAmount);
									abilityData.addXp(xp);
								} else if (target instanceof EntityDragon)
									AvatarEntityUtils.attackDragon((EntityDragon) target, AvatarDamageSource.causeFlamethrowerDamage(target, entity), damage);
								BattlePerformanceScore.addScore(entity, (int) performanceAmount);
								abilityData.addXp(xp);

							}
						}
					}
				}

				Raytrace.Result result = Raytrace.raytrace(world, start.toMinecraft(), look.toMinecraft(), range, false);
				if (result.hitSomething() && result.getPos() != null && world.getBlockState(result.getPos().toBlockPos()).getBlock() != Blocks.AIR) {
					BlockPos pos = result.getPos().toBlockPos();
					if (lightsFires)
						if (Blocks.FIRE.canPlaceBlockAt(world, pos) && !world.getBlockState(pos).isFullBlock() && !(world.getBlockState(pos) instanceof BlockLiquid))
							world.setBlockState(pos, Blocks.FIRE.getDefaultState());

				}


				//Particle code.
				if (world.isRemote) {
					if (CLIENT_CONFIG.fireRenderSettings.useFlamethrowerParticles)
						ParticleBuilder.create(ParticleBuilder.Type.FIRE).pos(start.toMinecraft()).scale(size).time(25).collide(true).vel(look.times(speedMult / 30).toMinecraft()).spawn(world);
					for (double i = 0; i < flamesPerSecond; i += 1) {
						Vector start1 = look.times(i / (double) flamesPerSecond).plus(eye.minusY(0.5));
						ParticleBuilder.create(ParticleBuilder.Type.FLASH).pos(start1.toMinecraft()).time(20 + AvatarUtils.getRandomNumberInRange(0, 5)).vel(look.times(speedMult / 30).toMinecraft()).
								clr(255, 60 + AvatarUtils.getRandomNumberInRange(0, 70), 40).collide(true).scale(size).spawn(world);
						if (!CLIENT_CONFIG.fireRenderSettings.useFlamethrowerParticles) {
							ParticleBuilder.create(ParticleBuilder.Type.FLASH).pos(start1.toMinecraft()).time(20 + AvatarUtils.getRandomNumberInRange(0, 5)).vel(look.times(speedMult / 30).toMinecraft()).
									clr(235 + AvatarUtils.getRandomNumberInRange(0, 20), 60, 40).collide(true).scale(size).spawn(world);
							ParticleBuilder.create(ParticleBuilder.Type.FLASH).pos(start1.toMinecraft()).time(20 + AvatarUtils.getRandomNumberInRange(0, 5)).vel(look.times(speedMult / 30).toMinecraft()).
									clr(255, 193 + AvatarUtils.getRandomNumberInRange(1, 60), 40).collide(true).scale(size).spawn(world);
						}
					}
				}

					world.playSound(null, entity.getPosition(), SoundEvents.ITEM_FIRECHARGE_USE,
							SoundCategory.PLAYERS, 0.2f, 0.8f);


			} else {
				if (world.isRemote) {
					for (int i = 0; i < 5; i++)
						ParticleBuilder.create(ParticleBuilder.Type.SNOW).collide(true).time(15).vel(world.rand.nextGaussian() / 50, world.rand.nextGaussian() / 50, world.rand.nextGaussian() / 50)
								.scale(1.5F + abilityData.getLevel() / 2F).pos(Vector.getEyePos(entity).plus(Vector.getLookRectangular(entity)).toMinecraft()).clr(0.75F, 0.75F, 0.75f).spawn(world);

				}
				Vector pos = Vector.getEyePos(entity).plus(Vector.getLookRectangular(entity));
				if (!world.isRemote && world instanceof WorldServer) {
					WorldServer World = (WorldServer) world;
					World.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, pos.x(), pos.y(), pos.z(), 3 + Math.max(abilityData.getLevel(), 0),
							0, 0, 0, 0.0015);
				}
				entity.world.playSound(null, new BlockPos(entity), SoundEvents.BLOCK_FIRE_EXTINGUISH, entity.getSoundCategory(),
						1.0F, 0.8F + world.rand.nextFloat() / 10);
				//makes sure the tick handler is removed
				return true;
			}


		} else
			// not enough chi
			return true;
		return duration > 4;
	}

}