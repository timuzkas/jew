package com.jedaiwm.models;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class JewPlayer {

    private final UUID uuid;
    private String name;
    private int level;
    private int piety;
    private int maxPietyCap;
    private String convertedBy;
    private long joinedAt;
    private long lastPrayerTime;
    private long lastTorahTime;
    private boolean barMitzvah;
    private boolean batMitzvah;
    private boolean nearSynagogue;

    public JewPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.level = 1;
        this.piety = 0;
        this.maxPietyCap = 100;
        this.convertedBy = null;
        this.joinedAt = System.currentTimeMillis();
        this.lastPrayerTime = 0;
        this.lastTorahTime = 0;
        this.barMitzvah = false;
        this.batMitzvah = false;
        this.nearSynagogue = false;
    }

    public JewPlayer(UUID uuid, String name, int level, int piety, int maxPietyCap, String convertedBy, long joinedAt, boolean barMitzvah, boolean batMitzvah, boolean nearSynagogue) {
        this.uuid = uuid;
        this.name = name;
        this.level = level;
        this.piety = piety;
        this.maxPietyCap = maxPietyCap;
        this.convertedBy = convertedBy;
        this.joinedAt = joinedAt;
        this.lastPrayerTime = 0;
        this.lastTorahTime = 0;
        this.barMitzvah = barMitzvah;
        this.batMitzvah = batMitzvah;
        this.nearSynagogue = nearSynagogue;
    }

    public boolean isNearSynagogue() {
        return nearSynagogue;
    }

    public void setNearSynagogue(boolean nearSynagogue) {
        this.nearSynagogue = nearSynagogue;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPiety() {
        return piety;
    }

    public int getMaxPietyCap() {
        return maxPietyCap;
    }

    public void setMaxPietyCap(int maxPietyCap) {
        this.maxPietyCap = maxPietyCap;
    }

    public void setPiety(int piety) {
        this.piety = Math.max(0, Math.min(maxPietyCap, piety));
    }

    public void addPiety(int amount) {
        setPiety(piety + amount);
    }

    public void removePiety(int amount) {
        setPiety(piety - amount);
    }

    public String getConvertedBy() {
        return convertedBy;
    }

    public void setConvertedBy(String convertedBy) {
        this.convertedBy = convertedBy;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public long getLastPrayerTime() {
        return lastPrayerTime;
    }

    public void setLastPrayerTime(long lastPrayerTime) {
        this.lastPrayerTime = lastPrayerTime;
    }

    public long getLastTorahTime() {
        return lastTorahTime;
    }

    public void setLastTorahTime(long lastTorahTime) {
        this.lastTorahTime = lastTorahTime;
    }

    public boolean hasBarMitzvah() {
        return barMitzvah;
    }

    public void setBarMitzvah(boolean barMitzvah) {
        this.barMitzvah = barMitzvah;
    }

    public boolean hasBatMitzvah() {
        return batMitzvah;
    }

    public void setBatMitzvah(boolean batMitzvah) {
        this.batMitzvah = batMitzvah;
    }

    public String getLevelName() {
        if (piety >= 90) return "Rebbe";
        if (piety >= 70) return "Talmid Chacham";
        if (piety >= 45) return "Ben Torah";
        if (piety >= 20) return "Am Ha'aretz";
        return "Ger (Newcomer)";
    }

    public int getLevelFromPiety(int piety) {
        if (piety >= 90) return 5;
        if (piety >= 70) return 4;
        if (piety >= 45) return 3;
        if (piety >= 20) return 2;
        return 1;
    }

    public void updateLevel() {
        int newLevel = getLevelFromPiety(piety);
        if (newLevel != level) {
            level = newLevel;
        }
    }

    public double getGoldMagnetRadius() {
        return 5.0 + (level * 0.5);
    }

    public void save(File folder) {
        File file = new File(folder, uuid.toString() + ".yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("name", name);
        config.set("level", level);
        config.set("piety", piety);
        config.set("maxPietyCap", maxPietyCap);
        config.set("convertedBy", convertedBy);
        config.set("joinedAt", joinedAt);
        config.set("lastPrayerTime", lastPrayerTime);
        config.set("lastTorahTime", lastTorahTime);
        config.set("barMitzvah", barMitzvah);
        config.set("batMitzvah", batMitzvah);
        config.set("nearSynagogue", nearSynagogue);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JewPlayer load(File folder, UUID uuid) {
        File file = new File(folder, uuid.toString() + ".yml");
        if (!file.exists()) return null;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = config.getString("name");
        int level = config.getInt("level", 1);
        int piety = config.getInt("piety", 0);
        int maxPietyCap = config.getInt("maxPietyCap", 100);
        String convertedBy = config.getString("convertedBy");
        long joinedAt = config.getLong("joinedAt", System.currentTimeMillis());
        boolean barMitzvah = config.getBoolean("barMitzvah", false);
        boolean batMitzvah = config.getBoolean("batMitzvah", false);
        
        JewPlayer player = new JewPlayer(uuid, name, level, piety, maxPietyCap, convertedBy, joinedAt, barMitzvah, batMitzvah, false);
        player.setLastPrayerTime(config.getLong("lastPrayerTime", 0));
        player.setLastTorahTime(config.getLong("lastTorahTime", 0));
        return player;
    }
}
