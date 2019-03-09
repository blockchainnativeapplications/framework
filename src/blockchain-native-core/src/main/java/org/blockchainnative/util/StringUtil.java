package org.blockchainnative.util;

/**
 * Provides static utility methods for working with {@code String}. <br>
 * <br>
 * The class is not intended to be instantiated as it only provides static methods.
 *
 * @author Matthias Veit
 * @since 1.0
 */
public final class StringUtil {

    /**
     * The class is not intended to be instantiated as it only provides static methods.
     */
    private StringUtil() {
    }

    /**
     * Checks whether a {@code String} is null or equals the empty {@code String} after using {@code String.trim()}.
     *
     * @param s String to be checked.
     * @return {@code true} if the {@code String} is empty, {@code false} otherwise.
     */
    public static boolean isNullOrEmpty(String s) {
        return null == s || "".equals(s.trim());
    }

    /**
     * Compares two {@code String} objects for case-insensitve equality.
     *
     * @param s1 a string
     * @param s2 a string
     * @return {@code true} if the {@code Strings} are equal without considering character casing, {@code false} otherwise.
     */
    public static boolean equalsIgnoreCase(String s1, String s2) {
        return (s1 == s2) || (s1 != null && s1.equalsIgnoreCase(s2));
    }
}
