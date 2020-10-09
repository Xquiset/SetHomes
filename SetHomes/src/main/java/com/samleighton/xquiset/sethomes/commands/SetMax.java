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
        if (cmd.getLabel().equalsIgnoreCase("setmax")) {
            final int l = args.length;
            String group;
            int homeNum;
            if (sender instanceof Player) {
                // We can cast sender as a player since we know sender is an instance of a Player object
                Player p = (Player) sender;

                // Test to see if the player has the proper permissions
                if (!p.hasPermission("homes.setmax")) {
                    // Send player permission error message
                    ChatUtils.permissionError(p);
                }

                // Test to ensure the proper number of arguments were passed
                if (l != 2) {
                    ChatUtils.sendError(p, "Wrong number of arguments passed!");
                    return false;
                }

                // Assign the group the value of the first argument passed
                group = args[0];

                // Attempt to assign the value of the second argument to homeNum
                try {
                    homeNum = Integer.parseInt(args[1]);
                    setMaxHomes(group, homeNum);
                    ChatUtils.sendError(p, "The group you entered does not exist!");
                } catch (NumberFormatException e) {
                    ChatUtils.sendError(p, "The second argument must be a number!");
                    return false;
                }
            } else {
                // Test to ensure the proper number of arguments were passed
                if (l != 2) {
                    ChatUtils.sendError(sender, "Wrong number of arguments passed!");
                    return false;
                }

                // Assign the group the value of the first argument passed
                group = args[0];

                // Attempt to assign the value of the second argument to homeNum
                try {
                    homeNum = Integer.parseInt(args[1]);
                    setMaxHomes(group, homeNum);
                    ChatUtils.sendSuccess(sender, "You have set the max homes to be '" + homeNum + "' for the group '" + group + "'!");
                } catch (NumberFormatException e) {
                    ChatUtils.sendError(sender, "The second argument must be a number!");
                    return false;
                }
            }

            return true;
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
        pl.getConfig().set("max-homes." + group, num);
        pl.saveConfig();
    }
}
