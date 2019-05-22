package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;

public class ListHomes implements CommandExecutor{

	private final SetHomes setHomes;
	
	public ListHomes(SetHomes plugin) {
		this.setHomes = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Make sure the sender of the command is a player
		if (!(sender instanceof Player)) {
			//Sends message to sender of command that they're not a player
			sender.sendMessage(ChatColor.DARK_RED + "This command is for players only!");
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("homes")) {
			Player p = (Player) sender;
			String uuid = p.getUniqueId().toString();
			
			if(args.length > 0) {
				//Tell the player if they've sent to many arguments with the command
				p.sendMessage(ChatColor.DARK_RED + "ERROR: Too many arguments!");
				return false;
			}
			
			//Tell the player if they have a default home set or not
			if(setHomes.hasUnknownHomes(uuid)) {
				p.sendMessage(ChatColor.DARK_GREEN + "Default Home is set!");
			} else {
				p.sendMessage(ChatColor.DARK_RED + "You have no Default Home!");
			}
			
			//Check to make sure the player has homes
			if(setHomes.hasNamedHomes(uuid)) {
				//Print the home with its description to the player
				for(String id : setHomes.getPlayersNamedHomes(uuid).keySet()) {
					String desc = setHomes.getPlayersNamedHomes(uuid).get(id).getDesc();
					if(desc != null) {
						p.sendMessage(ChatColor.DARK_GREEN + id + " - " + desc);
					} else {
						p.sendMessage(ChatColor.DARK_GREEN + id);
					}
				}
			}
			
			return true;
			
		//Checks if the command sent is /delhome
		}
		return false;
	}

}
