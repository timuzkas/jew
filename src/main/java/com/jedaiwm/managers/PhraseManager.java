package com.jedaiwm.managers;

import com.jedaiwm.JedaiWM;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PhraseManager {

    private final JedaiWM plugin;
    private FileConfiguration phrasesConfig;

    public PhraseManager(JedaiWM plugin) {
        this.plugin = plugin;
        loadPhrases();
    }

    public void loadPhrases() {
        File phrasesFile = new File(plugin.getDataFolder(), "phrases.yml");
        if (!phrasesFile.exists()) {
            plugin.saveResource("phrases.yml", false);
        }
        phrasesConfig = YamlConfiguration.loadConfiguration(phrasesFile);
        plugin.getLogger().info("Loaded phrases.yml");
    }

    public List<String> getPhrases(String key) {
        return phrasesConfig.getStringList("phrases." + key);
    }

    public void savePhrases() {
        try {
            phrasesConfig.save(new File(plugin.getDataFolder(), "phrases.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
