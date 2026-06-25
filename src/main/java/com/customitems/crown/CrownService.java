package com.customitems.crown;

import com.customitems.CustomItemsPlugin;
import com.customitems.config.CustomItemsConfig;
import com.customitems.item.CrownItem;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class CrownService {

    private static final int EFFECT_DURATION_TICKS = 220;
    private static final int EFFECT_AMPLIFIER = 0;

    private final CustomItemsConfig config;
    private final CrownItem crownItem;
    private final NamespacedKey healthModifierKey;

    public CrownService(CustomItemsPlugin plugin, CustomItemsConfig config, CrownItem crownItem) {
        this.config = config;
        this.crownItem = crownItem;
        this.healthModifierKey = new NamespacedKey(plugin, "crown_health_bonus");
    }

    public void sync(Player player) {
        boolean wearingCrown = config.isCrownEnabled()
                && crownItem.isCrown(player.getInventory().getHelmet());

        if (wearingCrown) {
            applyHealthBonus(player);
            applyEffects(player);
        } else if (hasHealthBonus(player)) {
            reset(player);
        }
    }

    public void reset(Player player) {
        removeHealthBonus(player);
        removeEffects(player);
    }

    private void applyHealthBonus(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        double extraHealth = config.getCrownExtraHealth();
        AttributeModifier existing = findModifier(attribute);

        if (existing != null) {
            if (existing.getAmount() == extraHealth) {
                return;
            }
            attribute.removeModifier(existing);
        }

        if (extraHealth <= 0) {
            return;
        }

        attribute.addModifier(new AttributeModifier(
                healthModifierKey,
                extraHealth,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ANY));
    }

    private void removeHealthBonus(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        AttributeModifier existing = findModifier(attribute);
        if (existing == null) {
            return;
        }

        attribute.removeModifier(existing);
        clampHealth(player, attribute);
    }

    private void clampHealth(Player player, AttributeInstance attribute) {
        double maxHealth = attribute.getValue();
        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private boolean hasHealthBonus(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        return attribute != null && findModifier(attribute) != null;
    }

    private AttributeModifier findModifier(AttributeInstance attribute) {
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (healthModifierKey.equals(modifier.getKey())) {
                return modifier;
            }
        }
        return null;
    }

    private void applyEffects(Player player) {
        if (config.isCrownResistance()) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.RESISTANCE, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER, true, false, true));
        }
        if (config.isCrownRegeneration()) {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER, true, false, true));
        }
    }

    private void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.REGENERATION);
    }
}
