package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Blacklist implements CommandExecutor {

    private final SetHomes pl;

    public Blacklist(SetHomes plugin) {
        this.pl = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Make sure the sender of the command is a player
        if (!(sender instanceof Player)) {
            //Sends message to sender of command that they're not a player
            ChatUtils.notPlayerError(sender);
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("blacklist")) {
            Player p = (Player) sender;
            String filler = StringUtils.repeat("-", 53);

            //If they pass no parameters to the command then just list the worlds blacklisted
            if (args.length == 0) {
                //Check for proper permissions
                if (p.hasPermission("homes.blacklist_list")) {
                    if (pl.getBlacklistedWorlds().size() > 0) {
                        p.sendMessage(ChatColor.DARK_RED + "All blacklisted worlds:");
                        p.sendMessage(filler);
                        for (String w : pl.getBlacklistedWorlds()) {
                            p.sendMessage(ChatColor.LIGHT_PURPLE + " - " + w);
                        }
                        return true;
                    } else {
                        //The size of the blacklist returned from config was 0
                        ChatUtils.sendInfo(p, "There are no worlds in the blacklist currently!");
                        return true;
                    }
                } else {
                    //Proper permissions were not found
                    ChatUtils.permissionError(p);
                    return true;
                }
            } else {
                //Adding a world to the blacklist
                if (args[0].equalsIgnoreCase("add")) {
                    //Check for proper permissions
                    if (p.hasPermission("homes.blacklist_add")) {
                        //They must specify a world name for this command.
                        if (args.length == 2) {
                            //Check to make sure what they entered is actually a valid world and that it is not in the blacklist already.
                            if (getAllWorlds().contains(args[1]) && !(pl.getBlacklistedWorlds().contains(args[1]))) {
                                //Add the world to the configuration list
                                List<String> temp = pl.getBlacklistedWorlds();
                                temp.add(args[1]);
                                pl.getBlacklist().getConfig().set("blacklisted_worlds", temp);
                                pl.getBlacklist().save();

                                ChatUtils.sendSuccess(p, "You have added the world '" + args[1] + "' to the blacklist!");
                                return true;
                            } else {
                                ChatUtils.sendError(p, "There was no world found by that name!");
                                return true;
                            }
                        } else {
                            ChatUtils.sendError(p, "You must specify a world name to add to the blacklist!");
                            return true;
                        }
                    } else {
                        //Proper permission was not found
                        ChatUtils.permissionError(p);
                        return true;
                    }
                    //Removing a world from the blacklist
                } else if (args[0].equalsIgnoreCase("remove")) {
                    //Check for proper permissions
                    if (p.hasPermission("homes.blacklist_remove")) {
                        //They must specify a world name for this command.
                        if (args.length == 2) {
                            //Check to make sure the world is actually in the blacklist
                            if (pl.getBlacklistedWorlds().contains(args[1])) {
                                //Remove the world from the configuration list
                                List<String> temp = pl.getBlacklistedWorlds();
                                temp.remove(args[1]);
                                pl.getBlacklist().getConfig().set("blacklisted_worlds", temp);
                                pl.getBlacklist().save();

                                ChatUtils.sendSuccess(p, "You have removed the world '" + args[1] + "' from the blacklist!");
                                return true;
                            } else {
                                ChatUtils.sendError(p, "There was no world by that name found in the blacklist!");
                                return true;
                            }
                        } else {
                            ChatUtils.sendError(p, "You must specify a world name to remove from the blacklist!");
                            return true;
                        }
                    } else {
                        //Proper permission was not found
                        ChatUtils.permissionError(p);
                        return true;
                    }
                } else {
                    ChatUtils.sendError(p, "There is no '" + args[0] + "' blacklist action!");
                    return false;
                }
            }
        }
        return false;
    }

    //Gets all the worlds by name currently installed on the server
    private List<String> getAllWorlds() {
        List<String> worldNames = new ArrayList<String>();

        for (World w : Bukkit.getWorlds()) {
            worldNames.add(w.getName());
        }

        return worldNames;
    }

}
