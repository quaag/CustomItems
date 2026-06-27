package com.customitems.spawner;

import com.customitems.CustomItemsPlugin;
import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SpawnerCommands {

    private static final String PERMISSION = "customitems.spawner.admin";
    private static final List<String> MODES = List.of("slow", "medium", "fast");
    private static final int TARGET_RANGE = 8;

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
            case "info" -> handleInfo(sender);
            case "mob" -> handleMob(sender, args);
            case "mode" -> handleMode(sender, args);
            case "rate" -> handleRate(sender, args);
            case "spawncount" -> handleSpawnCount(sender, args);
            case "maxnearby" -> handleMaxNearby(sender, args);
            case "range" -> handleRange(sender, args);
            case "defaults" -> handleDefaults(sender, args);
            default -> usage(sender);
        }
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(Component.text("Custom spawners loaded: " + manager.count(), NamedTextColor.GOLD));
    }

    private void handleInfo(CommandSender sender) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        sender.sendMessage(Component.text("Spawner at " + data.location().world() + " "
                + data.location().x() + ", " + data.location().y() + ", " + data.location().z(), NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Mob: " + data.getMobType()
                + " | mode: " + data.getRateMode() + " (" + data.activeRateSeconds() + "s)", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Rates - slow: " + data.getRateSlow()
                + "s, medium: " + data.getRateMedium() + "s, fast: " + data.getRateFast() + "s", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Spawn count: " + data.getSpawnCount()
                + " | max nearby: " + data.getMaxNearbyMobs()
                + " | range: " + data.getSpawnRange(), NamedTextColor.GRAY));
    }

    private void handleMob(CommandSender sender, String[] args) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems spawner mob <entitytype>", NamedTextColor.RED));
            return;
        }
        String raw = args[2].trim().toUpperCase();
        if (SpawnerManager.resolveType(raw) == null) {
            sender.sendMessage(Component.text("Unknown or non-mob entity type: " + args[2], NamedTextColor.RED));
            return;
        }
        data.setMobType(raw);
        manager.save();
        sender.sendMessage(Component.text("Mob type set to " + raw + " for this spawner.", NamedTextColor.GREEN));
    }

    private void handleMode(CommandSender sender, String[] args) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        String mode = requireMode(sender, args, 2);
        if (mode == null) {
            return;
        }
        data.setRateMode(mode);
        manager.save();
        sender.sendMessage(Component.text("Mode set to " + mode + " (" + data.activeRateSeconds()
                + "s) for this spawner.", NamedTextColor.GREEN));
    }

    private void handleRate(CommandSender sender, String[] args) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /customitems spawner rate <slow|medium|fast> <seconds>", NamedTextColor.RED));
            return;
        }
        String mode = requireMode(sender, args, 2);
        if (mode == null) {
            return;
        }
        Integer seconds = positiveInt(sender, args[3], "Seconds");
        if (seconds == null) {
            return;
        }
        data.setRate(mode, seconds);
        manager.save();
        sender.sendMessage(Component.text("Rate " + mode + " set to " + seconds
                + "s for this spawner.", NamedTextColor.GREEN));
    }

    private void handleSpawnCount(CommandSender sender, String[] args) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        Integer amount = requireArgInt(sender, args, "spawncount <amount>", "Amount");
        if (amount == null) {
            return;
        }
        data.setSpawnCount(amount);
        manager.save();
        sender.sendMessage(Component.text("Spawn count set to " + amount + " for this spawner.", NamedTextColor.GREEN));
    }

    private void handleMaxNearby(CommandSender sender, String[] args) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        Integer amount = requireArgInt(sender, args, "maxnearby <amount>", "Amount");
        if (amount == null) {
            return;
        }
        data.setMaxNearbyMobs(amount);
        manager.save();
        sender.sendMessage(Component.text("Max nearby set to " + amount + " for this spawner.", NamedTextColor.GREEN));
    }

    private void handleRange(CommandSender sender, String[] args) {
        SpawnerData data = target(sender);
        if (data == null) {
            return;
        }
        Integer blocks = requireArgInt(sender, args, "range <blocks>", "Range");
        if (blocks == null) {
            return;
        }
        data.setSpawnRange(blocks);
        manager.save();
        sender.sendMessage(Component.text("Spawn range set to " + blocks + " for this spawner.", NamedTextColor.GREEN));
    }

    private void handleDefaults(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems spawner defaults <mob|mode|rate|spawncount|maxnearby|range> ...", NamedTextColor.RED));
            return;
        }
        switch (args[2].toLowerCase()) {
            case "mob" -> setDefault(sender, args, "custom-mob-spawner.defaults.mob-type", true, false);
            case "mode" -> setDefault(sender, args, "custom-mob-spawner.defaults.rate", false, true);
            case "spawncount" -> setDefaultInt(sender, args, "custom-mob-spawner.defaults.spawn-count");
            case "maxnearby" -> setDefaultInt(sender, args, "custom-mob-spawner.defaults.max-nearby-mobs");
            case "range" -> setDefaultInt(sender, args, "custom-mob-spawner.defaults.spawn-range");
            case "rate" -> setDefaultRate(sender, args);
            default -> sender.sendMessage(Component.text("Unknown defaults option: " + args[2], NamedTextColor.RED));
        }
    }

    private void setDefault(CommandSender sender, String[] args, String path, boolean mob, boolean mode) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Missing value.", NamedTextColor.RED));
            return;
        }
        String value = args[3].trim();
        if (mob) {
            value = value.toUpperCase();
            if (SpawnerManager.resolveType(value) == null) {
                sender.sendMessage(Component.text("Unknown or non-mob entity type: " + args[3], NamedTextColor.RED));
                return;
            }
        }
        if (mode) {
            value = value.toLowerCase();
            if (!MODES.contains(value)) {
                sender.sendMessage(Component.text("Mode must be slow, medium or fast.", NamedTextColor.RED));
                return;
            }
        }
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        config.load();
        sender.sendMessage(Component.text("Default updated.", NamedTextColor.GREEN));
    }

    private void setDefaultInt(CommandSender sender, String[] args, String path) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Missing value.", NamedTextColor.RED));
            return;
        }
        Integer value = positiveInt(sender, args[3], "Value");
        if (value == null) {
            return;
        }
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        config.load();
        sender.sendMessage(Component.text("Default updated.", NamedTextColor.GREEN));
    }

    private void setDefaultRate(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(Component.text("Usage: /customitems spawner defaults rate <slow|medium|fast> <seconds>", NamedTextColor.RED));
            return;
        }
        String mode = args[3].toLowerCase();
        if (!MODES.contains(mode)) {
            sender.sendMessage(Component.text("Mode must be slow, medium or fast.", NamedTextColor.RED));
            return;
        }
        Integer seconds = positiveInt(sender, args[4], "Seconds");
        if (seconds == null) {
            return;
        }
        plugin.getConfig().set("custom-mob-spawner.defaults.rates." + mode, seconds);
        plugin.saveConfig();
        config.load();
        sender.sendMessage(Component.text("Default rate " + mode + " set to " + seconds + "s.", NamedTextColor.GREEN));
    }

    private SpawnerData target(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can target a spawner.", NamedTextColor.RED));
            return null;
        }
        Block block = player.getTargetBlockExact(TARGET_RANGE);
        SpawnerData data = block == null ? null : manager.get(block);
        if (data == null) {
            player.sendMessage(Component.text("Look at a CustomMobSpawner within " + TARGET_RANGE + " blocks.", NamedTextColor.RED));
        }
        return data;
    }

    private String requireMode(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            sender.sendMessage(Component.text("Mode must be slow, medium or fast.", NamedTextColor.RED));
            return null;
        }
        String mode = args[index].toLowerCase();
        if (!MODES.contains(mode)) {
            sender.sendMessage(Component.text("Mode must be slow, medium or fast.", NamedTextColor.RED));
            return null;
        }
        return mode;
    }

    private Integer requireArgInt(CommandSender sender, String[] args, String usage, String label) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems spawner " + usage, NamedTextColor.RED));
            return null;
        }
        return positiveInt(sender, args[2], label);
    }

    private Integer positiveInt(CommandSender sender, String raw, String label) {
        try {
            int value = Integer.parseInt(raw);
            if (value <= 0) {
                sender.sendMessage(Component.text(label + " must be a positive whole number.", NamedTextColor.RED));
                return null;
            }
            return value;
        } catch (NumberFormatException ex) {
            sender.sendMessage(Component.text(label + " must be a positive whole number.", NamedTextColor.RED));
            return null;
        }
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /customitems spawner <info|mob|mode|rate|spawncount|maxnearby|range|list|defaults>", NamedTextColor.YELLOW));
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 2) {
            return prefix(List.of("info", "mob", "mode", "rate", "spawncount", "maxnearby", "range", "list", "defaults"), args[1]);
        }
        if (args.length == 3 && (args[1].equalsIgnoreCase("rate") || args[1].equalsIgnoreCase("mode"))) {
            return prefix(MODES, args[2]);
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("defaults")) {
            return prefix(List.of("mob", "mode", "rate", "spawncount", "maxnearby", "range"), args[2]);
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("defaults")
                && (args[2].equalsIgnoreCase("rate") || args[2].equalsIgnoreCase("mode"))) {
            return prefix(MODES, args[3]);
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
