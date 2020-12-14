package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMax implements CommandExecutor {

    private final SetHomes pl;

    public SetMax(SetHomes plugin) {
        pl = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Ensure that the command passed was 'setmax'
        if (cmd.getLabel().equalsIgnoreCase("setmax")) {
            // Check to see if the command sender was a player
            if (sender instanceof Player) {
                // Sender is an instance of Player so we can safely cast
                Player p = (Player) sender;

                // Test to see if the player has the proper permissions
                if (!p.hasPermission("homes.setmax")) {
                    // Send player permission error message
                    ChatUtils.permissionError(p);
                    return true;
                }
            }
            // Attempt to perform the config update
            return doSetMaxHomes(sender, args);
        }
        return false;
    }

    /**
     * Used to set the max homes for a specific group
     *
     * @param group The group to set the maximum homes for
     * @param num   The maximum number of homes to allow
     */
    private void setMaxHomes(String group, int num) {
        // Update the config the new max-homes entry
        pl.getConfig().set("max-homes." + group, num);
        // Save the config
        pl.saveConfig();
    }

    /**
     * Used to perform the common parts of the set max homes between player and command
     * line senders i.e arg count checking and message sending
     *
     * @param sender, The sender of the command
     * @param args,   The arguments passed by the CommandSender
     * @return true on successful config update, false if there was an error
     */
    private boolean doSetMaxHomes(CommandSender sender, String[] args) {
        // The length of the command arguments array
        final int l = args.length;
        // Check to ensure the correct number of arguments were passed to the command
        final boolean arg_count_check = l != 2;

        // The string to hold the group
        String group;
        // The integer to hold the number of max homes
        int homeNum;

        // Test to ensure the proper number of arguments were passed
        if (arg_count_check) {
            // Notify the user of the error
            ChatUtils.sendError(sender, "Wrong number of arguments passed!");
            // return false because of the error
            return false;
        }

        // Assign the group the value of the first argument passed
        group = args[0];

        // Attempt to assign the value of the second argument to homeNum
        try {
            // Attempt to parse the last argument as an Integer
            homeNum = Integer.parseInt(args[1]);
            // Perform the config update
            setMaxHomes(group, homeNum);
            // Notify the user of successful config update
            ChatUtils.sendSuccess(sender, "You have set the max homes to be '" + homeNum + "' for the group '" + group + "'!");
            // Catch if the last argument was not parsable as an Integer
        } catch (NumberFormatException e) {
            // Notify the user of the error
            ChatUtils.sendError(sender, "The second argument must be a number!");
            // return false because of the error
            return false;
        }
        // return true for successful config update
        return true;
    }
}
