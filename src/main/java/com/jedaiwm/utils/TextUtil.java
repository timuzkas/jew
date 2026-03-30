package com.jedaiwm.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

public class TextUtil {

    public static Component createClickableText(String text, String hoverText, String command) {
        TextComponent.Builder builder = Component.text()
            .content(text)
            .color(NamedTextColor.AQUA)
            .decorate(TextDecoration.UNDERLINED)
            .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
            .clickEvent(ClickEvent.runCommand(command));
        return builder.build();
    }

    public static void sendPrayerMenu(Player player) {
        player.sendMessage(Component.text("=== Prayer Menu ===", NamedTextColor.GOLD));
        Component shacharit = createClickableText("[Shacharit]", "Morning Prayer", "/pray shacharit");
        Component mincha = createClickableText("[Mincha]", "Afternoon Prayer", "/pray mincha");
        Component maariv = createClickableText("[Maariv]", "Evening Prayer", "/pray maariv");
        player.sendMessage(shacharit.append(Component.text(" | ")).append(mincha).append(Component.text(" | ")).append(maariv));
    }

    public static void sendSynagogueMenu(Player player) {
        player.sendMessage(Component.text("=== Synagogue ===", NamedTextColor.GOLD));
        Component pray = createClickableText("[Pray]", "Pray at the synagogue", "/pray");
        Component torah = createClickableText("[Study Torah]", "Study the Torah", "/torah");
        Component status = createClickableText("[Check Piety]", "View your piety status", "/jew status");
        player.sendMessage(pray.append(Component.text(" | ")).append(torah).append(Component.text(" | ")).append(status));
    }

    public static void sendConversionInvite(Player target, Player inviter) {
        target.sendMessage(Component.text(inviter.getName() + " invites you to convert to Judaism.", NamedTextColor.GOLD));
        Component accept = createClickableText("[Accept]", "Accept the conversion", "/jew accept " + inviter.getName());
        Component decline = createClickableText("[Decline]", "Decline the invitation", "/jew decline " + inviter.getName());
        target.sendMessage(accept.append(Component.text(" | ")).append(decline));
    }

    public static Component successMessage(String message) {
        return Component.text(message, NamedTextColor.GREEN);
    }

    public static Component errorMessage(String message) {
        return Component.text(message, NamedTextColor.RED);
    }

    public static Component infoMessage(String message) {
        return Component.text(message, NamedTextColor.YELLOW);
    }
}
