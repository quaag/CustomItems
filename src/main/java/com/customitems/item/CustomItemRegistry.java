package com.customitems.item;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class CustomItemRegistry {

    private final CrownItem crownItem;
    private final MaskItem maskItem;

    public CustomItemRegistry(CrownItem crownItem, MaskItem maskItem) {
        this.crownItem = crownItem;
        this.maskItem = maskItem;
    }

    public CrownItem crown() {
        return crownItem;
    }

    public MaskItem mask() {
        return maskItem;
    }

    public List<String> ids() {
        return List.of(CrownItem.ID, MaskItem.ID);
    }

    public ItemStack create(String id) {
        if (CrownItem.ID.equalsIgnoreCase(id)) {
            return crownItem.create();
        }
        if (MaskItem.ID.equalsIgnoreCase(id)) {
            return maskItem.create();
        }
        return null;
    }

    public String displayName(String id) {
        if (CrownItem.ID.equalsIgnoreCase(id)) {
            return CrownItem.DISPLAY_NAME;
        }
        if (MaskItem.ID.equalsIgnoreCase(id)) {
            return MaskItem.DISPLAY_NAME;
        }
        return id;
    }
}
