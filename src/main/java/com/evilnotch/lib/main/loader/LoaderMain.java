package com.evilnotch.lib.main.loader;

import java.io.File;

import org.apache.logging.log4j.Logger;

import com.evilnotch.lib.api.mcp.MCPMappings;
import com.evilnotch.lib.asm.FMLCorePlugin;
import com.evilnotch.lib.main.Config;
import com.evilnotch.lib.main.MainJava;
import com.evilnotch.lib.main.eventhandler.LibEvents;
import com.evilnotch.lib.main.eventhandler.VanillaBugFixes;
import com.evilnotch.lib.minecraft.content.block.IBasicBlock;
import com.evilnotch.lib.minecraft.content.item.IBasicItem;
import com.evilnotch.lib.minecraft.content.item.armor.ArmorSet;
import com.evilnotch.lib.minecraft.content.item.armor.IBasicArmor;
import com.evilnotch.lib.minecraft.content.item.tool.ToolSet;
import com.evilnotch.lib.minecraft.content.tick.TickReg;
import com.evilnotch.lib.minecraft.content.world.FakeWorld;
import com.evilnotch.lib.minecraft.network.NetWorkHandler;
import com.evilnotch.lib.minecraft.registry.GeneralRegistry;
import com.evilnotch.lib.minecraft.util.EntityUtil;
import com.evilnotch.lib.minecraft.util.PlayerUtil;

import net.minecraft.block.Block;
import net.minecraft.command.ICommand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class LoaderMain {
	
	//lib stuffs
	public static boolean isDeObfuscated = true;
	public static boolean isClient = false;
	public static Logger logger;
	public static World fake_world = null;
	
	public static void loadpreinit(FMLPreInitializationEvent e)
	{
		loaderMainPreInit(e);
		LoaderItems.loadpreinit();
		LoaderBlocks.loadpreinit();
		LoaderCommands.load();
	}

	public static void loadInit(FMLInitializationEvent e)
	{
		NetWorkHandler.init();
		MainJava.proxy.initMod();
	}
	public static void loadPostInit(FMLPostInitializationEvent e)
	{
		fake_world = new FakeWorld();
		LoaderItems.loadpostinit();
		LoaderBlocks.loadpostinit();
		LoaderGen.load();
	    MainJava.proxy.postinit();//generate lang,generate shadow sizes
	}

	private static void loaderMainPreInit(FMLPreInitializationEvent e) 
	{
		isDeObfuscated = !FMLCorePlugin.isObf;
		logger = e.getModLog();
		
		MCPMappings.cacheMCPApplicable(e.getModConfigurationDirectory());
		LoaderFields.cacheFields();
		
		MainJava.proxy.proxyStart();
		MainJava.proxy.preinit(e);
		
		Config.loadConfig(e.getModConfigurationDirectory());
		GeneralRegistry.load();
		loadEvents();
	}
	
	private static void loadEvents() 
	{
		MinecraftForge.EVENT_BUS.register(new VanillaBugFixes());
		MinecraftForge.EVENT_BUS.register(new LibEvents());
		MinecraftForge.EVENT_BUS.register(new LoaderMain());
	}

	public static void serverStopping() 
	{
		//prevent memory leaks
		TickReg.garbageCollectServer();
		PlayerUtil.nbts.clear();
		VanillaBugFixes.playerFlags.clear();
		LibEvents.kicker.clear();
		LibEvents.isKickerIterating = false;
		LibEvents.msgs.clear();
	}

	public static void serverStart(FMLServerStartingEvent e) 
	{
		LoaderCommands.registerToWorld(e);
		
		//directories instantiate
		MinecraftServer server = e.getServer();
		VanillaBugFixes.worlDir = server.worlds[0].getSaveHandler().getWorldDirectory();
		VanillaBugFixes.playerDataDir = new File(VanillaBugFixes.worlDir,"playerdata");
		VanillaBugFixes.playerDataNames = new File(VanillaBugFixes.worlDir,"playerdata/names");
	}
	
	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event)
	{
		LoaderItems.registerItems();
	}
	@SubscribeEvent
	public void registerBlocks(RegistryEvent.Register<Block> event) 
	{
		LoaderBlocks.registerBlocks();
	}
	//recipe generators for basic toolsets/armorsets that can be auto generated
	@SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) 
	{
		LoaderItems.registerRecipes(event);
	}

}