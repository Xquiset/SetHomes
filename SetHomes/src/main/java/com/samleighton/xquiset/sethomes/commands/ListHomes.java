package com.samleighton.xquiset.sethomes.commands;

import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
			
			if(args.length == 1) {
				if(p.hasPermission("homes.gethomes")) {
					for(Player player : Bukkit.getOnlinePlayers()) {
						Bukkit.getServer().getLogger().log(Level.INFO, player.getName());
						if(player.getName().equalsIgnoreCase(args[0])) {
							listHomes(player, p);
						}
					}
					return true;
				} else {
					//Send player message because they didn't have the proper permissions
					p.sendMessage(ChatColor.DARK_RED + "You dont have permission to do that!");
					return true;
				}
			} else if(args.length == 0){
				//List the homes for the player who sent the command
				listHomes(p);
				return true;
			} else {
				//Tell the player if they've sent to many arguments with the command
				p.sendMessage(ChatColor.DARK_RED + "ERROR: Too many arguments!");
				return false;
			}
		}
		return false;
	}
	
	private void listHomes(Player p) {
		String uuid = p.getUniqueId().toString();
		String filler = StringUtils.repeat("-", 53);
		
		p.sendMessage(filler);
		//Tell the player if they have a default home set or not
		if(setHomes.hasUnknownHomes(uuid)) {
			//Gets the name of the world the home has been set in
			String world = setHomes.getPlayersUnnamedHome(uuid).getWorld().getName();
			p.sendMessage(ChatColor.GOLD + "Default Home - World: " + world);
		}
		
		//Check to make sure the player has homes
		if(setHomes.hasNamedHomes(uuid)) {
			//Print the home with its description to the player
			for(String id : setHomes.getPlayersNamedHomes(uuid).keySet()) {
				//Gets the name of the world the home has been set in
				String world = setHomes.getPlayersNamedHomes(uuid).get(id).getWorld();
				//Gets the description for the home
				String desc = setHomes.getPlayersNamedHomes(uuid).get(id).getDesc();
				if(desc != null) {
					p.sendMessage(ChatColor.DARK_GREEN + "Name: " + id + " - World: " + world + " - Desc: " + desc);
				} else {
					p.sendMessage(ChatColor.DARK_GREEN + "Name: " + id + " - World: " + world + " - Desc: ");
				}
			}
		}
		p.sendMessage(filler);
	}
	
	//List homes for one player to another
	private void listHomes(Player p, Player sender) {
		String uuid = p.getUniqueId().toString();
		String filler = StringUtils.repeat("-", 53);
		
		sender.sendMessage(ChatColor.DARK_RED + "Homes for the player - " + p.getName());
		sender.sendMessage(filler);
		
		//Tell the player if they have a default home set or not
		if(setHomes.hasUnknownHomes(uuid)) {
			//Gets the name of the world the home has been set in
			String world = setHomes.getPlayersUnnamedHome(uuid).getWorld().getName();
			sender.sendMessage(ChatColor.GOLD + "Default Home - World: " + world);
		}
		
		//Check to make sure the player has homes
		if(setHomes.hasNamedHomes(uuid)) {
			//Print the home with its description to the player
			for(String id : setHomes.getPlayersNamedHomes(uuid).keySet()) {
				//Gets the name of the world the home has been set in
				String world = setHomes.getPlayersNamedHomes(uuid).get(id).getWorld();
				//Gets the description for the home
				String desc = setHomes.getPlayersNamedHomes(uuid).get(id).getDesc();
				if(desc != null) {
					sender.sendMessage(ChatColor.DARK_GREEN + "Name: " + id + " - World: " + world + " - Desc: " + desc);
				} else {
					sender.sendMessage(ChatColor.DARK_GREEN + "Name: " + id + " - World: " + world + " - Desc: ");
				}
			}
		}
		sender.sendMessage(filler);
	}

}
