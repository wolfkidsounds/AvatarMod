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

import com.crowsofwar.avatar.common.bending.BendingManager;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.data.BendingState;
import com.crowsofwar.avatar.common.entity.EntityFireArc;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;

public class FirebendingState extends BendingState {
	
	private EntityFireArc fireArc;
	private boolean isFlamethrowing;
	
	public FirebendingState(AvatarPlayerData data) {
		super(data);
		fireArc = null;
	}
	
	public int getFireArcId() {
		return fireArc == null ? -1 : fireArc.getId();
	}
	
	public EntityFireArc getFireArc() {
		return fireArc;
	}
	
	public boolean isManipulatingFire() {
		return fireArc != null;
	}
	
	public void setFireArc(EntityFireArc arc) {
		fireArc = arc;
		save();
	}
	
	public void setNoFireArc() {
		setFireArc(null);
	}
	
	public boolean isFlamethrowing() {
		return isFlamethrowing;
	}
	
	public void setFlamethrowing(boolean flamethrowing) {
		this.isFlamethrowing = flamethrowing;
		save();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		setFlamethrowing(nbt.getBoolean("Flamethrowing"));
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("Flamethrowing", isFlamethrowing());
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		buf.writeInt(getFireArcId());
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		fireArc = EntityFireArc.findFromId(data.getPlayerEntity().worldObj, buf.readInt());
	}
	
	@Override
	public int getId() {
		return BendingManager.BENDINGID_FIREBENDING;
	}
	
}
