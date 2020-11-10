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

public class Homes extends Config {

    private final SetHomes pl;
    private FileConfiguration homes = null;
    private File homesFile = null;

    public Homes(SetHomes plugin) {
        super(plugin);
        this.pl = plugin;
    }

    @Override
    public void reloadConfig() {
        if (homesFile == null) {
            homesFile = new File(pl.getDataFolder().getPath(), "homes.yml");
        }

        homes = YamlConfiguration.loadConfiguration(homesFile);

        save();

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new FileReader(homesFile);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            homes.setDefaults(defConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (homes == null) {
            reloadConfig();
        }
        return homes;
    }

    @Override
    public void save() {
        if (homes == null || homesFile == null) {
            return;
        }

        try {
            getConfig().save(homesFile);
        } catch (Exception e) {
            pl.getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Could not save config!", e);
        }
    }
}
