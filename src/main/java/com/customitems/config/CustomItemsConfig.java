package com.customitems.config;

import com.customitems.CustomItemsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

public final class CustomItemsConfig {

    private final CustomItemsPlugin plugin;

    private boolean crownEnabled;
    private int extraHearts;
    private boolean resistance;
    private boolean regeneration;
    private int customModelData;
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
        extraHearts = Math.max(0, config.getInt("crown.extra-hearts", 10));
        resistance = config.getBoolean("crown.resistance", true);
        regeneration = config.getBoolean("crown.regeneration", true);
        customModelData = config.getInt("crown.custom-model-data", 1);
        debug = config.getBoolean("debug", false);

        noPermission = config.getString("messages.no-permission", "&cYou do not have permission.");
        given = config.getString("messages.given", "&aGave Crown to {player}.");
        received = config.getString("messages.received", "&aYou received the Crown.");
        reloaded = config.getString("messages.reloaded", "&aCustomItems reloaded.");
    }

    public boolean isCrownEnabled() {
        return crownEnabled;
    }

    public int getExtraHearts() {
        return extraHearts;
    }

    public double getExtraHealth() {
        return extraHearts * 2.0D;
    }

    public boolean isResistance() {
        return resistance;
    }

    public boolean isRegeneration() {
        return regeneration;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public boolean isDebug() {
        return debug;
    }

    public Component noPermissionMessage() {
        return color(noPermission);
    }

    public Component givenMessage(String playerName) {
        return color(given.replace("{player}", playerName));
    }

    public Component receivedMessage() {
        return color(received);
    }

    public Component reloadedMessage() {
        return color(reloaded);
    }

    private Component color(String raw) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }
}
