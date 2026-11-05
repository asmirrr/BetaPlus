package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public final class mod_BetaPlus extends BaseMod {
    private final BetaPlusConfig config = new BetaPlusConfig();
    private final BetaPlusMap map = new BetaPlusMap(config);
    private final BetaPlusWaypoints waypoints = new BetaPlusWaypoints();
    private final KeyBinding addWaypoint = new KeyBinding("BetaPlus add waypoint", Keyboard.KEY_B);
    private final KeyBinding toggle;
    private final KeyBinding zoomIn = new KeyBinding("BetaPlus zoom in", Keyboard.KEY_EQUALS);
    private final KeyBinding zoomOut = new KeyBinding("BetaPlus zoom out", Keyboard.KEY_MINUS);
    private final KeyBinding coordinates = new KeyBinding("BetaPlus coordinates", Keyboard.KEY_C);
    private final KeyBinding status = new KeyBinding("BetaPlus FPS and clock", Keyboard.KEY_V);
    private final BetaPlusCoordinates coordinateHud = new BetaPlusCoordinates();
    public mod_BetaPlus() {
        config.load();
        toggle = new KeyBinding("BetaPlus minimap", config.key);
        ModLoader.RegisterKey(this, toggle, false);
        ModLoader.RegisterKey(this, zoomIn, false);
        ModLoader.RegisterKey(this, zoomOut, false);
        ModLoader.RegisterKey(this, coordinates, false);
        ModLoader.RegisterKey(this, status, false);
        ModLoader.RegisterKey(this, addWaypoint, false);
        ModLoader.SetInGameHook(this, true, false);
        System.out.println("[BetaPlus] ModLoader initialized BetaPlus 1.0.0");
    }
    public boolean OnTickInGame(Minecraft game) {
        if (game.thePlayer != null && game.theWorld != null) {
            waypoints.update(game);
            if (config.enabled) map.update(game);
            if (game.currentScreen == null && !game.gameSettings.hideGUI && !game.gameSettings.showDebugInfo) {
                if (config.enabled) map.draw(game, waypoints);
                if (config.coordinates || config.status) coordinateHud.draw(game, config);
            }
        }
        return true;
    }
    public void KeyboardEvent(KeyBinding key) {
        if (ModLoader.getMinecraftInstance().currentScreen != null) return;
        if (key == addWaypoint) { waypoints.add(ModLoader.getMinecraftInstance()); return; }
        if (key == toggle) config.enabled = !config.enabled;
        else if (key == coordinates) config.coordinates = !config.coordinates;
        else if (key == status) config.status = !config.status;
        else if (key == zoomIn) config.zoom = Math.max(1, config.zoom / 2);
        else if (key == zoomOut) config.zoom = Math.min(4, config.zoom * 2);
        config.save();
    }
    public String Version() { return "1.0.0"; }
}
