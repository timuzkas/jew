package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            player.sendMessage(TextUtil.errorMessage("You are not a Jew."));
            return true;
        }

        JewPlayer jew = jewManager.getJew(player);
        long now = System.currentTimeMillis();
        
        long cooldown = TORAH_COOLDOWN;
        if (jew.isNearSynagogue()) {
            cooldown = cooldown / 2;
        }
        
        if (now - jew.getLastTorahTime() < cooldown) {
            long remaining = (cooldown - (now - jew.getLastTorahTime())) / 1000;
            player.sendMessage(TextUtil.errorMessage("You must wait " + remaining + " seconds before studying again."));
            return true;
        }

        File torahFile = new File(plugin.getDataFolder(), "torah.yml");
        YamlConfiguration torahConfig = YamlConfiguration.loadConfiguration(torahFile);
        List<String> quotes = torahConfig.getStringList("quotes");
        
        if (quotes.isEmpty()) {
            player.sendMessage(TextUtil.errorMessage("No Torah quotes found."));
            return true;
        }

        String randomQuote = quotes.get(random.nextInt(quotes.size()));

        player.sendMessage(TextUtil.infoMessage("You begin to study the Torah..."));
        ActionBarUtil.sendCountdown(player, "Reading... ", 3);

        jew.setLastTorahTime(now);
        
        int pietyGain = plugin.getConfig().getInt("piety.torah-gain", 3);
        jew.addPiety(pietyGain);
        jew.updateLevel();
        jew.save(new File(plugin.getDataFolder(), "jews"));

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(Component.text("=== Torah Study ===", NamedTextColor.GOLD));
                player.sendMessage(Component.text("\u201C" + randomQuote + "\u201D", NamedTextColor.AQUA));
                
                String shabbatTime = plugin.getShabbatManager().getShabbatTimeRemaining();
                String actionBar = ActionBarUtil.formatShabbatStatus(jew.getPiety(), jew.getLevelName(), shabbatTime);
                ActionBarUtil.sendActionBar(player, actionBar);
            }
        }, 60L);

        return true;
    }
}
