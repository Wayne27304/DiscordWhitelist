package org.dpdns.wayne227304.managers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PluginManager {
    private static PluginManager instance;
    
    private PluginManager() {}
    
    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    public void initialize(org.bukkit.plugin.java.JavaPlugin plugin) {
    }

    public boolean addToWhitelist(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        if (player.isWhitelisted()) {
            return false;
        }

        player.setWhitelisted(true);
        return true;
    }

    public boolean addBedrockToWhitelist(String playerName) {
        String cleanName = playerName.startsWith(".") ? playerName.substring(1) : playerName;
        UUID floodgateUuid = UUID.nameUUIDFromBytes(
                ("Floodgate:" + cleanName).getBytes(StandardCharsets.UTF_8));
        OfflinePlayer player = Bukkit.getOfflinePlayer(floodgateUuid);
        if (player.isWhitelisted()) {
            return false;
        }

        player.setWhitelisted(true);
        return true;
    }

    public void shutdown() {
    }
}
