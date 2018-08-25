package com.EvilNotch.lib.minecraft.network;

import com.EvilNotch.lib.main.MainJava;
import com.EvilNotch.lib.minecraft.network.packets.PacketClipBoard;
import com.EvilNotch.lib.minecraft.network.packets.PacketClipBoardHandler;
import com.EvilNotch.lib.minecraft.network.packets.PacketUUID;
import com.EvilNotch.lib.minecraft.network.packets.PacketUUIDHandler;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetWorkHandler {
	
	public static SimpleNetworkWrapper INSTANCE;
	static int networkid = 0;
	
	public static void init()
	{
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(MainJava.MODID);
		registerMessage(PacketUUIDHandler.class, PacketUUID.class, Side.CLIENT);
		registerMessage(PacketClipBoardHandler.class, PacketClipBoard.class, Side.CLIENT);
	}
	public static void registerMessage(Class handler, Class packet, Side side)
	{
		INSTANCE.registerMessage(handler, packet,networkid++, side);
	}
}