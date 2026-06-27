package com.customitems.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class PlayerHeadItem {

    public static final String ID = "consumable_player_head";

    private final ItemKeys keys;

    public PlayerHeadItem(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create(OfflinePlayer owner) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(owner);
        meta.displayName(Component.text(owner.getName() + "'s Head")
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isConsumableHead(ItemStack item) {
        return item != null && item.getType() == Material.PLAYER_HEAD && keys.hasId(item, ID);
    }
}
