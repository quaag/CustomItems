package com.customitems.health;

import com.customitems.config.CustomItemsConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;

public final class MaxHealthService {

    private static final double HEALTH_PER_ROW = 20.0;

    private final CustomItemsConfig config;
    private final NamespacedKey modifierKey;

    public MaxHealthService(Plugin plugin, CustomItemsConfig config) {
        this.config = config;
        this.modifierKey = new NamespacedKey(plugin, "server_hearts_bonus");
    }

    public void sync(Player player) {
        if (config.isMaxHealthEnabled()) {
            apply(player);
        } else {
            remove(player);
        }
    }

    public void apply(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) {
            return;
        }

        double bonus = Math.max(0.0, (config.getMaxHealthRows() - 1) * HEALTH_PER_ROW);
        AttributeModifier existing = findModifier(attribute);

        if (bonus <= 0.0) {
            if (existing != null) {
                attribute.removeModifier(existing);
                clampHealth(player, attribute);
            }
            return;
        }

        if (existing != null) {
            if (existing.getAmount() == bonus) {
                return;
            }
            attribute.removeModifier(existing);
        }

        attribute.addModifier(new AttributeModifier(
                modifierKey, bonus, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
    }

    public void remove(Player player) {
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

    private AttributeModifier findModifier(AttributeInstance attribute) {
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifierKey.equals(modifier.getKey())) {
                return modifier;
            }
        }
        return null;
    }
}
