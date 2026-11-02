package net.minecraft.src;

/** Minimal ModLoader discovery smoke test; no gameplay features yet. */
public final class mod_BetaPlus extends BaseMod {
    public mod_BetaPlus() {
        System.out.println("[BetaPlus] ModLoader initialized BetaPlus 0.1.0-dev");
    }

    public String Version() {
        return "0.1.0-dev";
    }
}
