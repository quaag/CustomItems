package com.customitems.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

public final class SmeltersPickaxeItem {

    public static final String ID = "smelters_pickaxe";
    public static final String DISPLAY_NAME = "Smelter's Pickaxe";

    private final ItemKeys keys;

    public SmeltersPickaxeItem(ItemKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of(1.0F));
        meta.setCustomModelDataComponent(modelData);

        meta.addEnchant(Enchantment.EFFICIENCY, 2, true);
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setEnchantmentGlintOverride(false);

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isSmeltersPickaxe(ItemStack item) {
        return item != null && item.getType() == Material.IRON_PICKAXE && keys.hasId(item, ID);
    }
}
