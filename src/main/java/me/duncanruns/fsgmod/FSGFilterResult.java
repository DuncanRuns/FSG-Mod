package me.duncanruns.fsgmod;

public final class FSGFilterResult {
    public final String seed;
    public final String token;
    public final long generationTime;

    public FSGFilterResult(String seed, String token, long generationTime) {
        this.seed = seed;
        this.token = token;
        this.generationTime = generationTime;
    }
}
