package com.evilnotch.lib.main;

import java.util.Map;

import com.evilnotch.lib.api.ReflectionUtil;
import com.evilnotch.lib.main.loader.LoaderMain;
import com.evilnotch.lib.minecraft.proxy.ServerProxy;
import com.google.common.collect.ListMultimap;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.patcher.ClassPatch;
import net.minecraftforge.fml.common.patcher.ClassPatchManager;

@Mod(modid = MainJava.MODID, name = MainJava.NAME, version = MainJava.VERSION)
public class MainJava {
	
	public static final String MODID =  "evilnotchlib";
	public static final String VERSION = "1.2.3";//SNAPSHOT 74
	public static final String NAME = "Evil Notch Lib";
	public static final String max_version = "4.0.0.0.0";//allows for 5 places in lib version
	@SidedProxy(clientSide = "com.evilnotch.lib.minecraft.proxy.ClientProxy", serverSide = "com.evilnotch.lib.minecraft.proxy.ServerProxy")
	public static ServerProxy proxy;
	
	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent e)
	{	
		LoaderMain.loadpreinit(e);
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent e) throws Exception
	{
		LoaderMain.loadInit(e);
	}
	
	@Mod.EventHandler
	public void post(FMLPostInitializationEvent e)
	{
		LoaderMain.loadPostInit(e);
	}
	
	@Mod.EventHandler
	public void complete(FMLLoadCompleteEvent e)
	{
		LoaderMain.loadComplete(e);
		Map<String,byte[]> init = (Map<String, byte[]>) ReflectionUtil.getObject(Launch.classLoader, LaunchClassLoader.class, "resourceCache");
		Map<String,Class<?>> transformedCache = (Map<String, Class<?>>) ReflectionUtil.getObject(Launch.classLoader, LaunchClassLoader.class, "cachedClasses");
		System.out.println("Did the clearing keep it's original form? resourceCache" + init.size() + " transformedCache" + transformedCache.size());
	}
	
	/**
	 * register all commands
	 */
	@Mod.EventHandler
	public void serverStart(FMLServerStartingEvent e)
	{
		LoaderMain.serverStart(e);
	}
	
	/**
	 * clear various data from memory
	 */
	@Mod.EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{	
		LoaderMain.serverStopping();
	}
	
}
