package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarQueue;
import com.jedaiwm.utils.EffectsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
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
    private final Map<UUID, Long> damageCooldowns = new HashMap<>();

    private static final long DAMAGE_PHRASE_COOLDOWN = 10_000;

    private static final List<String> GOLD_ITEMS = List.of(
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!jewManager.isJew(player)) continue;
            JewPlayer jew = jewManager.getJew(player);
            runGoldMagnet(player, jew);
            runJewSense(player);
            checkSynagogueProximity(player, jew);
        }
    }

    private void runGoldMagnet(Player player, JewPlayer jew) {
        if (!plugin.getConfig().getBoolean("gold-magnet.enabled", true)) return;
        double radius = jew.getGoldMagnetRadius();
        Location playerLoc = player.getLocation();
        for (Entity entity : player.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
            if (!(entity instanceof Item item)) continue;
            if (!isGoldItem(item.getItemStack().getType())) continue;
            Location itemLoc = item.getLocation();
            Vector dir = playerLoc.clone().subtract(itemLoc).toVector();
            double dist = dir.length();
            if (dist < 0.5) continue;
            dir.normalize().multiply(0.3 + (0.1 * jew.getLevel()));
            item.setVelocity(dir);
            if (dist > 1.5) EffectsUtil.spawnGoldMagnetTrail(player, itemLoc);
        }
    }

    private boolean isGoldItem(Material type) {
        String n = type.toString();
        if (n.contains("GOLD") || n.contains("GOLDEN")) return true;
        for (String g : GOLD_ITEMS) { if (n.equals(g)) return true; }
        return false;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!jewManager.isJew(player)) return;
        if (!isGoldItem(event.getItem().getItemStack().getType())) return;
        if (random.nextDouble() >= 0.35) return;
        ActionBarQueue.typewriter(player, "\uD83D\uDCB0 Yours now.", ActionBarQueue.PRIORITY_INFO, 3, 30);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!jewManager.isJew(player)) return;
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        if (now - damageCooldowns.getOrDefault(uuid, 0L) < DAMAGE_PHRASE_COOLDOWN) return;
        damageCooldowns.put(uuid, now);
        if (random.nextDouble() >= 0.4) return;
        ActionBarQueue.flash(player, "\u2620 Oy vey!", "\u2620 Not again...",
            ActionBarQueue.PRIORITY_INFO, 5, 40);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!jewManager.isJew(player)) return;
        EffectsUtil.playSoundDeath(player);
    }

    private void runJewSense(Player player) {
        if (!plugin.getConfig().getBoolean("jew-sense.enabled", true)) return;
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (now - jewSenseCooldowns.getOrDefault(uuid, 0L) < 3 * 60 * 1000L) return;
        for (Entity entity : player.getNearbyEntities(8, 8, 8)) {
            if (!(entity instanceof Player nearby) || nearby.equals(player)) continue;
            if (!jewManager.isJew(nearby)) {
                ActionBarQueue.typewriter(player, "\uD83D\uDC41 A gentile approaches.",
                    ActionBarQueue.PRIORITY_INFO, 3, 30);
                jewSenseCooldowns.put(uuid, now);
                break;
            }
        }
    }

    private void checkSynagogueProximity(Player player, JewPlayer jew) {
        if (!plugin.getConfig().getBoolean("synagogue-block.proximity-bonus", true)) {
            jew.setNearSynagogue(false);
            return;
        }
        long tick = plugin.getServer().getCurrentTick();
        if (tick % 200 != 0) return;

        String mat = plugin.getConfig().getString("synagogue-block.material", "GOLD_BLOCK");
        boolean near = false;
        outer:
        for (int x = -10; x <= 10; x++) {
            for (int y = -4; y <= 6; y++) {
                for (int z = -10; z <= 10; z++) {
                    if (player.getLocation().add(x, y, z).getBlock().getType().toString().equals(mat)) {
                        near = true; break outer;
                    }
                }
            }
        }
        if (near && !jew.isNearSynagogue()) {
            EffectsUtil.playSoundSynagogueAmbient(player);
            EffectsUtil.spawnSynagogueAmbientParticles(player);
            ActionBarQueue.typewriter(player, "\uD83D\uDD4D You are near a holy place.",
                ActionBarQueue.PRIORITY_INFO, 3, 40);
        }
        jew.setNearSynagogue(near);
    }

    public void checkAndTriggerBarMitzvah(Player player, JewPlayer jew) {
        if (jew.getLevel() < 3 || jew.hasBarMitzvah() || jew.hasBatMitzvah()) return;
        jew.setBarMitzvah(true);
        jew.setMaxPietyCap(110);
        jew.addPiety(20);

        EffectsUtil.playSoundBarMitzvah(player);
        ActionBarQueue.typewriter(player,
            "\uD83C\uDF89 \u05D1\u05E8 \u05DE\u05E6\u05D5\u05D4 \u2014 You are now a son of the commandment.",
            ActionBarQueue.PRIORITY_RITUAL, 2, 80);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline())
                player.sendTitle("\uD83C\uDF89 \u05D1\u05E8 \u05DE\u05E6\u05D5\u05D4",
                    "You are now a son of the commandment.", 10, 70, 20);
        }, 30L);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (jewManager.isJew(online) || online.hasPermission("jedaiwm.admin"))
                online.sendMessage(ChatColor.GOLD + "\u2721 " + player.getName() + " has had their Bar Mitzvah!");
        }

        spawnFireworks(player);
        jew.save(new java.io.File(plugin.getDataFolder(), "jews"));
    }

    private void spawnFireworks(Player player) {
        Location loc = player.getLocation();
        for (int i = 0; i < 3; i++) {
            final int delay = i * 15;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                Firework fw = player.getWorld().spawn(
                    loc.clone().add(random.nextDouble() * 4 - 2, 0, random.nextDouble() * 4 - 2), Firework.class);
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
                    .withFade(Color.fromRGB(255, 215, 0))
                    .with(FireworkEffect.Type.STAR).build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            }, delay);
        }
    }
}
