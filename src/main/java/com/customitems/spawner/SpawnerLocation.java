package com.customitems.spawner;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public record SpawnerLocation(String world, int x, int y, int z) {

    public static SpawnerLocation of(Block block) {
        return new SpawnerLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static SpawnerLocation fromKey(String key) {
        String[] parts = key.split(":");
        int size = parts.length;
        int z = Integer.parseInt(parts[size - 1]);
        int y = Integer.parseInt(parts[size - 2]);
        int x = Integer.parseInt(parts[size - 3]);
        String world = String.join(":", java.util.Arrays.copyOfRange(parts, 0, size - 3));
        return new SpawnerLocation(world, x, y, z);
    }

    public String key() {
        return world + ":" + x + ":" + y + ":" + z;
    }

    public World resolveWorld() {
        return Bukkit.getWorld(world);
    }

    public Location center() {
        World resolved = resolveWorld();
        if (resolved == null) {
            return null;
        }
        return new Location(resolved, x + 0.5, y, z + 0.5);
    }
}
