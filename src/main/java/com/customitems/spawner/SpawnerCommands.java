package com.customitems.spawner;

import com.customitems.CustomItemsPlugin;
import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpawnerCommands {

    private static final String PERMISSION = "customitems.spawner.admin";
    private static final List<String> MODES = List.of("slow", "medium", "fast");

    private final CustomItemsPlugin plugin;
    private final CustomItemsConfig config;
    private final SpawnerManager manager;

    public SpawnerCommands(CustomItemsPlugin plugin, CustomItemsConfig config, SpawnerManager manager) {
        this.plugin = plugin;
        this.config = config;
        this.manager = manager;
    }

    public void handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(config.noPermissionMessage());
            return;
        }
        if (args.length < 2) {
            usage(sender);
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list" -> handleList(sender);
            case "reload" -> handleReload(sender);
            case "rate" -> handleRate(sender, args);
            case "mode" -> handleMode(sender, args);
            case "mob" -> handleMob(sender, args);
            default -> usage(sender);
        }
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(Component.text("Custom spawners: " + manager.count(), NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Mob type: " + config.getSpawnerMobType()
                + " | mode: " + config.getSpawnerRateMode()
                + " (" + config.activeRateSeconds() + "s)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Rates - slow: " + config.rateForMode("slow")
                + "s, medium: " + config.rateForMode("medium")
                + "s, fast: " + config.rateForMode("fast") + "s", NamedTextColor.GRAY));
        for (SpawnerLocation location : manager.all()) {
            sender.sendMessage(Component.text(" - " + location.world() + " "
                    + location.x() + ", " + location.y() + ", " + location.z(), NamedTextColor.DARK_GRAY));
        }
    }

    private void handleReload(CommandSender sender) {
        config.load();
        manager.reload();
        sender.sendMessage(Component.text("Spawner config and data reloaded.", NamedTextColor.GREEN));
    }

    private void handleRate(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /customitems spawner rate <slow|medium|fast> <seconds>", NamedTextColor.RED));
            return;
        }
        String mode = args[2].toLowerCase();
        if (!MODES.contains(mode)) {
            sender.sendMessage(Component.text("Mode must be slow, medium or fast.", NamedTextColor.RED));
            return;
        }
        Integer seconds = parsePositiveInt(args[3]);
        if (seconds == null) {
            sender.sendMessage(Component.text("Seconds must be a positive whole number.", NamedTextColor.RED));
            return;
        }

        plugin.getConfig().set("custom-mob-spawner.rates." + mode, seconds);
        plugin.saveConfig();
        config.load();
        sender.sendMessage(Component.text("Rate " + mode + " set to " + seconds + "s.", NamedTextColor.GREEN));
    }

    private void handleMode(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems spawner mode <slow|medium|fast>", NamedTextColor.RED));
            return;
        }
        String mode = args[2].toLowerCase();
        if (!MODES.contains(mode)) {
            sender.sendMessage(Component.text("Mode must be slow, medium or fast.", NamedTextColor.RED));
            return;
        }

        plugin.getConfig().set("custom-mob-spawner.rate", mode);
        plugin.saveConfig();
        config.load();
        sender.sendMessage(Component.text("Active mode set to " + mode
                + " (" + config.activeRateSeconds() + "s).", NamedTextColor.GREEN));
    }

    private void handleMob(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems spawner mob <entitytype>", NamedTextColor.RED));
            return;
        }
        String raw = args[2].trim().toUpperCase();
        if (!isSpawnableMob(raw)) {
            sender.sendMessage(Component.text("Unknown or non-mob entity type: " + args[2], NamedTextColor.RED));
            return;
        }

        plugin.getConfig().set("custom-mob-spawner.mob-type", raw);
        plugin.saveConfig();
        config.load();
        sender.sendMessage(Component.text("Mob type set to " + raw + ".", NamedTextColor.GREEN));
    }

    private boolean isSpawnableMob(String raw) {
        try {
            EntityType type = EntityType.valueOf(raw);
            Class<?> entityClass = type.getEntityClass();
            return entityClass != null && Mob.class.isAssignableFrom(entityClass);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private Integer parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value > 0 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /customitems spawner <list|reload|rate|mode|mob>", NamedTextColor.YELLOW));
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 2) {
            return prefix(List.of("list", "reload", "rate", "mode", "mob"), args[1]);
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("rate") || args[1].equalsIgnoreCase("mode"))) {
            return prefix(MODES, args[2]);
        }
        return Collections.emptyList();
    }

    private List<String> prefix(List<String> options, String start) {
        String lower = start.toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
