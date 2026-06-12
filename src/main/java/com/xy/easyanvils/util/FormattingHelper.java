package com.xy.easyanvils.util;

/**
 * In 1.12.2 item display names are already plain strings carrying section-sign (§) formatting
 * codes, so the original's ComponentDecomposer/FormattedStringDecomposer collapse to this small
 * helper. Users type ampersand codes (&c, &l, ...) which are converted to § when the
 * "renamingSupportsFormatting" option is on.
 */
public final class FormattingHelper {

    public static final char SECTION = '§';

    private FormattingHelper() {
    }

    /** Converts ampersand formatting codes (&a, &l, &r ...) into section-sign codes. */
    public static String toFormattingCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(chars[i + 1]) > -1) {
                chars[i] = SECTION;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    /**
     * Removes characters Minecraft disallows in names, but keeps the section sign so formatting
     * survives the client -> server trip. Mirrors FormattedStringDecomposer.filterText.
     */
    public static String filterText(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == SECTION || c >= ' ' && c != 127) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
