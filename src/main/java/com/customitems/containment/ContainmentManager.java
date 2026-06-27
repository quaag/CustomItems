package com.customitems.containment;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ContainmentManager {

    private static final String ROOT = "containment";

    private final JavaPlugin plugin;
    private final Map<String, ContainmentRegion> regions = new LinkedHashMap<>();

    public ContainmentManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        regions.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection(ROOT);
        if (root == null) {
            return;
        }
        for (String name : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(name);
            if (section == null) {
                continue;
            }
            ContainmentRegion region = new ContainmentRegion(
                    name,
                    section.getString("world", "world"),
                    section.getInt("min.x"), section.getInt("min.y"), section.getInt("min.z"),
                    section.getInt("max.x"), section.getInt("max.y"), section.getInt("max.z"),
                    section.getDouble("teleport.x"), section.getDouble("teleport.y"), section.getDouble("teleport.z"),
                    (float) section.getDouble("teleport.yaw"), (float) section.getDouble("teleport.pitch"),
                    section.getStringList("items"));
            regions.put(name.toLowerCase(), region);
        }
    }

    public void save() {
        plugin.getConfig().set(ROOT, null);
        for (ContainmentRegion region : regions.values()) {
            String base = ROOT + "." + region.name() + ".";
            plugin.getConfig().set(base + "world", region.world());
            plugin.getConfig().set(base + "min.x", region.minX());
            plugin.getConfig().set(base + "min.y", region.minY());
            plugin.getConfig().set(base + "min.z", region.minZ());
            plugin.getConfig().set(base + "max.x", region.maxX());
            plugin.getConfig().set(base + "max.y", region.maxY());
            plugin.getConfig().set(base + "max.z", region.maxZ());
            plugin.getConfig().set(base + "teleport.x", region.teleportX());
            plugin.getConfig().set(base + "teleport.y", region.teleportY());
            plugin.getConfig().set(base + "teleport.z", region.teleportZ());
            plugin.getConfig().set(base + "teleport.yaw", region.teleportYaw());
            plugin.getConfig().set(base + "teleport.pitch", region.teleportPitch());
            plugin.getConfig().set(base + "items", new java.util.ArrayList<>(region.items()));
        }
        plugin.saveConfig();
    }

    public boolean exists(String name) {
        return regions.containsKey(name.toLowerCase());
    }

    public ContainmentRegion get(String name) {
        return regions.get(name.toLowerCase());
    }

    public void add(ContainmentRegion region) {
        regions.put(region.name().toLowerCase(), region);
        save();
    }

    public boolean remove(String name) {
        boolean removed = regions.remove(name.toLowerCase()) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Collection<ContainmentRegion> all() {
        return regions.values();
    }
}
