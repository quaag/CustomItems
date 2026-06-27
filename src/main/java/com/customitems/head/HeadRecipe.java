package com.customitems.head;

import com.customitems.item.GoldenHeadItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public final class HeadRecipe {

    private final Plugin plugin;
    private final GoldenHeadItem goldenHeadItem;
    private final NamespacedKey key;

    public HeadRecipe(Plugin plugin, GoldenHeadItem goldenHeadItem) {
        this.plugin = plugin;
        this.goldenHeadItem = goldenHeadItem;
        this.key = new NamespacedKey(plugin, "golden_head");
    }

    public void register() {
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, goldenHeadItem.create());
        recipe.shape("GGG", "GHG", "GGG");
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('H', Material.PLAYER_HEAD);
        Bukkit.addRecipe(recipe);
    }

    public void unregister() {
        Bukkit.removeRecipe(key);
    }
}
