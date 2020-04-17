package com.samleighton.xquiset.sethomes.configurations;

import com.samleighton.xquiset.sethomes.SetHomes;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;

public class WorldBlacklist extends Config {

    private SetHomes pl;
    private FileConfiguration worldBlacklistConfig = null;
    private File worldBlacklistFile = null;

    public WorldBlacklist(SetHomes plugin) {
        super(plugin);
        this.pl = plugin;
    }

    @Override
    public void reloadConfig() {
        if (worldBlacklistFile == null) {
            worldBlacklistFile = new File(pl.getDataFolder().getPath(), "world_blacklist.yml");
        }

        worldBlacklistConfig = YamlConfiguration.loadConfiguration(worldBlacklistFile);

        save();

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new FileReader(worldBlacklistFile);
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                worldBlacklistConfig.setDefaults(defConfig);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (worldBlacklistConfig == null) {
            reloadConfig();
        }
        return worldBlacklistConfig;
    }

    @Override
    public void save() {
        if (worldBlacklistConfig == null || worldBlacklistFile == null) {
            return;
        }

        try {
            getConfig().save(worldBlacklistFile);
        } catch (Exception e) {
            pl.getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Could not save config!", e);
        }
    }

}
