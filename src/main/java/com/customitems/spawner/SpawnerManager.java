package com.customitems.spawner;

import com.customitems.config.CustomItemsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class SpawnerManager {

    private static final int SPAWN_ATTEMPTS = 12;

    private final Plugin plugin;
    private final CustomItemsConfig config;
    private final SpawnerStore store;
    private final Map<String, SpawnerData> tracked = new LinkedHashMap<>();

    private BukkitTask task;

    public SpawnerManager(Plugin plugin, CustomItemsConfig config, SpawnerStore store) {
        this.plugin = plugin;
        this.config = config;
        this.store = store;
    }

    public void start() {
        load();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        store.save(tracked.values());
    }

    public void load() {
        tracked.clear();
        for (SpawnerData data : store.load(config)) {
            tracked.put(data.key(), data);
        }
    }

    public void reload() {
        load();
    }

    public void save() {
        store.save(tracked.values());
    }

    public SpawnerData create(Block block) {
        SpawnerData data = store.withDefaults(SpawnerLocation.of(block), config);
        tracked.put(data.key(), data);
        store.save(tracked.values());
        return data;
    }

    public SpawnerData get(Block block) {
        return tracked.get(SpawnerLocation.of(block).key());
    }

    public boolean isTracked(Block block) {
        return tracked.containsKey(SpawnerLocation.of(block).key());
    }

    public boolean untrack(Block block) {
        boolean removed = tracked.remove(SpawnerLocation.of(block).key()) != null;
        if (removed) {
            store.save(tracked.values());
        }
        return removed;
    }

    public Collection<SpawnerData> all() {
        return tracked.values();
    }

    public int count() {
        return tracked.size();
    }

    private void tick() {
        if (!config.isSpawnerEnabled() || tracked.isEmpty()) {
            return;
        }

        for (SpawnerData data : List.copyOf(tracked.values())) {
            data.incrementSecond();
            if (data.getSecondsElapsed() < data.activeRateSeconds()) {
                continue;
            }
            data.resetSeconds();
            attemptSpawn(data);
        }
    }

    private void attemptSpawn(SpawnerData data) {
        World world = data.resolveWorld();
        if (world == null) {
            return;
        }
        SpawnerLocation location = data.location();
        if (!world.isChunkLoaded(location.x() >> 4, location.z() >> 4)) {
            return;
        }

        Block block = world.getBlockAt(location.x(), location.y(), location.z());
        if (block.getType() != Material.SPAWNER) {
            tracked.remove(data.key());
            store.save(tracked.values());
            return;
        }

        EntityType type = resolveType(data.getMobType());
        if (type == null) {
            return;
        }

        int range = data.getSpawnRange();
        long nearby = world.getNearbyEntities(data.center(), range, range, range).stream()
                .filter(entity -> entity.getType() == type)
                .count();
        if (nearby >= data.getMaxNearbyMobs()) {
            return;
        }

        for (int i = 0; i < data.getSpawnCount(); i++) {
            Location target = findSpawnLocation(block, range);
            if (target != null) {
                world.spawnEntity(target, type);
            }
        }
    }

    private Location findSpawnLocation(Block origin, int range) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            int dx = random.nextInt(-range, range + 1);
            int dz = random.nextInt(-range, range + 1);
            int dy = random.nextInt(-1, 2);

            Block feet = origin.getRelative(dx, dy, dz);
            Block head = feet.getRelative(BlockFace.UP);
            Block ground = feet.getRelative(BlockFace.DOWN);

            if (feet.isPassable() && head.isPassable() && ground.getType().isSolid()) {
                return feet.getLocation().add(0.5, 0.0, 0.5);
            }
        }
        return null;
    }

    public static EntityType resolveType(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            EntityType type = EntityType.valueOf(raw.trim().toUpperCase());
            Class<?> entityClass = type.getEntityClass();
            if (entityClass != null && Mob.class.isAssignableFrom(entityClass)) {
                return type;
            }
        } catch (IllegalArgumentException ignored) {
        }
        return null;
    }
}
