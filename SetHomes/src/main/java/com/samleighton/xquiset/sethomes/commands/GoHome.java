package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;

public class GoHome implements CommandExecutor{
	private final SetHomes pl;
	
	public GoHome(SetHomes plugin) {
		pl = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Make sure the sender of the command is a player
		if (!(sender instanceof Player)) {
			//Sends message to sender of command that they're not a player
			ChatUtils.notPlayerError(sender);
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("home")) {
			Player p = (Player) sender;
			String uuid = p.getUniqueId().toString();
			
			if (args.length < 1) {
				//If they have no home tell them
				if (!(pl.hasUnknownHomes(uuid))) {
					ChatUtils.sendError(p, "You have no Default Home!");
					return true;
				} else {
					//Teleport the player to there home and send them a message telling them so
					p.teleport(pl.getPlayersUnnamedHome(uuid));
					ChatUtils.sendSuccess(p, "You have been teleported home!");
					return true;
				}
			} else if (args.length > 1) {
				//Tell the player if they've sent to many arguments with the command
				p.sendMessage(ChatColor.DARK_RED + "ERROR: Too many arguments!");
				return false;
			} else {
				//Check if they have any named homes or a home with the given name
				if(!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
					ChatUtils.sendError(p, "You have no homes by that name!");
					return true;
				}
				
				//Teleport the player to their home
				p.teleport(pl.getNamedHomeLocal(uuid, args[0]));
				ChatUtils.sendSuccess(p, "You have been teleported home!");
				return true;
			}
		//Checks if the command sent is /homes
		}
		return false;
	}

}
