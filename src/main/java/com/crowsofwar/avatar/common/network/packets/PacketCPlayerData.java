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

package com.crowsofwar.avatar.common.network.packets;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import com.crowsofwar.avatar.common.data.AbilityData;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.data.BendingState;
import com.crowsofwar.avatar.common.network.PacketRedirector;
import com.crowsofwar.gorecore.util.GoreCoreByteBufUtil;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Sent from server to client to notify the client of a player's current bending
 * controller.
 *
 * @author CrowsOfWar
 */
public class PacketCPlayerData extends AvatarPacket<PacketCPlayerData> {
	
	private UUID player;
	private int[] allControllers;
	private List<BendingState> states;
	private List<AbilityData> abilities;
	private ByteBuf buffer;
	
	public PacketCPlayerData() {}
	
	public PacketCPlayerData(AvatarPlayerData data) {
		player = data.getPlayerID();
		allControllers = new int[data.getBendingControllers().size()];
		for (int i = 0; i < allControllers.length; i++) {
			allControllers[i] = data.getBendingControllers().get(i).getID();
		}
		states = data.getAllBendingStates();
		abilities = data.getAllAbilityData();
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		player = GoreCoreByteBufUtil.readUUID(buf);
		// Read bending controllers
		allControllers = new int[buf.readInt()];
		for (int i = 0; i < allControllers.length; i++) {
			allControllers[i] = buf.readInt();
		}
		
		// Reading ability data is done elsewhere
		// Reading bending states is done elsewhere
		buffer = buf;
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		
		System.out.println("=================== WRITING");
		System.out.println("All controllers: " + Arrays.asList(ArrayUtils.toObject(allControllers)));
		System.out.println("All states     : " + states);
		
		GoreCoreByteBufUtil.writeUUID(buf, player);
		// Write bending controllers
		buf.writeInt(allControllers.length);
		for (int i = 0; i < allControllers.length; i++) {
			buf.writeInt(allControllers[i]);
		}
		// Write ability data
		buf.writeInt(abilities.size());
		for (int i = 0; i < abilities.size(); i++) {
			buf.writeInt(abilities.get(i).getAbility().getId());
			buf.writeFloat(abilities.get(i).getXp());
		}
		// Write bending states
		int lastIndex = buf.writerIndex();
		for (int i = 0; i < states.size(); i++) {
			buf.writeInt(states.get(i).getId());
			states.get(i).toBytes(buf);
			System.out.println(
					"State: " + states.get(i) + ": wrote " + (buf.writerIndex() - lastIndex) + " bytes");
			lastIndex = buf.writerIndex();
		}
	}
	
	@Override
	public Side getRecievedSide() {
		return Side.CLIENT;
	}
	
	public UUID getPlayer() {
		return player;
	}
	
	/**
	 * Get an array of the Ids of all the player's bending controllers.
	 */
	public int[] getAllControllersID() {
		return allControllers;
	}
	
	public ByteBuf getBuf() {
		return buffer;
	}
	
	public int getIndex() {
		return buffer.readerIndex();
	}
	
	public List<AbilityData> getAbilityData() {
		return abilities;
	}
	
	@Override
	protected AvatarPacket.Handler<PacketCPlayerData> getPacketHandler() {
		return PacketRedirector::redirectMessage;
	}
	
}
