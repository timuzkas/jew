package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class JewFeaturesListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();
    private final Map<UUID, Long> jewSenseCooldowns = new HashMap<>();
    private final Map<UUID, Long> phraseCooldowns = new HashMap<>();
    private final List<String> goldItems = List.of(
        "GOLD_INGOT", "GOLD_NUGGET", "GOLD_BLOCK", "GOLD_ORE", 
        "DEEPSLATE_GOLD_ORE", "RAW_GOLD", "GOLDEN_APPLE", 
        "GOLDEN_CARROT", "GLISTERING_MELON_SLICE", "CLOCK"
    );

    public JewFeaturesListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
        
        Bukkit.getScheduler().runTaskTimer(plugin, this::runJewFeatures, 0L, 40L);
    }

    private void runJewFeatures() {
        boolean isShabbat = plugin.getShabbatManager().isShabbat();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!jewManager.isJew(player)) continue;
            
            JewPlayer jew = jewManager.getJew(player);
            
            runGoldMagnet(player, jew);
            
            runJewSense(player);
            
            checkSynagogueProximity(player, jew);
            
            if (isShabbat) {
                ActionBarUtil.sendActionBar(player, "\uD83D\uDD4F Shabbat Shalom \u2014 Rest is commanded.");
            }
        }
    }

    private void runGoldMagnet(Player player, JewPlayer jew) {
        double radius = jew.getGoldMagnetRadius();
        Location playerLoc = player.getLocation();
        
        for (Entity entity : player.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
            if (entity instanceof Item item) {
                ItemStack itemStack = item.getItemStack();
                Material type = itemStack.getType();
                
                if (isGoldItem(type)) {
                    Location itemLoc = item.getLocation();
                    Vector direction = playerLoc.clone().subtract(itemLoc).normalize();
                    double speed = 0.15;
                    item.setVelocity(direction.multiply(speed));
                }
            }
        }
    }

    private boolean isGoldItem(Material type) {
        String name = type.toString();
        if (name.contains("GOLD")) return true;
        for (String gold : goldItems) {
            if (type == Material.getMaterial(gold)) return true;
        }
        return false;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!jewManager.isJew(player)) return;
        
        ItemStack item = event.getItem().getItemStack();
        if (isGoldItem(item.getType())) {
            JewPlayer jew = jewManager.getJew(player);
            if (jew != null && random.nextDouble() < 0.3) {
                ActionBarUtil.sendActionBar(player, "\uD83D\uDCB0 Yours now.");
                triggerPhrase(player, "gold_pickup");
            }
        }
    }

    private void runJewSense(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        if (jewSenseCooldowns.containsKey(uuid)) {
            if (now - jewSenseCooldowns.get(uuid) < 3 * 60 * 1000) {
                return;
            }
        }
        
        for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
            if (entity instanceof Player nearby && !nearby.equals(player)) {
                if (!jewManager.isJew(nearby)) {
                    ActionBarUtil.sendActionBar(player, "\uD83D\uDC41 A gentile approaches.");
                    jewSenseCooldowns.put(uuid, now);
                    break;
                }
            }
        }
    }

    private void checkSynagogueProximity(Player player, JewPlayer jew) {
        String blockMaterial = plugin.getConfig().getString("synagogue-block.material", "GOLD_BLOCK");
        boolean nearSynagogue = false;
        
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    Block block = player.getLocation().add(x, y, z).getBlock();
                    if (block.getType().toString().equals(blockMaterial)) {
                        nearSynagogue = true;
                        break;
                    }
                }
                if (nearSynagogue) break;
            }
            if (nearSynagogue) break;
        }
        
        jew.setNearSynagogue(nearSynagogue);
    }

    public void triggerPhrase(Player player, String trigger) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        if (phraseCooldowns.containsKey(uuid)) {
            if (now - phraseCooldowns.get(uuid) < 2 * 60 * 1000) {
                return;
            }
        }
        
        String phrase = getRandomPhrase(trigger);
        if (phrase != null) {
            player.sendMessage(ChatColor.GOLD + "* " + phrase + " *");
            phraseCooldowns.put(uuid, now);
        }
    }

    private String getRandomPhrase(String trigger) {
        List<String> phrases = switch (trigger) {
            case "gold_pickup" -> plugin.getConfig().getStringList("phrases.gold_pickup");
            case "damage" -> plugin.getConfig().getStringList("phrases.damage");
            case "death" -> plugin.getConfig().getStringList("phrases.death");
            case "forbidden_food" -> plugin.getConfig().getStringList("phrases.forbidden_food");
            case "rain" -> plugin.getConfig().getStringList("phrases.rain");
            case "low_piety" -> plugin.getConfig().getStringList("phrases.low_piety");
            default -> null;
        };
        
        if (phrases == null || phrases.isEmpty()) return null;
        return phrases.get(random.nextInt(phrases.size()));
    }

    public void checkAndTriggerBarMitzvah(Player player, JewPlayer jew) {
        if (jew.getLevel() >= 3 && !jew.hasBarMitzvah() && !jew.hasBatMitzvah()) {
            jew.setBarMitzvah(true);
            jew.setMaxPietyCap(110);
            jew.addPiety(20);
            
            player.sendTitle("\uD83C\uDF89 " + "\u05D1\u05E8 \u05DE\u05E6\u05D5\u05D4", "You are now a son of the commandment.", 10, 70, 20);
            
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (jewManager.isJew(online) || online.hasPermission("jedaiwm.admin")) {
                    online.sendMessage(ChatColor.GOLD + "\u2721 " + player.getName() + " has had their Bar Mitzvah!");
                }
            }
            
            spawnFireworks(player);
            
            ActionBarUtil.sendActionBar(player, "You are now accountable for your mitzvot.");
            
            jew.save(new java.io.File(plugin.getDataFolder(), "jews"));
        }
    }

    private void spawnFireworks(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 3; i++) {
            Firework fw = player.getWorld().spawn(loc.add(random.nextDouble() * 2 - 1, 0, random.nextDouble() * 2 - 1), Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                .withColor(Color.fromBGR(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
                .build());
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        }
    }
}
