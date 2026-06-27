package com.customitems.spawner;

import com.customitems.config.CustomItemsConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class SpawnerStore {

    private final Plugin plugin;
    private final File file;

    public SpawnerStore(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawners.yml");
    }

    public List<SpawnerData> load(CustomItemsConfig config) {
        List<SpawnerData> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);

        for (Map<?, ?> entry : data.getMapList("spawners")) {
            try {
                result.add(fromMap(entry, config));
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("[CustomItems] Skipping invalid spawner entry: " + entry);
            }
        }

        if (result.isEmpty()) {
            for (String key : data.getStringList("spawners")) {
                try {
                    result.add(withDefaults(SpawnerLocation.fromKey(key), config));
                } catch (RuntimeException ex) {
                    plugin.getLogger().warning("[CustomItems] Skipping invalid legacy spawner entry: " + key);
                }
            }
        }

        return result;
    }

    public void save(Collection<SpawnerData> spawners) {
        FileConfiguration data = new YamlConfiguration();
        List<Map<String, Object>> entries = new ArrayList<>();
        for (SpawnerData spawner : spawners) {
            entries.add(toMap(spawner));
        }
        data.set("spawners", entries);

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            data.save(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "[CustomItems] Failed to save spawners.yml", ex);
        }
    }

    public SpawnerData withDefaults(SpawnerLocation location, CustomItemsConfig config) {
        return new SpawnerData(location,
                config.getDefaultMobType(),
                config.getDefaultRateMode(),
                config.getDefaultRateSlow(),
                config.getDefaultRateMedium(),
                config.getDefaultRateFast(),
                config.getDefaultSpawnCount(),
                config.getDefaultMaxNearbyMobs(),
                config.getDefaultSpawnRange());
    }

    private Map<String, Object> toMap(SpawnerData spawner) {
        SpawnerLocation location = spawner.location();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", location.world());
        map.put("x", location.x());
        map.put("y", location.y());
        map.put("z", location.z());
        map.put("mob-type", spawner.getMobType());
        map.put("rate", spawner.getRateMode());
        map.put("rate-slow", spawner.getRateSlow());
        map.put("rate-medium", spawner.getRateMedium());
        map.put("rate-fast", spawner.getRateFast());
        map.put("spawn-count", spawner.getSpawnCount());
        map.put("max-nearby-mobs", spawner.getMaxNearbyMobs());
        map.put("spawn-range", spawner.getSpawnRange());
        return map;
    }

    private SpawnerData fromMap(Map<?, ?> map, CustomItemsConfig config) {
        SpawnerLocation location = new SpawnerLocation(
                String.valueOf(map.get("world")),
                intValue(map.get("x"), 0),
                intValue(map.get("y"), 0),
                intValue(map.get("z"), 0));

        return new SpawnerData(location,
                stringValue(map.get("mob-type"), config.getDefaultMobType()),
                stringValue(map.get("rate"), config.getDefaultRateMode()),
                intValue(map.get("rate-slow"), config.getDefaultRateSlow()),
                intValue(map.get("rate-medium"), config.getDefaultRateMedium()),
                intValue(map.get("rate-fast"), config.getDefaultRateFast()),
                intValue(map.get("spawn-count"), config.getDefaultSpawnCount()),
                intValue(map.get("max-nearby-mobs"), config.getDefaultMaxNearbyMobs()),
                intValue(map.get("spawn-range"), config.getDefaultSpawnRange()));
    }

    private int intValue(Object raw, int fallback) {
        if (raw instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    private String stringValue(Object raw, String fallback) {
        return raw == null ? fallback : String.valueOf(raw);
    }
}
