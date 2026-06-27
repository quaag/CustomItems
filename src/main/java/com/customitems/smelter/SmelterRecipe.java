package com.customitems.smelter;

import com.customitems.item.SmeltersPickaxeItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

public final class SmelterRecipe {

    private final SmeltersPickaxeItem pickaxeItem;
    private final NamespacedKey key;

    public SmelterRecipe(Plugin plugin, SmeltersPickaxeItem pickaxeItem) {
        this.pickaxeItem = pickaxeItem;
        this.key = new NamespacedKey(plugin, "smelters_pickaxe");
    }

    public void register() {
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, pickaxeItem.create());
        recipe.shape("RRR", "CPC", " L ");
        recipe.setIngredient('R', Material.RAW_IRON_BLOCK);
        recipe.setIngredient('C', Material.COAL_BLOCK);
        recipe.setIngredient('P', Material.IRON_PICKAXE);
        recipe.setIngredient('L', new RecipeChoice.MaterialChoice(Tag.LOGS));
        Bukkit.addRecipe(recipe);
    }

    public void unregister() {
        Bukkit.removeRecipe(key);
    }
}
