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
package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.BendingAi;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import com.crowsofwar.avatar.common.entity.EntityAirblade;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;
import static com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath.FIRST;
import static com.crowsofwar.avatar.common.data.AbilityData.AbilityTreePath.SECOND;
import static java.lang.Math.abs;

/**
 * @author CrowsOfWar
 */
public class AbilityAirblade extends Ability {

	public AbilityAirblade() {
		super(Airbending.ID, "airblade");
	}

	@Override
	public void execute(AbilityContext ctx) {

		EntityLivingBase entity = ctx.getBenderEntity();
		Bender bender = ctx.getBender();
		World world = ctx.getWorld();
		BendingData data = ctx.getData();

		if (!bender.consumeChi(STATS_CONFIG.chiAirblade)) return;

		double pitchDeg = entity.rotationPitch;
		if (abs(pitchDeg) > 30) {
			pitchDeg = pitchDeg / abs(pitchDeg) * 30;
		}
		float pitch = (float) Math.toRadians(pitchDeg);

		Vector look = Vector.toRectangular(Math.toRadians(entity.rotationYaw), pitch);
		Vector spawnAt = Vector.getEntityPos(entity).plus(look.times(2)).plus(0, 1, 0);

		AbilityData abilityData = ctx.getData().getAbilityData(this);
		float xp = abilityData.getTotalXp();

		float damage = STATS_CONFIG.airbladeSettings.damage;
		damage *= 1 + xp * .015f;
		damage *= ctx.getPowerRatingDamageMod();

		EntityAirblade airblade = new EntityAirblade(world);
		airblade.setPosition(spawnAt.x(), spawnAt.y(), spawnAt.z());
		airblade.setVelocity(look.times(ctx.getLevel() >= 1 ? 30 : 20));
		airblade.setDamage(damage);
		airblade.setOwner(entity);
		airblade.setAbility(this);
		airblade.setPierceArmor(abilityData.isMasterPath(SECOND));
		airblade.setChainAttack(abilityData.isMasterPath(FIRST));

		float chopBlocks = -1;
		if (abilityData.getLevel() >= 1) {
			chopBlocks = 0;
		}
		if (abilityData.isMasterPath(SECOND)) {
			chopBlocks = 2;
		}
		airblade.setChopBlocksThreshold(chopBlocks);

		world.spawnEntity(airblade);

	}

	@Override
	public int getCooldown(AbilityContext ctx) {
		EntityLivingBase entity = ctx.getBenderEntity();

		int coolDown = 100;

		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			coolDown = 0;
		}

		if (ctx.getLevel() == 1) {
			coolDown = 80;
		}
		if (ctx.getLevel() == 2) {
			coolDown = 60;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
			coolDown = 40;
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
			coolDown = 35;
		}
		return coolDown;
	}

	@Override
	public BendingAi getAi(EntityLiving entity, Bender bender) {
		return new AiAirblade(this, entity, bender);
	}

}
