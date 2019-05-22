package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;

public class DeleteHome implements CommandExecutor{
	private final SetHomes setHomes;
	
	public DeleteHome(SetHomes plugin) {
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
		
		if(cmd.getName().equalsIgnoreCase("delhome")) {
			Player p = (Player) sender;
			String uuid = p.getUniqueId().toString();
			
			//Check if they're trying to delete the default home or a named home base on parameters entered
			if(args.length < 1) {
				//Make sure they have a home before we try and delete one
				if(!setHomes.hasUnknownHomes(uuid)) {
					p.sendMessage(ChatColor.DARK_RED + "No Default Home is set!");
					return true;
				}
				
				//Delete the players default home
				setHomes.deleteUnknownHome(uuid);
				p.sendMessage(ChatColor.DARK_GREEN + "Default Home has been removed!");
				return true;
			} else if (args.length > 1) {
				//Tell the player if they've sent to many arguments with the command
				p.sendMessage(ChatColor.DARK_RED + "ERROR: Too many arguments!");
				return false;
			} else {
				//Check if they have any named homes or a home with the given name
				if(!(setHomes.hasNamedHomes(uuid)) || !(setHomes.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
					p.sendMessage(ChatColor.DARK_RED + "You have no homes by that name!");
					return true;
				}
				
				//Delete the home with the given name
				setHomes.deleteNamedHome(uuid, args[0]);
				setHomes.saveConfig();
				//Tell the player which home they have deleted
				p.sendMessage(ChatColor.DARK_GREEN + "You have deleted the home: " + args[0]);
				return true;
			}
		}
		return false;
	}

}
