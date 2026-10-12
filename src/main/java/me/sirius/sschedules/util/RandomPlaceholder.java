package me.sirius.sschedules.util;

import java.util.random.RandomGenerator;

// Sustituye cada %rand/min-max% por un numero al azar entre min y max, ambos
// incluidos. Un marcador mal escrito se deja tal cual y se para: el bucle no
// puede quedarse dando vueltas sobre un texto que ya no cambia.
public final class RandomPlaceholder {

    private static final String OPEN = "%rand/";

    private RandomPlaceholder() {
    }

    public static String apply(String text, RandomGenerator random) {
        if (text == null || !text.contains(OPEN)) {
            return text;
        }
        StringBuilder out = new StringBuilder(text.length());
        int from = 0;
        int start;
        while ((start = text.indexOf(OPEN, from)) >= 0) {
            int end = text.indexOf('%', start + OPEN.length());
            if (end < 0) {
                break;
            }
            out.append(text, from, start);
            String value = roll(text.substring(start + OPEN.length(), end), random);
            out.append(value != null ? value : text.substring(start, end + 1));
            from = end + 1;
        }
        return out.append(text, from, text.length()).toString();
    }

    private static String roll(String range, RandomGenerator random) {
        int dash = range.indexOf('-', 1);
        if (dash < 0) {
            return null;
        }
        try {
            long min = Long.parseLong(range.substring(0, dash).trim());
            long max = Long.parseLong(range.substring(dash + 1).trim());
            if (max < min) {
                return null;
            }
            return String.valueOf(random.nextLong(min, max + 1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
