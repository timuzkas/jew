package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.EffectsUtil;
import com.jedaiwm.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.io.File;
import java.util.Random;

public class PrayCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    private static final long PRAYER_COOLDOWN = 10 * 60 * 1000;

    public PrayCommand(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (!jewManager.isJew(player)) {
            player.sendMessage(TextUtil.errorMessage("You are not a Jew."));
            return true;
        }

        JewPlayer jew = jewManager.getJew(player);
        long now = System.currentTimeMillis();

        long cooldown = PRAYER_COOLDOWN;
        if (jew.isNearSynagogue()) cooldown = cooldown / 2;

        if (now - jew.getLastPrayerTime() < cooldown) {
            long remaining = (cooldown - (now - jew.getLastPrayerTime())) / 1000;
            player.sendMessage(TextUtil.errorMessage("You must wait " + remaining + " seconds before praying again."));
            return true;
        }

        player.sendMessage(TextUtil.infoMessage("You begin to pray..."));
        ActionBarUtil.sendCountdown(player, "Praying... ", 5);

        jew.setLastPrayerTime(now);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            int pietyGain = plugin.getConfig().getInt("piety.prayer-gain", 5);
            jew.addPiety(pietyGain);

            EffectsUtil.playSoundPrayerComplete(player);
            EffectsUtil.spawnPietyGainParticles(player);

            ActionBarUtil.sendTypewriterActionBar(player, "\u2721 Your prayer was heard. +" + pietyGain + " Piety", 2);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle("\u05D0\u05DE\u05DF", "Your prayer was heard.", 10, 70, 20);
            }, 20L);

            checkBarMitzvah(player, jew);
            jew.updateLevel();
            jew.save(new File(plugin.getDataFolder(), "jews"));

        }, 100L);

        return true;
    }

    private void checkBarMitzvah(Player player, JewPlayer jew) {
        if (!plugin.getConfig().getBoolean("bar-mitzvah.enabled", true)) return;
        if (jew.hasBarMitzvah() || jew.hasBatMitzvah()) return;

        int threshold = plugin.getConfig().getInt("bar-mitzvah.piety-threshold", 45);
        if (jew.getPiety() < threshold) return;

        jew.setBarMitzvah(true);
        jew.setMaxPietyCap(100 + plugin.getConfig().getInt("bar-mitzvah.max-piety-increase", 10));
        jew.addPiety(plugin.getConfig().getInt("bar-mitzvah.reward-piety", 20));

        EffectsUtil.playSoundBarMitzvah(player);

        ActionBarUtil.sendTypewriterActionBar(player, "\uD83C\uDF89 You are now a son of the commandment.", 2);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendTitle("\uD83C\uDF89 \u05D1\u05E8 \u05DE\u05E6\u05D5\u05D4",
                    "You are now a son of the commandment.", 10, 70, 20);
            }
        }, 30L);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (jewManager.isJew(online) || online.hasPermission("jedaiwm.admin")) {
                online.sendMessage(ChatColor.GOLD + "\u2721 " + player.getName() + " has had their Bar Mitzvah!");
            }
        }

        spawnFireworks(player);
        ActionBarUtil.sendPersistentActionBar(player, "You are now accountable for your mitzvot.", 200);
    }

    private void spawnFireworks(Player player) {
        for (int i = 0; i < 3; i++) {
            final int delay = i * 15;
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;
                Firework fw = player.getWorld().spawn(
                    player.getLocation().add(random.nextDouble() * 4 - 2, 0, random.nextDouble() * 4 - 2),
                    Firework.class
                );
                FireworkMeta meta = fw.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder()
                    .withColor(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
                    .withFade(Color.fromRGB(255, 215, 0))
                    .with(FireworkEffect.Type.STAR)
                    .build());
                meta.setPower(1);
                fw.setFireworkMeta(meta);
            }, delay);
        }
    }
}
