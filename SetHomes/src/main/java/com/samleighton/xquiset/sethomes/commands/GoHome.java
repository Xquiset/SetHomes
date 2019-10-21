package com.samleighton.xquiset.sethomes.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Level;

public class GoHome implements CommandExecutor, Listener {

    private final SetHomes pl;
    private int taskId, cooldown;
    private Map<String, Long> cooldownList = new HashMap<String, Long>();
    private boolean cancelOnMove;
    private Location locale = null;
    private Player p;

    public GoHome(SetHomes plugin) {
        pl = plugin;
        cooldown = pl.getConfig().getInt("tp-cooldown");
        cancelOnMove = pl.getConfig().getBoolean("tp-cancelOnMove");
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //Make sure the sender of the command is a player
        if (!(sender instanceof Player)) {
            //Sends message to sender of command that they're not a player
            ChatUtils.notPlayerError(sender);
            return false;
        }

        p = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("home")) {
            final String uuid = p.getUniqueId().toString();

            //If cooldown is active in config then check to see if player is in cooldown list
            if (!(cooldownList.containsKey(uuid)) || cooldown <= 0 || p.hasPermission("homes.config_bypass")) {
                //Player was not in cooldown list so try and teleport then
                if (teleportHome(p, uuid, args)) {
                    //Teleport was successful so we return true
                    return true;
                }
                //Teleport failed
                return false;
            } else {
                //Player was found in cooldown list
                //Calculate the amount of time left before they can run the command again
                long timeLeft = ((cooldownList.get(uuid) / 1000) + cooldown) - (System.currentTimeMillis() / 1000);
                //The player has not passed the amount of time needed
                if (timeLeft > 0) {
                    //Tell the player they need to wait still
                    ChatUtils.sendInfo(p, StringUtils.replace(pl.getConfig().getString("tp-cooldown-msg"), "%s", String.valueOf(timeLeft)));
                    return true;
                } else {
                    //The player has waited long enough so we remove them from the list and try to teleport them
                    cooldownList.remove(uuid);
                    if (teleportHome(p, uuid, args)) {
                        //The player was successfully teleported
                        return true;
                    }
                }
            }
        }

