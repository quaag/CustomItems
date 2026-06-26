package com.customitems.spawner;

import com.customitems.item.ItemKeys;
import com.customitems.item.SpawnerItem;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class SpawnerListener implements Listener {

    private final SpawnerItem spawnerItem;
    private final SpawnerManager manager;
    private final ItemKeys keys;

    public SpawnerListener(SpawnerItem spawnerItem, SpawnerManager manager, ItemKeys keys) {
        this.spawnerItem = spawnerItem;
        this.manager = manager;
        this.keys = keys;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!spawnerItem.isSpawnerItem(event.getItemInHand())) {
            return;
        }

        Block block = event.getBlockPlaced();
        manager.track(block);

        BlockState state = block.getState();
        if (state instanceof CreatureSpawner spawner) {
            spawner.getPersistentDataContainer().set(keys.customItemId(), PersistentDataType.STRING, SpawnerItem.ID);
            spawner.update(true, false);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!manager.isTracked(block)) {
            return;
        }

        manager.untrack(block);
        event.setDropItems(false);
        event.setExpToDrop(0);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), spawnerItem.create());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        untrackExploded(event.blockList());
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        untrackExploded(event.blockList());
    }

    private void untrackExploded(List<Block> blocks) {
        for (Block block : blocks) {
            if (manager.isTracked(block)) {
                manager.untrack(block);
            }
        }
    }
}
