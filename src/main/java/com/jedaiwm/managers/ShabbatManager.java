package com.jedaiwm.managers;

import com.jedaiwm.JedaiWM;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ShabbatManager {

    private final JedaiWM plugin;
    private final Configuration config;

    private boolean isShabbatActive;
    private String currentWeekday;

    private static final String[] WEEKDAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final Map<String, Integer> WEEKDAY_MAP = new HashMap<>();

    static {
        for (int i = 0; i < WEEKDAYS.length; i++) {
            WEEKDAY_MAP.put(WEEKDAYS[i].toLowerCase(), i);
        }
    }

    public ShabbatManager(JedaiWM plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.isShabbatActive = false;
        this.currentWeekday = "Sunday";
        updateShabbatStatus();
    }

    public void updateShabbatStatus() {
        String mode = config.getString("shabbat.mode", "real-time");

        if (mode.equalsIgnoreCase("minecraft-day")) {
            isShabbatActive = checkMinecraftShabbat();
        } else {
            isShabbatActive = checkRealTimeShabbat();
        }
    }

    private boolean checkMinecraftShabbat() {
        World overworld = plugin.getServer().getWorlds().get(0);
        if (overworld == null) return false;

        long dayTime = overworld.getFullTime();
        long dayNumber = dayTime / 24000;
        int dayOfWeek = (int) (dayNumber % 7);

        currentWeekday = WEEKDAYS[dayOfWeek];

        int shabbatStart = config.getInt("shabbat.mc-day-start", 5);
        int shabbatEnd = config.getInt("shabbat.mc-day-end", 6);

        if (shabbatStart < 0 || shabbatStart > 6) shabbatStart = 5;
        if (shabbatEnd < 0 || shabbatEnd > 6) shabbatEnd = 6;

        if (shabbatStart <= shabbatEnd) {
            return dayOfWeek >= shabbatStart && dayOfWeek <= shabbatEnd;
        } else {
            return dayOfWeek >= shabbatStart || dayOfWeek <= shabbatEnd;
        }
    }

    private boolean checkRealTimeShabbat() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.DayOfWeek day = now.getDayOfWeek();

        String startDayStr = config.getString("shabbat.real-day", "FRIDAY");
        int startHour = config.getInt("shabbat.start-hour", 18);
        int endHour = config.getInt("shabbat.end-hour-next-day", 19);

        java.time.DayOfWeek startDay;
        try {
            startDay = java.time.DayOfWeek.valueOf(startDayStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            startDay = java.time.DayOfWeek.FRIDAY;
        }

        java.time.LocalTime nowTime = now.toLocalTime();
        java.time.LocalTime startTime = java.time.LocalTime.of(startHour, 0);
        java.time.LocalTime endTime = java.time.LocalTime.of(endHour, 0);

        if (day == startDay) {
            currentWeekday = startDay.name().substring(0, 1) + startDay.name().substring(1).toLowerCase();
            return nowTime.isAfter(startTime) || nowTime.equals(startTime);
        } else if (day == startDay.plus(1)) {
            currentWeekday = startDay.plus(1).name().substring(0, 1) + startDay.plus(1).name().substring(1).toLowerCase();
            return nowTime.isBefore(endTime);
        }

        currentWeekday = day.name().substring(0, 1) + day.name().substring(1).toLowerCase();
        return false;
    }

    public boolean isShabbat() {
        return isShabbatActive;
    }

    public String getCurrentWeekday() {
        return currentWeekday;
    }

    public String getShabbatTimeRemaining() {
        String mode = config.getString("shabbat.mode", "real-time");

        if (mode.equalsIgnoreCase("minecraft-day")) {
            return getMinecraftShabbatTimeRemaining();
        }
        return getRealTimeShabbatTimeRemaining();
    }

    private String getMinecraftShabbatTimeRemaining() {
        World overworld = plugin.getServer().getWorlds().get(0);
        if (overworld == null) return "?";

        long dayTime = overworld.getFullTime();
        long dayNumber = dayTime / 24000;
        int dayOfWeek = (int) (dayNumber % 7);

        int shabbatStart = config.getInt("shabbat.mc-day-start", 5);
        if (shabbatStart < 0 || shabbatStart > 6) shabbatStart = 5;

        int daysUntil;
        if (isShabbatActive) {
            int shabbatEnd = config.getInt("shabbat.mc-day-end", 6);
            if (shabbatEnd < 0 || shabbatEnd > 6) shabbatEnd = 6;
            int daysLeft = (shabbatEnd - dayOfWeek + 7) % 7;
            daysUntil = daysLeft;
        } else {
            daysUntil = (shabbatStart - dayOfWeek + 7) % 7;
            if (daysUntil == 0) daysUntil = 7;
        }

        return daysUntil + " day" + (daysUntil > 1 ? "s" : "");
    }

    private String getRealTimeShabbatTimeRemaining() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.DayOfWeek day = now.getDayOfWeek();
        int startHour = config.getInt("shabbat.start-hour", 18);
        int endHour = config.getInt("shabbat.end-hour-next-day", 19);

        java.time.DayOfWeek startDay;
        try {
            startDay = java.time.DayOfWeek.valueOf(config.getString("shabbat.real-day", "FRIDAY").toUpperCase());
        } catch (IllegalArgumentException e) {
            startDay = java.time.DayOfWeek.FRIDAY;
        }

        java.time.LocalTime nowTime = now.toLocalTime();
        java.time.LocalTime startTime = java.time.LocalTime.of(startHour, 0);

        if (isShabbatActive) {
            java.time.DayOfWeek endDay = startDay.plus(1);
            java.time.LocalDateTime shabbatEnd;

            if (day == endDay) {
                shabbatEnd = now.with(endDay).with(startTime);
            } else {
                shabbatEnd = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(endDay)).with(startTime);
            }

            if (now.isAfter(shabbatEnd)) {
                shabbatEnd = shabbatEnd.plusDays(7);
            }

            long minutes = java.time.Duration.between(now, shabbatEnd).toMinutes();
            long hours = minutes / 60;
            minutes = minutes % 60;

            return hours + "h" + minutes + "m";
        } else {
            java.time.LocalDateTime nextShabbat;

            if (day == startDay) {
                if (nowTime.isAfter(startTime)) {
                    nextShabbat = now.with(java.time.temporal.TemporalAdjusters.next(startDay)).with(startTime);
                } else {
                    nextShabbat = now.with(startTime);
                }
            } else {
                nextShabbat = now.with(java.time.temporal.TemporalAdjusters.nextOrSame(startDay)).with(startTime);
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