        if(cmd.getName().equalsIgnoreCase("home-of")){
            if(!p.hasPermission("homes.home-of")){
                ChatUtils.permissionError(p);
                return false;
            }

            if(args.length < 1 || args.length > 2){
                ChatUtils.sendError(p, "ERROR: Incorrect number of arguments!");
                return false;
            }

            //Get current players location
            locale = p.getLocation();
            //Create offline player for their target player
            OfflinePlayer targetP = Bukkit.getServer().getOfflinePlayer(args[0]);

            //Check to make sure the offline player has player on the server before
            if(!targetP.hasPlayedBefore()){
                ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has never played here before!");
                return false;
            }

            //Store the offline players uuid as a string
            String uuid = targetP.getUniqueId().toString();
            //Attempt to teleport the player to the other players home
            if(teleportHomeOf(p, uuid, args)){
                return true;
            }
        }
        return false;
    }

    /**
     * @param p    The player we are trying to teleport
     * @param args the arguments the player passed via command
     * @return true on successful teleport, false otherwise
     */
    public boolean teleportHome(final Player p, final String uuid, String[] args) {
        locale = p.getLocation();
        if (args.length < 1) {
            //If they have no home tell them
            if (!(pl.hasUnknownHomes(uuid))) {
                ChatUtils.sendError(p, "You have no Default Home!");
                return false;
            } else {
                //Teleport the player to their home and send them a message telling them so
                if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
                    //Run a timer to countdown the amount of time for tp delay and display a message on the users screen
                    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                        int delay = pl.getConfig().getInt("tp-delay");
                        public void run() {
                            if (delay == 0) {
                                //Cancel this repeating task
                                pl.cancelTask(taskId);
                                //Teleport the player to their home
                                p.teleport(pl.getPlayersUnnamedHome(uuid));
                                //Add player to cooldown list
                                cooldownList.put(uuid, System.currentTimeMillis());
                            }else{
                                p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 0, 20, 0);
                                delay--;
                            }
                        }
                    }, 0L, 20L);
                } else {
                    //tp delay was not active in config so we teleport without starting repeating task
                    p.teleport(pl.getPlayersUnnamedHome(uuid));
                    ChatUtils.sendSuccess(p, "You have been teleported home!");
                    cooldownList.put(uuid, System.currentTimeMillis());
                }
                return true;
            }
        } else if (args.length > 1) {
            //Tell the player if they've sent to many arguments with the command
            ChatUtils.tooManyArgs(p);
            return false;
        } else {
            //Check if they have any named homes or a home with the given name
            if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(args[0]))) {
                ChatUtils.sendError(p, "You have no homes by that name!");
                return false;
            }
            final String homeName = args[0];
            //Teleport the player to there home and send them a message telling them so
            if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
                //Run a timer to countdown the amount of time for tp delay and display a message on the users screen
                taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                    int delay = pl.getConfig().getInt("tp-delay");
                    public void run() {
                        if (delay == 0) {
                            pl.cancelTask(taskId);
                            //Teleport the player to their home
                            p.teleport(pl.getNamedHomeLocal(uuid, homeName));
                            cooldownList.put(uuid, System.currentTimeMillis());
                        }else{
                            p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 5, 5, 5);
                            delay--;
                        }
                    }
                }, 0L, 20L);
            } else {
                //Teleport the player to their home
                p.teleport(pl.getNamedHomeLocal(uuid, args[0]));
                ChatUtils.sendSuccess(p, "You have been teleported home!");
                cooldownList.put(uuid, System.currentTimeMillis());
            }
            return true;
        }
    }

    /**
     *
     * @param p The player you are trying to teleport
     * @param uuid the unique id string of the player
     * @param args the arguments the player passed with the command
     * @return true on successful teleport false in all other cases
     */
    private boolean teleportHomeOf(final Player p, final String uuid, String[] args){
        //Only one argument passed so we are searching for a default home
        if(args.length == 1){
            //Check to make sure
            if(!pl.hasUnknownHomes(uuid)){
                ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has no default home set!");
                return false;
            }else{
                //Teleport the player to the home and send them a message telling them so
                if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
                    //Run a timer to countdown the amount of time for tp delay and display a message on the users screen
                    taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                        int delay = pl.getConfig().getInt("tp-delay");
                        public void run() {
                            if (delay == 0) {
                                //Cancel this repeating task
                                pl.cancelTask(taskId);
                                //Teleport the player to their home
                                p.teleport(pl.getPlayersUnnamedHome(uuid));
                                //Add player to cooldown list
                                cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                            }else{
                                p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 0, 20, 0);
                                delay--;
                            }
                        }
                    }, 0L, 20L);
                } else {
                    //tp delay was not active in config so we teleport without starting repeating task
                    p.teleport(pl.getPlayersUnnamedHome(uuid));
                    ChatUtils.sendSuccess(p, "You have been teleported!");
                    cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                }
                return true;
            }
        }else{
            final String homeName = args[1];
            if (!(pl.hasNamedHomes(uuid)) || !(pl.getPlayersNamedHomes(uuid).containsKey(homeName))) {
                ChatUtils.sendError(p, "That user has no homes by that name!");
                return false;
            }

            //Teleport the player to there home and send them a message telling them so
            if (pl.getConfig().getInt("tp-delay") > 0 && !p.hasPermission("homes.config_bypass")) {
                //Run a timer to countdown the amount of time for tp delay and display a message on the users screen
                taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, new Runnable() {
                    int delay = pl.getConfig().getInt("tp-delay");
                    public void run() {
                        if (delay == 0) {
                            pl.cancelTask(taskId);
                            //Teleport the player to their home
                            p.teleport(pl.getNamedHomeLocal(uuid, homeName));
                            cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                        }else{
                            p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 5, 5, 5);
                            delay--;
                        }
                    }
                }, 0L, 20L);
            } else {
                //Teleport the player to their home
                p.teleport(pl.getNamedHomeLocal(uuid, args[1]));
                ChatUtils.sendSuccess(p, "You have been teleported!");
                cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        //Make sure the player triggering the move event is the player currently trying to teleport
        if(e.getPlayer() == p){
            //Get the running delay task that was created for the player
            for (BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
                //Get the task that matches the one displaying for this player
                if(task.getTaskId() == taskId){
                    //Check to make sure the player moved from the location that they started the teleport at
                    if(e.getPlayer().getLocation().getX() != locale.getX() || e.getPlayer().getLocation().getY() != locale.getY()){
                        //Check our config var to see if we continue with canceling the task or if the player has bypass permissions
                        if (cancelOnMove && !e.getPlayer().hasPermission("homes.config_bypass")) {
                            //cancel the task
                            pl.cancelTask(taskId);
                            //Tell them the teleport has been canceled
                            ChatUtils.sendInfo(e.getPlayer(), pl.getConfig().getString("tp-cancelOnMove-msg"));
                        }
                    }
                }
            }
        }
    }
}
