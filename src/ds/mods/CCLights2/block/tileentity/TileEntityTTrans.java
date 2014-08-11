package ds.mods.CCLights2.block.tileentity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet132TileEntityData;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import static ds.mods.CCLights2.utils.TypeConverters.*;
import ds.mods.CCLights2.gpu.GPU;
import ds.mods.CCLights2.gpu.Monitor;
import ds.mods.CCLights2.utils.TabMesg;
import ds.mods.CCLights2.utils.TabMesg.Message;

public class TileEntityTTrans extends TileEntityMonitor implements IPeripheral {
	public UUID id = UUID.randomUUID();
	public ArrayList<UUID> tablets = new ArrayList<UUID>();
	
	public boolean update = true;
	
	public TileEntityTTrans()
	{
		mon = new Monitor(16*32,9*32,getMonitorObject());
		mon.tex.fill(Color.black);
		mon.tex.drawText("Tablet connected", 0, 0, Color.white);
		mon.tex.texUpdate();
	}
	
	public void onRemove()
	{
		for (UUID t : tablets)
		{
			TabMesg.pushMessage(t, new Message("disconnect"));
		}
	}

	@Override
	public void invalidate() {
		for (UUID t : tablets)
		{
			TabMesg.pushMessage(t, new Message("unload"));
		}
		
	}

	@Override
	public void validate() {
		for (UUID t : tablets)
		{
			TabMesg.pushMessage(t, new Message("load"));
		}
		
		update = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		//Read UUIDs
		if (nbt.getString("uuid").length() > 0)
		{
			id = UUID.fromString(nbt.getString("uuid"));
			NBTTagList lst = nbt.getTagList("tablets");
			tablets.clear();
			for (int i=0; i<lst.tagCount(); i++)
			{
				NBTTagString str = (NBTTagString) lst.tagAt(i);
				tablets.add(UUID.fromString(str.data));
			}
		}
		update = true;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		//Write UUIDs
		nbt.setString("uuid", id.toString());
		NBTTagList lst = new NBTTagList();
		for (UUID t : tablets)
		{
			lst.appendTag(new NBTTagString("", t.toString()));
		}
		nbt.setTag("tablets", lst);
		update = true;
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt) {
		readFromNBT(pkt.data);
	}

	@Override
	public void updateEntity() {
		//Receive tabmesgs.
		if (TabMesg.getTabVar(id, "x") == null)
		{
			TabMesg.setTabVar(id, "x", xCoord);
			TabMesg.setTabVar(id, "y", yCoord);
			TabMesg.setTabVar(id, "z", zCoord);
		}
		Message msg;
		while ((msg = TabMesg.popMessage(id)) != null)
		{
			if (msg.name.equals("connect"))
			{
				UUID tab = (UUID) msg.a; //A tablet asked to connect.
				tablets.add(tab);
				update = true;
				TabMesg.pushMessage(tab, new Message("connected"));
			}
			else if (msg.name.equals("ccevent"))
			{
				// A tablet wants to send an event to ComputerCraft
				UUID tab = (UUID) msg.a;
				String eventType = (String) msg.b;
				Object[] eventArgs = (Object[]) msg.c;
				if (mon != null)
				{
					if (mon.gpu != null)
					{
						for (GPU g : mon.gpu)
						{
							for (IComputerAccess c : g.tile.comp)
							{
								c.queueEvent(eventType, eventArgs);
							}
						}
					}
				}
			}
			else if (msg.name.equals("startClick"))
			{
				UUID tab = (UUID) msg.a;
				Object[] args = (Object[]) msg.b;
				if (mon != null)
					if (mon.gpu != null)
						for (GPU g : mon.gpu)
						{
							g.tile.startClick((Player)args[0], (Integer)args[1], (Integer)args[2], (Integer)args[3]);
						}
			}
			else if (msg.name.equals("moveClick"))
			{
				UUID tab = (UUID) msg.a;
				Object[] args = (Object[]) msg.b;
				if (mon != null)
					if (mon.gpu != null)
						for (GPU g : mon.gpu)
						{
							g.tile.moveClick((Player)args[0], (Integer)args[1], (Integer)args[2]);
						}
			}
			else if (msg.name.equals("endClick"))
			{
				UUID tab = (UUID) msg.a;
				Player player = (Player) msg.b;
				if (mon != null)
					if (mon.gpu != null)
						for (GPU g : mon.gpu)
						{
							g.tile.endClick(player);
						}
			}
		}
		
		if (update && !worldObj.isRemote)
		{
			Packet132TileEntityData pkt = new Packet132TileEntityData();
			pkt.data = new NBTTagCompound();
			pkt.xPosition = xCoord;
			pkt.yPosition = yCoord;
			pkt.zPosition = zCoord;
			writeToNBT(pkt.data);
			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 64, worldObj.provider.dimensionId, pkt);
			update = false;
		}
	}
	@Override
	public String[] getMethodNames() {
		return new String[]{"getResolution","getNumberOfTablets","getTabletUUID","disconnect"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,int method, Object[] arguments) 
			throws Exception {
		switch (method)
		{
		case 0:
		{
			return new Object[]{mon.getWidth(),mon.getHeight()};
		}
		case 1:
		{
			return new Object[]{tablets.size()};
		}
		case 2:
		{
			return new Object[]{tablets.get(checkInteger(arguments,0,"getTabletUUID")).toString()};
		}
		case 3:
		{
           invalidate();
		}
		}
		return null;
	}

	@Override
	public String getType() {return "TabletTransciever";
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other) {
		if(other.getType() == getType()){return true;}
		else return false;
	}

}
