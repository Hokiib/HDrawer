package fr.hokib.hdrawer.util;

import java.text.DecimalFormat;

public class NumberUtil {

    public static String format(final int amount) {
        if (amount < 1000) return String.valueOf(amount);
        else if (amount < 1000000) {
            return new DecimalFormat("#.#k").format(amount / 1000.0);
        }

        return new DecimalFormat("#.#M").format(amount / 1000000.0);
    }
}
