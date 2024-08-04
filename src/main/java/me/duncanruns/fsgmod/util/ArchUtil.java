package me.duncanruns.fsgmod.util;

public final class ArchUtil {
    private ArchUtil() {
    }

    public static Arch getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.startsWith("amd")) {
            return Arch.AMD64;
        }
        if (arch.contains("aarch") || arch.contains("arm")) {
            return Arch.ARM;
        }
        return Arch.OTHER;
    }

    public enum Arch {
        AMD64,
        ARM,
        OTHER,
    }
}
