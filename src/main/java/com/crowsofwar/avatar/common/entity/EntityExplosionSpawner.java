package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.config.ConfigStats;
import com.crowsofwar.avatar.common.data.BendingData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.crowsofwar.avatar.common.config.ConfigSkills.SKILLS_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigStats.STATS_CONFIG;

public class EntityExplosionSpawner extends AvatarEntity {
        private boolean unstoppable;

        private double maxTicksAlive;
        private float explosionStrength;
        private float frequency;

        /**
         * @param world
         */
        public EntityExplosionSpawner(World world) {
            super(world);
            setSize(1, 1);
            isImmuneToExplosions();
            isInvisible();

        }

        public void isUnstoppable (boolean isUnstoppable) {
            this.unstoppable = isUnstoppable;
        }
        public void maxTicks (float ticks) {this.maxTicksAlive = ticks;}
        public void setExplosionStrength (float explosionStrength) {this.explosionStrength = explosionStrength;}
        public void setExplosionFrequency (float explosionFrequency) {this.frequency = explosionFrequency;}

        @Override
        protected void readEntityFromNBT(NBTTagCompound nbt) {
            super.readEntityFromNBT(nbt);
        }

        @Override
        protected void writeEntityToNBT(NBTTagCompound nbt) {
            super.writeEntityToNBT(nbt);
            setDead();
        }

        @Override
        public void onUpdate() {
            super.onUpdate();

            if (!world.isRemote && ticksExisted >= maxTicksAlive) {
                setDead();
            }
            if(this.isDead){
                Thread.dumpStack();
            }

            BlockPos below = getPosition().offset(EnumFacing.DOWN);
            Block belowBlock = world.getBlockState(below).getBlock();

            if (ticksExisted % 3 == 0) world.playSound(posX, posY, posZ,
                    world.getBlockState(below).getBlock().getSoundType().getBreakSound(), SoundCategory.PLAYERS, 1, 1, false);

            float explosionSize = STATS_CONFIG.explosionSettings.explosionSize * explosionStrength;
            explosionSize += getPowerRating() * 2.0 / 100;
                    if (ticksExisted >=5 && ticksExisted % frequency == 0) {
                      world.createExplosion(this, this.posX, this.posY, this.posZ, explosionSize, false);
                    }

            if (!world.getBlockState(below).isNormalCube()) {
                setDead();
            }

            if (!world.isRemote && !ConfigStats.STATS_CONFIG.bendableBlocks.contains(belowBlock) && !unstoppable) {
                setDead();
            }

            // Destroy if in a block
            IBlockState inBlock = world.getBlockState(getPosition());
            if (inBlock.isFullBlock()) {
                setDead();
            }

            // Destroy non-solid blocks in the spawner
            if (inBlock.getBlock() != Blocks.AIR && !inBlock.isFullBlock()) {

                if (inBlock.getBlockHardness(world, getPosition()) == 0) {

                    breakBlock(getPosition());

                } else {

                    setDead();
                }
            }

            // amount of entities which were successfully attacked
            int attacked = 0;

            // Push collided entities back
        }

        @Override
        protected boolean canCollideWith(Entity entity) {
            return !(entity instanceof EntityExplosionSpawner) && (entity instanceof EntityLivingBase || super.canCollideWith(entity));

        }

         @Override
        public boolean onCollideWithSolid() {
              setDead();
            return false;
        }


    }

