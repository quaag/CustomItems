package com.customitems;

import com.customitems.command.CustomItemsCommand;
import com.customitems.config.CustomItemsConfig;
import com.customitems.crown.CrownService;
import com.customitems.item.CrownItem;
import com.customitems.listener.CrownListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class CustomItemsPlugin extends JavaPlugin {

    private static final long SYNC_PERIOD_TICKS = 100L;

    private CustomItemsConfig config;
    private CrownItem crownItem;
    private CrownService crownService;
    private BukkitTask syncTask;

    @Override
    public void onEnable() {
        config = new CustomItemsConfig(this);
        config.load();

        crownItem = new CrownItem(this, config);
        crownService = new CrownService(this, config, crownItem);

        CustomItemsCommand command = new CustomItemsCommand(this, config, crownItem);
        Objects.requireNonNull(getCommand("customitems")).setExecutor(command);
        Objects.requireNonNull(getCommand("customitems")).setTabCompleter(command);

        getServer().getPluginManager().registerEvents(new CrownListener(this, crownService), this);

        startSyncTask();
    }

    @Override
    public void onDisable() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }

        if (crownService != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                crownService.reset(player);
            }
        }
    }

    public void reloadEverything() {
        config.load();
        for (Player player : getServer().getOnlinePlayers()) {
            crownService.reset(player);
            crownService.sync(player);
        }
    }

    private void startSyncTask() {
        syncTask = getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                crownService.sync(player);
            }
        }, SYNC_PERIOD_TICKS, SYNC_PERIOD_TICKS);
    }
}
