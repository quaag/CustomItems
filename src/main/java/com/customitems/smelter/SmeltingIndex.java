package com.customitems.smelter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class SmeltingIndex {

    private final Map<Material, ItemStack> results = new EnumMap<>(Material.class);

    public void rebuild() {
        results.clear();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) {
                continue;
            }
            ItemStack result = furnaceRecipe.getResult();
            if (result == null || result.getType().isAir()) {
                continue;
            }
            for (Material material : inputMaterials(furnaceRecipe)) {
                results.putIfAbsent(material, result);
            }
        }
    }

    public ItemStack smelt(ItemStack input) {
        if (input == null) {
            return null;
        }
        ItemStack result = results.get(input.getType());
        if (result == null) {
            return null;
        }
        ItemStack smelted = result.clone();
        smelted.setAmount(result.getAmount() * input.getAmount());
        return smelted;
    }

    public boolean hasResult(Material material) {
        return results.containsKey(material);
    }

    private Iterable<Material> inputMaterials(FurnaceRecipe recipe) {
        RecipeChoice choice = recipe.getInputChoice();
        if (choice instanceof RecipeChoice.MaterialChoice materialChoice) {
            return materialChoice.getChoices();
        }
        if (choice instanceof RecipeChoice.ExactChoice exactChoice) {
            List<Material> materials = new ArrayList<>();
            for (ItemStack stack : exactChoice.getChoices()) {
                materials.add(stack.getType());
            }
            return materials;
        }
        return List.of();
    }
}
