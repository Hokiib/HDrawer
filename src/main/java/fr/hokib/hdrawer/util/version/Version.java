package fr.hokib.hdrawer.util.version;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Version {
    NONE,
    V1_19_4,
    V1_20, V1_20_1, V1_20_2, V1_20_3, V1_20_4, V1_20_6;

    public static final String REGEX = "MC: (\\d+\\.\\d+(\\.\\d+)?)";

    public static Version getCurrentVersion() {
        final String version = Bukkit.getVersion();

        final Pattern pattern = java.util.regex.Pattern.compile(REGEX);
        final Matcher matcher = pattern.matcher(version);

        try {
            if (matcher.find()) {
                final String versionNumber = matcher.group(1);

                return valueOf("V" + versionNumber.replace(".", "_"));
            }
        } catch (IllegalArgumentException ignored){}

        return NONE;
    }

    public boolean isOlderThan(final Version otherVersion) {
        return this.ordinal() < otherVersion.ordinal();
    }

    /**
     * If the other version is the same, it returns true
     */
    public boolean isNewerThan(final Version otherVersion) {
        return this.ordinal() >= otherVersion.ordinal();
    }
}