package com.samleighton.xquiset.sethomes.configurations;

import org.bukkit.configuration.file.FileConfiguration;

import com.samleighton.xquiset.sethomes.SetHomes;

public abstract class Config {
	SetHomes pl;
	
	public Config(SetHomes plugin) {
		this.pl = plugin;
	}
	
	public abstract void reloadConfig();

	public abstract FileConfiguration getConfig();
	
	public abstract void save();
}
