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
package com.crowsofwar.avatar.common.entity.mob;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static net.minecraft.util.EnumParticleTypes.HEART;
import static net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL;

import java.util.UUID;

import javax.annotation.Nullable;

import com.crowsofwar.avatar.common.bending.BendingAbility;
import com.crowsofwar.avatar.common.data.ctx.BenderInfo;
import com.crowsofwar.avatar.common.data.ctx.NoBenderInfo;
import com.crowsofwar.avatar.common.entity.data.AnimalCondition;
import com.crowsofwar.avatar.common.entity.data.OwnerAttribute;
import com.crowsofwar.avatar.common.util.AvatarDataSerializers;
import com.crowsofwar.gorecore.util.AccountUUIDs;
import com.crowsofwar.gorecore.util.Vector;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/**
 * EntityGhast EntityTameable
 * 
 * @author CrowsOfWar
 */
public class EntitySkyBison extends EntityBender implements IEntityOwnable {
	
	private static final DataParameter<BenderInfo> SYNC_OWNER = EntityDataManager
			.createKey(EntitySkyBison.class, AvatarDataSerializers.SERIALIZER_BENDER);
	
	private static final DataParameter<Boolean> SYNC_SITTING = EntityDataManager
			.createKey(EntitySkyBison.class, DataSerializers.BOOLEAN);
	
	private static final DataParameter<Float> SYNC_FOOD = EntityDataManager.createKey(EntitySkyBison.class,
			DataSerializers.FLOAT);
	
	private final OwnerAttribute ownerAttr;
	private Vector originalPos;
	private final AnimalCondition condition;
	
