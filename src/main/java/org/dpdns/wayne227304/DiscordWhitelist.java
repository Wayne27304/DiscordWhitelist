package org.dpdns.wayne227304;

import org.bukkit.plugin.java.JavaPlugin;
import org.dpdns.wayne227304.managers.PluginManager;
import org.dpdns.wayne227304.listeners.PlayerListener;
import org.dpdns.wayne227304.commands.WhitelistCommand;
import org.dpdns.wayne227304.discord.DiscordManager;

public class DiscordWhitelist extends JavaPlugin {
    
    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        PluginManager.getInstance().initialize(this);

        WhitelistCommand whitelistCommand = new WhitelistCommand(this);
        getCommand("白名單").setExecutor(whitelistCommand);
        getCommand("白名單").setTabCompleter(whitelistCommand);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        DiscordManager.getInstance().start(this);
        
        getLogger().info(getDescription().getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        DiscordManager.getInstance().stop();
        PluginManager.getInstance().shutdown();
        getLogger().info(getDescription().getName() + " has been disabled!");
    }
    
}
