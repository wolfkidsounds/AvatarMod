package com.crowsofwar.avatar.common.bending.earth;

import com.crowsofwar.avatar.common.bending.Ability;
import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.ctx.AbilityContext;
import com.crowsofwar.avatar.common.entity.EntityEarthspikeSpawner;
import com.crowsofwar.gorecore.util.Vector;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;
import scala.collection.script.Start;

//import java.util.logging.Level;
// import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.FMLLog;

import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class AbilityEarthSpikes extends Ability {
    public double i = 0;
    public float damage = 0.90F;

    private Logger logger = LogManager.getLogger(this.getClass());

    public AbilityEarthSpikes() {
        super(Earthbending.ID, "earthspike");
    }

    @Override
    public void execute(AbilityContext ctx) {
        AbilityData abilityData = ctx.getData().getAbilityData(this);
        float xp = abilityData.getTotalXp();
        EntityLivingBase entity = ctx.getBenderEntity();
        World world = ctx.getWorld();
        Bender bender = ctx.getBender();
        if (i >= 8){
            i = 0;
        }

        float chi = STATS_CONFIG.chiEarthspike;
        if (ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST)) {
            chi *= 2.5f;
            damage = 1.25f;
        }
        if (ctx.getLevel() == 1 || ctx.getLevel() == 2) {
            chi *= 1.5f;
            damage = 1F;
        }
        if (ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND)) {
            chi *= 2f;
            damage = 1.5F;
        }

        if (bender.consumeChi(chi)) {
            EntityEarthspikeSpawner earthspike = new EntityEarthspikeSpawner(world);
            Vector look = Vector.toRectangular(Math.toRadians(entity.rotationYaw), 0);
            double mult = ctx.getLevel() >= 1 ? 14 : 8;
            // Vector speed =(look.times(mult));
            double speed = 3;

            if (abilityData.getLevel() == 1) {
                earthspike.setVelocity(look.times(mult * 5));

            }
            if (abilityData.isMasterPath(AbilityData.AbilityTreePath.SECOND)) {
                earthspike.setVelocity(look.times(mult * 10));
            }

            //  Double angle = i * 45;
            // Vector direction =  Vector.toRectangular(Math.toRadians(entity.rotationYaw + i*45), Math.toRadians(entity.rotationPitch));
            // Vector velocity = direction.times(speed);

            earthspike.setOwner(entity);
            earthspike.setPosition(entity.posX, entity.posY, entity.posZ);
            earthspike.setVelocity(look.times(mult));
            earthspike.setDamageMult(damage + xp / 100);
            earthspike.setDistance(ctx.getLevel() >= 2 ? 16 : 10);
            earthspike.setBreakBlocks(ctx.isMasterLevel(AbilityData.AbilityTreePath.FIRST));
            earthspike.setDropEquipment(ctx.isMasterLevel(AbilityData.AbilityTreePath.SECOND));
            world.spawnEntity(earthspike);

            if (abilityData.isMasterPath(AbilityData.AbilityTreePath.FIRST)) {
                logger.warn("Inside condition");
                for (double i = 0; i < 8; i++) {
                    Vector direction1 = Vector.toRectangular(Math.toRadians(entity.rotationYaw + i*45), Math.toRadians(entity.rotationPitch));
                    Vector velocity = direction1.times(speed);
                    EntityEarthspikeSpawner spawner = new EntityEarthspikeSpawner(world);
                    spawner.setVelocity(velocity);
                    world.spawnEntity(spawner);
                    logger.warn("Spawn: "+i);
                    logger.warn("direction: "+direction1.toString());
                }
            }
            else {
                logger.warn("not in condition");
            }

        }
    }
}