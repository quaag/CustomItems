package com.customitems.spawner;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public final class SpawnerStore {

    private final Plugin plugin;
    private final File file;

    public SpawnerStore(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawners.yml");
    }

    public List<SpawnerLocation> load() {
        List<SpawnerLocation> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getStringList("spawners")) {
            try {
                result.add(SpawnerLocation.fromKey(key));
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("[CustomItems] Skipping invalid spawner entry: " + key);
            }
        }
        return result;
    }

    public void save(Collection<SpawnerLocation> locations) {
        FileConfiguration config = new YamlConfiguration();
        List<String> keys = new ArrayList<>();
        for (SpawnerLocation location : locations) {
            keys.add(location.key());
        }
        config.set("spawners", keys);

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            config.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "[CustomItems] Failed to save spawners.yml", ex);
        }
    }
}
