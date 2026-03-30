package com.jedaiwm.managers;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarQueue;
import com.jedaiwm.utils.EffectsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LowPietyManager {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    private final Map<UUID, Long> lastEventTime = new HashMap<>();
    private final Map<UUID, Long> thunderCooldowns = new HashMap<>();
    private final Map<UUID, Long> whisperCooldowns = new HashMap<>();
    private final Map<UUID, Long> godMessageCooldowns = new HashMap<>();

    private static final long GLOBAL_MIN_GAP_MS = 60_000;
    private static final long THUNDER_REPEAT_MS = 180_000;
    private static final long WHISPER_REPEAT_MS = 120_000;
    private static final long GOD_MSG_REPEAT_MS = 300_000;

    private static final int THRESHOLD_LOW = 15;
    private static final int THRESHOLD_DIRE = 5;

    private static final List<String> WHISPERS_LOW = List.of(
        "...remember...",
        "...return...",
        "...the path..."
    );

    private static final List<String> WHISPERS_DIRE = List.of(
        "...too late...",
        "...forgotten...",
        "...darkness..."
    );

    private static final List<String> GOD_MESSAGES_LOW = List.of(
        "My child, where have you gone?",
        "Return to the path.",
        "I am watching."
    );

    private static final List<String> GOD_MESSAGES_DIRE = List.of(
        "Repent. Now.",
        "Your name grows faint.",
        "Even Jonah returned."
    );

    public LowPietyManager(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
        startLoop();
    }

    private void startLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!jewManager.isJew(player)) continue;
                    JewPlayer jew = jewManager.getJew(player);
                    if (jew == null) continue;
                    if (jew.getPiety() >= THRESHOLD_LOW) continue;
                    if (ActionBarQueue.isRitualActive(player)) continue;
                    considerEvent(player, jew.getPiety());
                }
            }
        }.runTaskTimer(plugin, 100L, 200L);
    }

    private void considerEvent(Player player, int piety) {
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();

        if (now - lastEventTime.getOrDefault(uuid, 0L) < GLOBAL_MIN_GAP_MS) return;

        boolean isDire = piety < THRESHOLD_DIRE;
        double fireChance = isDire ? 0.4 : 0.2;
        if (random.nextDouble() > fireChance) return;

        int thunderWeight = canFire(thunderCooldowns, uuid, THUNDER_REPEAT_MS) ? (isDire ? 2 : 1) : 0;
        int whisperWeight = canFire(whisperCooldowns, uuid, WHISPER_REPEAT_MS) ? (isDire ? 3 : 2) : 0;
        int godMsgWeight = canFire(godMessageCooldowns, uuid, GOD_MSG_REPEAT_MS) ? (isDire ? 1 : 1) : 0;

        int total = thunderWeight + whisperWeight + godMsgWeight;
        if (total == 0) return;

        int roll = random.nextInt(total);
        int cursor = thunderWeight;
        if (roll < cursor) {
            fireThunder(player, isDire);
            thunderCooldowns.put(uuid, now);
            lastEventTime.put(uuid, now);
            return;
        }
        cursor += whisperWeight;
        if (roll < cursor) {
            fireWhisper(player, isDire);
            whisperCooldowns.put(uuid, now);
            lastEventTime.put(uuid, now);
            return;
        }
        fireGodMessage(player, isDire);
        godMessageCooldowns.put(uuid, now);
        lastEventTime.put(uuid, now);
    }

    private boolean canFire(Map<UUID, Long> map, UUID uuid, long cooldownMs) {
        return System.currentTimeMillis() - map.getOrDefault(uuid, 0L) >= cooldownMs;
    }

    private void fireThunder(Player player, boolean dire) {
        EffectsUtil.spawnThunderStrike(player);
        EffectsUtil.playSoundThunder(player);
        EffectsUtil.spawnHallucinationParticles(player);

        ActionBarQueue.typewriter(player, "\u26A0 Repent.", ActionBarQueue.PRIORITY_DIVINE, 2, 50);
    }

    private void fireWhisper(Player player, boolean dire) {
        List<String> pool = dire ? WHISPERS_DIRE : WHISPERS_LOW;
        String whisper = pool.get(random.nextInt(pool.size()));

        EffectsUtil.playSoundWhisper(player);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            Component msg = Component.text(whisper,
                Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));
            player.sendMessage(msg);
            EffectsUtil.spawnHallucinationParticles(player);
        }, 8L);
    }

    private void fireGodMessage(Player player, boolean dire) {
        List<String> pool = dire ? GOD_MESSAGES_DIRE : GOD_MESSAGES_LOW;
        String message = pool.get(random.nextInt(pool.size()));

        EffectsUtil.playSoundThunder(player);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            Component prefix = Component.text("[God] ",
                Style.style(NamedTextColor.GOLD, TextDecoration.BOLD));
            Component body = Component.text(message, NamedTextColor.YELLOW);
            player.sendMessage(Component.empty().append(prefix).append(body));

            ActionBarQueue.typewriter(player, "\u2721 " + message,
                ActionBarQueue.PRIORITY_DIVINE, 2, 60);
        }, 12L);
    }

    public void onPietyLoss(Player player, int newPiety) {
        EffectsUtil.spawnPietyLossParticles(player);
        EffectsUtil.playSoundSin(player);

        if (newPiety < THRESHOLD_DIRE) {
            long now = System.currentTimeMillis();
            UUID uuid = player.getUniqueId();
            if (now - lastEventTime.getOrDefault(uuid, 0L) > 20_000) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        fireWhisper(player, true);
                        whisperCooldowns.put(uuid, now);
                        lastEventTime.put(uuid, now);
                    }
                }, 20L);
            }
        }
    }

    public void onPietyGain(Player player, int newPiety) {
        EffectsUtil.spawnPietyGainParticles(player);
        EffectsUtil.playSoundPietyGain(player);
    }

    public void clearCooldowns(UUID uuid) {
        lastEventTime.remove(uuid);
        thunderCooldowns.remove(uuid);
        whisperCooldowns.remove(uuid);
        godMessageCooldowns.remove(uuid);
    }
}
