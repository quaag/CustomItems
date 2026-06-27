package com.customitems.command;

import com.customitems.CustomItemsPlugin;
import com.customitems.config.CustomItemsConfig;
import com.customitems.health.MaxHealthService;
import com.customitems.item.CustomItemRegistry;
import com.customitems.mask.MaskService;
import com.customitems.mask.MaskSkinService;
import com.customitems.spawner.SpawnerCommands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CustomItemsCommand implements CommandExecutor, TabCompleter {

    private final CustomItemsPlugin plugin;
    private final CustomItemsConfig config;
    private final CustomItemRegistry registry;
    private final MaskService maskService;
    private final MaskSkinService maskSkinService;
    private final SpawnerCommands spawnerCommands;
    private final MaxHealthService maxHealthService;

    public CustomItemsCommand(CustomItemsPlugin plugin, CustomItemsConfig config, CustomItemRegistry registry,
                              MaskService maskService, MaskSkinService maskSkinService,
                              SpawnerCommands spawnerCommands, MaxHealthService maxHealthService) {
        this.plugin = plugin;
        this.config = config;
        this.registry = registry;
        this.maskService = maskService;
        this.maskSkinService = maskSkinService;
        this.spawnerCommands = spawnerCommands;
        this.maxHealthService = maxHealthService;
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
            case "maskskin" -> handleMaskSkin(sender, args);
            case "signbook" -> handleSignBook(sender, args);
            case "spawner" -> spawnerCommands.handle(sender, args);
            case "hearts" -> handleHearts(sender, args);
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

    private void handleMaskSkin(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("status")) {
            sender.sendMessage(Component.text("Usage: /customitems maskskin status", NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Mask status", NamedTextColor.GOLD));
        sendStatusLine(sender, "tab name changing enabled", config.isMaskChangeTabName());
        sendStatusLine(sender, "tab skin spoofing enabled", config.isMaskChangeTabSkin());
        sendStatusLine(sender, "tab skin spoofing active", maskSkinService.isActive());
        sendStatusLine(sender, "nameplate hiding enabled", config.isMaskHideNameplate());

        if (sender instanceof Player player) {
            sendStatusLine(sender, "you are masked", maskService.isMasked(player));
            sendStatusLine(sender, "your nameplate hidden", maskService.isNameplateHidden(player));
            sendStatusLine(sender, "your profile is spoofed", maskSkinService.isSpoofed(player));
        }
    }

    private void sendStatusLine(CommandSender sender, String label, boolean value) {
        NamedTextColor color = value ? NamedTextColor.GREEN : NamedTextColor.RED;
        sender.sendMessage(Component.text(label + ": ", NamedTextColor.GRAY)
                .append(Component.text(value, color)));
    }

    private void handleSignBook(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can sign a book.", NamedTextColor.RED));
            return;
        }
        if (!player.hasPermission("customitems.signbook")) {
            player.sendMessage(config.noPermissionMessage());
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /customitems signbook <author>", NamedTextColor.RED));
            return;
        }

        String author = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (author.isEmpty()) {
            player.sendMessage(Component.text("Author name cannot be empty.", NamedTextColor.RED));
            return;
        }
        if (author.length() > 32) {
            player.sendMessage(Component.text("Author name cannot be longer than 32 characters.", NamedTextColor.RED));
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!registry.signingBook().isSigningBook(held)) {
            player.sendMessage(Component.text("You must be holding a CustomSigningBook.", NamedTextColor.RED));
            return;
        }

        BookMeta source = (BookMeta) held.getItemMeta();
        ItemStack written = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta target = (BookMeta) written.getItemMeta();
        target.pages(source.pages());
        target.author(Component.text(author));
        target.title(source.hasTitle() ? source.title() : Component.text("Custom Signed Book"));
        written.setItemMeta(target);

        player.getInventory().setItemInMainHand(written);
        player.sendMessage(Component.text("Signed book as " + author + ".", NamedTextColor.GREEN));
    }

    private void handleHearts(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customitems.hearts")) {
            sender.sendMessage(config.noPermissionMessage());
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /customitems hearts <on|off|rows|status>", NamedTextColor.RED));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "on" -> setHearts(sender, true);
            case "off" -> setHearts(sender, false);
            case "status" -> sender.sendMessage(Component.text("Server hearts: "
                    + (config.isMaxHealthEnabled() ? "on" : "off") + " | rows: " + config.getMaxHealthRows()
                    + (config.isMaxHealthEnabled() ? "" : " (1 while off)"), NamedTextColor.GOLD));
            case "rows" -> setRows(sender, args);
            default -> sender.sendMessage(Component.text("Usage: /customitems hearts <on|off|rows|status>", NamedTextColor.RED));
        }
    }

    private void setHearts(CommandSender sender, boolean enabled) {
        plugin.getConfig().set("max-health.enabled", enabled);
        plugin.saveConfig();
        config.load();
        plugin.applyMaxHealthToAll();
        sender.sendMessage(Component.text("Server hearts " + (enabled ? "enabled (" + config.getMaxHealthRows()
                + " rows)." : "disabled (1 row)."), NamedTextColor.GREEN));
    }

    private void setRows(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems hearts rows <amount>", NamedTextColor.RED));
            return;
        }
        int rows;
        try {
            rows = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            sender.sendMessage(Component.text("Rows must be a whole number of 1 or more.", NamedTextColor.RED));
            return;
        }
        if (rows < 1) {
            sender.sendMessage(Component.text("Rows must be a whole number of 1 or more.", NamedTextColor.RED));
            return;
        }

        plugin.getConfig().set("max-health.rows", rows);
        plugin.saveConfig();
        config.load();
        plugin.applyMaxHealthToAll();
        sender.sendMessage(Component.text("Server hearts rows set to " + rows + ".", NamedTextColor.GREEN));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /customitems <version|give|reload|maskskin|signbook|spawner|hearts>", NamedTextColor.YELLOW));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("version", "give", "reload", "maskskin", "signbook", "spawner", "hearts"), args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(registry.ids(), args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("maskskin")) {
            return filter(List.of("status"), args[1]);
        }

        if (args[0].equalsIgnoreCase("spawner")) {
            return spawnerCommands.tabComplete(args);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("hearts")) {
            return filter(List.of("on", "off", "rows", "status"), args[1]);
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
