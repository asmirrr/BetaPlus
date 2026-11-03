package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public final class mod_BetaPlus extends BaseMod {
    private final BetaPlusConfig config = new BetaPlusConfig();
    private final BetaPlusMap map = new BetaPlusMap(config);
    private final KeyBinding toggle;
    private final KeyBinding zoomIn = new KeyBinding("BetaPlus zoom in", Keyboard.KEY_EQUALS);
    private final KeyBinding zoomOut = new KeyBinding("BetaPlus zoom out", Keyboard.KEY_MINUS);
    private final KeyBinding coordinates = new KeyBinding("BetaPlus coordinates", Keyboard.KEY_C);
    private final BetaPlusCoordinates coordinateHud = new BetaPlusCoordinates();
    public mod_BetaPlus() {
        config.load();
        toggle = new KeyBinding("BetaPlus minimap", config.key);
        ModLoader.RegisterKey(this, toggle, false);
        ModLoader.RegisterKey(this, zoomIn, false);
        ModLoader.RegisterKey(this, zoomOut, false);
        ModLoader.RegisterKey(this, coordinates, false);
        ModLoader.SetInGameHook(this, true, false);
        System.out.println("[BetaPlus] ModLoader initialized BetaPlus 0.3.0-dev");
    }
    public boolean OnTickInGame(Minecraft game) {
        if (game.thePlayer != null && game.theWorld != null) {
            if (config.enabled) map.update(game);
            if (game.currentScreen == null && !game.gameSettings.hideGUI && !game.gameSettings.showDebugInfo) {
                if (config.enabled) map.draw(game);
                if (config.coordinates) coordinateHud.draw(game, config);
            }
        }
        return true;
    }
    public void KeyboardEvent(KeyBinding key) {
        if (ModLoader.getMinecraftInstance().currentScreen != null) return;
        if (key == toggle) config.enabled = !config.enabled;
        else if (key == coordinates) config.coordinates = !config.coordinates;
        else if (key == zoomIn) config.zoom = Math.max(1, config.zoom / 2);
        else if (key == zoomOut) config.zoom = Math.min(4, config.zoom * 2);
        config.save();
    }
    public String Version() { return "0.3.0-dev"; }
}
