package com.samleighton.xquiset.sethomes.configurations;

import java.util.HashMap;
import java.util.Objects;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

public class Permissions {
    private final HashMap<String, String> _simplePerms;
    private final Permission _vaultPerms;
    private final LuckPerms _luckPerms;
    private final HashMap<String, Integer> _maxHomes;

    public Permissions(HashMap<String, String> simplePerms, Permission vaultPerms, LuckPerms luckPerms, HashMap<String, Integer> maxHomes) {
        _simplePerms = simplePerms;
        _vaultPerms = vaultPerms;
        _luckPerms = luckPerms;
        _maxHomes = maxHomes;
    }

    public boolean permit(Player p, String permission) {
        if (p.hasPermission(String.format("homes.%s", permission))) {
            return true;
        }
        String result = _simplePerms.get(permission);
        if (result == null) {
            return false;
        }
        if ( result.equals("allow")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the maximum homes allowed for a player. Will take the greatest value when player
     * has multiple groups assigned to them
     *
     * @param p, The player we're attempting to get the homes for
     * @return maximum number of homes allowed for that player
     */
    public int getMaxHomesAllowed(Player p) {
        int maxHomes = 0;

        if (_luckPerms != null) {
            // First try luck perms
            // Loop over groups found in config list
            for (String group : _maxHomes.keySet()) {
                if (p.hasPermission("group." + group)) {
                    int max_home_val = _maxHomes.get(group);
                    if (maxHomes < max_home_val) {
                        maxHomes = max_home_val;
                    }
                }
            }
            return maxHomes;
        }
        if (_vaultPerms != null) {
            // Loop over groups found by Vault
            for (String group : _vaultPerms.getPlayerGroups(p)) {
                for (String g : _maxHomes.keySet()) {
                    if (group.equalsIgnoreCase(g)) {
                        if (_maxHomes.get(g) > maxHomes) {
                            maxHomes = _maxHomes.get(g);
                        }
                    }
                }
            }
            return maxHomes;
        }
        if (_maxHomes.get("default") != null) {
            // If not Vault or Luck then use the default
            return  _maxHomes.get("default");
        }

        return maxHomes;
    }
}