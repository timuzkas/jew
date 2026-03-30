package com.jedaiwm.managers;

import com.jedaiwm.JedaiWM;
import org.bukkit.configuration.Configuration;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public class ShabbatManager {

    private final JedaiWM plugin;
    private final Configuration config;
    
    private boolean isShabbatActive;

    public ShabbatManager(JedaiWM plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.isShabbatActive = false;
        updateShabbatStatus();
    }

    public void updateShabbatStatus() {
        String mode = config.getString("shabbat.mode", "real-time");
        
        if (mode.equalsIgnoreCase("real-time")) {
            isShabbatActive = checkRealTimeShabbat();
        } else {
            isShabbatActive = checkInGameShabbat();
        }
    }

    private boolean checkRealTimeShabbat() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        
        String startDayStr = config.getString("shabbat.real-day", "FRIDAY");
        int startHour = config.getInt("shabbat.start-hour", 18);
        int endHour = config.getInt("shabbat.end-hour-next-day", 19);
        
        DayOfWeek startDay;
        try {
            startDay = DayOfWeek.valueOf(startDayStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            startDay = DayOfWeek.FRIDAY;
        }
        
        LocalTime nowTime = now.toLocalTime();
        LocalTime startTime = LocalTime.of(startHour, 0);
        LocalTime endTime = LocalTime.of(endHour, 0);
        
        if (day == startDay) {
            return nowTime.isAfter(startTime) || nowTime.equals(startTime);
        } else if (day == startDay.plus(1)) {
            return nowTime.isBefore(endTime);
        }
        
        return false;
    }

    private boolean checkInGameShabbat() {
        return false;
    }

    public boolean isShabbat() {
        return isShabbatActive;
    }

    public String getShabbatTimeRemaining() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        int startHour = config.getInt("shabbat.start-hour", 18);
        int endHour = config.getInt("shabbat.end-hour-next-day", 19);
        
        DayOfWeek startDay;
        try {
            startDay = DayOfWeek.valueOf(config.getString("shabbat.real-day", "FRIDAY").toUpperCase());
        } catch (IllegalArgumentException e) {
            startDay = DayOfWeek.FRIDAY;
        }
        
        LocalTime nowTime = now.toLocalTime();
        LocalTime startTime = LocalTime.of(startHour, 0);
        
        if (isShabbatActive) {
            DayOfWeek endDay = startDay.plus(1);
            LocalDateTime shabbatEnd;
            
            if (day == endDay) {
                shabbatEnd = now.with(endDay).with(startTime);
            } else {
                shabbatEnd = now.with(TemporalAdjusters.nextOrSame(endDay)).with(startTime);
            }
            
            if (now.isAfter(shabbatEnd)) {
                shabbatEnd = shabbatEnd.plusDays(7);
            }
            
            long minutes = java.time.Duration.between(now, shabbatEnd).toMinutes();
            long hours = minutes / 60;
            minutes = minutes % 60;
            
            return hours + "h" + minutes + "m";
        } else {
            LocalDateTime nextShabbat;
            
            if (day == startDay) {
                if (nowTime.isAfter(startTime)) {
                    nextShabbat = now.with(TemporalAdjusters.next(startDay)).with(startTime);
                } else {
                    nextShabbat = now.with(startTime);
                }
            } else {
                nextShabbat = now.with(TemporalAdjusters.nextOrSame(startDay)).with(startTime);
            }
            
            if (nextShabbat.isBefore(now)) {
                nextShabbat = nextShabbat.plusDays(7);
            }
            
            long minutes = java.time.Duration.between(now, nextShabbat).toMinutes();
            long hours = minutes / 60;
            minutes = minutes % 60;
            
            return hours + "h" + minutes + "m";
        }
    }
}
