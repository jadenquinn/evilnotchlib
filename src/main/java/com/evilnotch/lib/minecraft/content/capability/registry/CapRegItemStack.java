package com.evilnotch.lib.minecraft.content.capability.registry;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public abstract class CapRegItemStack implements ICapRegistry<ItemStack>{
	
	@Override
	public Class getObjectClass() {
		return ItemStack.class;
	}

}