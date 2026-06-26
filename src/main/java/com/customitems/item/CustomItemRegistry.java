package com.customitems.item;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class CustomItemRegistry {

    private final CrownItem crownItem;
    private final MaskItem maskItem;
    private final SigningBookItem signingBookItem;

    public CustomItemRegistry(CrownItem crownItem, MaskItem maskItem, SigningBookItem signingBookItem) {
        this.crownItem = crownItem;
        this.maskItem = maskItem;
        this.signingBookItem = signingBookItem;
    }

    public CrownItem crown() {
        return crownItem;
    }

    public MaskItem mask() {
        return maskItem;
    }

    public SigningBookItem signingBook() {
        return signingBookItem;
    }

    public List<String> ids() {
        return List.of(CrownItem.ID, MaskItem.ID, SigningBookItem.ID);
    }

    public ItemStack create(String id) {
        if (CrownItem.ID.equalsIgnoreCase(id)) {
            return crownItem.create();
        }
        if (MaskItem.ID.equalsIgnoreCase(id)) {
            return maskItem.create();
        }
        if (SigningBookItem.ID.equalsIgnoreCase(id)) {
            return signingBookItem.create();
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
        if (SigningBookItem.ID.equalsIgnoreCase(id)) {
            return SigningBookItem.DISPLAY_NAME;
        }
        return id;
    }
}
