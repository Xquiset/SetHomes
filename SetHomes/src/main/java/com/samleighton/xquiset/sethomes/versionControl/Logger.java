package com.samleighton.xquiset.sethomes.versionControl;

import com.samleighton.xquiset.sethomes.SetHomes;
import org.bukkit.plugin.PluginLogger;

public class Logger {

    private PluginLogger logger;
    private SetHomes pl;

    public Logger(SetHomes pl) {
        this.pl = pl;
        setLogger(new PluginLogger(pl));
    }

    public PluginLogger getLogger() {
        return this.logger;
    }

    public void setLogger(PluginLogger logger) {
        this.logger = logger;
    }
}
