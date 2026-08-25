package me.sirius.sschedules.util;

import me.sirius.core.text.Text;

public class ColorUtil {

    public static String translate(String text) {
        return text == null ? null : Text.legacy(text);
    }
}
