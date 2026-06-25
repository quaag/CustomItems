package com.customitems.item;

import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public final class MaskItem {

    public static final String ID = "mask";
    public static final String DISPLAY_NAME = "Mask";

    private final CustomItemsConfig config;
    private final ItemKeys keys;

    public MaskItem(CustomItemsConfig config, ItemKeys keys) {
        this.config = config;
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));

        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(config.isMaskGlint());

        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of((float) config.getMaskCustomModelData()));
        meta.setCustomModelDataComponent(modelData);

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isMask(ItemStack item) {
        return item != null && item.getType() == Material.LEATHER_HELMET && keys.hasId(item, ID);
    }
}
