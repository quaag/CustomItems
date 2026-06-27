package com.customitems.smelter;

import com.customitems.config.CustomItemsConfig;
import com.customitems.item.SmeltersPickaxeItem;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SmelterListener implements Listener {

    private final CustomItemsConfig config;
    private final SmeltersPickaxeItem pickaxeItem;
    private final SmeltingIndex index;

    public SmelterListener(CustomItemsConfig config, SmeltersPickaxeItem pickaxeItem, SmeltingIndex index) {
        this.config = config;
        this.pickaxeItem = pickaxeItem;
        this.index = index;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.isSmeltersPickaxeEnabled() || event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!pickaxeItem.isSmeltersPickaxe(tool)) {
            return;
        }

        Block block = event.getBlock();
        Collection<ItemStack> drops = block.getDrops(tool, player);
        if (drops.isEmpty()) {
            return;
        }

        event.setDropItems(false);
        World world = block.getWorld();
        Location location = block.getLocation().add(0.5, 0.5, 0.5);
        for (ItemStack drop : drops) {
            ItemStack smelted = index.smelt(drop);
            world.dropItemNaturally(location, smelted != null ? smelted : drop);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!config.isSmeltersPickaxeEnabled()) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        if (!pickaxeItem.isSmeltersPickaxe(killer.getInventory().getItemInMainHand())) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        List<ItemStack> replaced = new ArrayList<>(drops.size());
        for (ItemStack drop : drops) {
            ItemStack smelted = index.smelt(drop);
            replaced.add(smelted != null ? smelted : drop);
        }
        drops.clear();
        drops.addAll(replaced);
    }
}
