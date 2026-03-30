package com.jedaiwm.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EffectsUtil {

    public static void playSoundPrayerComplete(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 0.9f);
    }

    public static void playSoundPietyGain(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.4f);
    }

    public static void playSoundSin(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.4f, 1.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.6f, 0.6f);
    }

    public static void playSoundDeath(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.3f, 1.5f);
    }

    public static void playSoundConversion(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 0.5f, 1.2f);
    }

    public static void playSoundBarMitzvah(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.9f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.1f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.6f, 1.3f);
    }

    public static void playSoundSynagogueAmbient(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 0.2f, 1.5f);
    }

    public static void playSoundWhisper(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VEX_AMBIENT, 0.15f, 0.5f);
    }

    public static void playSoundThunder(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9f, 0.8f);
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.7f, 1.0f);
    }

    public static void playSoundTeshuvah(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.8f);
    }

    public static void spawnPietyGainParticles(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(
            Particle.DUST,
            loc,
            20,
            0.4, 0.5, 0.4,
            new Particle.DustOptions(Color.fromRGB(255, 215, 0), 1.2f)
        );
    }

    public static void spawnPietyLossParticles(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(
            Particle.DUST,
            loc,
            25,
            0.5, 0.6, 0.5,
            new Particle.DustOptions(Color.fromRGB(180, 0, 0), 1.4f)
        );
        player.getWorld().spawnParticle(
            Particle.LARGE_SMOKE,
            loc,
            6,
            0.3, 0.3, 0.3,
            0.02
        );
    }

    public static void spawnGoldMagnetTrail(Player player, Location itemLoc) {
        Location mid = itemLoc.clone().add(
            (player.getLocation().getX() - itemLoc.getX()) * 0.3,
            (player.getLocation().getY() - itemLoc.getY()) * 0.3 + 0.5,
            (player.getLocation().getZ() - itemLoc.getZ()) * 0.3
        );
        player.getWorld().spawnParticle(
            Particle.DUST,
            mid,
            5,
            0.1, 0.1, 0.1,
            new Particle.DustOptions(Color.fromRGB(255, 200, 0), 0.8f)
        );
        player.getWorld().spawnParticle(
            Particle.CRIT,
            mid,
            3,
            0.1, 0.1, 0.1,
            0.05
        );
    }

    public static void spawnSynagogueAmbientParticles(Player player) {
        Location base = player.getLocation();
        double radius = 1.5;
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI / 8) * i;
            double dx = radius * Math.cos(angle);
            double dz = radius * Math.sin(angle);
            Location point = base.clone().add(dx, 0.1, dz);
            player.getWorld().spawnParticle(
                Particle.DUST,
                point,
                1,
                0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(255, 230, 100), 0.7f)
            );
        }
    }

    public static void spawnHallucinationParticles(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(
            Particle.DUST,
            loc,
            30,
            0.6, 0.8, 0.6,
            new Particle.DustOptions(Color.fromRGB(40, 0, 60), 1.5f)
        );
        player.getWorld().spawnParticle(
            Particle.LARGE_SMOKE,
            loc,
            10,
            0.4, 0.5, 0.4,
            0.01
        );
    }

    public static void spawnThunderStrike(Player player) {
        double offsetX = (Math.random() * 6) - 3;
        double offsetZ = (Math.random() * 6) - 3;
        Location strikeLoc = player.getLocation().add(offsetX, 0, offsetZ);
        player.getWorld().strikeLightningEffect(strikeLoc);
    }
}
