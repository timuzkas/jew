package com.jedaiwm.managers;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.models.JewPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JewManager {

    private final JedaiWM plugin;
    private final Map<UUID, JewPlayer> jews;
    private final File dataFolder;

    public JewManager(JedaiWM plugin) {
        this.plugin = plugin;
        this.jews = new ConcurrentHashMap<>();
        this.dataFolder = new File(plugin.getDataFolder(), "jews");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        loadData();
    }

    public void loadData() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String uuidStr = file.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    JewPlayer jewPlayer = JewPlayer.load(dataFolder, uuid);
                    if (jewPlayer != null) {
                        jews.put(uuid, jewPlayer);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in file: " + file.getName());
                }
            }
        }
        plugin.getLogger().info("Loaded " + jews.size() + " Jews.");
    }

    public void saveData() {
        for (JewPlayer jew : jews.values()) {
            jew.save(dataFolder);
        }
        plugin.getLogger().info("Saved " + jews.size() + " Jews.");
    }

    public boolean isJew(UUID uuid) {
        return jews.containsKey(uuid);
    }

    public boolean isJew(Player player) {
        return isJew(player.getUniqueId());
    }

    public JewPlayer getJew(UUID uuid) {
        return jews.get(uuid);
    }

    public JewPlayer getJew(Player player) {
        return getJew(player.getUniqueId());
    }

    public JewPlayer makeJew(Player player, String convertedBy) {
        JewPlayer jewPlayer = new JewPlayer(player.getUniqueId(), player.getName());
        if (convertedBy != null && !convertedBy.isEmpty()) {
            jewPlayer.setConvertedBy(convertedBy);
        }
        jewPlayer.setPiety(50);
        jews.put(player.getUniqueId(), jewPlayer);
        jewPlayer.save(dataFolder);
        return jewPlayer;
    }

    public JewPlayer makeJew(OfflinePlayer player, String convertedBy) {
        JewPlayer jewPlayer = new JewPlayer(player.getUniqueId(), player.getName());
        if (convertedBy != null && !convertedBy.isEmpty()) {
            jewPlayer.setConvertedBy(convertedBy);
        }
        jewPlayer.setPiety(50);
        jews.put(player.getUniqueId(), jewPlayer);
        jewPlayer.save(dataFolder);
        return jewPlayer;
    }

    public boolean removeJew(UUID uuid) {
        JewPlayer removed = jews.remove(uuid);
        if (removed != null) {
            File file = new File(dataFolder, uuid.toString() + ".yml");
            if (file.exists()) {
                file.delete();
            }
            return true;
        }
        return false;
    }

    public Collection<JewPlayer> getAllJews() {
        return jews.values();
    }

    public List<String> getJewNames() {
        List<String> names = new ArrayList<>();
        for (JewPlayer jew : jews.values()) {
            names.add(jew.getName());
        }
        return names;
    }
}
