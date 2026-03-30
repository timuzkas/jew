package com.jedaiwm.listeners;

import com.jedaiwm.JedaiWM;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.EquipmentSlot;

public class SynagogueListener implements Listener {

    private final JedaiWM plugin;
    private final JewManager jewManager;

    public SynagogueListener(JedaiWM plugin) {
        this.plugin = plugin;
        this.jewManager = plugin.getJewManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (!plugin.getConfig().getBoolean("synagogue-block.enabled", true)) {
            return;
        }

        String blockMaterial = plugin.getConfig().getString("synagogue-block.material", "GOLD_BLOCK");
        
        if (block.getType().toString().equals(blockMaterial)) {
            if (!jewManager.isJew(event.getPlayer())) {
                return;
            }

            event.setCancelled(true);
            TextUtil.sendSynagogueMenu(event.getPlayer());
        }
    }
}
