package com.customitems.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class SpawnerItem {

    public static final String ID = "custom_mob_spawner";
    public static final String DISPLAY_NAME = "CustomMobSpawner";

    private final ItemKeys keys;

    public SpawnerItem(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.SPAWNER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isSpawnerItem(ItemStack item) {
        return item != null && item.getType() == Material.SPAWNER && keys.hasId(item, ID);
    }
}
