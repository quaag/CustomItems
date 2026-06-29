package com.customitems;

import com.customitems.command.CustomItemsCommand;
import com.customitems.config.CustomItemsConfig;
import com.customitems.containment.ContainmentCommands;
import com.customitems.containment.ContainmentListener;
import com.customitems.containment.ContainmentManager;
import com.customitems.containment.ContainmentWand;
import com.customitems.containment.SelectionService;
import com.customitems.crown.CrownService;
import com.customitems.enchant.EnchantCommands;
import com.customitems.enchant.EnchantControlListener;
import com.customitems.enchant.EnchantControlManager;
import com.customitems.health.HealthListener;
import com.customitems.health.MaxHealthService;
import com.customitems.item.CrownItem;
import com.customitems.item.CustomItemRegistry;
import com.customitems.item.ItemKeys;
import com.customitems.item.MaskItem;
import com.customitems.item.SculkweaversShellItem;
import com.customitems.item.SigningBookItem;
import com.customitems.item.SmeltersPickaxeItem;
import com.customitems.item.SpawnerItem;
import com.customitems.listener.EquipmentListener;
import com.customitems.smelter.SmelterListener;
import com.customitems.smelter.SmelterRecipe;
import com.customitems.smelter.SmeltingIndex;
import com.customitems.mask.MaskService;
import com.customitems.mask.MaskSkinService;
import com.customitems.spawner.SpawnerCommands;
import com.customitems.spawner.SpawnerListener;
import com.customitems.spawner.SpawnerManager;
import com.customitems.spawner.SpawnerStore;
import com.customitems.villager.VillagerListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Objects;

public final class CustomItemsPlugin extends JavaPlugin {

    private static final long SYNC_PERIOD_TICKS = 100L;

    private CustomItemsConfig config;
    private CrownService crownService;
    private MaskService maskService;
    private SpawnerManager spawnerManager;
    private SmelterRecipe smelterRecipe;
    private MaxHealthService maxHealthService;
    private ContainmentManager containmentManager;
    private EnchantControlManager enchantControlManager;
    private BukkitTask syncTask;

    @Override
    public void onEnable() {
        config = new CustomItemsConfig(this);
        config.load();

        ItemKeys keys = new ItemKeys(this);
        CrownItem crownItem = new CrownItem(config, keys);
        MaskItem maskItem = new MaskItem(config, keys);
        SigningBookItem signingBookItem = new SigningBookItem(keys);
        SpawnerItem spawnerItem = new SpawnerItem(keys);
        SmeltersPickaxeItem smeltersPickaxeItem = new SmeltersPickaxeItem(keys);
        SculkweaversShellItem sculkweaversShellItem = new SculkweaversShellItem(keys);
        CustomItemRegistry registry = new CustomItemRegistry(crownItem, maskItem, signingBookItem, spawnerItem, smeltersPickaxeItem, sculkweaversShellItem);

        crownService = new CrownService(this, config, crownItem);
        MaskSkinService maskSkinService = new MaskSkinService(this, config);
        maskService = new MaskService(config, maskItem, maskSkinService);

        spawnerManager = new SpawnerManager(this, config, new SpawnerStore(this));
        spawnerManager.start();
        SpawnerCommands spawnerCommands = new SpawnerCommands(this, config, spawnerManager);

        maxHealthService = new MaxHealthService(this, config);

        containmentManager = new ContainmentManager(this);
        containmentManager.load();
        SelectionService selectionService = new SelectionService();
        ContainmentWand containmentWand = new ContainmentWand(keys);
        ContainmentCommands containmentCommands = new ContainmentCommands(config, containmentManager, selectionService, containmentWand);

        enchantControlManager = new EnchantControlManager(this);
        enchantControlManager.load();
        EnchantCommands enchantCommands = new EnchantCommands(config, enchantControlManager);

        CustomItemsCommand command = new CustomItemsCommand(this, config, registry, maskService, maskSkinService, spawnerCommands, maxHealthService, containmentCommands, enchantCommands);
        Objects.requireNonNull(getCommand("customitems")).setExecutor(command);
        Objects.requireNonNull(getCommand("customitems")).setTabCompleter(command);

        getServer().getPluginManager().registerEvents(
                new EquipmentListener(this, crownService, maskService), this);
        getServer().getPluginManager().registerEvents(
                new SpawnerListener(spawnerItem, spawnerManager, keys), this);

        SmeltingIndex smeltingIndex = new SmeltingIndex();
        smeltingIndex.rebuild();
        getServer().getPluginManager().registerEvents(
                new SmelterListener(config, smeltersPickaxeItem, smeltingIndex), this);

        smelterRecipe = new SmelterRecipe(this, smeltersPickaxeItem);
        smelterRecipe.register();

        getServer().getPluginManager().registerEvents(new HealthListener(maxHealthService), this);
        getServer().getPluginManager().registerEvents(
                new ContainmentListener(containmentManager, selectionService, containmentWand, keys, config), this);
        getServer().getPluginManager().registerEvents(new EnchantControlListener(enchantControlManager), this);
        getServer().getPluginManager().registerEvents(new VillagerListener(config), this);
        applyMaxHealthToAll();

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
            if (maxHealthService != null) {
                maxHealthService.remove(player);
            }
        }

        if (spawnerManager != null) {
            spawnerManager.shutdown();
        }
        if (smelterRecipe != null) {
            smelterRecipe.unregister();
        }
    }

    public void reloadEverything() {
        config.load();
        if (spawnerManager != null) {
            spawnerManager.reload();
        }
        if (containmentManager != null) {
            containmentManager.load();
        }
        if (enchantControlManager != null) {
            enchantControlManager.load();
        }
        for (Player player : getServer().getOnlinePlayers()) {
            crownService.reset(player);
            maskService.reset(player);
            crownService.sync(player);
            maskService.sync(player);
            maxHealthService.sync(player);
        }
    }

    public void applyMaxHealthToAll() {
        for (Player player : getServer().getOnlinePlayers()) {
            maxHealthService.sync(player);
        }
    }

    private void startSyncTask() {
        syncTask = getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : getServer().getOnlinePlayers()) {
                crownService.sync(player);
                maskService.sync(player);
                maxHealthService.sync(player);
            }
        }, SYNC_PERIOD_TICKS, SYNC_PERIOD_TICKS);
    }
}
