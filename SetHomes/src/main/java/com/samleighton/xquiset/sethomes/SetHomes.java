package com.samleighton.xquiset.sethomes;

import com.samleighton.xquiset.sethomes.commands.*;
import com.samleighton.xquiset.sethomes.configurations.Homes;
import com.samleighton.xquiset.sethomes.configurations.WorldBlacklist;
import com.samleighton.xquiset.sethomes.eventListeners.EventListener;
import com.samleighton.xquiset.sethomes.versionControl.Updater;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Xquiset
 * @version 1.2.6
 */
public class SetHomes extends JavaPlugin {

    public FileConfiguration config;
    private FileConfiguration homesCfg, blacklistCfg;
    private Permission perms = null;
    private WorldBlacklist blacklist = new WorldBlacklist(this);
    private Homes homes = new Homes(this);
    private String LOG_PREFIX = "[SetHomes] ";
    private String configHeader = StringUtils.repeat("-", 26)
            + "\n\tSetHomes Config\t\n" + StringUtils.repeat("-", 26) + "\n"
            + "Messages: \n\tYou can use chat colors in messages with this symbol §.\n"
            + "\tI.E: §b will change any text after it to an aqua blue color.\n"
            + "\tColor codes may be found here https://www.digminecraft.com/lists/color_list_pc.php\n"
            + "Time: \n\tAny time value is based in seconds.\n"
            + "Things to Note: \n\tSet any integer option to 0 for it to be ignored.\n"
            + "\tThe max-homes does not include the default un-named home.\n"
            + "\tUse %s as the seconds variable in the cooldown message.\n";

    @Override
    public void onEnable() {
        //Setup permission
        setupPermissions();
        //Load the configuration files on enable or reload
        loadConfigurationFiles();
        //Initialize the command executors
        registerCommands();
        //Register event listener
        new EventListener(this);
        // Check if auto-updating is enabled
        if (config.getBoolean("auto-update")) {
            getServer().getLogger().info(LOG_PREFIX + "Checking for updates...");

            Updater updater = new Updater(this, 312833, this.getFile(), Updater.UpdateType.DEFAULT, true);
            Updater.UpdateResult result = updater.getResult();

            switch (result) {
                case SUCCESS:
                    getServer().getLogger().log(Level.INFO, LOG_PREFIX + "Staged update for next reload/restart");
                    break;
                case DISABLED:
                    getServer().getLogger().log(Level.INFO, LOG_PREFIX + "Auto-Updating has been disabled, continuing...");
                    break;
                case NO_UPDATE:
                    getServer().getLogger().log(Level.INFO, LOG_PREFIX + "No new version detected");
                    break;
            }
        } else {
            getServer().getLogger().info(LOG_PREFIX + "Auto-Updating has been disabled, continuing...");
        }
    }

    //Unused
    @Override
    public void onDisable() {

    }

    /**
     * Loads in config if exists
     * If config does not exist
     * then we create it with
     * preset default paths
     */
    private void loadConfigurationFiles() {
        //Get the configs
        homesCfg = getHomes().getConfig();
        blacklistCfg = getBlacklist().getConfig();

        //Establish blacklist default config path
        if (!(blacklistCfg.isSet("blacklisted_worlds"))) {
            blacklistCfg.addDefault("blacklisted_worlds", new ArrayList<String>());
        }

        //Save defaults
        blacklistCfg.options().copyDefaults(true);
        getBlacklist().save();

        //Establish homes default paths
        if (!(homesCfg.isSet("allNamedHomes") || homesCfg.isSet("unknownHomes"))) {
            homesCfg.addDefault("allNamedHomes", new HashMap<String, HashMap<String, Home>>());
            homesCfg.addDefault("unknownHomes", new HashMap<String, Home>());
        }

        //Save defaults
        homesCfg.options().copyDefaults(true);
        getHomes().save();

        //Copy homes from old config if they were set and delete them from default config
        config = getConfig();
        copyHomes(config, getHomes());

        //Setup defaults for config
        if (!config.isSet("max-homes") || !config.isSet("max-homes-msg") || !config.isSet("tp-delay")
                || !config.isSet("tp-cooldown") || !config.isSet("tp-cancelOnMove")
                || !config.isSet("tp-cancelOnMove-msg") || !config.isSet("tp-cooldown-msg")
                || !config.isSet("auto-update")) {
            //Sets the max homes to unlimited by default

            if (!config.isSet("max-homes")) {
                config.set("max-homes.default", 0);
            }
            if (!config.isSet("max-homes-msg")) {
                config.set("max-homes-msg", "§4You have reached the maximum amount of saved homes!");
            }
            if (!config.isSet("tp-delay")) {
                config.set("tp-delay", 3);
            }
            if (!config.isSet("tp-cooldown")) {
                config.set("tp-cooldown", 0);
            }
            if (!config.isSet("tp-cancelOnMove")) {
                config.set("tp-cancelOnMove", false);
            }
            if (!config.isSet("tp-cancelOnMove-msg")) {
                config.set("tp-cancelOnMove-msg", "§4Movement detected! Teleporting has been cancelled!");
            }
            if (!config.isSet("tp-cooldown-msg")) {
                config.set("tp-cooldown-msg", "§4You must wait another %s second(s) before teleporting!");
            }
            if (!config.isSet("auto-update")) {
                config.set("auto-update", true);
            }
        }

        if (config.isSet("max-homes")) {
            if (config.getInt("max-homes") != 0) {
                int maxHomes = config.getInt("max-homes");
                Bukkit.getServer().getLogger().log(Level.WARNING, "[SetHomes] We've detected you previously set the max homes within config.yml. We have updated the config and suggest \n" +
                        "you read how to properly setup the config for your permission groups on the plugin page: https://dev.bukkit.org/projects/set-homes");
                config.set("max-homes.default", maxHomes);
            }
        }

        config.options().header(configHeader);
        config.options().copyDefaults(true);
        saveConfig();

        getHomes().reloadConfig();
        getBlacklist().reloadConfig();
    }

