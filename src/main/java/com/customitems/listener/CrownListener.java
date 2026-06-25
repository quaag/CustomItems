package com.customitems.listener;

import com.customitems.CustomItemsPlugin;
import com.customitems.crown.CrownService;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class CrownListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final CrownService crownService;

    public CrownListener(CustomItemsPlugin plugin, CrownService crownService) {
        this.plugin = plugin;
        this.crownService = crownService;
    }

    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            syncNextTick(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            syncNextTick(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        crownService.reset(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        crownService.reset(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        syncNextTick(event.getPlayer());
    }

    private void syncNextTick(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                crownService.sync(player);
            }
        }, 1L);
    }
}
