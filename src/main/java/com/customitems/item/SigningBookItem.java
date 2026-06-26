package com.customitems.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class SigningBookItem {

    public static final String ID = "custom_signing_book";
    public static final String DISPLAY_NAME = "CustomSigningBook";

    private final ItemKeys keys;

    public SigningBookItem(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isSigningBook(ItemStack item) {
        return item != null && item.getType() == Material.WRITABLE_BOOK && keys.hasId(item, ID);
    }
}
