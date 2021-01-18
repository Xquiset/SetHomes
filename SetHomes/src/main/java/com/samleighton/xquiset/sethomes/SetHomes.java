package com.samleighton.xquiset.sethomes;

import com.samleighton.xquiset.sethomes.commands.*;
import com.samleighton.xquiset.sethomes.configurations.Database;
import com.samleighton.xquiset.sethomes.configurations.Homes;
import com.samleighton.xquiset.sethomes.configurations.WorldBlacklist;
import com.samleighton.xquiset.sethomes.database.MySQLConnector;
import com.samleighton.xquiset.sethomes.eventListeners.EventListener;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Xquiset
 * @version 1.3.1
 */
public class SetHomes extends JavaPlugin {

    public FileConfiguration config;
    private FileConfiguration homesCfg;
    private Permission vaultPerms = null;
    private LuckPerms luckPermsApi = null;
    private MySQLConnector dbConnector = null;
    private final WorldBlacklist blacklist = new WorldBlacklist(this);
    private final Homes homes = new Homes(this);
    private final Database db = new Database(this);
    public final String LOG_PREFIX = "[SetHomes] ";
    private final String configHeader = StringUtils.repeat("-", 26)
            + "\n\tSetHomes Config\t\n" + StringUtils.repeat("-", 26) + "\n"
            + "Messages: \n\tYou can use chat colors in messages with this symbol §.\n"
            + "\tI.E: §b will change any text after it to an aqua blue color.\n"
            + "\tColor codes may be found here https://www.digminecraft.com/lists/color_list_pc.php\n"
            + "Time: \n\tAny time value is based in seconds.\n"
            + "Things to Note: \n\tSet any integer option to 0 for it to be ignored.\n"
            + "\tThe max-homes does not include the default un-named home.\n"
            + "\tUse %s as the seconds variable in the cool down message.\n";

    @Override
    public void onEnable() {
        // Try to setup LuckPerms
        if (!setupLuckPerms()) {
            // Setup Vault if no LuckPerms
            if (!setupVaultPermissions()) {
                Bukkit.getServer().getLogger().log(Level.WARNING, LOG_PREFIX + "Could not connect to a permissions plugin! Config setting \"max-homes\" will be ignored!");
            }
        }

        // Load the configuration files on enable or reload
        loadConfigurationFiles();
        // Initialize the command executors
        registerCommands();
        // Register event listener
        new EventListener(this);

        // Register the database connection if enabled from config
        if (isDBEnabled()) {
            registerDB();
        }
    }

    @Override
    public void onDisable() {
        // Close any existing mysql connections
        if (dbConnector != null) {
            dbConnector.closeConnection();
        }
    }

