package com.samleighton.xquiset.sethomes.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {
    public static void broadcastMessage(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.YELLOW + msg);
        }
    }

    public static void sendInfo(CommandSender s, String msg) {
        s.sendMessage(ChatColor.WHITE + msg);
    }

    public static void sendError(CommandSender s, String msg) {
        s.sendMessage(ChatColor.DARK_RED + msg);
    }

    public static void sendSuccess(CommandSender s, String msg) {
        s.sendMessage(ChatColor.GOLD + msg);
    }

    public static void notPlayerError(CommandSender s) {
        s.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command!");
    }

    public static void permissionError(CommandSender s) {
        sendError(s, "You dont have permission to do that!");
    }

    public static void tooManyArgs(CommandSender s) {
        sendError(s, "ERROR: Too many arguments!");
    }
}
