package fr.hokib.hdrawer.util;

import fr.hokib.hdrawer.HDrawer;
import fr.hokib.hdrawer.config.Config;

import java.text.DecimalFormat;

public class NumberUtil {
    final static Config config = HDrawer.get().getConfiguration();

    public static String format(final int amount) {
        if (amount < 1000 || !config.getFormat_km()) return String.valueOf(amount);
        else if (amount < 1000000) {
            return new DecimalFormat("#.#k").format(amount / 1000.0);
        }

        return new DecimalFormat("#.#M").format(amount / 1000000.0);
    }
}
