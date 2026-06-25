package com.customitems.item;

import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;

import java.util.List;

public final class CrownItem {

    public static final String ID = "crown";
    public static final String DISPLAY_NAME = "Crown";

    private final CustomItemsConfig config;
    private final ItemKeys keys;

    public CrownItem(CustomItemsConfig config, ItemKeys keys) {
        this.config = config;
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(DISPLAY_NAME)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);

        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of((float) config.getCrownCustomModelData()));
        meta.setCustomModelDataComponent(modelData);

        EquippableComponent equippable = meta.getEquippable();
        equippable.setSlot(EquipmentSlot.HEAD);
        equippable.setModel(keys.equipmentAsset(ID));
        meta.setEquippable(equippable);

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isCrown(ItemStack item) {
        return item != null && item.getType() == Material.DIAMOND_HELMET && keys.hasId(item, ID);
    }
}
