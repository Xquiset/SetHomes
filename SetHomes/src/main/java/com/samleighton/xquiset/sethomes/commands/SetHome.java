package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.Home;
import com.samleighton.xquiset.sethomes.SetHomes;

public class SetHome implements CommandExecutor{
	private final SetHomes setHomes;
	//private static FileConfiguration config;
	
	public SetHome(SetHomes plugin) {
		setHomes = plugin;
		//config = setHomes.getConfig();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Make sure the sender of the command is a player
		if (!(sender instanceof Player)) {
			//Sends message to sender of command that they're not a player
			sender.sendMessage(ChatColor.DARK_RED + "This command is for players only!");
			return false;
		}
		//Checks if the command sent is /sethome
		if (cmd.getName().equalsIgnoreCase("sethome")) {
			//Since we know sender is a player we can Cast sender as such
			Player p = (Player) sender;
			String uuid = p.getUniqueId().toString();
			Location home = p.getLocation();
			
			//Create a home at the players location
			Home playersHome = new Home(home);
			
			//No home name provided
			if(args.length < 1) {
				//If player has no homes create a new section for their UUID
				if(!(setHomes.hasUnknownHomes(uuid))) {
					setHomes.config.createSection("unknownHomes." + uuid);
				}
				
				//Save the home
				setHomes.saveUnknownHome(uuid, playersHome);
				
				p.sendMessage(ChatColor.GOLD + "You have set a default home!");
				
				return true;
			//They have provided a home name and possibly description too
			} else {
				if(p.hasPermission("homes.sethome")) {
					if(!(setHomes.hasNamedHomes(uuid))) {
						setHomes.config.createSection("allNamedHomes." + uuid);
					}
					
					//Check if the player already has a home with the name they gave us
					if(setHomes.getPlayersNamedHomes(uuid).containsKey(args[0])) {
						p.sendMessage(ChatColor.DARK_RED + "You already have a home with that name, try a different one!");
						return true;
					}
					
					//Set the home name to the given name
					playersHome.setHomeName(args[0]);
					
					//Build the description as a combination of all other arguments passed
					String desc = "";
					for (int i = 1; i <= args.length - 1; i++) {
						desc += args[i] + " ";
					}
					
					if(desc != "") {
						playersHome.setDesc(desc.substring(0, desc.length() - 1));
					}
					
					setHomes.saveNamedHome(uuid, playersHome);
					
					p.sendMessage(ChatColor.DARK_GREEN + "Your home \'" + playersHome.getHomeName() + "\' has been set!");
					return true;
				}
				//Send player message because they didn't have the proper permissions
				p.sendMessage(ChatColor.DARK_RED + "You dont have permission to do that!");
				return true;
			}
		}
		return false;
	}
}
