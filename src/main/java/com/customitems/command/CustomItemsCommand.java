package com.customitems.command;

import com.customitems.CustomItemsPlugin;
import com.customitems.config.CustomItemsConfig;
import com.customitems.item.CustomItemRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CustomItemsCommand implements CommandExecutor, TabCompleter {

    private final CustomItemsPlugin plugin;
    private final CustomItemsConfig config;
    private final CustomItemRegistry registry;

    public CustomItemsCommand(CustomItemsPlugin plugin, CustomItemsConfig config, CustomItemRegistry registry) {
        this.plugin = plugin;
        this.config = config;
        this.registry = registry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "version" -> handleVersion(sender);
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleVersion(CommandSender sender) {
        sender.sendMessage(Component.text("CustomItems v" + plugin.getPluginMeta().getVersion(), NamedTextColor.GOLD));
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customitems.give")) {
            sender.sendMessage(config.noPermissionMessage());
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /customitems give <" + String.join("|", registry.ids()) + "> [player]", NamedTextColor.RED));
            return;
        }

        String itemId = args[1].toLowerCase();
        ItemStack item = registry.create(itemId);
        if (item == null) {
            sender.sendMessage(Component.text("Unknown item: " + args[1], NamedTextColor.RED));
            return;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[2], NamedTextColor.RED));
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage(Component.text("Console must specify a player.", NamedTextColor.RED));
            return;
        }

        String itemName = registry.displayName(itemId);
        target.getInventory().addItem(item);
        target.sendMessage(config.receivedMessage(itemName));

        if (!sender.equals(target)) {
            sender.sendMessage(config.givenMessage(itemName, target.getName()));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("customitems.reload")) {
            sender.sendMessage(config.noPermissionMessage());
            return;
        }

        plugin.reloadEverything();
        sender.sendMessage(config.reloadedMessage());
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /customitems <version|give|reload>", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("version", "give", "reload"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(registry.ids(), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                names.add(player.getName());
            }
            return filter(names, args[2]);
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
