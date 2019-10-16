package com.samleighton.xquiset.sethomes.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoHome implements CommandExecutor{
	private final SetHomes pl;
	private int taskId;
	private int cooldown;
	private Map<String, Long> cooldownList = new HashMap<String, Long>();
	private boolean cancelOnMove;
	
	public GoHome(SetHomes plugin) {
		pl = plugin;
		cooldown = pl.getConfig().getInt("tp-cooldown");
		cancelOnMove = pl.getConfig().getBoolean("tp-cancelOnMove");
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

			//If cooldown is active then check to see if player is in cooldown list
			if(!(cooldownList.containsKey(uuid)) || cooldown <= 0){
				//Player was not in cooldown list so try and teleport then
				if(teleportHome(p, uuid, args)) {
					//Teleport was successful so we return true
					return true;
				}
				//Teleport failed
				return false;
			}else{
				//Player was found in cooldown list
				//Calculate the amount of time left before they can run the command again
				long timeLeft = ((cooldownList.get(uuid)/1000) + cooldown) - (System.currentTimeMillis()/1000);
				//The player has not passed the amount of time needed
				if(timeLeft > 0){
					//Tell the player they need to wait still
					ChatUtils.sendInfo(p, "You must wait another " + timeLeft + " second(s) before teleporting!");
					return true;
				}else{
					//The player has waited long enough so we remove them from the list and try to teleport them
					cooldownList.remove(uuid);
					if(teleportHome(p, uuid, args)){
						//The player was successfully teleported
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 *
	 * @param p The player we are trying to teleport
	 * @param args the arguments the player passed via command
	 * @return true on successful teleport, false otherwise
	 */
	private boolean teleportHome(final Player p, final String uuid, String[] args){
		final Location locale = p.getLocation();
		if (args.length < 1) {
			//If they have no home tell them
			if (!(pl.hasUnknownHomes(uuid))) {
				ChatUtils.sendError(p, "You have no Default Home!");
				return false;
			} else {
				//Teleport the player to their home and send them a message telling them so
				if(pl.getConfig().getInt("tp-delay") > 0){
					taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
						int delay = pl.getConfig().getInt("tp-delay");
						public void run() {
							if(cancelOnMove && (p.getLocation().getX() != locale.getX() || p.getLocation().getY() != locale.getY())){
								pl.cancelTask(taskId);
								ChatUtils.sendInfo(p, pl.getConfig().getString("tp-cancelOnMove-msg"));
							}
							p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null,5, 5, 5);
							if(delay == 0){
								pl.cancelTask(taskId);
								p.teleport(pl.getPlayersUnnamedHome(uuid));
								cooldownList.put(uuid, System.currentTimeMillis());
							}
							delay--;
						}
					}, 0L, 20L);
				}else{
					p.teleport(pl.getPlayersUnnamedHome(uuid));
					ChatUtils.sendSuccess(p, "You have been teleported home!");
					cooldownList.put(uuid, System.currentTimeMillis());
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
				return false;
			}
			final String homeName = args[0];
			//Teleport the player to there home and send them a message telling them so
			if(pl.getConfig().getInt("tp-delay") > 0){
				taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
					int delay = pl.getConfig().getInt("tp-delay");
					public void run() {
						if(cancelOnMove && (p.getLocation().getX() != locale.getX() || p.getLocation().getY() != locale.getY())){
							pl.cancelTask(taskId);
							ChatUtils.sendInfo(p, pl.getConfig().getString("tp-cancelOnMove-msg"));
						}
						p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null,5, 5, 5);
						if(delay == 0){
							pl.cancelTask(taskId);
							//Teleport the player to their home
							p.teleport(pl.getNamedHomeLocal(uuid, homeName));
							cooldownList.put(uuid, System.currentTimeMillis());
						}
						delay--;
					}
				}, 0L, 20L);
			}else{
				//Teleport the player to their home
				p.teleport(pl.getNamedHomeLocal(uuid, args[0]));
				ChatUtils.sendSuccess(p, "You have been teleported home!");
				cooldownList.put(uuid, System.currentTimeMillis());
			}
			return true;
		}
	}
}
