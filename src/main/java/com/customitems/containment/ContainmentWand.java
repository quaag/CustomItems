package com.customitems.containment;

import com.customitems.item.ItemKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ContainmentWand {

    public static final String ID = "containment_wand";

    private final ItemKeys keys;

    public ContainmentWand(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Containment Wand")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isWand(ItemStack item) {
        return item != null && item.getType() == Material.WOODEN_AXE && keys.hasId(item, ID);
    }
}
