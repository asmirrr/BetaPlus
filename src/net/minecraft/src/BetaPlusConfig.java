package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

final class BetaPlusConfig {
    boolean enabled = true;
    boolean coordinates = true;
    int size = 96, zoom = 2, key = Keyboard.KEY_M;
    float opacity = 0.9F;
    String position = "top-right";
    private final File file = new File(Minecraft.getMinecraftDir(), "config/BetaPlus.properties");
    void load() {
        Properties p = new Properties();
        try {
            if (file.isFile()) { FileInputStream in = new FileInputStream(file); try { p.load(in); } finally { in.close(); } }
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            coordinates = Boolean.parseBoolean(p.getProperty("coordinates", "true"));
            size = clamp(integer(p, "size", 96), 64, 160);
            int z = integer(p, "zoom", 2); zoom = z == 1 || z == 2 || z == 4 ? z : 2;
            key = clamp(integer(p, "key", Keyboard.KEY_M), 1, 255);
            try { opacity = Float.parseFloat(p.getProperty("opacity", "0.9")); } catch (NumberFormatException ignored) { opacity = 0.9F; }
            if (Float.isNaN(opacity) || Float.isInfinite(opacity)) opacity = 0.9F;
            opacity = Math.max(0.2F, Math.min(1.0F, opacity));
            String pos = p.getProperty("position", "top-right");
            if (pos.equals("top-left") || pos.equals("top-right") || pos.equals("bottom-left") || pos.equals("bottom-right")) position = pos;
        } catch (Exception e) { System.err.println("[BetaPlus] Could not read settings: " + e); }
        save();
    }
    void save() {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled)); p.setProperty("size", Integer.toString(size));
        p.setProperty("coordinates", Boolean.toString(coordinates));
        p.setProperty("zoom", Integer.toString(zoom)); p.setProperty("position", position);
        p.setProperty("opacity", Float.toString(opacity)); p.setProperty("key", Integer.toString(key));
        try { file.getParentFile().mkdirs(); FileOutputStream out = new FileOutputStream(file); try { p.store(out, "BetaPlus settings; key is an LWJGL 2 key code"); } finally { out.close(); } }
        catch (Exception e) { System.err.println("[BetaPlus] Could not save settings: " + e); }
    }
    private static int integer(Properties p, String name, int fallback) { try { return Integer.parseInt(p.getProperty(name, Integer.toString(fallback))); } catch (NumberFormatException e) { return fallback; } }
    private static int clamp(int n, int min, int max) { return Math.max(min, Math.min(max, n)); }
}
