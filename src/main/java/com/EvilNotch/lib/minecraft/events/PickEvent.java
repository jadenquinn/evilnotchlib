package com.EvilNotch.lib.minecraft.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PickEvent extends Event{
	
	public final ItemStack initStack;
	public ItemStack current;
	public RayTraceResult targ;
	public EntityPlayer player;
	public World world;
	public boolean canPick;
	
	public PickEvent(ItemStack resault,RayTraceResult target, EntityPlayer player, World world)
	{
		this.initStack = resault.copy();
		this.current = resault;
		this.targ = target;
		this.player = player;
		this.world = world;
		this.canPick = player.capabilities.isCreativeMode;
	}
	
	public static class Entity extends PickEvent
	{
		public Entity(ItemStack resault, RayTraceResult target, EntityPlayer player, World world) 
		{
			super(resault, target, player, world);
		}	
	}
	
	public static class Block extends PickEvent
	{
		public IBlockState state;
		public TileEntity tile;
		public BlockPos pos;
		public boolean copyTE;

		public Block(ItemStack resault, RayTraceResult target, EntityPlayer player, World world,IBlockState state) 
		{
			super(resault, target, player, world);
			this.state = state;
			this.pos = target.getBlockPos();
			this.copyTE = GuiScreen.isCtrlKeyDown();
	        if(state.getBlock().hasTileEntity(state))
	        	this.tile = world.getTileEntity(target.getBlockPos());
	        else
	        	this.tile = null;
		}
	}

}