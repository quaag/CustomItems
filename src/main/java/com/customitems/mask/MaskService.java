package com.customitems.mask;

import com.customitems.config.CustomItemsConfig;
import com.customitems.item.MaskItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MaskService {

    private static final String TEAM_NAME = "ci_masked";

    private final CustomItemsConfig config;
    private final MaskItem maskItem;
    private final MaskSkinService skinService;
    private final Set<UUID> maskedPlayers = new HashSet<>();

    public MaskService(CustomItemsConfig config, MaskItem maskItem, MaskSkinService skinService) {
        this.config = config;
        this.maskItem = maskItem;
        this.skinService = skinService;
    }

    public boolean isMasked(Player player) {
        return maskedPlayers.contains(player.getUniqueId());
    }

    public Component maskedName() {
        return Component.text(config.getMaskedName());
    }

    public void sync(Player player) {
        boolean wearingMask = config.isMaskEnabled()
                && maskItem.isMask(player.getInventory().getHelmet());

        if (wearingMask) {
            apply(player);
        } else if (isMasked(player)) {
            reset(player);
        }
    }

    public void reset(Player player) {
        boolean wasMasked = maskedPlayers.remove(player.getUniqueId());

        player.displayName(null);
        player.playerListName(null);
        removeFromNameplateTeam(player);
        skinService.restore(player);

        if (wasMasked && config.isDebug()) {
            player.getServer().getLogger().info("[CustomItems] Restored identity for " + player.getName());
        }
    }

    public void disable(Player player) {
        maskedPlayers.remove(player.getUniqueId());

        player.displayName(null);
        player.playerListName(null);
        removeFromNameplateTeam(player);
        skinService.restoreImmediate(player);
    }

    private void apply(Player player) {
        maskedPlayers.add(player.getUniqueId());
        Component masked = maskedName();

        if (config.isMaskChangeChatName()) {
            player.displayName(masked);
        }
        if (config.isMaskChangeTabName()) {
            player.playerListName(masked);
        }
        if (config.isMaskTryChangeNameplate()) {
            addToNameplateTeam(player);
        }
        skinService.apply(player);
    }

    private void addToNameplateTeam(Player player) {
        Scoreboard scoreboard = player.getServer().getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
        }
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    private void removeFromNameplateTeam(Player player) {
        Scoreboard scoreboard = player.getServer().getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null && team.hasEntry(player.getName())) {
            team.removeEntry(player.getName());
        }
    }
}