	/**
	 * @param world
	 */
	public EntitySkyBison(World world) {
		super(world);
		moveHelper = new SkyBisonMoveHelper(this);
		ownerAttr = new OwnerAttribute(this, SYNC_OWNER);
		condition = new AnimalCondition(this, 30, SYNC_FOOD);
		setSize(3, 2);
	}
	
	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_SITTING, false);
		dataManager.register(SYNC_FOOD, 20f);
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(2);
	}
	
	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
		
		this.tasks.addTask(1, BendingAbility.ABILITY_AIR_BUBBLE.getAi(this, this));
		this.tasks.addTask(2, BendingAbility.ABILITY_AIR_GUST.getAi(this, this));
		this.tasks.addTask(3, BendingAbility.ABILITY_AIRBLADE.getAi(this, this));
		
		this.tasks.addTask(4, new EntityAiBisonSit(this));
		this.tasks.addTask(5, new EntityAiBisonFollowOwner(this));
		this.tasks.addTask(6, new EntityAiBisonWander(this));
		
	}
	
	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
			@Nullable IEntityLivingData livingdata) {
		
		originalPos = Vector.getEntityPos(this);
		return super.onInitialSpawn(difficulty, livingdata);
		
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		originalPos = Vector.readFromNbt(nbt);
		ownerAttr.load(nbt);
		setSitting(nbt.getBoolean("Sitting"));
		condition.readFromNbt(nbt);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		originalPos.writeToNbt(nbt);
		ownerAttr.save(nbt);
		nbt.setBoolean("Sitting", isSitting());
		condition.writeToNbt(nbt);
	}
	
	// ================================================================================
	// GETTERS AND SETTERS
	// ================================================================================
	
	public Vector getOriginalPos() {
		return originalPos;
	}
	
	@Override
	public UUID getOwnerId() {
		return ownerAttr.getId();
	}
	
	public void setOwnerId(UUID id) {
		ownerAttr.setOwnerInfo(id == null ? new NoBenderInfo() : new BenderInfo(true, id));
	}
	
	public boolean hasOwner() {
		return getOwnerId() != null;
	}
	
	@Override
	public EntityPlayer getOwner() {
		return (EntityPlayer) ownerAttr.getOwner();
	}
	
	public void setOwner(EntityPlayer owner) {
		ownerAttr.setOwner(owner);
	}
	
	public boolean isSitting() {
		return dataManager.get(SYNC_SITTING);
	}
	
	public void setSitting(boolean sitting) {
		dataManager.set(SYNC_SITTING, sitting);
	}
	
	public double getFlySpeedMultiplier() {
		return condition.getSpeedMultiplier();
	}
	
	// ================================================================================
	// ENTITY LOGIC
	// ================================================================================
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		condition.onUpdate();
		if (condition.getFoodPoints() == 0) {
			// setSitting(true);
		} else if (!hasOwner()) {
			// setSitting(false);
		}
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		
		if (stack.getItem() == Items.APPLE && !hasOwner()) {
			System.out.println("Tame");
			playTameEffect(true);
			setOwnerId(AccountUUIDs.getId(player.getName()).getUUID());
			return true;
		}
		
		if (stack.getItem() == Items.REDSTONE && hasOwner()) {
			playTameEffect(false);
			System.out.println("Untame");
			setOwnerId(null);
			return true;
		}
		
		if (stack.getItem() instanceof ItemFood) {
			System.out.println("Consume some food!");
			ItemFood food = (ItemFood) stack.getItem();
			condition.addFood(food.getHealAmount(stack));
			return true;
		}
		
		if (!player.isSneaking()) {
			player.startRiding(this);
			return true;
		}
		
		if (player.isSneaking() && getOwner() == player) {
			setSitting(!isSitting());
			return true;
		}
		
		return super.processInteract(player, hand);
		
	}
	
	@Override
	public void updatePassenger(Entity passenger) {
		
		int index = getPassengers().indexOf(passenger);
		
		if (index > -1) {
			
			double angle = index * Math.PI;
			
			passenger.setPosition(posX + sin(angle), posY + getMountedYOffset() + passenger.getYOffset(),
					posZ + cos(angle));
			
		}
		
	}
	
	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return getPassengers().size() < 2;
	}
	
	@Override
	public Entity getControllingPassenger() {
		if (getPassengers().contains(getOwner())) {
			return getOwner();
		} else {
			return null;
		}
	}
	
	// ================================================================================
	// COPIED FROM ENTITYFLYING
	// ================================================================================
	
	@Override
	public void fall(float distance, float damageMultiplier) {}
	
	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {}
	
	@Override
	public void moveEntityWithHeading(float strafe, float forward) {
		if (this.isInWater()) {
			this.moveRelative(strafe, forward, 0.02F);
			this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929D;
			this.motionY *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		} else if (this.isInLava()) {
			this.moveRelative(strafe, forward, 0.02F);
			this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		} else {
			float f = 0.91F;
			
			if (this.onGround) {
				f = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.posX),
						MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1,
						MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.91F;
			}
			
			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, forward, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;
			
			if (this.onGround) {
				f = this.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(this.posX),
						MathHelper.floor_double(this.getEntityBoundingBox().minY) - 1,
						MathHelper.floor_double(this.posZ))).getBlock().slipperiness * 0.91F;
			}
			
			this.moveEntity(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= f;
			this.motionY *= f;
			this.motionZ *= f;
		}
		
		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.posX - this.prevPosX;
		double d0 = this.posZ - this.prevPosZ;
		float f2 = MathHelper.sqrt_double(d1 * d1 + d0 * d0) * 4.0F;
		
		if (f2 > 1.0F) {
			f2 = 1.0F;
		}
		
		this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
	}
	
	@Override
	public boolean isOnLadder() {
		return false;
	}
	
	// ================================================================================
	// COPIED FROM ENTITYTAMEABLE
	// ================================================================================
	
	protected void playTameEffect(boolean success) {
		EnumParticleTypes particle = success ? HEART : SMOKE_NORMAL;
		
		for (int i = 0; i < 7; i++) {
			double mx = this.rand.nextGaussian() * 0.02D;
			double my = this.rand.nextGaussian() * 0.02D;
			double mz = this.rand.nextGaussian() * 0.02D;
			this.worldObj.spawnParticle(particle,
					this.posX + this.rand.nextFloat() * this.width * 2.0F - this.width,
					this.posY + 0.5D + this.rand.nextFloat() * this.height,
					this.posZ + this.rand.nextFloat() * this.width * 2.0F - this.width, //
					mx, my, mz, new int[0]);
		}
		
	}
	
}
