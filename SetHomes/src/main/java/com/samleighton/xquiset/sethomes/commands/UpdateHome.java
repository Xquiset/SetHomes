package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.Home;
import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class UpdateHome implements CommandExecutor {

    private final SetHomes pl;

    public UpdateHome(SetHomes plugin) {
        this.pl = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Make sure that the command is sent by a player
        if (!(sender instanceof Player)) {
            ChatUtils.notPlayerError(sender);
            return false;
        }

        // Cast sender as a player
        Player p = (Player) sender;

        // Check to make sure uhome was called
        if (cmd.getName().equalsIgnoreCase("uhome")) {
            // Ensure the player has proper permission
            if (!p.hasPermission("homes.uhome")) {
                // Send player permission error notice
                ChatUtils.permissionError(p);
                return false;
            }

            // Capture the uuid of the player as a string
            String uuid = p.getPlayer().getUniqueId().toString();
            Location home = p.getLocation();

            // Check to make sure the player is not trying to bypass the blacklist
            if (pl.getBlacklistedWorlds().contains(home.getWorld().getName()) && !p.hasPermission("homes.config_bypass")) {
                ChatUtils.sendError(p, "This world does not allow the usage of homes!");
                return true;
            }

            // Create a new home at the players location
            Home newHome = new Home(home);

            // Check if the player is trying to update a name or unnamed home based on args length
            if (args.length < 1) {
                // Check to make sure the player has a named home
                if (!pl.hasUnknownHomes(uuid)) {
                    ChatUtils.sendError(p, "No Default Home is currently set!");
                    return true;
                }

                // Save the new unknown home
                pl.saveUnknownHome(uuid, newHome);

                // Notify the player of success
                ChatUtils.sendSuccess(p, "You have successfully updated your default home!");
                return true;
            } else {
                // Check to make sure the player actually has named homes
                if (pl.hasNamedHomes(uuid)) {
                    //Check if the player already has a home with the name they gave us
                    if (!pl.getPlayersNamedHomes(uuid).containsKey(args[0])) {
                        // Notify player that no home was found
                        ChatUtils.sendError(p, "You do not have a home with that name, try a different one!");
                        return true;
                    } else {
                        // A home was found so we set the new home name as the same one
                        newHome.setHomeName(args[0]);

                        // See if the player supplied a new description
                        if (args.length > 1) {
                            //Build the description as a combination of all other arguments passed
                            String desc = "";
                            for (int i = 1; i <= args.length - 1; i++) {
                                desc += args[i] + " ";
                            }

                            // Set the description for the new home
                            if (!desc.equals("")) {
                                newHome.setDesc(desc.substring(0, desc.length() - 1));
                            }
                        } else {
                            // Get the players homes so that we can retrieve the description of the home they're updating
                            HashMap<String, Home> playersHomes = pl.getPlayersNamedHomes(uuid);
                            // Get the home they supplied
                            Home oldHome = playersHomes.get(args[0]);
                            // Grab the description of the old home
                            String desc = oldHome.getDesc();
                            // Set the description of the new home to be the old home description
                            newHome.setDesc(desc);
                        }

                        // Save the new home in place of the old one
                        pl.saveNamedHome(uuid, newHome);

                        // Notify the player of the updated home success
                        ChatUtils.sendSuccess(p, "Your home '" + newHome.getHomeName() + "' has been updated!");
                        return true;
                    }
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("uhome-of")) {
            // Ensure the player has proper permission
            if (!p.hasPermission("homes.uhome-of")) {
                // Send player permission error notice
                ChatUtils.permissionError(p);
                return false;
            }

            // Ensure the player has given a player name to update the home for
            if (args.length < 1 || args.length > 2) {
                ChatUtils.sendError(p, "ERROR: Incorrect number of arguments!");
                return false;
            }

            // Create offline player for their target player
            @SuppressWarnings({"deprecated"})
            OfflinePlayer targetP = Bukkit.getServer().getOfflinePlayer(args[0]);
            // Check to make sure the offline player has played on the server before
            if (!targetP.hasPlayedBefore()) {
                ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has never played before!");
                return true;
            }

            //Store the offline players uuid as a string
            String uuid = targetP.getUniqueId().toString();
            // Create a new home at the players location
            Home newHome = new Home(p.getLocation());

            if (args.length == 1) {
                // Check to make sure the player has a unnamed home
                if (!pl.hasUnknownHomes(uuid)) {
                    ChatUtils.sendError(p, "No Default Home is currently set for the player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + "!");
                    return true;
                }

                // Save the new unknown home
                pl.saveUnknownHome(uuid, newHome);

                // Notify the player of success
                ChatUtils.sendSuccess(p, "You have successfully updated the default home for the player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + "!");
                return true;

            }

            if (args.length == 2) {
                //Check if the player already has a home with the name they gave us
                if (!pl.getPlayersNamedHomes(uuid).containsKey(args[1])) {
                    // Notify player that no home was found
                    ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " does not have a home with that name!");
                    return true;
                }
                // A home was found so we set the new home name as the same one
                newHome.setHomeName(args[1]);
                // Get the players homes so that we can retrieve the description of the home they're updating
                HashMap<String, Home> playersHomes = pl.getPlayersNamedHomes(uuid);
                // Get the home they supplied
                Home oldHome = playersHomes.get(args[1]);
                // Grab the description of the old home
                String desc = oldHome.getDesc();
                // Set the description of the new home to be the old home description
                newHome.setDesc(desc);

                // Save the new home in place of the old one
                pl.saveNamedHome(uuid, newHome);

                // Notify the player of the updated home success
                ChatUtils.sendSuccess(p, "You have updated the home '" + newHome.getHomeName() + "' for the player named " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + "!");
                return true;
            }
        }
        return false;
    }
}
