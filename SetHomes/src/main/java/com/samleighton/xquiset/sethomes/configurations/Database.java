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

public class Database extends Config {

    private final SetHomes pl;
    private FileConfiguration dbConfig = null;
    private File dbFile = null;

    public Database(SetHomes plugin) {
        super(plugin);
        this.pl = plugin;
    }

    @Override
    public void reloadConfig() {
        if (dbFile == null) {
            dbFile = new File(pl.getDataFolder().getPath(), "database.yml");
        }

        dbConfig = YamlConfiguration.loadConfiguration(dbFile);

        save();

        // Look for defaults in the jar
        try {
            Reader defConfigStream = new FileReader(dbFile);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            dbConfig.setDefaults(defConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        if (dbConfig == null) {
            reloadConfig();
        }
        return dbConfig;
    }

    @Override
    public void save() {
        if (dbConfig == null || dbFile == null) {
            return;
        }

        try {
            getConfig().save(dbFile);
        } catch (Exception e) {
            pl.getLogger().log(Level.SEVERE, ChatColor.DARK_RED + "Could not save config!", e);
        }
    }
}
