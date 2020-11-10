package com.samleighton.xquiset.sethomes.commands;

import com.samleighton.xquiset.sethomes.SetHomes;
import com.samleighton.xquiset.sethomes.utils.ChatUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class GoHome implements CommandExecutor, Listener {

    private final SetHomes pl;
    private int taskId;
    private final int cooldown;
    private final Map<String, Long> cooldownList = new HashMap<>();
    private final boolean cancelOnMove;
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
            if (isTeleporting()) {
                ChatUtils.sendError(p, "You may not use this command while teleporting!");
                return true;
            }

            final String uuid = p.getUniqueId().toString();

            //If cooldown is active in config then check to see if player is in cooldown list
            if (!(cooldownList.containsKey(uuid)) || cooldown <= 0 || p.hasPermission("homes.config_bypass")) {
                //Player was not in cooldown list so try and teleport then
                //Teleport was successful so we return true
                return teleportHome(p, uuid, args);
                //Teleport failed
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

        if (cmd.getName().equalsIgnoreCase("home-of")) {
            if (!p.hasPermission("homes.home-of")) {
                ChatUtils.permissionError(p);
                return false;
            }

            if (isTeleporting()) {
                ChatUtils.sendError(p, "You may not use this command while teleporting!");
                return true;
            }

            if (args.length < 1 || args.length > 2) {
                ChatUtils.sendError(p, "ERROR: Incorrect number of arguments!");
                return false;
            }

            //Get current players location
            locale = p.getLocation();
            //Create offline player for their target player
            @SuppressWarnings({"deprecated"})
            OfflinePlayer targetP = Bukkit.getServer().getOfflinePlayer(args[0]);

            //Check to make sure the offline player has player on the server before
            if (!targetP.hasPlayedBefore()) {
                ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has never played before!");
                return false;
            }

            //Store the offline players uuid as a string
            String uuid = targetP.getUniqueId().toString();
            //Attempt to teleport the player to the other players home
            return teleportHomeOf(p, uuid, args);
        }
        return false;
    }

    /**
     * @param p    The player we are trying to teleport
     * @param args the arguments the player passed via command
     * @return true on successful teleport, false otherwise
     */
    private boolean teleportHome(final Player p, final String uuid, String[] args) {
        //The players location upon command execution
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
                                //Spawn particles at players feet after teleport
                                p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                                //Play note on teleport
                                p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                                //Add player to cooldown list
                                cooldownList.put(uuid, System.currentTimeMillis());
                            } else {
                                //Send title to player every second
                                p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 0, 20, 0);
                                //Play note every second
                                p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                                //Decrement time left by 1 every second
                                delay--;
                            }
                        }
                    }, 0L, 20L);
                } else {
                    //tp delay was not active in config so we teleport without starting repeating task
                    p.teleport(pl.getPlayersUnnamedHome(uuid));
                    //Spawn particles at players feet after teleport
                    p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                    //Player note on teleport
                    p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                    //Notify player of successful teleport
                    ChatUtils.sendSuccess(p, "You have been teleported home!");
                    //Add player to cooldown list
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
                            //Spawn particles at players feet after teleport
                            p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                            //Play note on teleport
                            p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                            //Add player to cooldown list
                            cooldownList.put(uuid, System.currentTimeMillis());
                        } else {
                            //Send title every second
                            p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 5, 5, 5);
                            //Play note every second
                            p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                            //Decrement the time left
                            delay--;
                        }
                    }
                }, 0L, 20L);
            } else {
                //Teleport the player to their home
                p.teleport(pl.getNamedHomeLocal(uuid, args[0]));
                //Spawn particles at players feet after teleport
                p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                //Play note on teleport
                p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                //Tell the player they've teleported
                ChatUtils.sendSuccess(p, "You have been teleported home!");
                //Add player to cooldown list
                cooldownList.put(uuid, System.currentTimeMillis());
            }
            return true;
        }
    }

    /**
     * @param p    The player you are trying to teleport
     * @param uuid the unique id string of the player they are trying to get homes of
     * @param args the arguments the player passed via command
     * @return true on successful teleport false in all other cases
     */
    private boolean teleportHomeOf(final Player p, final String uuid, String[] args) {
        locale = p.getLocation();
        //Only one argument passed so we are searching for a default home
        if (args.length == 1) {
            //Check to make sure
            if (!pl.hasUnknownHomes(uuid)) {
                ChatUtils.sendError(p, "The player " + ChatColor.WHITE + ChatColor.BOLD + args[0] + ChatColor.DARK_RED + " has no default home set!");
                return false;
            } else {
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
                                //Spawn particles at players feet after teleport
                                p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                                //Play note on teleport
                                p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                                //Add player to cooldown list
                                cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                            } else {
                                //Send title every second
                                p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 0, 20, 0);
                                //Play note every second
                                p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                                //Decrement time left by 1
                                delay--;
                            }
                        }
                    }, 0L, 20L);
                } else {
                    //tp delay was not active in config so we teleport without starting repeating task
                    p.teleport(pl.getPlayersUnnamedHome(uuid));
                    //Spawn particles at players feet after teleport
                    p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                    //Play note on teleport
                    p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                    //Notify player of successful teleport
                    ChatUtils.sendSuccess(p, "You have been teleported!");
                    //Add player to cooldown list
                    cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                }
                return true;
            }
        } else {
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
                            //Spawn particles at players feet after teleport
                            p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                            //Play note on teleport
                            p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                            //Add player to cooldown list
                            cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
                        } else {
                            //Send the player a title every second
                            p.sendTitle(ChatColor.GOLD + "Teleporting in " + delay + "...", null, 5, 5, 5);
                            //Play note every second
                            p.playNote(p.getLocation(), Instrument.DIDGERIDOO, Note.sharp(2, Note.Tone.F));
                            //Decrement time left by 1 every second
                            delay--;
                        }
                    }
                }, 0L, 20L);
            } else {
                //Teleport the player to their home
                p.teleport(pl.getNamedHomeLocal(uuid, args[1]));
                //Spawn particles at players feet after teleport
                p.spawnParticle(Particle.PORTAL, p.getLocation(), 100);
                //Play note on teleport
                p.playNote(p.getLocation(), Instrument.BELL, Note.sharp(2, Note.Tone.F));
                //Notify player of successful teleport
                ChatUtils.sendSuccess(p, "You have been teleported!");
                //Add player to cooldown list
                cooldownList.put(p.getUniqueId().toString(), System.currentTimeMillis());
            }
        }
        return true;
    }

    public boolean isTeleporting() {
        //Loop through all running/pending tasks
        for (BukkitTask t : Bukkit.getScheduler().getPendingTasks()) {
            //Attempt to find our taskId in the list
            if (t.getTaskId() == taskId) {
                //Task id was found return true
                return true;
            }
        }
        //No taskId found return false
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        //Make sure the player triggering the move event is the player currently trying to teleport
        if (e.getPlayer() == p) {
            //Get the running delay task that was created for the player
            if (isTeleporting()) {
                //Check to make sure the player moved from the location that they started the teleport at
                if (e.getPlayer().getLocation().getX() != locale.getX() || e.getPlayer().getLocation().getY() != locale.getY()) {
                    //Check our config var to see if we continue with canceling the task or if the player has bypass permissions
                    if (cancelOnMove && !e.getPlayer().hasPermission("homes.config_bypass")) {
                        //cancel the task
                        pl.cancelTask(taskId);
                        //Tell them the teleport has been canceled
                        ChatUtils.sendInfo(e.getPlayer(), pl.getConfig().getString("tp-cancelOnMove-msg"));
                        //Play a snare sound when the player moves during the teleport delay
                        e.getPlayer().playNote(e.getPlayer().getLocation(), Instrument.SNARE_DRUM, Note.natural(0, Note.Tone.F));
                    }
                }
            }
        }
    }
}