package com.customitems.enchant;

import com.customitems.config.CustomItemsConfig;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EnchantCommands {

    private static final String PERMISSION = "customitems.enchants.admin";

    private final CustomItemsConfig config;
    private final EnchantControlManager manager;

    public EnchantCommands(CustomItemsConfig config, EnchantControlManager manager) {
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
            case "blockcombine" -> blockCombine(sender, args, true);
            case "unblockcombine" -> blockCombine(sender, args, false);
            case "blockedcombine" -> listBlocked(sender);
            case "captable" -> capTable(sender, args, true);
            case "uncaptable" -> capTable(sender, args, false);
            case "cappedtable" -> listCapped(sender);
            case "reload" -> reload(sender);
            default -> usage(sender);
        }
    }

    private void blockCombine(CommandSender sender, String[] args, boolean block) {
        Enchantment enchantment = requireEnchant(sender, args);
        if (enchantment == null) {
            return;
        }
        boolean changed = block ? manager.addBlocked(enchantment) : manager.removeBlocked(enchantment);
        if (!changed) {
            sender.sendMessage(Component.text(block ? "Already blocked." : "Was not blocked.", NamedTextColor.YELLOW));
            return;
        }
        sender.sendMessage(Component.text((block ? "Blocked combining " : "Unblocked combining ")
                + key(enchantment) + ".", NamedTextColor.GREEN));
    }

    private void capTable(CommandSender sender, String[] args, boolean cap) {
        Enchantment enchantment = requireEnchant(sender, args);
        if (enchantment == null) {
            return;
        }
        if (cap) {
            manager.addCapped(enchantment, 1, 30);
            sender.sendMessage(Component.text("Capped " + key(enchantment)
                    + " to level 1 from the level 30 option.", NamedTextColor.GREEN));
        } else {
            if (manager.removeCapped(enchantment)) {
                sender.sendMessage(Component.text("Removed table cap for " + key(enchantment) + ".", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("That enchantment was not capped.", NamedTextColor.YELLOW));
            }
        }
    }

    private void listBlocked(CommandSender sender) {
        sender.sendMessage(Component.text("Blocked combining:", NamedTextColor.GOLD));
        if (manager.blockedCombining().isEmpty()) {
            sender.sendMessage(Component.text(" (none)", NamedTextColor.GRAY));
            return;
        }
        for (Enchantment enchantment : manager.blockedCombining()) {
            sender.sendMessage(Component.text(" - " + key(enchantment), NamedTextColor.GRAY));
        }
    }

    private void listCapped(CommandSender sender) {
        sender.sendMessage(Component.text("Table-capped enchantments:", NamedTextColor.GOLD));
        if (manager.tableCapped().isEmpty()) {
            sender.sendMessage(Component.text(" (none)", NamedTextColor.GRAY));
            return;
        }
        manager.tableCapped().forEach((enchantment, rule) -> sender.sendMessage(Component.text(
                " - " + key(enchantment) + " max " + rule.maxLevel() + ", require " + rule.requireLevel(),
                NamedTextColor.GRAY)));
    }

    private void reload(CommandSender sender) {
        manager.reload();
        sender.sendMessage(Component.text("Enchant control reloaded.", NamedTextColor.GREEN));
    }

    private Enchantment requireEnchant(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Specify an enchantment.", NamedTextColor.RED));
            return null;
        }
        Enchantment enchantment = EnchantControlManager.parse(args[2]);
        if (enchantment == null) {
            sender.sendMessage(Component.text("Unknown enchantment: " + args[2], NamedTextColor.RED));
        }
        return enchantment;
    }

    private String key(Enchantment enchantment) {
        return enchantment.getKey().getKey();
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /customitems enchants <blockcombine|unblockcombine|blockedcombine|captable|uncaptable|cappedtable|reload>", NamedTextColor.YELLOW));
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 2) {
            return prefix(List.of("blockcombine", "unblockcombine", "blockedcombine",
                    "captable", "uncaptable", "cappedtable", "reload"), args[1]);
        }
        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            if (sub.equals("blockcombine") || sub.equals("unblockcombine")
                    || sub.equals("captable") || sub.equals("uncaptable")) {
                List<String> names = new ArrayList<>();
                for (Enchantment enchantment : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)) {
                    names.add(enchantment.getKey().getKey());
                }
                return prefix(names, args[2]);
            }
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
