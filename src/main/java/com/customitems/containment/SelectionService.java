package com.customitems.containment;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SelectionService {

    private final Map<UUID, Location> firstPositions = new HashMap<>();
    private final Map<UUID, Location> secondPositions = new HashMap<>();

    public void setFirst(Player player, Location location) {
        firstPositions.put(player.getUniqueId(), location.clone());
    }

    public void setSecond(Player player, Location location) {
        secondPositions.put(player.getUniqueId(), location.clone());
    }

    public Location getFirst(Player player) {
        return firstPositions.get(player.getUniqueId());
    }

    public Location getSecond(Player player) {
        return secondPositions.get(player.getUniqueId());
    }
}