    /**
     * Registers the command classes
     * to handle the execution of these
     * commands
     */
    private void registerCommands() {
        this.getCommand("sethome").setExecutor(new SetHome(this));
        this.getCommand("homes").setExecutor(new ListHomes(this));
        this.getCommand("delhome").setExecutor(new DeleteHome(this));
        this.getCommand("home").setExecutor(new GoHome(this));
        this.getCommand("strike").setExecutor(new Strike(this));
        this.getCommand("blacklist").setExecutor(new Blacklist(this));
        this.getCommand("home-of").setExecutor(new GoHome(this));
        this.getCommand("delhome-of").setExecutor(new DeleteHome(this));
        this.getCommand("uhome").setExecutor(new UpdateHome(this));
        this.getCommand("uhome-of").setExecutor(new UpdateHome(this));
        this.getCommand("setmax").setExecutor(new SetMax(this));
    }

    /**
     * Used to initialize the Permissions service with VaultAPI
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * @param uuid of the player we're attempting to get homes for
     * @return a hashmap of all the players homes
     */
    public HashMap<String, Home> getPlayersNamedHomes(String uuid) {
        HashMap<String, Home> playersNamedHomes = new HashMap<String, Home>();
        String homesPath = "allNamedHomes." + uuid;
        homesCfg = getHomes().getConfig();

        //Loop through the players home list and create a hash map with the home names as a key and home as value
        for (String id : homesCfg.getConfigurationSection(homesPath).getKeys(false)) {
            String path = homesPath + "." + id + ".";
            World world = getServer().getWorld(homesCfg.getString(path + ".world"));
            double x = homesCfg.getDouble(path + ".x");
            double y = homesCfg.getDouble(path + ".y");
            double z = homesCfg.getDouble(path + ".z");
            float pitch = Float.parseFloat(homesCfg.getString(path + ".pitch"));
            float yaw = Float.parseFloat(homesCfg.getString(path + ".yaw"));

            Location home = new Location(world, x, y, z, yaw, pitch);
            Home h = new Home(home);

            //Check if there is a desc set
            if (homesCfg.isSet(path + ".desc")) {
                h.setDesc(homesCfg.getString(path + ".desc"));
            }

            playersNamedHomes.put(id, h);
        }

        return playersNamedHomes;
    }

    /**
     * @param uuid     player for which to get the home from
     * @param homeName the name of the home to create the location for
     * @return location of the players named home
     */
    public Location getNamedHomeLocal(String uuid, String homeName) {
        Home h = getPlayersNamedHomes(uuid).get(homeName);
        World world = getServer().getWorld(h.getWorld());
        Location home = new Location(world, h.getX(), h.getY(), h.getZ(), h.getYaw(), h.getPitch());

        return home;
    }

    /**
     * Gets the Map of Groups to Max homes from the config
     *
     * @return a HashMap with the Group as the Key and the max number of homes as the value
     */
    public HashMap<String, Integer> getMaxHomes() {
        HashMap<String, Integer> maxHomes = new HashMap<String, Integer>();
        String maxHomesPath = "max-homes";
        for (String id : config.getConfigurationSection(maxHomesPath).getKeys(false)) {
            String path = maxHomesPath + "." + id;
            maxHomes.put(id, config.getInt(maxHomesPath + "." + id));
        }

        return maxHomes;
    }

    /**
     * @param uuid of the player we're checking named homes for
     * @return true || false
     */
    public boolean hasNamedHomes(String uuid) {
        homesCfg = getHomes().getConfig();
        return homesCfg.contains("allNamedHomes." + uuid) && homesCfg.isSet("allNamedHomes." + uuid);
    }

