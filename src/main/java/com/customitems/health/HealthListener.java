package com.customitems.health;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class HealthListener implements Listener {

    private final MaxHealthService maxHealthService;

    public HealthListener(MaxHealthService maxHealthService) {
        this.maxHealthService = maxHealthService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        maxHealthService.sync(event.getPlayer());
    }
}
