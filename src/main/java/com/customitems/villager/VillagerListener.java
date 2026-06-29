package com.customitems.villager;

import com.customitems.config.CustomItemsConfig;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityTransformEvent;

public final class VillagerListener implements Listener {

    private final CustomItemsConfig config;

    public VillagerListener(CustomItemsConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!config.isVillagerControlEnabled()) {
            return;
        }
        SpawnReason reason = event.getSpawnReason();
        if (isManual(reason)) {
            return;
        }

        EntityType type = event.getEntityType();
        if (config.isPreventZombieVillagers() && type == EntityType.ZOMBIE_VILLAGER) {
            event.setCancelled(true);
            return;
        }
        if (config.isStopVillagerBreeding() && type == EntityType.VILLAGER && reason == SpawnReason.BREEDING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTransform(EntityTransformEvent event) {
        if (!config.isVillagerControlEnabled() || !config.isPreventZombieVillagers()) {
            return;
        }
        if (event.getTransformedEntity().getType() == EntityType.ZOMBIE_VILLAGER) {
            event.setCancelled(true);
        }
    }

    private boolean isManual(SpawnReason reason) {
        return reason == SpawnReason.COMMAND
                || reason == SpawnReason.CUSTOM
                || reason == SpawnReason.SPAWNER_EGG
                || reason == SpawnReason.DISPENSE_EGG;
    }
}
