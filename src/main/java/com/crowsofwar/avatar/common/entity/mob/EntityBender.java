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

import com.crowsofwar.avatar.common.data.Bender;
import com.crowsofwar.avatar.common.data.BenderEntityComponent;
import com.crowsofwar.avatar.common.data.BendingData;
import com.crowsofwar.avatar.common.util.AvatarUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

import static com.crowsofwar.avatar.common.config.ConfigClient.CLIENT_CONFIG;
import static com.crowsofwar.avatar.common.config.ConfigMobs.MOBS_CONFIG;

/**
 * @author CrowsOfWar
 */
public abstract class EntityBender extends EntityCreature {

	private Bender bender;
	private static final DataParameter<Integer> SYNC_LEVEL = EntityDataManager
			.createKey(EntityBender.class, DataSerializers.VARINT);

	/**
	 * @param world
	 */
	public EntityBender(World world) {
		super(world);
	}

	protected Bender initBender() {
		return new BenderEntityComponent(this);
	}

	public int getLevel() {
		return dataManager.get(SYNC_LEVEL);
	}

	public void setLevel(int level) {
		dataManager.set(SYNC_LEVEL, level);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		// Initialize the bender here (instead of constructor) so the bender will be ready for
		// initEntityAI - Constructor is called AFTER initEntityAI
		bender = initBender();
		dataManager.register(SYNC_LEVEL, AvatarUtils.getRandomNumberInRange(1, MOBS_CONFIG.benderSettings.maxLevel));
		applyAbilityLevels(getLevel());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		bender.getData().readFromNbt(nbt);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		bender.getData().writeToNbt(nbt);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		bender.onUpdate();
	}

	public abstract void applyAbilityLevels(int level);

	public Bender getBender() {
		return bender;
	}

	public BendingData getData() {
		return bender.getData();
	}

}
