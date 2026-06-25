package com.customitems.item;

import com.customitems.CustomItemsPlugin;
import com.customitems.config.CustomItemsConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public final class CrownItem {

    public static final String CROWN_ID = "crown";

    private final CustomItemsConfig config;
    private final NamespacedKey itemIdKey;

    public CrownItem(CustomItemsPlugin plugin, CustomItemsConfig config) {
        this.config = config;
        this.itemIdKey = new NamespacedKey(plugin, "custom_item_id");
    }

    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Crown")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);

        CustomModelDataComponent modelData = meta.getCustomModelDataComponent();
        modelData.setFloats(List.of((float) config.getCustomModelData()));
        meta.setCustomModelDataComponent(modelData);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(itemIdKey, PersistentDataType.STRING, CROWN_ID);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isCrown(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_HELMET || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        return CROWN_ID.equals(id);
    }
}
