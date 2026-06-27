package com.customitems.enchant;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EnchantControlManager {

    public record CapRule(int maxLevel, int requireLevel) {
    }

    private static final String ROOT = "enchant-control";

    private final JavaPlugin plugin;
    private boolean enabled;
    private final Set<Enchantment> blockedCombining = new LinkedHashSet<>();
    private final Map<Enchantment, CapRule> tableCapped = new LinkedHashMap<>();
    private String blockedCombineMessage;

    public EnchantControlManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        var config = plugin.getConfig();
        enabled = config.getBoolean(ROOT + ".enabled", true);

        blockedCombining.clear();
        for (String raw : config.getStringList(ROOT + ".blocked-combining")) {
            Enchantment enchantment = parse(raw);
            if (enchantment != null) {
                blockedCombining.add(enchantment);
            }
        }

        tableCapped.clear();
        ConfigurationSection capped = config.getConfigurationSection(ROOT + ".table-capped");
        if (capped != null) {
            for (String key : capped.getKeys(false)) {
                Enchantment enchantment = parse(key);
                if (enchantment == null) {
                    continue;
                }
                int max = capped.getInt(key + ".max-level", 1);
                int require = capped.getInt(key + ".require-level", 30);
                tableCapped.put(enchantment, new CapRule(max, require));
            }
        }

        blockedCombineMessage = config.getString(ROOT + ".messages.blocked-combine",
                "&cThat enchantment cannot be combined.");
    }

    public void reload() {
        plugin.reloadConfig();
        load();
    }

    public void save() {
        var config = plugin.getConfig();
        config.set(ROOT + ".enabled", enabled);

        List<String> blocked = new ArrayList<>();
        for (Enchantment enchantment : blockedCombining) {
            blocked.add(enchantment.getKey().getKey());
        }
        config.set(ROOT + ".blocked-combining", blocked);

        config.set(ROOT + ".table-capped", null);
        for (Map.Entry<Enchantment, CapRule> entry : tableCapped.entrySet()) {
            String base = ROOT + ".table-capped." + entry.getKey().getKey() + ".";
            config.set(base + "max-level", entry.getValue().maxLevel());
            config.set(base + "require-level", entry.getValue().requireLevel());
        }

        config.set(ROOT + ".messages.blocked-combine", blockedCombineMessage);
        plugin.saveConfig();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<Enchantment> blockedCombining() {
        return blockedCombining;
    }

    public Map<Enchantment, CapRule> tableCapped() {
        return tableCapped;
    }

    public boolean addBlocked(Enchantment enchantment) {
        boolean added = blockedCombining.add(enchantment);
        if (added) {
            save();
        }
        return added;
    }

    public boolean removeBlocked(Enchantment enchantment) {
        boolean removed = blockedCombining.remove(enchantment);
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean addCapped(Enchantment enchantment, int maxLevel, int requireLevel) {
        tableCapped.put(enchantment, new CapRule(maxLevel, requireLevel));
        save();
        return true;
    }

    public boolean removeCapped(Enchantment enchantment) {
        boolean removed = tableCapped.remove(enchantment) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Component blockedCombineMessage() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(blockedCombineMessage);
    }

    public static Enchantment parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String trimmed = raw.trim().toLowerCase();
            NamespacedKey key = trimmed.indexOf(':') >= 0
                    ? NamespacedKey.fromString(trimmed)
                    : NamespacedKey.minecraft(trimmed);
            if (key == null) {
                return null;
            }
            return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
