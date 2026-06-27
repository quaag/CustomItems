package com.customitems.item;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public final class ItemKeys {

    private final Plugin plugin;
    private final NamespacedKey customItemId;

    public ItemKeys(Plugin plugin) {
        this.plugin = plugin;
        this.customItemId = new NamespacedKey(plugin, "custom_item_id");
    }

    public NamespacedKey customItemId() {
        return customItemId;
    }

    public NamespacedKey equipmentAsset(String id) {
        return new NamespacedKey(plugin, id);
    }

    public NamespacedKey namespaced(String id) {
        return new NamespacedKey(plugin, id);
    }

    public void writeId(ItemMeta meta, String id) {
        meta.getPersistentDataContainer().set(customItemId, PersistentDataType.STRING, id);
    }

    public String readId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(customItemId, PersistentDataType.STRING);
    }

    public boolean hasId(ItemStack item, String id) {
        return id.equals(readId(item));
    }
}
