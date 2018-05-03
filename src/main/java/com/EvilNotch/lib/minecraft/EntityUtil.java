package com.EvilNotch.lib.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Level;

import com.EvilNotch.lib.main.Config;
import com.EvilNotch.lib.main.MainJava;
import com.EvilNotch.lib.main.events.LibEvents;
import com.EvilNotch.lib.main.events.TickHandler;
import com.EvilNotch.lib.util.JavaUtil;
import com.EvilNotch.lib.util.Line.LineBase;
import com.EvilNotch.lib.minecraft.proxy.ClientProxy;
import com.EvilNotch.lib.minecraft.registry.SpawnListEntryAdvanced;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityUtil {
	
	public static boolean cached = false;
	public static List<ResourceLocation> end_ents = new ArrayList<ResourceLocation>();

	public static HashMap<ResourceLocation,String> living_names = new HashMap<ResourceLocation, String>();//Used for everything else	
	public static HashMap<ResourceLocation,String> nonLiving_names = new HashMap<ResourceLocation, String>();//Used for everything else
	public static HashMap<ResourceLocation,String> livingBase_names = new HashMap<ResourceLocation, String>();//Used for everything else
	public static HashMap<Integer,String> entityIdToName = new HashMap();
	
	public static Set<ResourceLocation> forgemobs = new HashSet();//forge mobs that are entity living
	
	public static Set<ResourceLocation> ents_worldneedy = new HashSet();//List of entities that need the world how greedy?
	public static Set<ResourceLocation> ent_blacklist = new HashSet();//List of all failed Entities
	public static Set<ResourceLocation> ent_blacklist_commandsender = new HashSet();//List of all failed Entities
	public static Set<ResourceLocation> ent_blacklist_nbt = new HashSet();
	
	public static HashMap<ResourceLocation,Entity> livingCache = new HashMap();//Used for displaying
	public static HashMap<ResourceLocation,Entity> nonLivingCache = new HashMap();//Used for displaying
	public static HashMap<ResourceLocation,Entity> livingBaseCache = new HashMap();//Used for displaying
	
	public static String TransLateEntity(NBTTagCompound nbt,World w)
	{
	   nbt = nbt.copy();
	   nbt.removeTag("CustomName");
	   String id = nbt.getString("id");
	   Entity e = createEntityFromNBTQuietly(new ResourceLocation(id), nbt, w,true);
	   	
	   return TransLateEntity(e,w);
	}
	 
	 /**
     * Prefer Command Sender Name If applicable else use general name
     */
    public static String TransLateEntity(Entity entity, World w)
    {
    	if(entity == null || w == null)
    		return null;
    	ResourceLocation loc = getEntityResourceLocation(entity);
    	if(loc != null)
    	{
    		if(Config.cmdBlacklist.contains(loc) )
    		{
    			String str = translateEntityGeneral(entity,w);
    			if(str == null)
    				return null;
    			//if mistranslated fix it
    	    	if(isMistranslated(str))
    	    		str = str.substring(7, str.length()-5 );
        		return str;
    		}
    	}
    	
    	String strentity = translateEntityCmd(entity,w);
    	
    	//if cmd fails try general name
    	if(strentity == null || isMistranslated(strentity) )
    	{
    		String generalName = translateEntityGeneral(entity,w);
    		//don't check mistranslation of new string since the strentity can be null or is mistranslated to begin with
    		if(generalName != null)
    			strentity = generalName;
    	}
    	if(strentity == null)
    		return null;//if both fail to return null return before a null point exception happens
    	
    	//if general translation fails and cmd fails get rid of the ugly failed text
    	if(isMistranslated(strentity))
    		strentity = strentity.substring(7, strentity.length()-5 );

    	return strentity;
    }
    public static boolean isMistranslated(String str) {
		if(str == null || str.startsWith("entity.") && str.endsWith(".name"))
			return true;
		return false;
	}

	/**
	 * Translates general name for entity so pink sheep will return sheep
	 * Has the entity.entityNameHere.name removed if not translated properly
	 */
	public static String translateEntityGeneral(Entity entity,World world)
	{
		if(entity == null)
			return null;
	   String EntityName = EntityList.getEntityString(entity);
	   try{
		EntityName = I18n.translateToLocal("entity." + EntityName + ".name");
	   }catch(Throwable t){return null;}
	    
	   return EntityName;
	}
	/**
	 * get command sender name and returns null if vanilla does it's funky general thing
	 */
	public static String translateEntityCmd(Entity entity, World world)
	{
		String name = getcommandSenderName(entity);
		if(name != null)
		{
			if(name.equals("generic") || name.equals("entity." + "generic" + ".name") )
				return null;
		}
		return name;
	}
	
	public static String getcommandSenderName(Entity entity) 
	{
		try{
			if(entity == null)
				return null;
			String name = entity.getName();
			if(name != null)
				ent_blacklist_commandsender.remove(EntityUtil.getEntityResourceLocation(entity));
			return name;
		}catch(Throwable t){
			ent_blacklist_commandsender.add(getEntityResourceLocation(entity));
			MainJava.logger.error("Entity Has Thrown an Error when entity.getName() Report to mod author:" + EntityList.getEntityString(entity));
		}
		return null;
	}
	public static Entity createEntityFromNBTQuietly(ResourceLocation loc,NBTTagCompound nbt, World worldIn)
	{
		return createEntityFromNBTQuietly(loc,nbt,worldIn,false);
	}
	
	 public static Entity createEntityFromNBTQuietly(ResourceLocation loc,NBTTagCompound nbt, World worldIn,boolean constructor)
	 {
	   try{
		   Entity e = createEntityByNameQuietly(loc,worldIn,constructor);
		   if(e != null)
			   e.readFromNBT(nbt);
		   return e;
	  	}catch(Throwable e){}
	  	return null;
	 }
	 
	public static Entity createEntityByNameQuietly(ResourceLocation loc, World worldIn)
	{
		return createEntityByNameQuietly(loc,worldIn,false);
	}
	 
    public static Entity createEntityByNameQuietly(ResourceLocation loc, World worldIn,boolean constructor)
    {
    	if(!constructor)
    	{
    		try{
    			net.minecraftforge.fml.common.registry.EntityEntry entry = net.minecraftforge.fml.common.registry.ForgeRegistries.ENTITIES.getValue(loc);
    			return entry == null ? null : entry.newInstance(worldIn);
    		}catch(Throwable e){return null;}
    	}
    	else
    	{
    		try
    		{
    			Class clazz = EntityList.getClass(loc);
    			Constructor c = clazz.getConstructor(new Class[] {World.class});
    			return (Entity) c.newInstance(worldIn);
    		}
    		catch(Throwable t){return null;}
    	}
    }
    
    public static ResourceLocation getEntityResourceLocation(Entity e)
	{
		net.minecraftforge.fml.common.registry.EntityEntry entry = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(e.getClass());
		if(entry != null)
			return entry.getRegistryName();
		return null;
	}
    
    public static void printChat(EntityPlayer player,String c_player, String c_msg, String messege)
	{
		player.sendMessage(new TextComponentString(c_player + player.getName() + " " + c_msg + messege));
	}
    /**
     * Spawn Entity by spawnlistentry from Scratch
     */
	public static boolean spawnEntityEntry(World w, SpawnListEntry entry,double x, double y, double z) {
		try {
			//if doesn't contain nbt or is legacy do normal spawning
			if(legacySpawnListEntry(entry))
			{
				EntityLiving living = entry.newInstance(w);
				living.setLocationAndAngles(x, y, z, living.rotationYaw, living.rotationPitch);
				living.onInitialSpawn(w.getDifficultyForLocation(new BlockPos(x,y,z)), (IEntityLivingData)null);
				w.spawnEntity(living);
				return true;
			}
		   SpawnListEntryAdvanced advanced = (SpawnListEntryAdvanced)entry;
		   NBTTagCompound compound = advanced.NBT.copy();
		   compound.setString("id", advanced.loc.toString());
		   Entity e = getEntityJockey(compound,w,x,y,z,true);
		   return e != null;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Doesn't force nbt if you don't need it to unlike vanilla this is the forum of the /summon command
	 * silkspawners eggs will support multiple indexes but, not to this extent not requring recursion use only when fully supporting new format
	 */
	public static Entity getEntityJockey(NBTTagCompound compound,World worldIn, double x, double y, double z,boolean firstcall) 
	{	
        Entity entity = getEntity(compound,worldIn,new BlockPos(x,y,z),firstcall,true);
        if(entity == null)
        	return null;
        
        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        entity.forceSpawn = true;
        if(!worldIn.spawnEntity(entity))
        	return null;
        
        if (compound.hasKey("Passengers", 9))
        {
             NBTTagList nbttaglist = compound.getTagList("Passengers", 10);
             for (int i = 0; i < nbttaglist.tagCount(); ++i)
             {
                 Entity entity1 = getEntityJockey(nbttaglist.getCompoundTagAt(i), worldIn, x, y, z,false);
                  if (entity1 != null)
                  {
                      entity1.startRiding(entity, true);
                  }
             }
        }

       return entity;
	}

	/**
	 * first index is to determine if your on the first part of the opening of the nbt if so treat nbt like normal
	 * @return
	 */
	private static Entity getEntity(NBTTagCompound nbt,World world,BlockPos pos,boolean firstIndex,boolean useInterface) {
		Entity e = null;
		if(getEntityProps(nbt).getSize() > 0 || !nbt.hasKey("Passengers") && firstIndex)
			e = EntityUtil.createEntityFromNBTQuietly(new ResourceLocation(nbt.getString("id")), nbt, world);
		else{
			e = EntityUtil.createEntityByNameQuietly(new ResourceLocation(nbt.getString("id")),world);
			if(e instanceof EntityLiving && useInterface)
				((EntityLiving) e).onInitialSpawn(world.getDifficultyForLocation(pos), (IEntityLivingData)null);
		}
		return e;
	}

	private static boolean legacySpawnListEntry(SpawnListEntry entry) throws Exception {
		if(!(entry instanceof SpawnListEntryAdvanced) || ((SpawnListEntryAdvanced)entry).NBT == null )
			return true;
		return false;
	}

	private static NBTTagCompound getEntityProps(NBTTagCompound nbt) {
		if(nbt == null)
			return null;
		nbt = nbt.copy();
		nbt.removeTag("Passengers");
		nbt.removeTag("id");
		return nbt;
	}
	public static String getEntityString(Entity e)
	{
		return EntityList.getEntityString(e);
	}
	
	/**
	 * Gets Entity's NBT
	 */
	public static NBTTagCompound getEntityNBT(Entity e)
	{
		if(e == null)
			return null;
		
		return e.writeToNBT(new NBTTagCompound());
	}
	
	/**
	 * Returns the uuidFile or cached file based on uuid boolean
	 * @param player
	 * @param uuid
	 * @return player file
	 */
	public static File getPlayerFile(EntityPlayer player,boolean uuid)
	{
		if(uuid)
		{
			return new File(LibEvents.playerDataDir,player.getUniqueID().toString() + ".dat");
		}
		else
			return new File(LibEvents.playerDataNames,player.getName() + ".dat");
	}
	/**
	 * Update Player file
	 */
	public static void updatePlayerFile(File file, NBTTagCompound nbt) 
	{
		if(!file.exists())
		{
			try {
				file.createNewFile();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		NBTUtil.updateNBTFile(file,nbt);
	}
	/**
	 * Gets cached playerdata from name needs instantiated world or will cause exceptions every time
	 * @param display
	 * @return
	 */
	public static NBTTagCompound getPlayerFileNBT(String display,boolean uuid) 
	{
		try{
			FileInputStream stream = null;
			stream = !uuid ? new FileInputStream(new File(LibEvents.playerDataNames,display + ".dat")) : new FileInputStream(new File(LibEvents.playerDataDir,display + ".dat"));
			NBTTagCompound nbt = CompressedStreamTools.readCompressed(stream);
			stream.close();
			return nbt;
		}catch(Exception e){e.printStackTrace();}
		return null;
	}
	
	//Returns true for survival mode unless debug mode is on
	public static boolean isSurvival(EntityPlayer player) 
	{
		if(player == null || player.capabilities.isCreativeMode && !Config.debug|| !player.capabilities.allowEdit && !Config.debug)
			return false;
		return true;
	}
	/**
	 * returns entitymodid:entitymodname
	 * it is legacy do not use unless you know what you are doing
	 */
	@Deprecated
	public static LineBase getEntityMod(Entity entity)
	{
	   	String modName = "";
	   	String modid = "";
	   	try{
	   		EntityRegistration er = EntityRegistry.instance().lookupModSpawn(entity.getClass(), true);
	   		ModContainer modC = er.getContainer();
	   		modName = modC.getName();
	   		modid = modC.getModId();
	   	} catch (NullPointerException e){
	   		modName = "Minecraft";
    		modid = "minecraft";
    	}
		return new LineBase("\"" + modid + ":" + modName + "\"");
    }
	/**GetColor Based on Entity Attributes and or classes
	 * returns EnumChatFormatting format to apply to a string to colorize it
	 * Dynamic Colored Text if not vanilla
	 * Custom Vanilla Support
	 * Custom Configed Support
	 */
	public static String getColoredEntityText(Entity e,boolean isEnd)
	{
		if(e == null)//|| Config.colorText == false)
			return EnumChatFormatting.WHITE + "";
		//Sees if it's and ender mob
		if(EntityUtil.end_ents.contains(EntityList.getEntityString(e)) && EntityList.getEntityString(e) != null || isEnd)
				return EnumChatFormatting.DARK_PURPLE + "";
		
		//Scans Enum Class for modded enum types
		EnumCreatureType[] k = EnumCreatureType.values();
		boolean ismoded = false;
		for(Object a : k)
		{
			String b = a.toString();
			if(!b.equals("ambient") && !b.equals("creature") && !b.equals("creature") && !b.equals("monster") && !b.equals("waterCreature"))
				ismoded = e.isCreatureType((EnumCreatureType) a, false);
		}
			if(ismoded)
				return EnumChatFormatting.STRIKETHROUGH + "" + EnumChatFormatting.BOLD + "";
		
		boolean ambient = e.isCreatureType(EnumCreatureType.AMBIENT, false);
		boolean creature = e.isCreatureType(EnumCreatureType.CREATURE, false);
		boolean water = e.isCreatureType(EnumCreatureType.WATER_CREATURE, false);
		if(!water && e instanceof EntityLivingBase)
			water = ((EntityLivingBase)e).canBreatheUnderwater();
		boolean fire = e.isImmuneToFire();
		boolean monster = e.isCreatureType(EnumCreatureType.MONSTER, false);

		/*
		if(e instanceof EntityLivingBase)
		{
			EntityLivingBase en = (EntityLivingBase)e;
			EnumCreatureAttribute enums = en.getCreatureAttribute();
			boolean undead = enums == EnumCreatureAttribute.UNDEAD;
			boolean arthropod = enums == EnumCreatureAttribute.ARTHROPOD;
			boolean undefined = enums == EnumCreatureAttribute.UNDEFINED;
		}*/
		boolean boss = e instanceof EntityDragon || e instanceof EntityWither;
		
		//Checks Enum Attributes
		if(ambient && !fire && !(e instanceof EntityTameable) && !(e instanceof EntityFlying) && !(e instanceof IRangedAttackMob) && !boss && !(e instanceof IEntityMultiPart))
			return EnumChatFormatting.DARK_GRAY + "";
		if(creature && !fire && !(e instanceof EntityTameable) && !(e instanceof EntityFlying) && !(e instanceof IRangedAttackMob) && !boss && !(e instanceof IEntityMultiPart))
			return EnumChatFormatting.LIGHT_PURPLE + "";
		if(water && !fire  && !(e instanceof EntityTameable) && !(e instanceof EntityFlying) && !(e instanceof IRangedAttackMob) && !boss && !(e instanceof IEntityMultiPart))
			return EnumChatFormatting.AQUA + "";
		if(fire && !(e instanceof EntityTameable) && !(e instanceof EntityFlying) && !(e instanceof IRangedAttackMob) && !boss && !(e instanceof IEntityMultiPart))
			return EnumChatFormatting.GOLD + "";
		if(monster && !fire  && !(e instanceof EntityTameable) && !(e instanceof EntityFlying) && !(e instanceof IRangedAttackMob) && !boss && !(e instanceof IEntityMultiPart))
			return EnumChatFormatting.RED + "";
		
		
		//Checks Classes if hasn't Returned
		if(e instanceof EntityTameable)
			return EnumChatFormatting.DARK_BLUE + "";
		
		if(boss || e instanceof IEntityMultiPart)
			return EnumChatFormatting.BOLD + "" + EnumChatFormatting.DARK_PURPLE;
		
		if(e instanceof EntityFlying)
			return EnumChatFormatting.BOLD + "" + EnumChatFormatting.YELLOW;
		
		if(e instanceof IRangedAttackMob && e instanceof EntityMob)
			return EnumChatFormatting.DARK_RED + "";	
		
		if(e instanceof EntityAmbientCreature && !(e instanceof EntityAnimal))
			return EnumChatFormatting.DARK_GRAY + "";
		
		if(e instanceof EntityAnimal && !(e instanceof EntityCreature) || e instanceof IAnimals && !(e instanceof EntityCreature))
			return EnumChatFormatting.LIGHT_PURPLE + "";
		
		if (e instanceof EntityAgeable)
			return EnumChatFormatting.LIGHT_PURPLE + "";
		
		if(e instanceof EntityCreature)
			return EnumChatFormatting.GREEN + "";
		
		if(fire)
			return EnumChatFormatting.GOLD + "";
		
		if(e instanceof EntityWaterMob)
			return EnumChatFormatting.AQUA + "";
		
		if(e instanceof EntityMob)
			return EnumChatFormatting.RED + "";
		
		return EnumChatFormatting.WHITE + "";
	}
	
	//Prints Colored Chat from player
	public static void printChat(EntityPlayer player, String color, EnumChatFormatting colormsg, String messege)
	{
		player.sendMessage(new TextComponentString(color + player.getDisplayName() + " " + colormsg + messege));
	}
	/**
	 * Currently Used for only Item Mob Spawners
	 * Only use on client side shadows don't exists on server side for some reason
	 */
	public static float getScaleBasedOnShadow(Entity e,float scale)
	{
		float shadowSize = getShadowSize(e);//isn't object oriented is bound to change via 1.7.10 backport
		
		if(e == null)
			return 0.0F;
		float f1 = scale;//0.4375F;
        if(shadowSize > 1.5 && shadowSize < 5.0)
            f1 = 0.20F;//0.20F
        if(shadowSize >= 5.0 && shadowSize < 8.0)
        	f1 = 0.125F;
        if(shadowSize >= 8.0)
        	f1 = 0.09F;
        
        return f1;
	}
	@Deprecated
	public static float getShadowSize(Entity e) {
		return e.height / 2.0F;
	}

	public static Entity getEntityFromCache(ResourceLocation loc,World w)
	{
		if(loc == null)
			return null;
		Entity e = livingCache.get(loc);
		if(e == null)
			e = livingBaseCache.get(loc);
		if(e == null)
			e = nonLivingCache.get(loc);
		return getEntityFromCache(e,w);
	}
	public static Entity getEntityFromCache(Entity e,World w)
	{
		if(e == null || EntityList.getEntityString(e) == null)
			return null;
		String s = EntityList.getEntityString(e);
		
		if(e instanceof EntityLiving)
			return copyEntity(livingCache.get(s), w);
		if(e instanceof EntityLivingBase)
			return copyEntity(livingBaseCache.get(s),w);
		
		return copyEntity(nonLivingCache.get(s),w);
	}
    /**
     * Returns a copy of the current entity object from nbt. Doesn't set locations or angles just copies from NBT
     * no jockey support
     */
	public static Entity copyEntity(Entity ent,World w) 
	{
		if(ent == null || EntityUtil.getEntityString(ent) == null)
			return null;
		String str = EntityUtil.getEntityString(ent);
		if(ent_blacklist.contains(str))
			return null;
		if(ent_blacklist_nbt.contains(str))
			return EntityUtil.createEntityByNameQuietly(new ResourceLocation(str), w);
		
		NBTTagCompound nbt = EntityUtil.getEntityNBT(ent);
		nbt.removeTag("UUIDMost");
		nbt.removeTag("UUIDLeast");
		nbt.setString("id", EntityList.getEntityString(ent));
		return EntityUtil.createEntityFromNBTQuietly(new ResourceLocation(str), nbt, w,true);
	}
	public static boolean isEntityOnFire(Entity ent) 
	{
		NBTTagCompound nbt = getEntityNBT(ent);
		return nbt.getInteger("Fire") > 0;
	}
	public static boolean entityHasPumkin(Entity ent) 
	{
		NBTTagList list = getEntityNBT(ent).getTagList("Equipment", 10);
		if(list.tagCount() < 4)
			return false;
		return list.getCompoundTagAt(4).getInteger("id") == 86;
	}
	public static String translateCachedEntity(Entity e)
	{
		return translateCachedEntity(getEntityResourceLocation(e));
	}
	/**
	 * return null if it isn't cached
	 */
	public static String translateCachedEntity(ResourceLocation loc)
	{
		if(!cached || ent_blacklist.contains(loc))
			cacheEnts();
		String str = living_names.get(loc);
		if(str == null)
			str = livingBase_names.get(loc);
		if(str == null)
			str = nonLiving_names.get(loc);
		return str;
	}
	public static void cacheEnts()
	{
		cacheEnts((List)null,MainJava.fake_world,true);
	}
	public static void cacheEnts(Set<ResourceLocation> set,World w)
	{
		List<ResourceLocation> list = JavaUtil.asList(set);
		cacheEnts(list,w,false);
	}
	
	/**
	 * Adds a basic level cache from loc to entity without interface
	 * This also checks for broken entities, logs them and then stores their locs in array lists
	 */
	public static void cacheEnts(List<ResourceLocation> list,World world,boolean printLists)
	{
		if(cached && list == null)
			return;
		if(list == null)
			list = JavaUtil.asList(EntityList.getEntityNameList());
		
		for(ResourceLocation loc : list)
		{
			Class clazz = EntityList.getClass(loc);
			if(clazz == null)
			{
				MainJava.logger.log(Level.ERROR,"Skipping Broken Entity No Class Found Report to mod autoher:" + loc);
				continue;
			}
			boolean isAbstract = Modifier.isAbstract(clazz.getModifiers());
			boolean isInterface = Modifier.isInterface(clazz.getModifiers());
			try 
			{
				Constructor k = clazz.getConstructor(new Class[] {World.class});
			} 
			catch (Throwable t) 
			{
				ent_blacklist.add(loc);
				MainJava.logger.log(Level.ERROR,"Skipping Broken Entity No Default World Constructor Report to mod autoher:" + loc);
				continue;
			}
			//not a real entity if is interface or abstract so don't even blacklist it
			if(isAbstract || isInterface)
				continue;
			
			Entity e = EntityUtil.createEntityByNameQuietly(loc, world,true);
			
			boolean living = e instanceof EntityLiving; 
			boolean base = e instanceof EntityLivingBase && !(e instanceof EntityLiving);
			boolean nonliving = !(e instanceof EntityLivingBase);
			String translation = TransLateEntity(e, world);
			if(e == null || translation == null)
			{
				ent_blacklist.add(loc);//Entity failed cache it's string id for debugging
				MainJava.logger.log(Level.ERROR,"Skipping Broken Entity Creation/Translation Failed Report to mod autoher:" + loc);
				continue;
			}
			ent_blacklist.remove(loc);
			NBTTagCompound tag = getEntityNBTSafley(e);
			
			cacheWorldNeedy(loc);
			cacheNBTMob(loc,e,tag);
			getcommandSenderName(e);//forces it to error if it is going to
			cacheForgeMob(loc);//is depreciated so I know to change it when backporting
			
			if(living)
			{
				try
				{
					Entity entity = EntityUtil.createEntityByNameQuietly(loc, world,true);
					((EntityLiving)entity).onInitialSpawn(e.world.getDifficultyForLocation(new BlockPos(0,4,0)), (IEntityLivingData)null);
				}
				catch(Throwable t)
				{
					ent_blacklist.add(loc);
					MainJava.logger.log(Level.ERROR,"Skipping broken Entity Failed to read onInitialSpawn() aka onSpawnWithEgg() Report to mod author:" + loc);
				}
    			
    			if(e instanceof EntitySlime)
    			{
    				try
    				{
    					tag.setInteger("Size",Config.slimeInventorySize);
    					e.readFromNBT(tag);
    				}
    				catch(Throwable t)
    				{
    					System.out.println("EntitySlime Proper Instantiation Failed to Cache Properly report to mod author:" + loc);
    				}
    			}
				livingCache.put(loc, e);
				living_names.put(loc,translation);
			}
			else if(base){
				livingBaseCache.put(loc, e);
				livingBase_names.put(loc,translation);
			}
			else if(nonliving){
				nonLivingCache.put(loc, e);
				nonLiving_names.put(loc,translation);
			}
		}
		
		JavaUtil.sortByValues(living_names);
		JavaUtil.sortByValues(livingBase_names);
		JavaUtil.sortByValues(nonLiving_names);
		
		if(printLists)
		{
			System.out.println("blacklist:" + ent_blacklist);
			System.out.println("blacklistNBT:" + ent_blacklist_nbt);
			System.out.println("blacklist CMD:" + ent_blacklist_commandsender);
			if(Config.debug)
				System.out.println("worldNeedyMobs:" + ents_worldneedy);
		}
		
		cached = true;
	}

	/**
	 * Is allowed to return null, however it null just means it failed
	 * @param e
	 * @return
	 */
	public static NBTTagCompound getEntityNBTSafley(Entity e) 
	{
		if(e == null)
			return null;
		try
		{
			NBTTagCompound nbt = EntityUtil.getEntityNBT(e);
			return nbt;
		}
		catch(Throwable t)
		{
			return null;
		}
	}

	public static void cacheNBTMob(ResourceLocation loc,Entity e, NBTTagCompound tag) 
	{
		if(tag == null)
		{
			ent_blacklist_nbt.add(loc);
			MainJava.logger.log(Level.ERROR,"Entity Serialization Has Been Broken When Reading It's Own NBT Report to mod autoher:" + loc);
			return;
		}
		try
		{
			e.readFromNBT(tag);
			ent_blacklist_nbt.remove(loc);
		}
		catch(Throwable t)
		{
			ent_blacklist_nbt.add(loc);
			MainJava.logger.log(Level.ERROR,"Entity Serialization Has Been Broken When Reading It's Own NBT Report to mod autoher:" + loc);
		}
	}

	/**
	 * Warning is bound to change in backported versions
	 */
	@Deprecated
	public static void cacheForgeMob(ResourceLocation loc) {
		if(!loc.getResourceDomain().equals("minecraft"))
			forgemobs.add(loc);
	}

	public static void cacheWorldNeedy(ResourceLocation loc) 
	{
		try
		{
			Entity e2 = EntityUtil.createEntityByNameQuietly(loc, null,true);
		}
		catch(Throwable t)
		{
			ents_worldneedy.add(loc);
		}
	}

}