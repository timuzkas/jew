package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarQueue;
import com.jedaiwm.utils.EffectsUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class FoodListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public FoodListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!(event.getPlayer() instanceof org.bukkit.entity.Player player)) return;
        if (!jewManager.isJew(player)) return;

        JewPlayer jew = jewManager.getJew(player);
        ItemStack item = event.getItem();
        Material type = item.getType();

        List<String> forbiddenFoods = plugin.getConfig().getStringList("restrictions.forbidden-foods");

        if (forbiddenFoods.contains(type.toString())) {
            event.setCancelled(true);
            int pietyLoss = plugin.getConfig().getInt("piety.treif-loss", 10);
            jew.removePiety(pietyLoss);
            jew.updateLevel();
            jew.save(new File(plugin.getDataFolder(), "jews"));

            EffectsUtil.playSoundSin(player);
            EffectsUtil.spawnPietyLossParticles(player);

            ActionBarQueue.typewriter(player,
                "\u26A0 Treif. -" + pietyLoss + " Piety.",
                ActionBarQueue.PRIORITY_SIN, 2, 50);

            if (jew.getPiety() < 8) {
                plugin.getLowPietyManager().onPietyLoss(player, jew.getPiety());
            }
            return;
        }

        if (plugin.getConfig().getBoolean("restrictions.milk-meat-check", true)) {
            boolean violation = (isDairy(type) && hasMeatInInventory(player))
                             || (isMeat(type) && hasDairyInInventory(player));
            if (violation) {
                event.setCancelled(true);
                int pietyLoss = plugin.getConfig().getInt("piety.treif-loss", 10);
                jew.removePiety(pietyLoss);
                jew.updateLevel();
                jew.save(new File(plugin.getDataFolder(), "jews"));

                EffectsUtil.playSoundSin(player);
                EffectsUtil.spawnPietyLossParticles(player);
                ActionBarQueue.typewriter(player,
                    "\u26A0 Meat and dairy. -" + pietyLoss + " Piety.",
                    ActionBarQueue.PRIORITY_SIN, 2, 50);

                if (jew.getPiety() < 8) {
                    plugin.getLowPietyManager().onPietyLoss(player, jew.getPiety());
                }
            }
        }
    }

    private boolean isDairy(Material type) {
        String n = type.toString();
        return n.contains("MILK") || n.contains("CHEESE") || type == Material.CAKE || type == Material.BREAD;
    }

    private boolean isMeat(Material type) {
        String n = type.toString();
        return n.contains("PORK") || n.contains("BEEF") || n.contains("CHICKEN")
            || n.contains("MUTTON") || n.contains("RABBIT") || n.contains("STEW");
    }

    private boolean hasMeatInInventory(org.bukkit.entity.Player player) {
        for (ItemStack i : player.getInventory().getContents()) { if (i != null && isMeat(i.getType())) return true; }
        return false;
    }

    private boolean hasDairyInInventory(org.bukkit.entity.Player player) {
        for (ItemStack i : player.getInventory().getContents()) { if (i != null && isDairy(i.getType())) return true; }
        return false;
    }
}
