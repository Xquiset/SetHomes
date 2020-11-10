package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteHome implements CommandExecutor {
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

        //The player that sent the command
        Player p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("delhome")) {
            //Check for proper permissions
            if (!p.hasPermission("homes.delhome")) {
                ChatUtils.permissionError(p);
                return false;
            }

            //The uuid of the player that sent the command
            String uuid = p.getUniqueId().toString();

            //Check if they're trying to delete the default home or a named home base on parameters entered
            if (args.length < 1) {
                //Make sure they have a home before we try and delete one
                if (!pl.hasUnknownHomes(uuid)) {
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
                if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
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

        if (cmd.getName().equalsIgnoreCase("delhome-of")) {
            //Check for proper permissions
            if (!p.hasPermission("homes.delhome-of")) {
                ChatUtils.permissionError(p);
                return false;
            }

            //Check for the correct range of argument numbers
            if (args.length < 1 || args.length > 2) {
                ChatUtils.sendError(p, "ERROR: Incorrect number of arguments!");
                return false;
            }

            //Create new offline player for the player name they entered
            @SuppressWarnings({"deprecated"})
            OfflinePlayer targetP = Bukkit.getServer().getOfflinePlayer(args[0]);
            //Check to be sure the player has played on the server before
            if (!targetP.hasPlayedBefore()) {
                ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has never played here before!");
                return false;
            }

            String uuid = targetP.getUniqueId().toString();
            //If only player name given try deleting the default home
            if (args.length == 1) {
                //Check to see if the player has a default home set before we try to delete it
                if (!pl.hasUnknownHomes(uuid)) {
                    ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has no default home!");
                    return false;
                } else {
                    //Perform deletion of the un-named home
                    pl.deleteUnknownHome(uuid);
                    ChatUtils.sendSuccess(p, "You have deleted the default home for player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.GOLD + "!");
                    return true;
                }
            } else {
                //Attempt to find a named home with the named passed to us
                String homeName = args[1];
                if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(homeName))) {
                    ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has no homes by that name!");
                    return false;
                } else {
                    //Perform deletion of the named home
                    pl.deleteNamedHome(uuid, homeName);
                    ChatUtils.sendSuccess(p, "You have deleted the '" + homeName + "' home for player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.GOLD + "!");
                    return true;
                }
            }
        }
        return false;
    }

}
