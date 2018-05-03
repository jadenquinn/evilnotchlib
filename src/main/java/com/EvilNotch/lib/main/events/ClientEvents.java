package com.EvilNotch.lib.main.events;

import java.util.Map;

import com.EvilNotch.lib.Api.FieldAcessClient;
import com.EvilNotch.lib.Api.ReflectionUtil;
import com.EvilNotch.lib.main.Config;
import com.EvilNotch.lib.minecraft.content.client.gui.GuiFakeMenu;
import com.EvilNotch.lib.minecraft.content.client.gui.IMenu;
import com.EvilNotch.lib.minecraft.content.client.gui.MenuRegistry;
import com.EvilNotch.lib.minecraft.content.client.rp.CustomResourcePack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class ClientEvents {
	
	/**
	 * set the gui to something mods are never going to be looking at
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onGuiOpenPre(GuiOpenEvent e)
	{
		if(e.getGui() == null)
			return;
		if(!(e.getGui() instanceof GuiMainMenu))
		{
			return;
		}
		e.setGui(new GuiFakeMenu());
	}
	/**
	 * set gui after mods are stopping looking for the main screen
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onGuiOpen(GuiOpenEvent e)
	{
		if(e.getGui() == null)
			return;
		if(!(e.getGui() instanceof GuiFakeMenu))
		{
			return;
		}
		e.setGui(MenuRegistry.getCurrentGui());
	}
	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post e)
	{
		if(e.getGui() == null)
			return;
		if(MenuRegistry.getMenuSize() > 1)
		{
			Class clazz = e.getGui().getClass();
			if(!MenuRegistry.containsMenu(clazz))
			{
				return;
			}
			IMenu menu = MenuRegistry.getCurrentMenu();
			if(menu.allowButtonOverlay())
			{
				e.getButtonList().add(menu.getButton(true));
				e.getButtonList().add(menu.getButton(false));
			}
		}
	}
	@SubscribeEvent
	public void guiButtonClick(GuiScreenEvent.ActionPerformedEvent.Pre e)
	{
		if(e.getGui() == null)
			return;
		Class clazz = e.getGui().getClass();
		if(!MenuRegistry.containsMenu(clazz))
			return;
		if(e.getButton().id == 498)
		{
			MenuRegistry.advancePreviousMenu();
			Minecraft.getMinecraft().displayGuiScreen(MenuRegistry.getCurrentGui());
			Config.saveMenuIndex();
		}
		else if(e.getButton().id == 499)
		{
			MenuRegistry.advanceNextMenu();
			Minecraft.getMinecraft().displayGuiScreen(MenuRegistry.getCurrentGui());
			Config.saveMenuIndex();
		}
	}
	
}