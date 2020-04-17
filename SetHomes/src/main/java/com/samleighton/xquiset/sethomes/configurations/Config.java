package com.samleighton.xquiset.sethomes.configurations;

import com.samleighton.xquiset.sethomes.SetHomes;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class Config {
    SetHomes pl;

    public Config(SetHomes plugin) {
        this.pl = plugin;
    }

    public abstract void reloadConfig();

    public abstract FileConfiguration getConfig();

    public abstract void save();
}
