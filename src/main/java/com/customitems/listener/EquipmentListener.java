package com.customitems.listener;

import com.customitems.CustomItemsPlugin;
import com.customitems.crown.CrownService;
import com.customitems.mask.MaskService;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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

public final class EquipmentListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final CrownService crownService;
    private final MaskService maskService;

    public EquipmentListener(CustomItemsPlugin plugin, CrownService crownService, MaskService maskService) {
        this.plugin = plugin;
        this.crownService = crownService;
        this.maskService = maskService;
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
        maskService.reset(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        maskKillerInDeathMessage(event);
        crownService.reset(event.getEntity());
        maskService.reset(event.getEntity());
    }

    private void maskKillerInDeathMessage(PlayerDeathEvent event) {
        if (!maskService.changeKillMessages()) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer == null || killer.equals(event.getEntity())) {
            return;
        }
        if (!maskService.isMasked(killer)) {
            return;
        }

        Component message = event.deathMessage();
        if (message == null) {
            return;
        }

        TextReplacementConfig replacement = TextReplacementConfig.builder()
                .matchLiteral(killer.getName())
                .replacement(maskService.maskedName())
                .build();
        event.deathMessage(message.replaceText(replacement));
    }

    private void syncNextTick(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                crownService.sync(player);
                maskService.sync(player);
            }
        }, 1L);
    }
}
