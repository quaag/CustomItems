package com.customitems.mask;

import com.customitems.config.CustomItemsConfig;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MaskSkinService {

    private static final String TEXTURES = "textures";

    private final Plugin plugin;
    private final CustomItemsConfig config;
    private final Map<UUID, PlayerProfile> originalProfiles = new HashMap<>();

    public MaskSkinService(Plugin plugin, CustomItemsConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean isActive() {
        return config.isMaskChangeTabSkin() && config.hasMaskSkin();
    }

    public void apply(Player player) {
        debug("Skin apply starting for " + player.getName());
        debug("Config skin value exists: " + config.hasMaskSkin());

        if (!isActive()) {
            debug("Skin spoofing inactive (change-tab-skin=" + config.isMaskChangeTabSkin()
                    + ", hasValue=" + config.hasMaskSkin() + ")");
            return;
        }
        if (originalProfiles.containsKey(player.getUniqueId())) {
            debug("Profile already spoofed for " + player.getName() + ", skipping");
            return;
        }

        originalProfiles.put(player.getUniqueId(), player.getPlayerProfile());

        PlayerProfile masked = player.getPlayerProfile();
        masked.removeProperty(TEXTURES);
        masked.setProperty(new ProfileProperty(TEXTURES, config.getMaskSkinValue(), signatureOrNull()));

        boolean propertySet = masked.hasProperty(TEXTURES);
        debug("Texture property set on profile: " + propertySet);

        player.setPlayerProfile(masked);
        refresh(player, false);
    }

    public void restore(Player player) {
        PlayerProfile original = originalProfiles.remove(player.getUniqueId());
        if (original == null) {
            return;
        }

        player.setPlayerProfile(original);
        refresh(player, false);
        debug("Original profile restored for " + player.getName());
    }

    public void restoreImmediate(Player player) {
        PlayerProfile original = originalProfiles.remove(player.getUniqueId());
        if (original == null) {
            return;
        }

        player.setPlayerProfile(original);
        refresh(player, true);
        debug("Original profile restored immediately for " + player.getName());
    }

    public boolean isSpoofed(Player player) {
        return originalProfiles.containsKey(player.getUniqueId());
    }

    private void debug(String message) {
        if (config.isDebug()) {
            plugin.getLogger().info("[maskskin] " + message);
        }
    }

    private String signatureOrNull() {
        String signature = config.getMaskSkinSignature();
        return signature == null || signature.isEmpty() ? null : signature;
    }

    private void refresh(Player player, boolean immediate) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                viewer.hidePlayer(plugin, player);
            }
        }

        Runnable show = () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.equals(player) && player.isOnline()) {
                    viewer.showPlayer(plugin, player);
                }
            }
            debug("Hide/show refresh ran for " + player.getName() + " (immediate=" + immediate + ")");
        };

        if (immediate) {
            show.run();
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, show, 2L);
        }
    }
}
