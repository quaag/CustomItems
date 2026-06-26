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
    private final Map<String, SpawnerLocation> tracked = new LinkedHashMap<>();

    private BukkitTask task;
    private int secondsElapsed;

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
        for (SpawnerLocation location : store.load()) {
            tracked.put(location.key(), location);
        }
    }

    public void reload() {
        load();
        secondsElapsed = 0;
    }

    public void track(Block block) {
        SpawnerLocation location = SpawnerLocation.of(block);
        tracked.put(location.key(), location);
        store.save(tracked.values());
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

    public Collection<SpawnerLocation> all() {
        return tracked.values();
    }

    public int count() {
        return tracked.size();
    }

    private void tick() {
        if (!config.isSpawnerEnabled() || tracked.isEmpty()) {
            return;
        }

        secondsElapsed++;
        if (secondsElapsed < config.activeRateSeconds()) {
            return;
        }
        secondsElapsed = 0;

        EntityType type = resolveType(config.getSpawnerMobType());
        if (type == null) {
            return;
        }

        for (SpawnerLocation location : List.copyOf(tracked.values())) {
            attemptSpawn(location, type);
        }
    }

    private void attemptSpawn(SpawnerLocation location, EntityType type) {
        World world = location.resolveWorld();
        if (world == null) {
            return;
        }
        if (!world.isChunkLoaded(location.x() >> 4, location.z() >> 4)) {
            return;
        }

        Block block = world.getBlockAt(location.x(), location.y(), location.z());
        if (block.getType() != Material.SPAWNER) {
            tracked.remove(location.key());
            store.save(tracked.values());
            return;
        }

        Location center = location.center();
        int range = config.getSpawnerSpawnRange();
        long nearby = world.getNearbyEntities(center, range, range, range).stream()
                .filter(entity -> entity.getType() == type)
                .count();
        if (nearby >= config.getSpawnerMaxNearbyMobs()) {
            return;
        }

        for (int i = 0; i < config.getSpawnerSpawnCount(); i++) {
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

    private EntityType resolveType(String raw) {
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
