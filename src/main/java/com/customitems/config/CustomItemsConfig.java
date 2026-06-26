package com.customitems.config;

import com.customitems.CustomItemsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public final class CustomItemsConfig {

    private final CustomItemsPlugin plugin;

    private boolean crownEnabled;
    private int crownExtraHearts;
    private boolean crownResistance;
    private boolean crownRegeneration;
    private int crownCustomModelData;
    private boolean crownDiamondHelmetStats;

    private boolean maskEnabled;
    private int maskCustomModelData;
    private String maskedName;
    private boolean maskChangeChatName;
    private boolean maskChangeTabName;
    private boolean maskChangeKillMessages;
    private boolean maskHideNameplate;
    private boolean maskChangeTabSkin;
    private boolean maskGlint;
    private String maskSkinValue;
    private String maskSkinSignature;

    private boolean spawnerEnabled;
    private String spawnerMobType;
    private String spawnerRateMode;
    private int spawnerRateSlow;
    private int spawnerRateMedium;
    private int spawnerRateFast;
    private int spawnerSpawnCount;
    private int spawnerMaxNearbyMobs;
    private int spawnerSpawnRange;

    private boolean debug;

    private String noPermission;
    private String given;
    private String received;
    private String reloaded;

    public CustomItemsConfig(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();

        crownEnabled = config.getBoolean("crown.enabled", true);
        crownExtraHearts = Math.max(0, config.getInt("crown.extra-hearts", 10));
        crownResistance = config.getBoolean("crown.resistance", true);
        crownRegeneration = config.getBoolean("crown.regeneration", true);
        crownCustomModelData = config.getInt("crown.custom-model-data", 1);
        crownDiamondHelmetStats = config.getBoolean("crown.diamond-helmet-stats", true);

        maskEnabled = config.getBoolean("mask.enabled", true);
        maskCustomModelData = config.getInt("mask.custom-model-data", 2);
        maskedName = config.getString("mask.masked-name", "Masked Player");
        maskChangeChatName = config.getBoolean("mask.change-chat-name", true);
        maskChangeTabName = config.getBoolean("mask.change-tab-name", false);
        maskChangeKillMessages = config.getBoolean("mask.change-kill-messages", true);
        maskHideNameplate = config.getBoolean("mask.hide-nameplate", true);
        maskChangeTabSkin = config.getBoolean("mask.change-tab-skin", false);
        maskGlint = config.getBoolean("mask.glint", false);
        maskSkinValue = config.getString("mask.skin.value", "");
        maskSkinSignature = config.getString("mask.skin.signature", "");

        spawnerEnabled = config.getBoolean("custom-mob-spawner.enabled", true);
        spawnerMobType = config.getString("custom-mob-spawner.mob-type", "ZOMBIE");
        spawnerRateMode = config.getString("custom-mob-spawner.rate", "medium");
        spawnerRateSlow = config.getInt("custom-mob-spawner.rates.slow", 15);
        spawnerRateMedium = config.getInt("custom-mob-spawner.rates.medium", 7);
        spawnerRateFast = config.getInt("custom-mob-spawner.rates.fast", 3);
        spawnerSpawnCount = Math.max(1, config.getInt("custom-mob-spawner.spawn-count", 1));
        spawnerMaxNearbyMobs = Math.max(0, config.getInt("custom-mob-spawner.max-nearby-mobs", 6));
        spawnerSpawnRange = Math.max(1, config.getInt("custom-mob-spawner.spawn-range", 4));

        debug = config.getBoolean("debug", false);

        noPermission = config.getString("messages.no-permission", "&cYou do not have permission.");
        given = config.getString("messages.given", "&aGave {item} to {player}.");
        received = config.getString("messages.received", "&aYou received {item}.");
        reloaded = config.getString("messages.reloaded", "&aCustomItems reloaded.");
    }

    public boolean isCrownEnabled() {
        return crownEnabled;
    }

    public double getCrownExtraHealth() {
        return crownExtraHearts * 2.0D;
    }

    public boolean isCrownResistance() {
        return crownResistance;
    }

    public boolean isCrownRegeneration() {
        return crownRegeneration;
    }

    public int getCrownCustomModelData() {
        return crownCustomModelData;
    }

    public boolean isCrownDiamondHelmetStats() {
        return crownDiamondHelmetStats;
    }

    public boolean isMaskEnabled() {
        return maskEnabled;
    }

    public int getMaskCustomModelData() {
        return maskCustomModelData;
    }

    public String getMaskedName() {
        return maskedName;
    }

    public boolean isMaskChangeChatName() {
        return maskChangeChatName;
    }

    public boolean isMaskChangeTabName() {
        return maskChangeTabName;
    }

    public boolean isMaskChangeKillMessages() {
        return maskChangeKillMessages;
    }

    public boolean isMaskHideNameplate() {
        return maskHideNameplate;
    }

    public boolean isMaskChangeTabSkin() {
        return maskChangeTabSkin;
    }

    public boolean isMaskGlint() {
        return maskGlint;
    }

    public String getMaskSkinValue() {
        return maskSkinValue;
    }

    public String getMaskSkinSignature() {
        return maskSkinSignature;
    }

    public boolean hasMaskSkin() {
        return maskSkinValue != null && !maskSkinValue.isEmpty();
    }

    public boolean isSpawnerEnabled() {
        return spawnerEnabled;
    }

    public String getSpawnerMobType() {
        return spawnerMobType;
    }

    public String getSpawnerRateMode() {
        return spawnerRateMode;
    }

    public int getSpawnerSpawnCount() {
        return spawnerSpawnCount;
    }

    public int getSpawnerMaxNearbyMobs() {
        return spawnerMaxNearbyMobs;
    }

    public int getSpawnerSpawnRange() {
        return spawnerSpawnRange;
    }

    public int rateForMode(String mode) {
        if (mode == null) {
            return spawnerRateMedium;
        }
        return switch (mode.toLowerCase()) {
            case "slow" -> spawnerRateSlow;
            case "fast" -> spawnerRateFast;
            default -> spawnerRateMedium;
        };
    }

    public int activeRateSeconds() {
        return Math.max(1, rateForMode(spawnerRateMode));
    }

    public boolean isDebug() {
        return debug;
    }

    public Component noPermissionMessage() {
        return color(noPermission);
    }

    public Component givenMessage(String itemName, String playerName) {
        return color(given.replace("{item}", itemName).replace("{player}", playerName));
    }

    public Component receivedMessage(String itemName) {
        return color(received.replace("{item}", itemName));
    }

    public Component reloadedMessage() {
        return color(reloaded);
    }

    private Component color(String raw) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }
}
