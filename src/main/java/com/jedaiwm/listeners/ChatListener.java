package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    private final Map<String, List<String>> triggerResponses = new HashMap<>();
    private final Map<String, List<String>> triggerDevoutResponses = new HashMap<>();
    private final Map<String, Set<String>> triggerAliases = new HashMap<>();
    private final Map<UUID, Map<String, Long>> playerCooldowns = new HashMap<>();

    private final Map<String, String> triggerCategory = new HashMap<>();

    private static final long COOLDOWN_MS = 45_000;
    private static final int MAX_DELAY_TICKS = 60;

    public ChatListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
        loadTriggers();
    }

    public void loadTriggers() {
        triggerResponses.clear();
        triggerDevoutResponses.clear();
        triggerAliases.clear();
        triggerCategory.clear();

        File triggersFile = new File(plugin.getDataFolder(), "triggers.yml");
        if (!triggersFile.exists()) {
            plugin.saveResource("triggers.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(triggersFile);
        ConfigurationSection categories = config.getConfigurationSection("categories");

        ConfigurationSection triggers = config.getConfigurationSection("triggers");
        if (triggers == null) return;

        for (String key : triggers.getKeys(false)) {
            ConfigurationSection trigger = triggers.getConfigurationSection(key);
            if (trigger == null) continue;

            String cat = categories != null && categories.contains(key) 
                ? key 
                : "default";

            List<String> aliases = new ArrayList<>();
            aliases.add(key.toLowerCase());
            if (trigger.contains("aliases")) {
                for (String alias : trigger.getStringList("aliases")) {
                    aliases.add(alias.toLowerCase());
                }
            }

            for (String alias : aliases) {
                triggerResponses.put(alias, trigger.getStringList("responses"));
                triggerDevoutResponses.put(alias, trigger.getStringList("responses").isEmpty() 
                    ? trigger.getStringList("responses")
                    : trigger.getStringList("responses"));
                if (trigger.contains("devout")) {
                    triggerDevoutResponses.put(alias, trigger.getStringList("devout"));
                }
                triggerCategory.put(alias, key);
            }
        }

        plugin.getLogger().info("Loaded " + triggerResponses.size() + " chat triggers.");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage().toLowerCase();

        for (String trigger : triggerResponses.keySet()) {
            if (message.contains(trigger)) {
                handleTrigger(event.getPlayer(), trigger, message);
                break;
            }
        }
    }

    private void handleTrigger(Player sender, String trigger, String message) {
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories." + triggerCategory.getOrDefault(trigger, "political"));
        if (categories != null && !categories.getBoolean("enabled", true)) return;

        File triggersFile = new File(plugin.getDataFolder(), "triggers.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(triggersFile);
        String category = triggerCategory.getOrDefault(trigger, "political");
        if (config.contains("categories." + category + ".enabled") && !config.getBoolean("categories." + category + ".enabled", true)) {
            return;
        }

        List<Player> jews = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (jewManager.isJew(p)) {
                jews.add(p);
            }
        }

        if (jews.isEmpty()) return;

        Set<String> usedPhrases = new HashSet<>();

        for (Player jew : jews) {
            JewPlayer jewData = jewManager.getJew(jew);
            if (jewData == null) continue;

            if (jew.equals(sender)) continue;

            if (!checkCooldown(jew.getUniqueId(), trigger)) continue;

            int level = jewData.getLevel();
            int piety = jewData.getPiety();

            double baseChance = (level / 5.0) * 0.5;
            if (piety > 50) {
                baseChance += Math.min(0.1, ((piety - 50) / 20.0) * 0.05);
            }

            if (random.nextDouble() > baseChance) continue;

            List<String> pool = (piety >= 80) ? triggerDevoutResponses.get(trigger) : triggerResponses.get(trigger);
            if (pool == null || pool.isEmpty()) continue;

            String phrase = pool.get(random.nextInt(pool.size()));
            if (usedPhrases.contains(phrase)) {
                for (String p : pool) {
                    if (!usedPhrases.contains(p)) {
                        phrase = p;
                        break;
                    }
                }
            }
            usedPhrases.add(phrase);

            setCooldown(jew.getUniqueId(), trigger);

            int delay = random.nextInt(MAX_DELAY_TICKS);
            final String finalPhrase = phrase;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (jew.isOnline()) {
                    Component response = Component.text()
                        .append(Component.text("<" + jew.getName() + "> ", NamedTextColor.GOLD))
                        .append(Component.text(finalPhrase, NamedTextColor.WHITE))
                        .build();
                    Bukkit.getServer().sendMessage(response);
                }
            }, delay);
        }
    }

    private boolean checkCooldown(UUID playerId, String trigger) {
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) return true;
        Long lastTrigger = cooldowns.get(trigger);
        return lastTrigger == null || System.currentTimeMillis() - lastTrigger > COOLDOWN_MS;
    }

    private void setCooldown(UUID playerId, String trigger) {
        playerCooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(trigger, System.currentTimeMillis());
    }
}
