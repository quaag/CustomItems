package com.customitems.containment;

import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContainmentCommands {

    private static final String PERMISSION = "customitems.containment.admin";

    private final CustomItemsConfig config;
    private final ContainmentManager manager;
    private final SelectionService selection;
    private final ContainmentWand wand;

    public ContainmentCommands(CustomItemsConfig config, ContainmentManager manager,
                               SelectionService selection, ContainmentWand wand) {
        this.config = config;
        this.manager = manager;
        this.selection = selection;
        this.wand = wand;
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
            case "wand" -> handleWand(sender);
            case "pos1" -> handlePos(sender, true);
            case "pos2" -> handlePos(sender, false);
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            case "tp" -> handleTp(sender, args);
            case "bind" -> handleBind(sender, args, true);
            case "unbind" -> handleBind(sender, args, false);
            default -> usage(sender);
        }
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can receive the wand.", NamedTextColor.RED));
            return;
        }
        player.getInventory().addItem(wand.create());
        player.sendMessage(Component.text("Containment wand given. Left-click = pos1, right-click = pos2.", NamedTextColor.GREEN));
    }

    private void handlePos(CommandSender sender, boolean first) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can set positions.", NamedTextColor.RED));
            return;
        }
        Location location = player.getLocation();
        if (first) {
            selection.setFirst(player, location);
        } else {
            selection.setSecond(player, location);
        }
        player.sendMessage(Component.text((first ? "Position 1" : "Position 2") + " set: "
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ(), NamedTextColor.GREEN));
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can create a region.", NamedTextColor.RED));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems containment create <name>", NamedTextColor.RED));
            return;
        }
        String name = args[2];
        if (manager.exists(name)) {
            sender.sendMessage(Component.text("A region named " + name + " already exists.", NamedTextColor.RED));
            return;
        }

        Location pos1 = selection.getFirst(player);
        Location pos2 = selection.getSecond(player);
        if (pos1 == null || pos2 == null) {
            sender.sendMessage(Component.text("Set both positions first (wand or pos1/pos2).", NamedTextColor.RED));
            return;
        }
        if (pos1.getWorld() == null || pos2.getWorld() == null || !pos1.getWorld().equals(pos2.getWorld())) {
            sender.sendMessage(Component.text("Both positions must be in the same world.", NamedTextColor.RED));
            return;
        }

        Location teleport = player.getLocation();
        ContainmentRegion region = new ContainmentRegion(
                name, pos1.getWorld().getName(),
                pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ(),
                teleport.getX(), teleport.getY(), teleport.getZ(),
                teleport.getYaw(), teleport.getPitch(), List.of());

        if (!region.contains(teleport)) {
            sender.sendMessage(Component.text("Your location (teleport point) must be inside the region.", NamedTextColor.RED));
            return;
        }

        manager.add(region);
        sender.sendMessage(Component.text("Region " + name + " created and saved.", NamedTextColor.GREEN));
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /customitems containment delete <name>", NamedTextColor.RED));
            return;
        }
        if (manager.remove(args[2])) {
            sender.sendMessage(Component.text("Region " + args[2] + " deleted.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("No region named " + args[2] + ".", NamedTextColor.RED));
        }
    }

    private void handleList(CommandSender sender) {
        if (manager.all().isEmpty()) {
            sender.sendMessage(Component.text("No containment regions defined.", NamedTextColor.GOLD));
            return;
        }
        sender.sendMessage(Component.text("Containment regions:", NamedTextColor.GOLD));
        for (ContainmentRegion region : manager.all()) {
            sender.sendMessage(Component.text(" - " + region.name() + " (" + region.world() + ") items: "
                    + String.join(", ", region.items()), NamedTextColor.GRAY));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        ContainmentRegion region = requireRegion(sender, args);
        if (region == null) {
            return;
        }
        sender.sendMessage(Component.text("Region " + region.name(), NamedTextColor.GOLD));
        sender.sendMessage(Component.text("World: " + region.world(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Min: " + region.minX() + ", " + region.minY() + ", " + region.minZ()
                + " | Max: " + region.maxX() + ", " + region.maxY() + ", " + region.maxZ(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Teleport: " + region.teleportX() + ", " + region.teleportY()
                + ", " + region.teleportZ(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Items: " + String.join(", ", region.items()), NamedTextColor.GRAY));
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only a player can teleport.", NamedTextColor.RED));
            return;
        }
        ContainmentRegion region = requireRegion(sender, args);
        if (region == null) {
            return;
        }
        Location teleport = region.teleportLocation();
        if (teleport == null) {
            player.sendMessage(Component.text("Region world is not loaded.", NamedTextColor.RED));
            return;
        }
        player.teleport(teleport);
    }

    private void handleBind(CommandSender sender, String[] args, boolean bind) {
        if (args.length < 4) {
            sender.sendMessage(Component.text("Usage: /customitems containment "
                    + (bind ? "bind" : "unbind") + " <region> <custom_item_id>", NamedTextColor.RED));
            return;
        }
        ContainmentRegion region = manager.get(args[2]);
        if (region == null) {
            sender.sendMessage(Component.text("No region named " + args[2] + ".", NamedTextColor.RED));
            return;
        }
        String id = args[3];
        boolean changed = bind ? region.addItem(id) : region.removeItem(id);
        if (!changed) {
            sender.sendMessage(Component.text(bind ? "Item already bound." : "Item was not bound.", NamedTextColor.YELLOW));
            return;
        }
        manager.save();
        sender.sendMessage(Component.text((bind ? "Bound " : "Unbound ") + id + " "
                + (bind ? "to " : "from ") + region.name() + ".", NamedTextColor.GREEN));
    }

    private ContainmentRegion requireRegion(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Specify a region name.", NamedTextColor.RED));
            return null;
        }
        ContainmentRegion region = manager.get(args[2]);
        if (region == null) {
            sender.sendMessage(Component.text("No region named " + args[2] + ".", NamedTextColor.RED));
        }
        return region;
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /customitems containment <wand|pos1|pos2|create|delete|list|info|tp|bind|unbind>", NamedTextColor.YELLOW));
    }

    public List<String> tabComplete(String[] args) {
        if (args.length == 2) {
            return prefix(List.of("wand", "pos1", "pos2", "create", "delete", "list", "info", "tp", "bind", "unbind"), args[1]);
        }
        if (args.length == 3) {
            String sub = args[1].toLowerCase();
            if (sub.equals("delete") || sub.equals("info") || sub.equals("tp") || sub.equals("bind") || sub.equals("unbind")) {
                List<String> names = new ArrayList<>();
                for (ContainmentRegion region : manager.all()) {
                    names.add(region.name());
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
