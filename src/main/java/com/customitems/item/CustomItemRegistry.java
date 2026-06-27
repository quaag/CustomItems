package com.customitems.item;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class CustomItemRegistry {

    private final CrownItem crownItem;
    private final MaskItem maskItem;
    private final SigningBookItem signingBookItem;
    private final SpawnerItem spawnerItem;
    private final GoldenHeadItem goldenHeadItem;

    public CustomItemRegistry(CrownItem crownItem, MaskItem maskItem, SigningBookItem signingBookItem,
                              SpawnerItem spawnerItem, GoldenHeadItem goldenHeadItem) {
        this.crownItem = crownItem;
        this.maskItem = maskItem;
        this.signingBookItem = signingBookItem;
        this.spawnerItem = spawnerItem;
        this.goldenHeadItem = goldenHeadItem;
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

    public SpawnerItem spawner() {
        return spawnerItem;
    }

    public GoldenHeadItem goldenHead() {
        return goldenHeadItem;
    }

    public List<String> ids() {
        return List.of(CrownItem.ID, MaskItem.ID, SigningBookItem.ID, SpawnerItem.ID, GoldenHeadItem.ID);
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
        if (SpawnerItem.ID.equalsIgnoreCase(id)) {
            return spawnerItem.create();
        }
        if (GoldenHeadItem.ID.equalsIgnoreCase(id)) {
            return goldenHeadItem.create();
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
        if (SpawnerItem.ID.equalsIgnoreCase(id)) {
            return SpawnerItem.DISPLAY_NAME;
        }
        if (GoldenHeadItem.ID.equalsIgnoreCase(id)) {
            return GoldenHeadItem.DISPLAY_NAME;
        }
        return id;
    }
}
