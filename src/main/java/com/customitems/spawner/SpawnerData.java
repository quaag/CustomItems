package com.customitems.spawner;

import org.bukkit.Location;
import org.bukkit.World;

public final class SpawnerData {

    private final SpawnerLocation location;
    private String mobType;
    private String rateMode;
    private int rateSlow;
    private int rateMedium;
    private int rateFast;
    private int spawnCount;
    private int maxNearbyMobs;
    private int spawnRange;
    private int secondsElapsed;

    public SpawnerData(SpawnerLocation location, String mobType, String rateMode,
                       int rateSlow, int rateMedium, int rateFast,
                       int spawnCount, int maxNearbyMobs, int spawnRange) {
        this.location = location;
        this.mobType = mobType;
        this.rateMode = rateMode;
        this.rateSlow = rateSlow;
        this.rateMedium = rateMedium;
        this.rateFast = rateFast;
        this.spawnCount = spawnCount;
        this.maxNearbyMobs = maxNearbyMobs;
        this.spawnRange = spawnRange;
    }

    public SpawnerLocation location() {
        return location;
    }

    public String key() {
        return location.key();
    }

    public World resolveWorld() {
        return location.resolveWorld();
    }

    public Location center() {
        return location.center();
    }

    public String getMobType() {
        return mobType;
    }

    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    public String getRateMode() {
        return rateMode;
    }

    public void setRateMode(String rateMode) {
        this.rateMode = rateMode;
    }

    public int getRateSlow() {
        return rateSlow;
    }

    public int getRateMedium() {
        return rateMedium;
    }

    public int getRateFast() {
        return rateFast;
    }

    public void setRate(String mode, int seconds) {
        switch (mode.toLowerCase()) {
            case "slow" -> rateSlow = seconds;
            case "fast" -> rateFast = seconds;
            default -> rateMedium = seconds;
        }
    }

    public int getSpawnCount() {
        return spawnCount;
    }

    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    public int getMaxNearbyMobs() {
        return maxNearbyMobs;
    }

    public void setMaxNearbyMobs(int maxNearbyMobs) {
        this.maxNearbyMobs = maxNearbyMobs;
    }

    public int getSpawnRange() {
        return spawnRange;
    }

    public void setSpawnRange(int spawnRange) {
        this.spawnRange = spawnRange;
    }

    public int activeRateSeconds() {
        int seconds = switch (rateMode == null ? "medium" : rateMode.toLowerCase()) {
            case "slow" -> rateSlow;
            case "fast" -> rateFast;
            default -> rateMedium;
        };
        return Math.max(1, seconds);
    }

    public void incrementSecond() {
        secondsElapsed++;
    }

    public int getSecondsElapsed() {
        return secondsElapsed;
    }

    public void resetSeconds() {
        secondsElapsed = 0;
    }
}
