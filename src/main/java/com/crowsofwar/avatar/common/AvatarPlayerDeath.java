package com.crowsofwar.avatar.common;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.crowsofwar.avatar.AvatarInfo;
import com.crowsofwar.avatar.common.analytics.*;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.entity.mob.EntityBender;

/**
 * @author CrowsOfWar
 */
@Mod.EventBusSubscriber(modid = AvatarInfo.MODID)
public class AvatarPlayerDeath {

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent e) {

		EntityLivingBase died = e.getEntityLiving();
		if (died instanceof EntityPlayer) {

			Bender bender = Bender.get(died);
			//noinspection ConstantConditions
			bender.onDeath();

			sendDeathAnalytic(e);

		}

	}

	/**
	 * Possibly sends analytics for the player being killed by PvP or Av2 entity.
	 */
	private static void sendDeathAnalytic(LivingDeathEvent e) {

		if (!e.getEntity().world.isRemote) {
			DamageSource source = e.getSource();
			Entity causeEntity = source.getTrueSource();

			if (causeEntity instanceof EntityPlayer) {
				if (AvatarDamageSource.isAvatarDamageSource(source)) {
					// Chop off initial "avatar_" from the damage source name
					String dsName = source.getDamageType().substring("avatar_".length());
					AvatarAnalytics.INSTANCE.pushEvent(AnalyticEvents.onPvpKillWithAbility(dsName));
				}
			}

			if (causeEntity instanceof EntityBender) {
				String mobName = EntityList.getEntityString(causeEntity);
				AvatarAnalytics.INSTANCE.pushEvent(AnalyticEvents.onPlayerDeathWithMob(mobName));
			}

		}

	}

}
