package com.customitems.item;

import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;

import java.util.List;

public final class CrownItem {

    public static final String ID = "crown";
    public static final String DISPLAY_NAME = "Crown";

    private static final double DIAMOND_HELMET_ARMOR = 3.0;
    private static final double DIAMOND_HELMET_TOUGHNESS = 2.0;

    private final CustomItemsConfig config;
    private final ItemKeys keys;

    public CrownItem(CustomItemsConfig config, ItemKeys keys) {
        this.config = config;
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
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
        equippable.setModel(null);
        equippable.setCameraOverlay(null);
        meta.setEquippable(equippable);

        meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(
                keys.namespaced("crown_armor"), DIAMOND_HELMET_ARMOR,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));
        meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(
                keys.namespaced("crown_armor_toughness"), DIAMOND_HELMET_TOUGHNESS,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD));

        keys.writeId(meta, ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isCrown(ItemStack item) {
        return item != null && item.getType() == Material.CARVED_PUMPKIN && keys.hasId(item, ID);
    }
}
