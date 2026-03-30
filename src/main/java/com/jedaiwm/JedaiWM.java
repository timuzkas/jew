package com.jedaiwm;

import com.jedaiwm.commands.JewCommand;
import com.jedaiwm.commands.PrayCommand;
import com.jedaiwm.commands.TorahCommand;
import com.jedaiwm.commands.TeshuvahCommand;
import com.jedaiwm.commands.GefilteFishCommand;
import com.jedaiwm.listeners.FoodListener;
import com.jedaiwm.listeners.HaggleListener;
import com.jedaiwm.listeners.JewFeaturesListener;
import com.jedaiwm.listeners.ShabbatListener;
import com.jedaiwm.listeners.SynagogueListener;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.managers.ShabbatManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JedaiWM extends JavaPlugin {

    private static JedaiWM instance;
    private JewManager jewManager;
    private ShabbatManager shabbatManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        
        jewManager = new JewManager(this);
        shabbatManager = new ShabbatManager(this);

        getCommand("jew").setExecutor(new JewCommand(this));
        getCommand("pray").setExecutor(new PrayCommand(this));
        getCommand("torah").setExecutor(new TorahCommand(this));
        getCommand("teshuvah").setExecutor(new TeshuvahCommand(this));
        getCommand("gefiltefish").setExecutor(new GefilteFishCommand(this));

        getServer().getPluginManager().registerEvents(new FoodListener(this), this);
        getServer().getPluginManager().registerEvents(new ShabbatListener(this), this);
        getServer().getPluginManager().registerEvents(new SynagogueListener(this), this);
        getServer().getPluginManager().registerEvents(new JewFeaturesListener(this), this);
        getServer().getPluginManager().registerEvents(new HaggleListener(this), this);

        getLogger().info("JedaiWM enabled!");
    }

    @Override
    public void onDisable() {
        if (jewManager != null) {
            jewManager.saveData();
        }
        getLogger().info("JedaiWM disabled!");
    }

    public static JedaiWM getInstance() {
        return instance;
    }

    public JewManager getJewManager() {
        return jewManager;
    }

    public ShabbatManager getShabbatManager() {
        return shabbatManager;
    }
}
