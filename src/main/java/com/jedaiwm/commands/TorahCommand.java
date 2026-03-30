package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.EffectsUtil;
import com.jedaiwm.utils.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Random;

public class TorahCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    private static final long TORAH_COOLDOWN = 5 * 60 * 1000;

    public TorahCommand(JedaiWM plugin) {
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
            ActionBarUtil.sendActionBar(player, "\u26A0 You are not a Jew.");
            return true;
        }

        JewPlayer jew = jewManager.getJew(player);
        long now = System.currentTimeMillis();

        long cooldown = TORAH_COOLDOWN;
        if (jew.isNearSynagogue()) cooldown = cooldown / 2;

        if (now - jew.getLastTorahTime() < cooldown) {
            long remaining = (cooldown - (now - jew.getLastTorahTime())) / 1000;
            ActionBarUtil.sendActionBar(player, "\u26A0 Wait " + remaining + "s");
            return true;
        }

        File torahFile = new File(plugin.getDataFolder(), "torah.yml");
        YamlConfiguration torahConfig = YamlConfiguration.loadConfiguration(torahFile);
        List<String> quotes = torahConfig.getStringList("quotes");

        if (quotes.isEmpty()) {
            ActionBarUtil.sendActionBar(player, "\u26A0 No quotes found.");
            return true;
        }

        String randomQuote = quotes.get(random.nextInt(quotes.size()));

        EffectsUtil.playSoundPrayerComplete(player);
        EffectsUtil.spawnPietyGainParticles(player);

        ActionBarUtil.sendCountdown(player, "Reading... ", 3);

        jew.setLastTorahTime(now);

        int pietyGain = plugin.getConfig().getInt("piety.torah-gain", 3);
        jew.addPiety(pietyGain);
        jew.updateLevel();
        jew.save(new File(plugin.getDataFolder(), "jews"));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            Component quote = Component.text()
                .append(Component.text(" \u250C ", NamedTextColor.GRAY))
                .append(Component.text("\u201C", NamedTextColor.WHITE, TextDecoration.ITALIC))
                .append(Component.text(randomQuote, NamedTextColor.WHITE, TextDecoration.ITALIC))
                .append(Component.text("\u201D", NamedTextColor.WHITE, TextDecoration.ITALIC))
                .build();

            player.sendMessage(Component.empty());
            player.sendMessage(quote);
            player.sendMessage(Component.empty());

            String shabbatTime = plugin.getShabbatManager().getShabbatTimeRemaining();
            ActionBarUtil.sendActionBar(player, ActionBarUtil.formatShabbatStatus(jew.getPiety(), jew.getLevelName(), shabbatTime));
        }, 60L);

        return true;
    }
}
