package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarQueue;
import com.jedaiwm.utils.EffectsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JewAbilityCommand implements CommandExecutor, TabCompleter {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    private static final long MINUTE = 60 * 1000;

    public JewAbilityCommand(JedaiWM plugin) {
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
            ActionBarQueue.send(player, "\u26A0 You are not a Jew.", ActionBarQueue.PRIORITY_INFO, 30);
            return true;
        }

        if (args.length == 0) {
            sendAbilityList(player);
            return true;
        }

        String ability = args[0].toLowerCase();

        switch (ability) {
            case "cleanse" -> doCleanse(player);
            case "shield" -> doShield(player);
            case "bless" -> {
                if (args.length > 1) {
                    doRabbiBless(player, args[1]);
                } else {
                    doBlessedInventory(player);
                }
            }
            case "wrath" -> doWrath(player);
            case "charisma" -> {
                if (args.length > 1) {
                    doCharisma(player, args[1]);
                } else {
                    ActionBarQueue.send(player, "\u26A0 /jewability charisma <player>", ActionBarQueue.PRIORITY_INFO, 30);
                }
            }
            case "smell" -> {
                if (args.length > 1) {
                    doSmell(player, args[1]);
                } else {
                    ActionBarQueue.send(player, "\u26A0 /jewability smell <player>", ActionBarQueue.PRIORITY_INFO, 30);
                }
            }
            case "lick" -> {
                if (args.length > 1) {
                    doLick(player, args[1]);
                } else {
                    ActionBarQueue.send(player, "\u26A0 /jewability lick <player>", ActionBarQueue.PRIORITY_INFO, 30);
                }
            }
            default -> sendAbilityList(player);
        }

        return true;
    }

    private void sendAbilityList(Player player) {
        JewPlayer jew = jewManager.getJew(player);
        long now = System.currentTimeMillis();

        player.sendMessage(Component.text("=== Abilities ===", NamedTextColor.GOLD));
        
        checkAndShowAbility(player, jew, "cleanse", 2, 15, 15, now);
        checkAndShowAbility(player, jew, "bless (self)", 2, 25, 30, now);
        checkAndShowAbility(player, jew, "charisma <player>", 2, 20, 15, now);
        checkAndShowAbility(player, jew, "smell <player>", 1, 5, 3, now);
        checkAndShowAbility(player, jew, "lick <player>", 2, 8, 5, now);
        checkAndShowAbility(player, jew, "shield", 3, 30, 20, now);
        checkAndShowAbility(player, jew, "bless <player>", 4, 40, 25, now);
        checkAndShowAbility(player, jew, "wrath", 4, 55, 30, now);
    }

    private void checkAndShowAbility(Player player, JewPlayer jew, String name, int minLevel, int cost, int cooldownMin, long now) {
        long cooldown = 0;
        switch (name.split(" ")[0]) {
            case "cleanse" -> cooldown = jew.getCleanseCooldown();
            case "shield" -> cooldown = jew.getShieldCooldown();
            case "bless" -> cooldown = jew.getBlessCooldown();
            case "wrath" -> cooldown = jew.getWrathCooldown();
            case "charisma" -> cooldown = jew.getCharismaCooldown();
            case "smell" -> cooldown = jew.getSmellCooldown();
            case "lick" -> cooldown = jew.getLickCooldown();
        }

        boolean ready = now - cooldown >= cooldownMin * MINUTE && jew.getLevel() >= minLevel && jew.getPiety() >= cost;
        String status = ready ? "\u2705" : "\u274C";
        String cd = now - cooldown < cooldownMin * MINUTE ? 
            ((cooldownMin * MINUTE - (now - cooldown)) / 60000 + 1) + "m" : "ready";

        player.sendMessage(Component.text(status + " " + name + " (" + cost + " piety, " + cd + ") - Lv." + minLevel,
            ready ? NamedTextColor.GREEN : NamedTextColor.GRAY));
    }

    private void doCleanse(Player player) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 2, 15, jew.getCleanseCooldown(), 15)) return;

        jew.removePiety(15);
        jew.setCleanseCooldown(System.currentTimeMillis());
        jew.save(new File(plugin.getDataFolder(), "jews"));

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        EffectsUtil.playSoundPietyGain(player);
        ActionBarQueue.send(player, "\u2721 Cleanse complete.", ActionBarQueue.PRIORITY_INFO, 40);

        broadcastToJews("* The body is a temple. *");
    }

    private void doShield(Player player) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 3, 30, jew.getShieldCooldown(), 20)) return;

        jew.removePiety(30);
        jew.setShieldCooldown(System.currentTimeMillis());
        jew.setShieldActive(true);
        jew.save(new File(plugin.getDataFolder(), "jews"));

        ActionBarQueue.countdown(player, "Reciting Shema... ", ActionBarQueue.PRIORITY_RITUAL, 5);
        player.sendTitle("\u05E9\u05DE\u05E2", "You are protected.", 10, 40, 20);

        // Visual effect while shield active
        startShieldEffect(player);

        // Schedule shield deactivation after 2 min
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            JewPlayer j = jewManager.getJew(player);
            if (j != null && j.isShieldActive()) {
                j.setShieldActive(false);
                j.save(new File(plugin.getDataFolder(), "jews"));
                ActionBarQueue.send(player, "\u2721 The Shema shield has faded.", ActionBarQueue.PRIORITY_INFO, 40);
            }
        }, 2400L);
    }

    private void startShieldEffect(Player player) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            JewPlayer jew = jewManager.getJew(player);
            if (jew != null && jew.isShieldActive() && player.isOnline()) {
                player.getWorld().spawnParticle(org.bukkit.Particle.GLOW, player.getLocation().add(0, 1, 0), 3, 0.5, 0.5, 0.5, 0);
                startShieldEffect(player);
            }
        }, 20L);
    }

    private void doBlessedInventory(Player player) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 2, 25, jew.getBlessCooldown(), 30)) return;

        jew.removePiety(25);
        jew.setBlessCooldown(System.currentTimeMillis());
        jew.setBlessedInventory(true, System.currentTimeMillis() + 15 * 60 * 1000);
        jew.save(new File(plugin.getDataFolder(), "jews"));

        ActionBarQueue.send(player, "\u2721 Your items are protected by the covenant.", ActionBarQueue.PRIORITY_INFO, 50);
    }

    private void doRabbiBless(Player player, String targetName) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 4, 40, jew.getBlessCooldown(), 25)) return;

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            ActionBarQueue.send(player, "\u26A0 Player not found.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        if (!jewManager.isJew(target)) {
            ActionBarQueue.send(player, "\u26A0 Target must be a Jew.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        JewPlayer targetJew = jewManager.getJew(target);
        targetJew.addPiety(10);
        targetJew.updateLevel();
        targetJew.save(new File(plugin.getDataFolder(), "jews"));

        target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));

        jew.removePiety(40);
        jew.setBlessCooldown(System.currentTimeMillis());
        jew.save(new File(plugin.getDataFolder(), "jews"));

        ActionBarQueue.send(target, "\u2721 You have been blessed by " + player.getName() + ".", ActionBarQueue.PRIORITY_INFO, 50);
        broadcastToJews("* Go with God. *");
    }

    private void doWrath(Player player) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 4, 55, jew.getWrathCooldown(), 30)) return;

        Player target = getTargetPlayer(player);
        if (target == null) {
            ActionBarQueue.send(player, "\u26A0 No target in sight.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        jew.removePiety(55);
        jew.setWrathCooldown(System.currentTimeMillis());
        jew.save(new File(plugin.getDataFolder(), "jews"));

        EffectsUtil.spawnThunderStrike(target);
        EffectsUtil.playSoundThunder(target);
        target.damage(4, player);
        target.sendMessage(ChatColor.RED + "You have angered the chosen people.");
    }

    private void doCharisma(Player player, String targetName) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 2, 20, jew.getCharismaCooldown(), 15)) return;

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            ActionBarQueue.send(player, "\u26A0 Player not found.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        if (jewManager.isJew(target)) {
            ActionBarQueue.send(player, "\u26A0 Target must not be a Jew.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        jew.removePiety(20);
        jew.setCharismaCooldown(System.currentTimeMillis());
        jew.save(new File(plugin.getDataFolder(), "jews"));

        target.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));

        Component msg = Component.text(player.getName() + " smiles at you warmly.")
            .hoverEvent(HoverEvent.showText(Component.text("\u2721 A gift from the chosen.")));
        target.sendMessage(msg);

        ActionBarQueue.send(player, "\uD83D\uDCAC Charm applied.", ActionBarQueue.PRIORITY_INFO, 30);
        broadcastToJews("* You're practically mishpacha. *");
    }

    private void doSmell(Player player, String targetName) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 1, 5, jew.getSmellCooldown(), 3)) return;

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            ActionBarQueue.send(player, "\u26A0 Player not found.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        jew.removePiety(5);
        jew.setSmellCooldown(System.currentTimeMillis());
        jew.save(new File(plugin.getDataFolder(), "jews"));

        ActionBarQueue.send(player, "\uD83D\uDC41 Sniffing...", ActionBarQueue.PRIORITY_INFO, 20);

        int score = calculateWealthScore(target);
        String message = getSmellMessage(score, target.getName());

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GOLD + message);
            }
        }, 20L);
    }

    private int calculateWealthScore(Player target) {
        int score = 0;

        for (org.bukkit.inventory.ItemStack item : target.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            int amount = item.getAmount();
            String type = item.getType().toString();
            if (type.contains("GOLD")) score += amount * 3;
            else if (type.contains("DIAMOND")) score += amount * 5;
            else if (type.contains("EMERALD")) score += amount * 2;
        }

        org.bukkit.inventory.ItemStack armor = target.getInventory().getChestplate();
        if (armor != null && armor.getType() != Material.AIR) {
            score += getArmorTier(armor.getType());
        }

        org.bukkit.inventory.ItemStack hand = target.getInventory().getItemInMainHand();
        if (hand != null && hand.getType() != Material.AIR) {
            score += getArmorTier(hand.getType());
        }

        return score;
    }

    private int getArmorTier(Material mat) {
        String t = mat.toString();
        if (t.contains("LEATHER")) return 1;
        if (t.contains("IRON")) return 3;
        if (t.contains("GOLD")) return 4;
        if (t.contains("DIAMOND")) return 8;
        if (t.contains("NETHERITE")) return 12;
        return 0;
    }

    private String getSmellMessage(int score, String name) {
        if (score > 100) return "\uD83D\uDC41 You tremble slightly. " + name + " smells like generational wealth.";
        if (score > 75) return "\uD83D\uDC41 " + name + "... *inhales deeply* ...magnificent. Truly magnificent.";
        if (score > 50) return "\uD83D\uDC41 " + name + " radiates the warmth of gold. Very interesting.";
        if (score > 30) return "\uD83D\uDC41 Hmm... " + name + " smells of something promising.";
        if (score > 15) return "\uD83D\uDC41 " + name + " carries a faint scent of modest means.";
        return "\uD83D\uDC41 " + name + " smells of dirt and disappointment.";
    }

    private void doLick(Player player, String targetName) {
        JewPlayer jew = jewManager.getJew(player);
        if (!checkRequirements(player, jew, 2, 8, jew.getLickCooldown(), 5)) return;

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            ActionBarQueue.send(player, "\uD83D\uDC41 Player not found.", ActionBarQueue.PRIORITY_INFO, 30);
            return;
        }

        jew.removePiety(8);
        jew.setLickCooldown(System.currentTimeMillis());
        jew.save(new File(plugin.getDataFolder(), "jews"));

        org.bukkit.inventory.ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            ActionBarQueue.send(player, "\uD83D\uDC44 Nothing in hand. Tasteless.", ActionBarQueue.PRIORITY_INFO, 30);
            target.sendMessage(ChatColor.GOLD + player.getName() + " eyes your item with great interest.");
            return;
        }

        int enchantCount = 0;
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            enchantCount = item.getItemMeta().getEnchants().size();
        }

        String phrase = getLickPhrase(enchantCount);
        String itemName = item.getType().toString().replace("_", " ");
        int durability = item.getDurability();
        int maxDurability = item.getType().getMaxDurability();
        int remaining = maxDurability - durability;

        StringBuilder enchants = new StringBuilder();
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            for (java.util.Map.Entry<org.bukkit.enchantments.Enchantment, Integer> e : item.getItemMeta().getEnchants().entrySet()) {
                enchants.append("\n  \u2022 ").append(e.getKey().getKey().toString().replace("_", " ")).append(" ").append(toRoman(e.getValue()));
            }
        }

        player.sendMessage(ChatColor.GOLD + phrase);
        player.sendMessage(ChatColor.GOLD + "\uD83D\uDC44 You lick your lips examining " + target.getName() + "'s " + itemName + ":");
        player.sendMessage(ChatColor.GRAY + "  Durability: " + remaining + " / " + maxDurability + enchants);

        target.sendMessage(ChatColor.GOLD + player.getName() + " eyes your item with great interest.");
    }

    private String toRoman(int num) {
        String[] roman = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return num < roman.length ? roman[num] : String.valueOf(num);
    }

    private String getLickPhrase(int enchantCount) {
        if (enchantCount == 0) return "\uD83D\uDC44 Plain. Unenchanted. Sad.";
        if (enchantCount <= 2) return "\uD83D\uDC44 Modest. But something is there.";
        if (enchantCount <= 4) return "\uD83D\uDC44 Now we're talking...";
        return "\uD83D\uDC44 *licks lips* ...this is a very good item.";
    }

    private boolean checkRequirements(Player player, JewPlayer jew, int minLevel, int cost, long cooldown, int cooldownMin) {
        long now = System.currentTimeMillis();
        if (jew.getLevel() < minLevel) {
            ActionBarQueue.send(player, "\u26A0 Requires level " + minLevel, ActionBarQueue.PRIORITY_INFO, 30);
            return false;
        }
        if (jew.getPiety() < cost) {
            ActionBarQueue.send(player, "\u26A0 Not enough piety (" + cost + " required).", ActionBarQueue.PRIORITY_INFO, 30);
            return false;
        }
        if (now - cooldown < cooldownMin * MINUTE) {
            long remaining = (cooldownMin * MINUTE - (now - cooldown)) / 60000 + 1;
            ActionBarQueue.send(player, "\u26A0 Cooldown: " + remaining + " min", ActionBarQueue.PRIORITY_INFO, 30);
            return false;
        }
        return true;
    }

    private Player getTargetPlayer(Player player) {
        double range = 20;
        Player target = null;
        double closest = range;

        for (Player p : player.getWorld().getPlayers()) {
            double dist = player.getLocation().distance(p.getLocation());
            if (dist < closest && !p.equals(player)) {
                closest = dist;
                target = p;
            }
        }
        return target;
    }

    private void broadcastToJews(String phrase) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (jewManager.isJew(p) || p.hasPermission("jedaiwm.admin")) {
                p.sendMessage(ChatColor.GOLD + phrase);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!(sender instanceof Player player)) return completions;
        if (!jewManager.isJew(player)) return completions;

        JewPlayer jew = jewManager.getJew(player);
        long now = System.currentTimeMillis();

        if (args.length == 1) {
            List<String> abilities = new ArrayList<>();

            if (now - jew.getCleanseCooldown() >= 15 * MINUTE && jew.getLevel() >= 2 && jew.getPiety() >= 15)
                abilities.add("cleanse");
            if (now - jew.getShieldCooldown() >= 20 * MINUTE && jew.getLevel() >= 3 && jew.getPiety() >= 30)
                abilities.add("shield");
            if (now - jew.getBlessCooldown() >= 30 * MINUTE && jew.getLevel() >= 2 && jew.getPiety() >= 25)
                abilities.add("bless");
            if (now - jew.getWrathCooldown() >= 30 * MINUTE && jew.getLevel() >= 4 && jew.getPiety() >= 55)
                abilities.add("wrath");
            if (now - jew.getCharismaCooldown() >= 15 * MINUTE && jew.getLevel() >= 2 && jew.getPiety() >= 20)
                abilities.add("charisma");
            if (now - jew.getSmellCooldown() >= 3 * MINUTE && jew.getLevel() >= 1 && jew.getPiety() >= 5)
                abilities.add("smell");
            if (now - jew.getLickCooldown() >= 5 * MINUTE && jew.getLevel() >= 2 && jew.getPiety() >= 8)
                abilities.add("lick");

            for (String a : abilities) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase())) completions.add(a);
            }
        } else if (args.length == 2) {
            String ability = args[0].toLowerCase();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (ability.equals("charisma") || ability.equals("smell") || ability.equals("lick")) {
                    if (!jewManager.isJew(p) && !p.equals(player) && p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                } else if (ability.equals("bless")) {
                    if (jewManager.isJew(p) && !p.equals(player) && p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
            }
        }

        return completions;
    }
}
