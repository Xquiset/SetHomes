package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;

public class DeleteHome implements CommandExecutor{
	private final SetHomes pl;
	
	public DeleteHome(SetHomes plugin) {
		this.pl = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Make sure the sender of the command is a player
		if (!(sender instanceof Player)) {
			//Sends message to sender of command that they're not a player
			ChatUtils.notPlayerError(sender);
			return false;
		}
		
		if(cmd.getName().equalsIgnoreCase("delhome")) {
			Player p = (Player) sender;
			String uuid = p.getUniqueId().toString();
			
			//Check if they're trying to delete the default home or a named home base on parameters entered
			if(args.length < 1) {
				//Make sure they have a home before we try and delete one
				if(!pl.hasUnknownHomes(uuid)) {
					ChatUtils.sendError(p, "No Default Home is currently set!");
					return true;
				}
				
				//Delete the players default home
				pl.deleteUnknownHome(uuid);
				ChatUtils.sendSuccess(p, "Default Home has been removed!");
				return true;
			} else if (args.length > 1) {
				//Tell the player if they've sent to many arguments with the command
				ChatUtils.tooManyArgs(p);
				return false;
			} else {
				//Check if they have any named homes or a home with the given name
				if(!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
					ChatUtils.sendError(p, "You have no homes by that name!");
					return true;
				}
				
				//Delete the home with the given name
				pl.deleteNamedHome(uuid, args[0]);
				//Tell the player which home they have deleted
				ChatUtils.sendSuccess(p, "You have deleted the home: " + args[0]);
				return true;
			}
		}
		return false;
	}

}
