package com.customitems;

import com.customitems.command.CustomItemsCommand;
import com.customitems.config.CustomItemsConfig;
import com.customitems.crown.CrownService;
import com.customitems.item.CrownItem;
import com.customitems.item.CustomItemRegistry;
import com.customitems.item.ItemKeys;
import com.customitems.item.MaskItem;
import com.customitems.item.SigningBookItem;
import com.customitems.listener.EquipmentListener;
import com.customitems.mask.MaskService;
import com.customitems.mask.MaskSkinService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class CustomItemsPlugin extends JavaPlugin {

    private static final long SYNC_PERIOD_TICKS = 100L;

    private CustomItemsConfig config;
    private CrownService crownService;
    private MaskService maskService;
    private BukkitTask syncTask;

    @Override
    public void onEnable() {
        config = new CustomItemsConfig(this);
        config.load();

        ItemKeys keys = new ItemKeys(this);
        CrownItem crownItem = new CrownItem(config, keys);
        MaskItem maskItem = new MaskItem(config, keys);
        SigningBookItem signingBookItem = new SigningBookItem(keys);
        CustomItemRegistry registry = new CustomItemRegistry(crownItem, maskItem, signingBookItem);

        crownService = new CrownService(this, config, crownItem);
        MaskSkinService maskSkinService = new MaskSkinService(this, config);
        maskService = new MaskService(config, maskItem, maskSkinService);

        CustomItemsCommand command = new CustomItemsCommand(this, config, registry, maskService, maskSkinService);
        Objects.requireNonNull(getCommand("customitems")).setExecutor(command);
        Objects.requireNonNull(getCommand("customitems")).setTabCompleter(command);

        getServer().getPluginManager().registerEvents(
                new EquipmentListener(this, crownService, maskService), this);

        startSyncTask();
    }

    @Override
    public void onDisable() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }

        for (Player player : getServer().getOnlinePlayers()) {
            if (crownService != null) {
                crownService.reset(player);
            }
            if (maskService != null) {
                maskService.disable(player);
            }
        }
    }

    public void reloadEverything() {
        config.load();
        for (Player player : getServer().getOnlinePlayers()) {
            crownService.reset(player);
            maskService.reset(player);
            crownService.sync(player);
            maskService.sync(player);
        }
    }

    private void startSyncTask() {
        syncTask = getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                crownService.sync(player);
                maskService.sync(player);
            }
        }, SYNC_PERIOD_TICKS, SYNC_PERIOD_TICKS);
    }
}
