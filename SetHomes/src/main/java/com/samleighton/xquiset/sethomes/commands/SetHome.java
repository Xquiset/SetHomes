package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.Home;
import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.configurations.Permissions;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Objects;

public class SetHome implements CommandExecutor {
    private final SetHomes pl;
    private final Permissions permissions;

    public SetHome(SetHomes plugin) {
        pl = plugin;
        permissions = plugin.getPermissions();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Make sure the sender of the command is a player
        if (!(sender instanceof Player)) {
            //Sends message to sender of command that they're not a player
            ChatUtils.notPlayerError(sender);
            return false;
        }
        //Checks if the command sent is /sethome
        if (cmd.getName().equalsIgnoreCase("sethome")) {
            //Since we know sender is a player we can Cast sender as such
            Player p = (Player) sender;
            String uuid = p.getUniqueId().toString();
            Location home = p.getLocation();

            // Check to make sure the home world is not blacklisted
            if (pl.getBlacklistedWorlds().contains(Objects.requireNonNull(home.getWorld()).getName()) && !permissions.permit(p, "config_bypass")) {
                ChatUtils.sendError(p, "This world does not allow the usage of homes!");
                return true;
            }

            //Create a home at the players location
            Home playersHome = new Home(home);

            //No home name provided
            if (args.length < 1) {
                //Save the home
                pl.saveUnknownHome(uuid, playersHome);

                ChatUtils.sendSuccess(p, "You have set a default home!");
                //They have provided a home name and possibly description too
            } else {
                if (permissions.permit(p, "sethome")) {
                    //Check if players amount of homes vs the config max homes allowed
                    if (pl.hasNamedHomes(uuid)) {
                        int maxHomes = permissions.getMaxHomesAllowed(p);
                        Bukkit.getServer().getLogger().info("Max Homes: " + maxHomes);
                        if ((pl.getPlayersNamedHomes(uuid).size() >= maxHomes && maxHomes != 0) && !permissions.permit(p, "config_bypass")) {
                            ChatUtils.sendInfo(p, pl.config.getString("max-homes-msg"));
                            return true;
                        }
                        //Check if the player already has a home with the name they gave us
                        if (pl.getPlayersNamedHomes(uuid).containsKey(args[0])) {
                            ChatUtils.sendError(p, "You already have a home with that name, try a different one!");
                            return true;
                        }
                    }

                    // Cleanse the input argument of any non alphanumeric characters
                    String homeName = args[0].replaceAll("[^a-zA-Z0-9]", "");

                    // Ensure that after cleansing the homename still has a value
                    if (homeName.length() > 0) {
                        // Set the home name to the given name
                        playersHome.setHomeName(homeName);
                    } else {
                        ChatUtils.sendError(p, "Please use a valid home name! Only a-z & 0-9 characters are allowed.");
                        return true;
                    }


                    //Build the description as a combination of all other arguments passed
                    StringBuilder desc = new StringBuilder();
                    for (int i = 1; i <= args.length - 1; i++) {
                        desc.append(args[i]).append(" ");
                    }

                    if (!desc.toString().equals("")) {
                        playersHome.setDesc(desc.substring(0, desc.length() - 1));
                    }

                    //Save the new home
                    pl.saveNamedHome(uuid, playersHome);

                    ChatUtils.sendSuccess(p, "Your home '" + playersHome.getHomeName() + "' has been set!");
                    return true;
                }
                //Send player message because they didn't have the proper permissions
                ChatUtils.permissionError(p);
            }
            return true;
        }
        return false;
    }
}
