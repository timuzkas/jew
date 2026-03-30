package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DebateCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;
    private final Random random = new Random();

    private static final Map<UUID, DebateSession> activeDebates = new HashMap<>();

    private static class DebateSession {
        Player p1, p2;
        String question, correct;
    }

    public DebateCommand(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!plugin.getConfig().getBoolean("debate.enabled", true)) {
            ActionBarUtil.sendActionBar(player, "\u26A0 Debates disabled.");
            return true;
        }

        if (!jewManager.isJew(player)) {
            ActionBarUtil.sendActionBar(player, "\u26A0 Not a Jew.");
            return true;
        }

        if (args.length == 0) {
            ActionBarUtil.sendActionBar(player, "\u26A0 /debate <player>");
            return true;
        }

        if (args[0].equalsIgnoreCase("agree")) {
            return handleVote(player, "agree");
        } else if (args[0].equalsIgnoreCase("disagree")) {
            return handleVote(player, "disagree");
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            ActionBarUtil.sendActionBar(player, "\u26A0 Player not found.");
            return true;
        }

        if (target.equals(player) || !jewManager.isJew(target)) {
            ActionBarUtil.sendActionBar(player, "\u26A0 Can't debate.");
            return true;
        }

        if (activeDebates.containsKey(player.getUniqueId()) || activeDebates.containsKey(target.getUniqueId())) {
            ActionBarUtil.sendActionBar(player, "\u26A0 Already debating.");
            return true;
        }

        startDebate(player, target);
        return true;
    }

    private void startDebate(Player p1, Player p2) {
        File f = new File(plugin.getDataFolder(), "debates.yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        List<String> list = cfg.getStringList("debates");

        if (list.isEmpty()) {
            ActionBarUtil.sendActionBar(p1, "\u26A0 No debates found.");
            return;
        }

        String q = list.get(random.nextInt(list.size()));
        String correct = q.contains("not") || q.contains("no") || q.contains("shouldn't") ? "disagree" : "agree";

        DebateSession s = new DebateSession();
        s.p1 = p1; s.p2 = p2; s.question = q; s.correct = correct;
        activeDebates.put(p1.getUniqueId(), s);
        activeDebates.put(p2.getUniqueId(), s);

        sendMenu(p1, p2.getName(), q);
        sendMenu(p2, p1.getName(), q);

        ActionBarUtil.sendActionBar(p1, "\uD83D\uDCDE Debate with " + p2.getName() + "!");
        ActionBarUtil.sendActionBar(p2, "\uD83D\uDCDE Debate with " + p1.getName() + "!");

        new BukkitRunnable() {
            public void run() {
                if (activeDebates.containsKey(p1.getUniqueId())) {
                    ActionBarUtil.sendActionBar(p1, "\u23F0 Timeout.");
                    ActionBarUtil.sendActionBar(p2, "\u23F0 Timeout.");
                    activeDebates.remove(p1.getUniqueId());
                    activeDebates.remove(p2.getUniqueId());
                }
            }
        }.runTaskLater(plugin, 400L);
    }

    private void sendMenu(Player p, String opponent, String q) {
        p.sendMessage(Component.text("=== Debate with " + opponent + " ===", NamedTextColor.GOLD));
        p.sendMessage(Component.text(q, NamedTextColor.AQUA));

        Component agree = Component.text("[Agree]").color(NamedTextColor.GREEN).decorate(TextDecoration.UNDERLINED)
            .hoverEvent(HoverEvent.showText(Component.text("Agree"))).clickEvent(ClickEvent.runCommand("/debate agree"));
        Component disagree = Component.text("[Disagree]").color(NamedTextColor.RED).decorate(TextDecoration.UNDERLINED)
            .hoverEvent(HoverEvent.showText(Component.text("Disagree"))).clickEvent(ClickEvent.runCommand("/debate disagree"));

        p.sendMessage(agree.append(Component.text(" | ")).append(disagree));
    }

    private boolean handleVote(Player p, String vote) {
        DebateSession s = activeDebates.get(p.getUniqueId());
        if (s == null) {
            ActionBarUtil.sendActionBar(p, "\u26A0 No active debate.");
            return true;
        }

        Player other = s.p1.equals(p) ? s.p2 : s.p1;
        if (!activeDebates.containsKey(other.getUniqueId())) {
            activeDebates.remove(p.getUniqueId());
            ActionBarUtil.sendActionBar(p, "\u26A0 Other player left.");
            return true;
        }

        activeDebates.remove(p.getUniqueId());
        activeDebates.remove(other.getUniqueId());

        boolean pCorrect = vote.equals(s.correct);
        boolean oCorrect = random.nextBoolean();
        if (pCorrect == oCorrect) oCorrect = !oCorrect;

        int reward = plugin.getConfig().getInt("debate.piety-reward", 5);

        if (pCorrect && !oCorrect) {
            JewPlayer jew = jewManager.getJew(p);
            jew.addPiety(reward);
            jew.updateLevel();
            jew.save(new File(plugin.getDataFolder(), "jews"));
            ActionBarUtil.sendActionBar(p, "\uD83C\uDF89 Won! +" + reward + " Piety");
            ActionBarUtil.sendActionBar(other, "\u26A0 You'll understand when you're older.");
        } else if (!pCorrect && oCorrect) {
            JewPlayer jew = jewManager.getJew(other);
            jew.addPiety(reward);
            jew.updateLevel();
            jew.save(new File(plugin.getDataFolder(), "jews"));
            ActionBarUtil.sendActionBar(other, "\uD83C\uDF89 Won! +" + reward + " Piety");
            ActionBarUtil.sendActionBar(p, "\u26A0 You'll understand when you're older.");
        } else {
            ActionBarUtil.sendActionBar(p, "\uD83C\uDFAF Tie!");
            ActionBarUtil.sendActionBar(other, "\uD83C\uDFAF Tie!");
        }

        return true;
    }
}
