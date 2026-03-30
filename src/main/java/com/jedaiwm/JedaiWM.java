package com.jedaiwm;

import com.jedaiwm.commands.JewCommand;
import com.jedaiwm.commands.JewCommandCompleter;
import com.jedaiwm.commands.PrayCommand;
import com.jedaiwm.commands.TabCompleters;
import com.jedaiwm.commands.TorahCommand;
import com.jedaiwm.commands.TeshuvahCommand;
import com.jedaiwm.commands.GefilteFishCommand;
import com.jedaiwm.commands.DebateCommand;
import com.jedaiwm.commands.JewAbilityCommand;
import com.jedaiwm.listeners.FoodListener;
import com.jedaiwm.listeners.GefilteFishListener;
import com.jedaiwm.listeners.HaggleListener;
import com.jedaiwm.listeners.JewFeaturesListener;
import com.jedaiwm.listeners.RecipeListener;
import com.jedaiwm.listeners.ShabbatListener;
import com.jedaiwm.listeners.SynagogueListener;
import com.jedaiwm.managers.JewManager;
import com.jedaiwm.managers.LowPietyManager;
import com.jedaiwm.managers.ShabbatManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JedaiWM extends JavaPlugin {

    private static JedaiWM instance;
    private JewManager jewManager;
    private ShabbatManager shabbatManager;
    private LowPietyManager lowPietyManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        jewManager = new JewManager(this);
        shabbatManager = new ShabbatManager(this);
        lowPietyManager = new LowPietyManager(this);

        getCommand("jew").setExecutor(new JewCommand(this));
        getCommand("jew").setTabCompleter(new JewCommandCompleter(this));
        getCommand("pray").setExecutor(new PrayCommand(this));
        getCommand("pray").setTabCompleter(new TabCompleters.PrayCompleter());
        getCommand("torah").setExecutor(new TorahCommand(this));
        getCommand("torah").setTabCompleter(new TabCompleters.TorahCompleter());
        getCommand("teshuvah").setExecutor(new TeshuvahCommand(this));
        getCommand("teshuvah").setTabCompleter(new TabCompleters.TeshuvahCompleter());
        getCommand("gefiltefish").setExecutor(new GefilteFishCommand(this));
        getCommand("gefiltefish").setTabCompleter(new TabCompleters.GefilteFishCompleter());

        getCommand("debate").setExecutor(new DebateCommand(this));

        JewAbilityCommand abilityCmd = new JewAbilityCommand(this);
        getCommand("jewability").setExecutor(abilityCmd);
        getCommand("jewability").setTabCompleter(abilityCmd);

        getServer().getPluginManager().registerEvents(new FoodListener(this), this);
        getServer().getPluginManager().registerEvents(new ShabbatListener(this), this);
        getServer().getPluginManager().registerEvents(new SynagogueListener(this), this);
        getServer().getPluginManager().registerEvents(new JewFeaturesListener(this), this);
        getServer().getPluginManager().registerEvents(new HaggleListener(this), this);
        getServer().getPluginManager().registerEvents(new GefilteFishListener(this), this);
        getServer().getPluginManager().registerEvents(new RecipeListener(this), this);

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

    public LowPietyManager getLowPietyManager() {
        return lowPietyManager;
    }
}
