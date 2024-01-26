package fr.hokib.hdrawer.util;

import java.text.DecimalFormat;
import java.util.SplittableRandom;

public class NumberUtil {

    private static final SplittableRandom RANDOM = new SplittableRandom();

    public static String format(final int amount) {
        if (amount < 1000) return String.valueOf(amount);
        else if (amount < 1000000) {
            return new DecimalFormat("#.#k").format(amount / 1000.0);
        }

        return new DecimalFormat("#.#M").format(amount / 1000000.0);
    }

    public static int getRandomId() {
        return RANDOM.nextInt() * Integer.MAX_VALUE;
    }
}
