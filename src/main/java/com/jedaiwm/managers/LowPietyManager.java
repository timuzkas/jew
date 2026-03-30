package com.jedaiwm.managers;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
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

    private final Map<UUID, Long> thunderCooldowns = new HashMap<>();
    private final Map<UUID, Long> whisperCooldowns = new HashMap<>();
    private final Map<UUID, Long> godMessageCooldowns = new HashMap<>();

    private static final long THUNDER_COOLDOWN_MS = 45_000;
    private static final long WHISPER_COOLDOWN_MS = 30_000;
    private static final long GOD_MESSAGE_COOLDOWN_MS = 120_000;

    private static final int THRESHOLD_BAD = 20;
    private static final int THRESHOLD_DIRE = 8;

    private static final List<String> WHISPERS_BAD = List.of(
        "...you have forgotten the way...",
        "...your ancestors weep...",
        "...the covenant slips from your hands...",
        "...is this who you are?...",
        "...straying from the path brings only darkness...",
        "...even the Rebbe would turn away...",
        "...remember what you are...",
        "...the tradition dies with the faithless..."
    );

    private static final List<String> WHISPERS_DIRE = List.of(
        "...you are no longer His...",
        "...the gates close for the wicked...",
        "...your name is being forgotten...",
        "...darkness follows the faithless...",
        "...there is no covenant for sinners...",
        "...turn back before it is too late...",
        "...can you hear them? your ancestors cry...",
        "...you are a disgrace to the tribe..."
    );

    private static final List<String> GOD_MESSAGES_BAD = List.of(
        "My child, where have you gone?",
        "I have not forgotten you — but have you forgotten Me?",
        "Return to the path. It is not too late.",
        "Your piety wanes. I am watching.",
        "The covenant demands more of you.",
        "Do you think I cannot see?"
    );

    private static final List<String> GOD_MESSAGES_DIRE = List.of(
        "You test my patience, child.",
        "The wicked shall not stand in the congregation.",
        "I created you. I can uncreate you.",
        "Repent. Now.",
        "Your name grows faint in the Book of Life.",
        "Even Jonah found his way back. Will you?"
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

                    int piety = jew.getPiety();

                    if (piety < THRESHOLD_BAD) {
                        handleLowPiety(player, piety);
                    }
                }
            }
        }.runTaskTimer(plugin, 60L, 60L);
    }

    private void handleLowPiety(Player player, int piety) {
        long now = System.currentTimeMillis();
        boolean isDire = piety < THRESHOLD_DIRE;

        long thunderCooldown = isDire ? THUNDER_COOLDOWN_MS / 2 : THUNDER_COOLDOWN_MS;
        if (now - thunderCooldowns.getOrDefault(player.getUniqueId(), 0L) > thunderCooldown) {
            if (random.nextDouble() < (isDire ? 0.4 : 0.2)) {
                triggerThunder(player);
                thunderCooldowns.put(player.getUniqueId(), now);
            }
        }

        long whisperCooldown = isDire ? WHISPER_COOLDOWN_MS / 2 : WHISPER_COOLDOWN_MS;
        if (now - whisperCooldowns.getOrDefault(player.getUniqueId(), 0L) > whisperCooldown) {
            if (random.nextDouble() < (isDire ? 0.5 : 0.25)) {
                triggerWhisper(player, isDire);
                whisperCooldowns.put(player.getUniqueId(), now);
            }
        }

        if (now - godMessageCooldowns.getOrDefault(player.getUniqueId(), 0L) > GOD_MESSAGE_COOLDOWN_MS) {
            if (random.nextDouble() < (isDire ? 0.35 : 0.15)) {
                triggerGodMessage(player, isDire);
                godMessageCooldowns.put(player.getUniqueId(), now);
            }
        }
    }

    private void triggerThunder(Player player) {
        EffectsUtil.spawnThunderStrike(player);
        EffectsUtil.playSoundThunder(player);
        EffectsUtil.spawnHallucinationParticles(player);

        ActionBarUtil.sendFlashingActionBar(
            player,
            "\u26A0 The heavens are angry with you.",
            "\u26A0 Repent before it is too late.",
            4, 80
        );
    }

    private void triggerWhisper(Player player, boolean dire) {
        List<String> pool = dire ? WHISPERS_DIRE : WHISPERS_BAD;
        String whisper = pool.get(random.nextInt(pool.size()));

        Component msg = Component.text(whisper,
            Style.style(NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC));

        EffectsUtil.playSoundWhisper(player);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(msg);
                EffectsUtil.spawnHallucinationParticles(player);
            }
        }, 10L);
    }

    private void triggerGodMessage(Player player, boolean dire) {
        List<String> pool = dire ? GOD_MESSAGES_DIRE : GOD_MESSAGES_BAD;
        String message = pool.get(random.nextInt(pool.size()));

        Component prefix = Component.text("[God] ", NamedTextColor.GOLD,
            TextDecoration.BOLD);
        Component body = Component.text(message, NamedTextColor.YELLOW);
        Component full = prefix.append(body);

        EffectsUtil.playSoundThunder(player);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(Component.text(" ", NamedTextColor.GRAY));
                player.sendMessage(full);
                player.sendMessage(Component.text(" ", NamedTextColor.GRAY));
                ActionBarUtil.sendTypewriterActionBar(player, "\u2721 " + message, 2);
            }
        }, 15L);
    }

    public void onPietyLoss(Player player, int newPiety) {
        EffectsUtil.spawnPietyLossParticles(player);
        EffectsUtil.playSoundSin(player);

        if (newPiety < THRESHOLD_DIRE) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    triggerWhisper(player, true);
                }
            }, 30L);
        }
    }

    public void onPietyGain(Player player, int newPiety) {
        EffectsUtil.spawnPietyGainParticles(player);
        EffectsUtil.playSoundPietyGain(player);
    }

    public void clearCooldowns(UUID uuid) {
        thunderCooldowns.remove(uuid);
        whisperCooldowns.remove(uuid);
        godMessageCooldowns.remove(uuid);
    }
}
