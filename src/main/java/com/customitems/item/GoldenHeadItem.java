package com.customitems.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class GoldenHeadItem {

    public static final String ID = "golden_head";
    public static final String DISPLAY_NAME = "Golden Head";

    private final ItemKeys keys;

    public GoldenHeadItem(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.setEnchantmentGlintOverride(true);

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isGoldenHead(ItemStack item) {
        return item != null && item.getType() == Material.PLAYER_HEAD && keys.hasId(item, ID);
    }
}
