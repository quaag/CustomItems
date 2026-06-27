package com.customitems.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public final class SculkweaversShellItem {

    public static final String ID = "sculkweavers_shell";
    public static final String DISPLAY_NAME = "Sculkweavers Shell";

    private final ItemKeys keys;

    public SculkweaversShellItem(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.SOUL_LANTERN);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.DARK_AQUA)
                .decoration(TextDecoration.ITALIC, false));

        meta.setUnbreakable(true);

        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of(3.0F));
        meta.setCustomModelDataComponent(modelData);

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isSculkweaversShell(ItemStack item) {
        return item != null && item.getType() == Material.SOUL_LANTERN && keys.hasId(item, ID);
    }
}
