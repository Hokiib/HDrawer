package fr.hokib.hdrawer.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");

    public static String color(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);

        while (matcher.find()) {
            final String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, String.valueOf(ChatColor.of(color.substring(1))));
            matcher = HEX_PATTERN.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> color(final List<String> messages) {
        final List<String> result = new ArrayList<>();

        for (final String message : messages) {
            result.add(color(message));
        }

        return result;
    }
}