    /**
     * Loads in configs if they exist otherwise
     * if config does not exist then we create it with
     * preset defaults
     */
    private void loadConfigurationFiles() {
        // Get the configs
        homesCfg = getHomes().getConfig();
        FileConfiguration blacklistCfg = getBlacklist().getConfig();
        FileConfiguration dbCfg = getDb().getConfig();

        // Establish blacklist default config path
        if (!(blacklistCfg.isSet("blacklisted_worlds"))) {
            blacklistCfg.addDefault("blacklisted_worlds", new ArrayList<String>());
        }

        // Save defaults
        blacklistCfg.options().copyDefaults(true);
        getBlacklist().save();

        // Establish homes default paths
        if (!(homesCfg.isSet("allNamedHomes") || homesCfg.isSet("unknownHomes"))) {
            homesCfg.addDefault("allNamedHomes", new HashMap<String, HashMap<String, Home>>());
            homesCfg.addDefault("unknownHomes", new HashMap<String, Home>());
        }

        // Save defaults
        homesCfg.options().copyDefaults(true);
        getHomes().save();

        // Establish database default paths
        dbCfg.addDefault("enabled", false);
        dbCfg.addDefault("host", "localhost");
        dbCfg.addDefault("database", "sethomes");
        dbCfg.addDefault("username", "root");
        dbCfg.addDefault("password", "root");
        dbCfg.addDefault("port", 3306);
        dbCfg.addDefault("db_prefix", "mc_");

        // Save database defaults
        dbCfg.options().copyDefaults(true);
        getDb().save();

        // Copy homes from old config if they were set and delete them from default config
        config = getConfig();
        copyHomes(config, getHomes());

        // Setup defaults for config
        if (!config.isSet("max-homes") || !config.isSet("max-homes-msg") || !config.isSet("tp-delay")
                || !config.isSet("tp-cooldown") || !config.isSet("tp-cancelOnMove")
                || !config.isSet("tp-cancelOnMove-msg") || !config.isSet("tp-cooldown-msg")
                || !config.isSet("auto-update")) {
            // Sets the max homes to unlimited by default

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
        getDb().reloadConfig();
    }

    /**
     * Registers the command classes
     * to handle the execution of these
     * commands
     */
    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("sethome")).setExecutor(new SetHome(this));
        Objects.requireNonNull(this.getCommand("homes")).setExecutor(new ListHomes(this));
        Objects.requireNonNull(this.getCommand("delhome")).setExecutor(new DeleteHome(this));
        Objects.requireNonNull(this.getCommand("home")).setExecutor(new GoHome(this));
        Objects.requireNonNull(this.getCommand("strike")).setExecutor(new Strike(this));
        Objects.requireNonNull(this.getCommand("blacklist")).setExecutor(new Blacklist(this));
        Objects.requireNonNull(this.getCommand("home-of")).setExecutor(new GoHome(this));
        Objects.requireNonNull(this.getCommand("delhome-of")).setExecutor(new DeleteHome(this));
        Objects.requireNonNull(this.getCommand("uhome")).setExecutor(new UpdateHome(this));
        Objects.requireNonNull(this.getCommand("uhome-of")).setExecutor(new UpdateHome(this));
        Objects.requireNonNull(this.getCommand("setmax")).setExecutor(new SetMax(this));
    }

    /**
     * Creates the MySQLConnector object for the plugin
     */
    private void registerDB() {
        this.dbConnector = new MySQLConnector(this);
    }

    /**
     * Used to initialize the Permissions service with VaultAPI
     *
     * @return true if Vault was setup, false otherwise
     */
    private boolean setupVaultPermissions() {
        try {
            // Attempt to get service provider
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);

            // Service provider was successfully obtained
            if (rsp != null) {
                vaultPerms = rsp.getProvider();
                Bukkit.getServer().getLogger().info(LOG_PREFIX + "Hooked into Vault!");
                return true;
            }
        } catch (NoClassDefFoundError ignored) {
            Bukkit.getServer().getLogger().info(LOG_PREFIX + "Vault was not found.");
        }
        return false;
    }

    /**
     * Used to initialize the Permissions service with LuckPermsAPI
     *
     * @return true if LuckPerms was setup, false otherwise
     */
    private boolean setupLuckPerms() {
        try {
            // Attempt to get service provider
            RegisteredServiceProvider<LuckPerms> rsp = Bukkit.getServicesManager().getRegistration(LuckPerms.class);

            // Service provider was successfully obtained
            if (rsp != null) {
                luckPermsApi = rsp.getProvider();
                Bukkit.getServer().getLogger().info(LOG_PREFIX + "Hooked into LuckPerms!");
                return true;
            }
        } catch (NoClassDefFoundError ignored) {
            Bukkit.getServer().getLogger().info(LOG_PREFIX + "Luck perms was not found! Reverting to vault...");
        }

        return false;
    }

    /**
     * Used to create the map of homes for a given player UUID
     *
     * @param uuid of the player we're attempting to get homes for
     * @return a hashmap of all the players homes
     */
    public HashMap<String, Home> getPlayersNamedHomes(String uuid) {
        HashMap<String, Home> playersNamedHomes = new HashMap<>();

        // Check if we're saving to DB or file
        if (isDBEnabled()) {
            try {
                ResultSet rs = getDbConnector().executeSQL(
                        String.format(
                                "SELECT * FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name != ''",
                                getDbConnector().getPrefix(),
                                getDbConnector().getPrefix(),
                                uuid
                        )
                );

                if (rs != null) {
                    while (rs.next()) {
                        Home home = new Home(
                                rs.getString("home_name"),
                                rs.getString("world"),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("pitch"),
                                rs.getFloat("yaw"),
                                rs.getString("home_desc")
                        );

                        playersNamedHomes.put(home.getHomeName(), home);
                    }
                }
            } catch (SQLException e) {
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            String homesPath = "allNamedHomes." + uuid;
            homesCfg = getHomes().getConfig();

            // Loop through the players home list and create a hash map with the home names as a key and home as value
            for (String id : Objects.requireNonNull(homesCfg.getConfigurationSection(homesPath)).getKeys(false)) {
                String path = homesPath + "." + id + ".";

                // Create the home object so we can add the description to it
                Location home = getHomeLocaleFromConfig(path);
                Home h = new Home(home);

                // Check if there is a desc set
                if (homesCfg.isSet(path + ".desc")) {
                    h.setDesc(homesCfg.getString(path + ".desc"));
                }

                playersNamedHomes.put(id, h);
            }
        }

        return playersNamedHomes;
    }

    /**
     * Used to get the players named home as a location from their map
     *
     * @param uuid     player for which to get the home from
     * @param homeName the name of the home to create the location for
     * @return location of the players named home
     */
    public Location getNamedHomeLocal(String uuid, String homeName) {
        Home h = getPlayersNamedHomes(uuid).get(homeName);
        return h.toLocation();
    }

    /**
     * Gets the Map of Groups to Max homes from the config
     *
     * @return a HashMap with the Group as the Key and the max number of homes as the value
     */
    public HashMap<String, Integer> getMaxHomes() {
        HashMap<String, Integer> maxHomes = new HashMap<>();
        String maxHomesPath = "max-homes";

        for (String id : Objects.requireNonNull(config.getConfigurationSection(maxHomesPath)).getKeys(false)) {
            maxHomes.put(id, config.getInt(maxHomesPath + "." + id));
        }

        return maxHomes;
    }

    /**
     * @param uuid of the player we're checking named homes for
     * @return true || false
     */
    public boolean hasNamedHomes(String uuid) {
        boolean hasHomes = false;

        // Check if we're saving to DB or file
        if (isDBConnected()) {
            try {
                String sql = String.format(
                        "SELECT COUNT(*) AS `home_num` FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name != ''",
                        getDbConnector().getPrefix(),
                        getDbConnector().getPrefix(),
                        uuid
                );
                // Query the database for any rows belonging to this uuid
                ResultSet rs = getDbConnector().executeSQL(sql);

                if (rs != null) {
                    while (rs.next()) {
                        hasHomes = rs.getInt("home_num") > 0;
                    }
                }
            } catch (SQLException e) {
                // Print any database errors
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            // Get the homes config
            homesCfg = getHomes().getConfig();
            // Determine if this uuid has any named homes set
            hasHomes = homesCfg.contains("allNamedHomes." + uuid) && homesCfg.isSet("allNamedHomes." + uuid);
        }

        return hasHomes;
    }

    /**
     * Used to save a named home into the config
     *
     * @param uuid of the player
     * @param home object of the home object to save
     */
    public void saveNamedHome(String uuid, Home home) {
        if (isDBConnected()) {
            int userID = getDbConnector().getPlayerID(uuid);
            String sql = String.format(
                    "INSERT INTO `%ssh_homes` (uuid, home_name, world, x, y, z, pitch, yaw, home_desc) " +
                            "VALUES (%d, '%s', '%s', %f, %f, %f, %f, %f, '%s')",
                    getDbConnector().getPrefix(),
                    userID,
                    home.getHomeName() == null ? "" : home.getHomeName(),
                    home.getWorld(),
                    home.getX(),
                    home.getY(),
                    home.getZ(),
                    home.getPitch(),
                    home.getYaw(),
                    home.getDesc() == null ? "" : home.getDesc()
            );
            getDbConnector().executeSQL(sql);
            getDbConnector().close();
        } else {
            String path = "allNamedHomes." + uuid + "." + home.getHomeName();
            saveHomeToConfig(home, path);
            homesCfg.set(path + ".desc", home.getDesc());
            getHomes().save();
        }
    }

    /**
     * Used to delete a players named home from the config
     *
     * @param uuid     of the player to get home list for
     * @param homeName name of home to delete from list
     */
    public void deleteNamedHome(String uuid, String homeName) {
        if (isDBConnected()) {
            int userID = getDbConnector().getPlayerID(uuid);
            String sql = String.format("DELETE FROM `%ssh_homes` WHERE uuid = %d AND home_name LIKE '%s'", getDbConnector().getPrefix(), userID, homeName);
            getDbConnector().executeSQL(sql);
            getDbConnector().close();
        } else {
            String path = "allNamedHomes." + uuid + "." + homeName;
            getHomes().getConfig().set(path, null);
            getHomes().save();
            getHomes().reloadConfig();
        }
    }

    /**
     * Used to get the location object of an un-named home
     *
     * @param uuid of the player to get a home for
     * @return the home to teleport the player to
     */
    public Location getPlayersUnnamedHome(String uuid) {
        Location homeLocation = null;
        if (isDBConnected()) {
            try {
                String sql = String.format(
                        "SELECT * FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name = '' AND sh.home_desc = ''",
                        getDbConnector().getPrefix(),
                        getDbConnector().getPrefix(),
                        uuid
                );
                ResultSet rs = getDbConnector().executeSQL(sql);

                if (rs != null) {
                    while (rs.next()) {
                        homeLocation = new Location(
                                Bukkit.getWorld(rs.getString("world")),
                                rs.getDouble("x"),
                                rs.getDouble("y"),
                                rs.getDouble("z"),
                                rs.getFloat("yaw"),
                                rs.getFloat("pitch")
                        );
                    }
                }
            } catch (SQLException e) {
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            //Grabs all the data from the configuration file
            String path = "unknownHomes." + uuid;
            homesCfg = getHomes().getConfig();
            homeLocation = getHomeLocaleFromConfig(path);
        }

        // Return the home as a location
        return homeLocation;
    }

    /**
     * Used to check if a player has un-named homes
     *
     * @param uuid of the player we're checking unnamed homes for
     * @return true || false
     */
    public boolean hasUnknownHomes(String uuid) {
        boolean hasHomes = false;
        if (isDBConnected()) {
            try {
                String sql = String.format(
                        "SELECT COUNT(*) AS `home_num` FROM `%ssh_homes` sh JOIN `%ssh_users` su ON sh.uuid = su.id WHERE su.uuid = '%s' AND sh.home_name = '' AND sh.home_desc = ''",
                        getDbConnector().getPrefix(),
                        getDbConnector().getPrefix(),
                        uuid
                );
                // Query the database for any rows belonging to this uuid
                ResultSet rs = getDbConnector().executeSQL(sql);

                if (rs != null) {
                    while (rs.next()) {
                        hasHomes = rs.getInt("home_num") > 0;
                    }
                }
            } catch (SQLException e) {
                // Print any database errors
                printDBError(e);
            } finally {
                getDbConnector().close();
            }
        } else {
            homesCfg = getHomes().getConfig();
            hasHomes = homesCfg.contains("unknownHomes." + uuid);
        }

        return hasHomes;
    }

    /**
     * Used to save the Unknown home to the config
     *
     * @param uuid of the player to save a home for
     * @param home to save
     */
    public void saveUnknownHome(String uuid, Home home) {
        if (isDBConnected()) {
            saveNamedHome(uuid, home);
        } else {
            //Saves the variables to construct a home location to the configuration file
            String path = "unknownHomes." + uuid;
            saveHomeToConfig(home, path);
            getHomes().save();
        }
    }

    /**
     * Helper method for saving a home object to the config file
     *
     * @param home, The home object to save
     * @param path, The path in the config to save the home
     */
    private void saveHomeToConfig(Home home, String path) {
        homesCfg = getHomes().getConfig();
        homesCfg.set(path + ".world", home.getWorld());
        homesCfg.set(path + ".x", home.getX());
        homesCfg.set(path + ".y", home.getY());
        homesCfg.set(path + ".z", home.getZ());
        homesCfg.set(path + ".pitch", home.getPitch());
        homesCfg.set(path + ".yaw", home.getYaw());
    }

    /**
     * Helper method to extract a homes location from the config file
     *
     * @param path, The path to the home data
     * @return the location object of the home
     */
    private Location getHomeLocaleFromConfig(String path) {
        World world = getServer().getWorld(Objects.requireNonNull(homesCfg.getString(path + ".world")));
        double x = homesCfg.getDouble(path + ".x");
        double y = homesCfg.getDouble(path + ".y");
        double z = homesCfg.getDouble(path + ".z");
        float pitch = Float.parseFloat(Objects.requireNonNull(homesCfg.getString(path + ".pitch")));
        float yaw = Float.parseFloat(Objects.requireNonNull(homesCfg.getString(path + ".yaw")));

        return new Location(world, x, y, z, pitch, yaw);
    }

    /**
     * Remove an unknown home from the config
     *
     * @param uuid of the player to delete default home for
     */
    public void deleteUnknownHome(String uuid) {
        if (isDBConnected()) {
            int userID = getDbConnector().getPlayerID(uuid);
            String sql = String.format("DELETE FROM `%ssh_homes` WHERE uuid = %d AND home_name = '' AND home_desc = ''", getDbConnector().getPrefix(), userID);
            getDbConnector().executeSQL(sql);
            getDbConnector().close();
        } else {
            //Set the path to the players id as null
            String path = "unknownHomes." + uuid;
            getHomes().getConfig().set(path, null);
            getHomes().save();
            getHomes().reloadConfig();
        }
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
     * Used to get the Database configuration parameters
     *
     * @return the Database Configuration object
     */
    public Database getDb() {
        return db;
    }

    /**
     * Used to obtain the MySQLConnector object for database communication
     *
     * @return the MySQLConnector object
     */
    public MySQLConnector getDbConnector() {
        return this.dbConnector;
    }

    /**
     * Used to determine if database support has been enabled or not in config
     *
     * @return boolean | True if database is support is enabled false otherwise
     */
    public boolean isDBEnabled() {
        return getDb().getConfig().getBoolean("enabled");
    }

    /**
     * Check to see if the DB connection has been established by our object
     *
     * @return True if MySQLConnector object has been registered in onEnable()
     */
    public boolean isDBConnected() {
        return dbConnector != null;
    }

    /**
     * Used to cancel a bukkit runnable task
     *
     * @param taskId, The id of the task to cancel
     */
    public void cancelTask(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    /**
     * Used to get the servers permissions
     *
     * @return the permissions handler
     */
    public Permission getVaultPermissions() {
        return this.vaultPerms;
    }

    /**
     * Used to obtain the instance ot LuckPerms API
     *
     * @return instance of LuckPerms
     */
    public LuckPerms getLuckPermsApi() {
        return this.luckPermsApi;
    }

    /**
     * Used to print any database errors encountered
     *
     * @param e, The SQLException object which caused this error
     */
    public void printDBError(SQLException e) {
        getLogger().log(Level.SEVERE, LOG_PREFIX + "SQLException: " + e.getMessage());
        getLogger().log(Level.SEVERE, LOG_PREFIX + "SQLState: " + e.getSQLState());
        getLogger().log(Level.SEVERE, LOG_PREFIX + "VendorError: " + e.getErrorCode());
    }
}