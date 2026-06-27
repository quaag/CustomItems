package com.customitems.head;

import com.customitems.config.CustomItemsConfig;
import com.customitems.item.GoldenHeadItem;
import com.customitems.item.PlayerHeadItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeadListener implements Listener {

    private final CustomItemsConfig config;
    private final PlayerHeadItem playerHeadItem;
    private final GoldenHeadItem goldenHeadItem;
    private final Map<UUID, Long> playerHeadCooldowns = new HashMap<>();
    private final Map<UUID, Long> goldenHeadCooldowns = new HashMap<>();

    public HeadListener(CustomItemsConfig config, PlayerHeadItem playerHeadItem, GoldenHeadItem goldenHeadItem) {
        this.config = config;
        this.playerHeadItem = playerHeadItem;
        this.goldenHeadItem = goldenHeadItem;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!config.isHeadsEnabled() || !config.isPlayerHeadDropOnKill()) {
            return;
        }
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) {
            return;
        }
        event.getDrops().add(playerHeadItem.create(victim));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!config.isHeadsEnabled() || event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (goldenHeadItem.isGoldenHead(item)) {
            event.setCancelled(true);
            consumeGoldenHead(event.getPlayer());
        } else if (playerHeadItem.isConsumableHead(item)) {
            event.setCancelled(true);
            consumePlayerHead(event.getPlayer());
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!isGoldenHeadRecipe(event.getRecipe())) {
            return;
        }
        if (!playerHeadItem.isConsumableHead(centerItem(event.getInventory().getMatrix()))) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!isGoldenHeadRecipe(event.getRecipe())) {
            return;
        }
        if (!playerHeadItem.isConsumableHead(centerItem(event.getInventory().getMatrix()))) {
            event.setCancelled(true);
        }
    }

    private boolean isGoldenHeadRecipe(Recipe recipe) {
        return recipe != null && goldenHeadItem.isGoldenHead(recipe.getResult());
    }

    private ItemStack centerItem(ItemStack[] matrix) {
        if (matrix.length == 9) {
            return matrix[4];
        }
        return null;
    }

    private void consumePlayerHead(Player player) {
        if (isOnCooldown(player, playerHeadCooldowns)) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                config.getPlayerHeadDuration() * 20, config.getPlayerHeadRegenAmplifier()));

        decrementMainHand(player);
        setCooldown(player, playerHeadCooldowns, config.getPlayerHeadCooldown());
    }

    private void consumeGoldenHead(Player player) {
        if (isOnCooldown(player, goldenHeadCooldowns)) {
            return;
        }
        int duration = config.getGoldenHeadDuration() * 20;
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, config.getGoldenHeadRegenAmplifier()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, config.getGoldenHeadAbsorptionAmplifier()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, config.getGoldenHeadSpeedAmplifier()));

        decrementMainHand(player);
        setCooldown(player, goldenHeadCooldowns, config.getGoldenHeadCooldown());
    }

    private boolean isOnCooldown(Player player, Map<UUID, Long> cooldowns) {
        long until = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        long now = System.currentTimeMillis();
        if (now < until) {
            long remaining = (until - now + 999L) / 1000L;
            player.sendActionBar(Component.text("On cooldown: " + remaining + "s", NamedTextColor.RED));
            return true;
        }
        return false;
    }

    private void setCooldown(Player player, Map<UUID, Long> cooldowns, int seconds) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    private void decrementMainHand(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        int amount = hand.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(amount - 1);
            player.getInventory().setItemInMainHand(hand);
        }
    }
}
