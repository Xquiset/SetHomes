package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.scheduler.BukkitRunnable;

public class GoHome implements CommandExecutor{
	private final SetHomes pl;
	private int taskId;
	
	public GoHome(SetHomes plugin) {
		pl = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Make sure the sender of the command is a player
		if (!(sender instanceof Player)) {
			//Sends message to sender of command that they're not a player
			ChatUtils.notPlayerError(sender);
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("home")) {
			final Player p = (Player) sender;
			final String uuid = p.getUniqueId().toString();
			
			if (args.length < 1) {
				//If they have no home tell them
				if (!(pl.hasUnknownHomes(uuid))) {
					ChatUtils.sendError(p, "You have no Default Home!");
					return true;
				} else {
					//Teleport the player to there home and send them a message telling them so
					if(pl.getConfig().getInt("tp-delay") != -1){
						taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
							int delay = pl.getConfig().getInt("tp-delay");
							public void run() {
								p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null,5, 5, 5);
								if(delay == 0){
									pl.cancelTask(taskId);
									p.teleport(pl.getPlayersUnnamedHome(uuid));
									ChatUtils.sendSuccess(p, "You have been teleported home!");
								}
								delay--;
							}
						}, 0L, 20L);
					}else{
						p.teleport(pl.getPlayersUnnamedHome(uuid));
						ChatUtils.sendSuccess(p, "You have been teleported home!");
					}

					return true;
				}
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
				final String homeName = args[0];
				//Teleport the player to there home and send them a message telling them so
				if(pl.getConfig().getInt("tp-delay") != -1){
					taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
						int delay = pl.getConfig().getInt("tp-delay");
						public void run() {
							p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null,5, 5, 5);
							if(delay == 0){
								pl.cancelTask(taskId);
								//Teleport the player to their home
								p.teleport(pl.getNamedHomeLocal(uuid, homeName));
								ChatUtils.sendSuccess(p, "You have been teleported home!");
							}
							delay--;
						}
					}, 0L, 20L);
				}else{
					//Teleport the player to their home
					p.teleport(pl.getNamedHomeLocal(uuid, args[0]));
					ChatUtils.sendSuccess(p, "You have been teleported home!");
				}
				return true;
			}
		//Checks if the command sent is /homes
		}
		return false;
	}

}
