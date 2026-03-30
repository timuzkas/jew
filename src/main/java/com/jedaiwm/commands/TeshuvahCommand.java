package com.jedaiwm.commands;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.models.JewPlayer;
import com.jedaiwm.utils.ActionBarUtil;
import com.jedaiwm.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeshuvahCommand implements CommandExecutor {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public TeshuvahCommand(JedaiWM plugin) {
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
        
        if (jew.getPiety() >= 20) {
            player.sendMessage(TextUtil.errorMessage("You do not need to do Teshuvah. Your piety is above 20."));
            return true;
        }

        String blockMaterial = plugin.getConfig().getString("synagogue-block.material", "GOLD_BLOCK");
        boolean foundSynagogue = false;
        
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = player.getLocation().add(x, y, z).getBlock();
                    if (block.getType().toString().equals(blockMaterial)) {
                        foundSynagogue = true;
                        break;
                    }
                }
                if (foundSynagogue) break;
            }
            if (foundSynagogue) break;
        }

        if (!foundSynagogue) {
            player.sendMessage(TextUtil.errorMessage("You must be near a Synagogue block (Gold Block) to do Teshuvah."));
            return true;
        }

        player.sendMessage(TextUtil.infoMessage("You begin the Teshuvah (repentance) ritual..."));
        ActionBarUtil.sendCountdown(player, "Repenting... ", 30);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                int restoreAmount = plugin.getConfig().getInt("piety.teshuvah-restore", 25);
                jew.setPiety(restoreAmount);
                jew.updateLevel();
                jew.save(new java.io.File(plugin.getDataFolder(), "jews"));
                
                player.sendTitle("\u05EA\u05E9\u05D5\u05D1\u05D4", "Your sins are forgiven.", 10, 70, 20);
                
                String shabbatTime = plugin.getShabbatManager().getShabbatTimeRemaining();
                String actionBar = ActionBarUtil.formatShabbatStatus(jew.getPiety(), jew.getLevelName(), shabbatTime);
                ActionBarUtil.sendActionBar(player, actionBar);
            }
        }, 600L);

        return true;
    }
}