    /**
     * @param uuid of the player
     * @param uuid player to save home for
     * @param home object of the home object to save
     */
    public void saveNamedHome(String uuid, Home home) {
        String path = "allNamedHomes." + uuid + "." + home.getHomeName();
        homesCfg = getHomes().getConfig();
        homesCfg.set(path + ".world", home.getWorld());
        homesCfg.set(path + ".x", home.getX());
        homesCfg.set(path + ".y", home.getY());
        homesCfg.set(path + ".z", home.getZ());
        homesCfg.set(path + ".pitch", home.getPitch());
        homesCfg.set(path + ".yaw", home.getYaw());
        homesCfg.set(path + ".desc", home.getDesc());
        getHomes().save();
    }

    /**
     * @param uuid     of the player to get home list for
     * @param homeName name of home to delete from list
     */
    public void deleteNamedHome(String uuid, String homeName) {
        String path = "allNamedHomes." + uuid + "." + homeName;
        getHomes().getConfig().set(path, null);
        getHomes().save();
        getHomes().reloadConfig();
    }

    /**
     * @param uuid of the player to get a home for
     * @return the home to teleport the player to
     */
    public Location getPlayersUnnamedHome(String uuid) {
        //Grabs all the data from the configuration file
        String path = "unknownHomes." + uuid;
        homesCfg = getHomes().getConfig();
        World world = getServer().getWorld(homesCfg.getString(path + ".world"));
        double x = homesCfg.getDouble(path + ".x");
        double y = homesCfg.getDouble(path + ".y");
        double z = homesCfg.getDouble(path + ".z");
        float pitch = Float.parseFloat(homesCfg.getString(path + ".pitch"));
        float yaw = Float.parseFloat(homesCfg.getString(path + ".yaw"));

        Location home = new Location(world, x, y, z, yaw, pitch);

        return home;
    }

    /**
     * @param uuid of the player we're checking unnamed homes for
     * @return true || false
     */
    public boolean hasUnknownHomes(String uuid) {
        homesCfg = getHomes().getConfig();
        return homesCfg.contains("unknownHomes." + uuid);
    }

    /**
     * @param uuid of the player to save a home for
     * @param home to save
     */
    public void saveUnknownHome(String uuid, Home home) {
        //Saves the variables to construct a home location to the configuration file
        String path = "unknownHomes." + uuid;
        homesCfg = getHomes().getConfig();
        homesCfg.set(path + ".world", home.getWorld());
        homesCfg.set(path + ".x", home.getX());
        homesCfg.set(path + ".y", home.getY());
        homesCfg.set(path + ".z", home.getZ());
        homesCfg.set(path + ".pitch", home.getPitch());
        homesCfg.set(path + ".yaw", home.getYaw());
        getHomes().save();
    }

    /**
     * @param uuid of the player to delete default home for
     */
    public void deleteUnknownHome(String uuid) {
        //Set the path to the players id as null
        String path = "unknownHomes." + uuid;
        getHomes().getConfig().set(path, null);
        getHomes().save();
        getHomes().reloadConfig();
    }

    /**
     * Used to manipulate the WorldBlacklist configuration file
     *
     * @return WorldBlacklist object
     */
    public WorldBlacklist getBlacklist() {
        return blacklist;
    }

    /**
     * Used for reading the world names in from the blacklist config
     *
     * @return list of World Names
     */
    public List<String> getBlacklistedWorlds() {
        return getBlacklist().getConfig().getStringList("blacklisted_worlds");
    }

    /**
     * Used to get the homes
     *
     * @return Homes object
     */
    public Homes getHomes() {
        return homes;
    }

    /**
     * Used to copy homes from default config into new homes config
     *
     * @param config     Orginal old configuration
     * @param homeConfig New homes configuration
     */
    private void copyHomes(FileConfiguration config, Homes homeConfig) {
        if (config.contains("allNamedHomes")) {
            if (config.isSet("allNamedHomes")) {
                homeConfig.getConfig().set("allNamedHomes", config.get("allNamedHomes"));
                config.set("allNamedHomes", null);
                homeConfig.save();
                saveConfig();
            }
        }

        if (config.contains("unknownHomes")) {
            if (config.isSet("unknownHomes")) {
                homeConfig.getConfig().set("unknownHomes", config.get("unknownHomes"));
                config.set("unknownHomes", null);
                homeConfig.save();
                saveConfig();
            }
        }
    }

    /**
     * Used to cancel a bukkit runnable task
     *
     * @param taskId
     */
    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    /**
     * Used to get the servers permissions plugin
     *
     * @return the permissions handler
     */
    public Permission getPermissions() {
        return this.perms;
    }
}