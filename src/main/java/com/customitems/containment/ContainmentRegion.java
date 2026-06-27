package com.customitems.containment;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class ContainmentRegion {

    private final String name;
    private final String world;
    private final int minX;
    private final int minY;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final double teleportX;
    private final double teleportY;
    private final double teleportZ;
    private final float teleportYaw;
    private final float teleportPitch;
    private final Set<String> items;

    public ContainmentRegion(String name, String world,
                             int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                             double teleportX, double teleportY, double teleportZ,
                             float teleportYaw, float teleportPitch, Collection<String> items) {
        this.name = name;
        this.world = world;
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
        this.teleportX = teleportX;
        this.teleportY = teleportY;
        this.teleportZ = teleportZ;
        this.teleportYaw = teleportYaw;
        this.teleportPitch = teleportPitch;
        this.items = new LinkedHashSet<>(items);
    }

    public String name() {
        return name;
    }

    public String world() {
        return world;
    }

    public int minX() {
        return minX;
    }

    public int minY() {
        return minY;
    }

    public int minZ() {
        return minZ;
    }

    public int maxX() {
        return maxX;
    }

    public int maxY() {
        return maxY;
    }

    public int maxZ() {
        return maxZ;
    }

    public double teleportX() {
        return teleportX;
    }

    public double teleportY() {
        return teleportY;
    }

    public double teleportZ() {
        return teleportZ;
    }

    public float teleportYaw() {
        return teleportYaw;
    }

    public float teleportPitch() {
        return teleportPitch;
    }

    public Set<String> items() {
        return items;
    }

    public boolean addItem(String id) {
        return items.add(id);
    }

    public boolean removeItem(String id) {
        return items.remove(id);
    }

    public boolean hasBoundItems() {
        return !items.isEmpty();
    }

    public boolean contains(Location location) {
        if (location.getWorld() == null || !location.getWorld().getName().equals(world)) {
            return false;
        }
        return containsBlock(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public boolean containsBlock(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    public Location teleportLocation() {
        World resolved = Bukkit.getWorld(world);
        if (resolved == null) {
            return null;
        }
        return new Location(resolved, teleportX, teleportY, teleportZ, teleportYaw, teleportPitch);
    }
}
