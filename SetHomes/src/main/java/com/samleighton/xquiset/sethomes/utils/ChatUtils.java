package com.samleighton.xquiset.sethomes.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {
	public static void broadcastMessage(String msg) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(ChatColor.YELLOW + msg);
		}
	}
	
	public static void sendInfo(Player p, String msg) {
		p.sendMessage(ChatColor.WHITE + msg);
	}
	
	public static void sendError(Player p, String msg) {
		p.sendMessage(ChatColor.DARK_RED + msg);
	}
	
	public static void sendSuccess(Player p, String msg) {
		p.sendMessage(ChatColor.GOLD + msg);
	}
	
	public static void notPlayerError(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command!");
	}
	
	public static void permissionError(Player p) {
		sendError(p, "You dont have permission to do that!");
	}
	
	public static void tooManyArgs(Player p) {
		sendError(p, "ERROR: Too many arguments!");
	}
}
