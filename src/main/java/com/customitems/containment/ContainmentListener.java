package com.customitems.containment;

import com.customitems.config.CustomItemsConfig;
import com.customitems.item.ItemKeys;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public final class ContainmentListener implements Listener {

    private final ContainmentManager manager;
    private final SelectionService selection;
    private final ContainmentWand wand;
    private final ItemKeys keys;
    private final CustomItemsConfig config;

    public ContainmentListener(ContainmentManager manager, SelectionService selection,
                               ContainmentWand wand, ItemKeys keys, CustomItemsConfig config) {
        this.manager = manager;
        this.selection = selection;
        this.wand = wand;
        this.keys = keys;
        this.config = config;
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        if (!wand.isWand(event.getItem()) || event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            selection.setFirst(player, event.getClickedBlock().getLocation());
            player.sendMessage(positionMessage("Position 1", event.getClickedBlock().getLocation()));
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            selection.setSecond(player, event.getClickedBlock().getLocation());
            player.sendMessage(positionMessage("Position 2", event.getClickedBlock().getLocation()));
        }
    }

    @EventHandler
    public void onWandBreak(BlockBreakEvent event) {
        if (wand.isWand(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        for (ContainmentRegion region : manager.all()) {
            if (!region.world().equals(worldName) || !region.hasBoundItems()) {
                continue;
            }
            if (!region.contains(from) || region.contains(to)) {
                continue;
            }
            if (!carryingBoundItem(player, region)) {
                continue;
            }

            Location teleport = region.teleportLocation();
            if (teleport != null) {
                event.setTo(teleport);
                player.sendMessage(config.containmentBlockedMessage());
            }
            return;
        }
    }

    private boolean carryingBoundItem(Player player, ContainmentRegion region) {
        for (ItemStack item : player.getInventory().getContents()) {
            String id = keys.readId(item);
            if (id != null && region.items().contains(id)) {
                return true;
            }
        }
        return false;
    }

    private net.kyori.adventure.text.Component positionMessage(String label, Location location) {
        return net.kyori.adventure.text.Component.text(label + " set: "
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(),
                net.kyori.adventure.text.format.NamedTextColor.GREEN);
    }
}
