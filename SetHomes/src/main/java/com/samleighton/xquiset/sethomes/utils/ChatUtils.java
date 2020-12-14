package com.samleighton.xquiset.sethomes.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {
    /**
     * Used to broadcast a message to the entire server
     *
     * @param msg, The message to broadcast
     */
    public static void broadcastMessage(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.YELLOW + msg);
        }
    }

    /**
     * Used to send a 'info' formatted message to a command sender
     *
     * @param s,   The CommandSender object
     * @param msg, The message to send
     */
    public static void sendInfo(CommandSender s, String msg) {
        s.sendMessage(ChatColor.WHITE + msg);
    }

    /**
     * Used to send a 'error' formatted message to a command sender
     *
     * @param s,   The CommandSender object
     * @param msg, The message to send
     */
    public static void sendError(CommandSender s, String msg) {
        s.sendMessage(ChatColor.DARK_RED + msg);
    }

    /**
     * Used to send a 'success' formatted message to a command sender
     *
     * @param s,   The CommandSender object
     * @param msg, The message to send
     */
    public static void sendSuccess(CommandSender s, String msg) {
        s.sendMessage(ChatColor.GOLD + msg);
    }

    /**
     * Used to send a not player error message to a command sender
     *
     * @param s, The CommandSender object
     */
    public static void notPlayerError(CommandSender s) {
        s.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command!");
    }

    /**
     * Used to send a permissions error message to a command sender
     *
     * @param s, The CommandSender object
     */
    public static void permissionError(CommandSender s) {
        sendError(s, "You dont have permission to do that!");
    }

    /**
     * Used to send a to many arguments error message to a command sender
     *
     * @param s, The CommandSender object
     */
    public static void tooManyArgs(CommandSender s) {
        sendError(s, "ERROR: Too many arguments!");
    }
}
