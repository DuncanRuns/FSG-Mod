package me.duncanruns.fsgmod.util;

import org.apache.commons.lang3.StringUtils;

import java.util.OptionalLong;

public final class SeedUtil {
    private SeedUtil() {
    }

    public static OptionalLong getSeedFromString(String string) {
        OptionalLong optionalLong;
        if (StringUtils.isEmpty(string)) {
            optionalLong = OptionalLong.empty();
        } else {
            OptionalLong optionalLong2 = tryParseLong(string);
            if (optionalLong2.isPresent() && optionalLong2.getAsLong() != 0L) {
                optionalLong = optionalLong2;
            } else {
                optionalLong = OptionalLong.of(string.hashCode());
            }
        }
        return optionalLong;
    }

    public static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }
}
