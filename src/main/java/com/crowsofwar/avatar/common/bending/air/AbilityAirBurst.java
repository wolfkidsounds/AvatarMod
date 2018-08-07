package com.crowsofwar.avatar.common.bending.air;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.data.TickHandler;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.UUID;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class AbilityAirBurst extends Ability {

	public AbilityAirBurst() {
		super(Airbending.ID, "air_burst");
	}

	@Override
	public void execute(AbilityContext ctx) {
		Bender bender = ctx.getBender();
		EntityLivingBase entity = ctx.getBenderEntity();
		BendingData data = ctx.getData();

		float chi = 6;
		//6
		boolean hasAirCharge = data.hasTickHandler(TickHandler.AIRBURST_CHARGE_HANDLER);

		if (ctx.getLevel() == 1) {
			chi = 7;
		}

		if (ctx.getLevel() == 2) {
			chi = 9;
			//7
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
			chi = STATS_CONFIG.chiWaterCannon * 1.6F;
			//11
		}
		if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
			chi = STATS_CONFIG.chiWaterCannon * 1.4F;
			//11
		}


		if (bender.consumeChi(chi) && !hasAirCharge) {
			data.addTickHandler(TickHandler.AIRBURST_CHARGE_HANDLER);
		} else if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) {
			if (!hasAirCharge) {
				data.addTickHandler(TickHandler.AIRBURST_CHARGE_HANDLER);
			}
		}
	}
}