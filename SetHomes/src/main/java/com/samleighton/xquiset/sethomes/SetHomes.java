package com.samleighton.xquiset.sethomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.samleighton.xquiset.sethomes.commands.Blacklist;
import com.samleighton.xquiset.sethomes.commands.DeleteHome;
import com.samleighton.xquiset.sethomes.commands.GoHome;
import com.samleighton.xquiset.sethomes.commands.ListHomes;
import com.samleighton.xquiset.sethomes.commands.SetHome;
import com.samleighton.xquiset.sethomes.commands.Strike;
import com.samleighton.xquiset.sethomes.configurations.WorldBlacklist;
import com.samleighton.xquiset.sethomes.eventListeners.EventListener;

//Author: Xquiset
//Plugin: SetHomes
public class SetHomes extends JavaPlugin {
	
	public FileConfiguration config;
	public WorldBlacklist blacklist = new WorldBlacklist(this);
	
	@Override
	public void onEnable() {
		//Load the configuration on enable or reload
		loadConfigurationFile();
		getBlacklist().reloadConfig();
		//Initialize the command executors
		this.getCommand("sethome").setExecutor(new SetHome(this));
		this.getCommand("homes").setExecutor(new ListHomes(this));
		this.getCommand("delhome").setExecutor(new DeleteHome(this));
		this.getCommand("home").setExecutor(new GoHome(this));
		this.getCommand("strike").setExecutor(new Strike(this));
		this.getCommand("blacklist").setExecutor(new Blacklist(this));
		new EventListener(this);
	}

	//Unused
	@Override
	public void onDisable() {

	}
	
	/**
	 * Loads in config if exists
	 * If config does not exist
	 * then we create it with
	 * preset default paths 
	 */
	public void loadConfigurationFile() {
		
		config = getConfig();
		
		if( !(config.isSet("allNamedHomes") || config.isSet("unknownHomes") ) ) {
			config.addDefault("allNamedHomes", new HashMap<String, HashMap<String, Home>>());
			config.addDefault("unknownHomes", new HashMap<String, Home>());
		}
		
		getConfig().options().copyDefaults(true);

		saveConfig();
		
		if( !(getBlacklist().getConfig().isSet("blacklisted_worlds"))) {
			getBlacklist().getConfig().addDefault("blacklisted_worlds", new ArrayList<String>());
		}
		
		getBlacklist().getConfig().options().copyDefaults(true);
		
		getBlacklist().save();
	}
	
	/**
	 * 
	 * @param uuid of the player we're attempting to get homes for
	 * @return a hashmap of all the players homes
	 */
	public HashMap<String, Home> getPlayersNamedHomes(String uuid){
		HashMap<String, Home> playersNamedHomes = new HashMap<String, Home>();
		String homesPath = "allNamedHomes." + uuid;
		
		//Loop through the players home list and create a hash map with the home names as a key and home as value
		for(String id : config.getConfigurationSection(homesPath).getKeys(false)) {
			String path = homesPath + "." + id + ".";
			World world = getServer().getWorld(config.getString(path + ".world"));
			double x = config.getDouble(path + ".x");
			double y = config.getDouble(path + ".y");
			double z = config.getDouble(path + ".z");
			float pitch = Float.parseFloat(config.getString(path + ".pitch"));
			float yaw = Float.parseFloat(config.getString(path + ".yaw"));
			
			Location home = new Location(world, x, y, z, yaw, pitch);
			Home h = new Home(home);
			
			//Check if there is a desc set
			if(config.isSet(path + ".desc")) {
				h.setDesc(config.getString(path + ".desc"));
			}
			
			playersNamedHomes.put(id, h);
		}
		
		return playersNamedHomes;
	}
	
	/**
	 * 
	 * @param uuid player for which to get the home from
	 * @param homeName the name of the home to create the location for
	 * @return location of the players named home
	 */
	public Location getNamedHomeLocal(String uuid, String homeName) {
		Home h = getPlayersNamedHomes(uuid).get(homeName);
		World world = getServer().getWorld(h.getWorld());
		Location home = new Location(world, h.getX(), h.getY(), h.getZ(), h.getYaw(), h.getPitch());
		
		return home;
	}
	
	/**
	 * 
	 * @param uuid of the player we're checking named homes for
	 * @return true || false
	 */
	public boolean hasNamedHomes(String uuid) {
		if(config.contains("allNamedHomes." + uuid) && config.isSet("allNamedHomes." + uuid)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param uuid of the player
	 * @param homeName name to give the new home
	 * @param homeDesc description to add for the home
	 */
	public void saveNamedHome(String uuid, Home home) {
		String path = "allNamedHomes." + uuid + "." + home.getHomeName();
		config.set(path + ".world", home.getWorld());
		config.set(path + ".x", home.getX());
		config.set(path + ".y", home.getY());
		config.set(path + ".z", home.getZ());
		config.set(path + ".pitch", home.getPitch());
		config.set(path + ".yaw", home.getYaw());
		config.set(path + ".desc", home.getDesc());
		
		saveConfig();
	}
	
	/**
	 * 
	 * @param uuid of the player to get home list for
	 * @param homeName name of home to delete from list
	 */
	public void deleteNamedHome(String uuid, String homeName) {
		String path = "allNamedHomes." + uuid + "." + homeName;
		config.set(path, null);
		saveConfig();
	}
	
	/**
	 * 
	 * @param uuid of the player to get a home for
	 * @return the home to teleport the player to
	 */
	public Location getPlayersUnnamedHome(String uuid) {
		//Grabs all the data from the configuration file
		String path = "unknownHomes." + uuid;
		World world = getServer().getWorld(config.getString(path + ".world"));
		double x = config.getDouble(path + ".x");
		double y = config.getDouble(path + ".y");
		double z = config.getDouble(path + ".z");
		float pitch = Float.parseFloat(config.getString(path + ".pitch"));
		float yaw = Float.parseFloat(config.getString(path + ".yaw"));
		
		Location home = new Location(world, x, y, z, yaw, pitch);
		
		return home;
	}
	
	/**
	 * 
	 * @param uuid of the player we're checking unnamed homes for
	 * @return true || false
	 */
	public boolean hasUnknownHomes(String uuid) {
		if(config.contains("unknownHomes." + uuid)){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param uuid of the player to save a home for
	 * @param home to save
	 */
	public void saveUnknownHome(String uuid, Home home) {
		//Saves the variables to construct a home location to the configuration file
		String path = "unknownHomes." + uuid;
		config.set(path + ".world", home.getWorld());
		config.set(path + ".x", home.getX());
		config.set(path + ".y", home.getY());
		config.set(path + ".z", home.getZ());
		config.set(path + ".pitch", home.getPitch());
		config.set(path + ".yaw", home.getYaw());
		
		saveConfig();
	}
	
	/**
	 * @param uuid of the player to delete default home for
	 */
	public void deleteUnknownHome(String uuid) {
		//Set the path to the players id as null
		String path = "unknownHomes." + uuid;
		config.set(path, null);
		saveConfig();
	}
	
	/**
	 * Used to manipulate the WorldBlacklist configuration file
	 * @return WorldBlacklist object
	 */
	public WorldBlacklist getBlacklist() {
		return blacklist;
	}
	
	/**
	 * Used for reading the world names in from the blacklist config
	 * @return list of World Names
	 */
	public List<String> getBlacklistedWorlds(){
		return getBlacklist().getConfig().getStringList("blacklisted_worlds");
	}
}