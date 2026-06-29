package com.customitems.enchant;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public final class EnchantControlListener implements Listener {

    private static final int ANVIL_RESULT_SLOT = 2;

    private final EnchantControlManager manager;

    public EnchantControlListener(EnchantControlManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onAnvilTake(InventoryClickEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        if (!(event.getInventory() instanceof AnvilInventory anvil) || event.getRawSlot() != ANVIL_RESULT_SLOT) {
            return;
        }

        ItemStack result = event.getCurrentItem();
        ItemStack target = anvil.getFirstItem();
        ItemStack sacrifice = anvil.getSecondItem();
        if (result == null || result.getType().isAir() || sacrifice == null) {
            return;
        }

        for (Enchantment enchantment : manager.blockedCombining()) {
            if (isCombineBlocked(enchantment, result, target, sacrifice)) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    player.sendMessage(manager.blockedCombineMessage());
                }
                return;
            }
        }
    }

    private boolean isCombineBlocked(Enchantment enchantment, ItemStack result, ItemStack target, ItemStack sacrifice) {
        if (levelOf(result, enchantment) == 0) {
            return false;
        }
        int sacrificeLevel = levelOf(sacrifice, enchantment);
        if (sacrificeLevel == 0) {
            return false;
        }
        if (isBook(target) && isBook(sacrifice)) {
            return true;
        }
        return sacrificeLevel <= levelOf(target, enchantment);
    }

    private boolean isBook(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_BOOK;
    }

    private int levelOf(ItemStack item, Enchantment enchantment) {
        if (item == null) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return 0;
        }
        int level = meta.getEnchantLevel(enchantment);
        if (level == 0 && meta instanceof EnchantmentStorageMeta storage) {
            level = storage.getStoredEnchantLevel(enchantment);
        }
        return level;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (!manager.isEnabled()) {
            return;
        }
        int optionLevel = event.getExpLevelCost();
        Map<Enchantment, Integer> toAdd = event.getEnchantsToAdd();

        for (Map.Entry<Enchantment, EnchantControlManager.CapRule> entry : manager.tableCapped().entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = toAdd.get(enchantment);
            if (level == null) {
                continue;
            }
            EnchantControlManager.CapRule rule = entry.getValue();
            if (optionLevel < rule.requireLevel()) {
                toAdd.remove(enchantment);
            } else if (level > rule.maxLevel()) {
                toAdd.put(enchantment, rule.maxLevel());
            }
        }
    }

}
