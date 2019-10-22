package com.crowsofwar.avatar.common.entity;

import com.crowsofwar.avatar.common.AvatarParticles;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class EntityOffensive extends AvatarEntity implements IOffensiveEntity {

	//Used for all entities that damage things
	private static final DataParameter<Float> SYNC_DAMAGE = EntityDataManager
			.createKey(EntityOffensive.class, DataSerializers.FLOAT);
	private static final DataParameter<Integer> SYNC_LIFETIME = EntityDataManager
			.createKey(EntityOffensive.class, DataSerializers.VARINT);
	private static final DataParameter<Float> SYNC_HEIGHT = EntityDataManager
			.createKey(EntityOffensive.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> SYNC_WIDTh = EntityDataManager
			.createKey(EntityOffensive.class, DataSerializers.FLOAT);

	private AxisAlignedBB expandedHitbox;
	private float xp;
	private int fireTime;
	private int performanceAmount;
	private Vec3d knockbackMult;
	private int ticks = 0;


	public EntityOffensive(World world) {
		super(world);
		this.expandedHitbox = getExpandedHitbox(this);
		this.performanceAmount = 20;
		this.fireTime = 3;
		this.xp = 3;
	}

	public float getHeight() {
		return dataManager.get(SYNC_HEIGHT);
	}

	public float getWidth() {
		return dataManager.get(SYNC_WIDTh);
	}

	public float getAvgSize() {
		if (getHeight() == getWidth()) {
			return getHeight();
		} else return (getHeight() + getWidth()) / 2;
	}

	public void setEntitySize(float height, float width) {
		dataManager.set(SYNC_HEIGHT, height);
		dataManager.set(SYNC_WIDTh, width);
	}

	public void setEntitySize(float size) {
		dataManager.set(SYNC_HEIGHT, size);
		dataManager.set(SYNC_WIDTh, size);
	}

	public float getDamage() {
		return dataManager.get(SYNC_DAMAGE);
	}

	public void setDamage(float damage) {
		dataManager.set(SYNC_DAMAGE, damage);
	}

	//This just makes the methods easier to use.
	public void Explode() {
		Explode(world, this, getOwner());
	}

	public void applyPiercingCollision() {
		applyPiercingCollision(this);
	}

	public void Dissipate() {
		Dissipate(this);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(SYNC_DAMAGE, 1F);
		dataManager.register(SYNC_LIFETIME, 100);
		dataManager.register(SYNC_WIDTh, 1.0F);
		dataManager.register(SYNC_HEIGHT, 1.0F);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!world.isRemote) {
			List<Entity> targets = world.getEntitiesWithinAABB(Entity.class, getExpandedHitbox());
			if (!targets.isEmpty()) {
				for (Entity hit : targets) {
					if (canDamageEntity(hit) && this != hit) {
						onCollideWithEntity(hit);
					}
				}
			}
		}
		if (noClip && (shouldExplode() || shouldDissipate())) {
			IBlockState state = world.getBlockState(getPosition());
			if (state.getBlock() != Blocks.AIR && !(state.getBlock() instanceof BlockLiquid) && state.isFullBlock()) {
				ticks++;
			}
			if (ticks > 1) {
				Dissipate();
			}
		}
		if (ticksExisted >= getLifeTime()) {
			Dissipate();
		}
		setSize(getWidth(), getHeight());
	}

	@Override
	public EntityLivingBase getController() {
		return getOwner();
	}

	@Override
	public void onCollideWithEntity(Entity entity) {
		super.onCollideWithEntity(entity);
		if (!isPiercing() && isProjectile() && shouldExplode())
			Explode();
		else if (!isPiercing() && shouldDissipate()) {
			attackEntity(this, entity, false, new Vec3d(getKnockback().x * getKnockbackMult().x, getKnockback().y * getKnockbackMult().y,
					getKnockback().z * getKnockbackMult().z));
			Dissipate();
		} else applyPiercingCollision();
		if (entity instanceof AvatarEntity)
			applyElementalContact((AvatarEntity) entity);

	}

	@Override
	public Vec3d getKnockback() {
		return new Vec3d(motionX / 2, motionY / 2, motionZ / 2);
	}

	@Override
	public float getXpPerHit() {
		return xp;
	}


	@Override
	public boolean onCollideWithSolid() {
		if (isProjectile() && shouldExplode())
			Explode();
		if (isProjectile() && shouldDissipate())
			Dissipate();
		setDead();
		return true;
	}

	public int getLifeTime() {
		return dataManager.get(SYNC_LIFETIME);
	}

	public void setLifeTime(int lifeTime) {
		dataManager.set(SYNC_LIFETIME, lifeTime);
	}

	@Override
	public float getAoeDamage() {
		return 1;
	}

	@Override
	public Vec3d getKnockbackMult() {
		return knockbackMult;
	}

	public void setKnockbackMult(Vec3d mult) {
		this.knockbackMult = mult;
	}

	@Override
	public EnumParticleTypes getParticle() {
		return AvatarParticles.getParticleFlames();
	}

	@Override
	public int getNumberofParticles() {
		return 50;
	}

	@Override
	public double getParticleSpeed() {
		return 0.02;
	}

	@Override
	public int getPerformanceAmount() {
		return this.performanceAmount;
	}

	public void setPerformanceAmount(int amount) {
		this.performanceAmount = amount;
	}

	@Override
	public float getVolume() {
		return 1.0F + AvatarUtils.getRandomNumberInRange(1, 100) / 500F;
	}

	@Override
	public float getPitch() {
		return 1.0F + AvatarUtils.getRandomNumberInRange(1, 100) / 500F;
	}

	public DamageSource getDamageSource(Entity target) {
		return getDamageSource(target, getOwner());
	}

	@Override
	public double getExpandedHitboxWidth() {
		return 0.25;
	}

	@Override
	public double getExpandedHitboxHeight() {
		return 0.25;
	}

	@Override
	public int getFireTime() {
		return this.fireTime;
	}

	public void setFireTime(int time) {
		this.fireTime = time;
	}

	@Override
	public boolean isPiercing() {
		return false;
	}

	@Override
	public boolean shouldDissipate() {
		return false;
	}

	@Override
	public boolean shouldExplode() {
		return true;
	}

	public AxisAlignedBB getExpandedHitbox() {
		return expandedHitbox;
	}

	@Override
	public double getExplosionHitboxGrowth() {
		return 1;
	}

	@Override
	public void applyElementalContact(AvatarEntity entity) {

	}


	public void setXp(float xp) {
		this.xp = xp;
	}

	@Override
	public boolean canBePushed() {
		return !isPiercing();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

}
