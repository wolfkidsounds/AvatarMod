package com.crowsofwar.avatar.common.damageutils;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.bending.BattlePerformanceScore;
import com.crowsofwar.avatar.common.data.AbilityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.util.DamageSource;

public class DamageUtils {

	//Handles dragon damage; I recommend using this to avoid weird shenanigans with ender dragons. EntityOffensive has this built in.
	public static void attackEntity(EntityLivingBase attacker, Entity hit, DamageSource source, float damage, int performance, Ability ability, float xp) {
		if (hit != null && attacker != null && ability != null) {
			boolean ds = hit.attackEntityFrom(source, damage);
			AbilityData data = AbilityData.get(attacker, ability.getName());
			if (!ds && hit instanceof EntityDragon) {
				((EntityDragon) hit).attackEntityFromPart(((EntityDragon) hit).dragonPartBody, source,
						damage);
				BattlePerformanceScore.addScore(attacker, performance);
				data.addXp(xp);
			} else if (hit instanceof EntityLivingBase && ds) {
				BattlePerformanceScore.addScore(attacker, performance);
				data.addXp(xp);
			}
		}
	}
}