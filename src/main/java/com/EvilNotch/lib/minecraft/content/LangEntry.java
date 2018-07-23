package com.EvilNotch.lib.minecraft.content;

import net.minecraft.util.ResourceLocation;

public class LangEntry {
	
	public String langDisplayName = null;
	public String langType = null;
	public String langId = null;
	public String meta = null;
	public ResourceLocation loc = null;
	
	public static final String en_us = "en_us";
	
	/**
	 * use this one if your manually calling it for advanced constructors in basic items/blocks
	 */
	public LangEntry(String display,String langType){
		this.langDisplayName = display;
		this.langType = langType;
	}
	public LangEntry(String display,String langType,String meta){
		this.langDisplayName = display;
		this.langType = langType;
		this.meta = meta;
	}
	
	@Override
	public String toString(){return this.getString();}
	
	public String getString()
	{
		if(this.meta != null)
			return this.langId + "=" + this.langDisplayName;
		return this.langId + "_" + this.meta + "=" + this.langDisplayName;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof LangEntry))
			return false;
		LangEntry lang = (LangEntry)obj;
		return this.langId.equals(lang.langId) && this.langType.equals(lang.langType);
	}

}
