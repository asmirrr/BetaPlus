package net.minecraft.src;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.Minecraft;

/** Single-player waypoint storage, scoped to the save and dimension. */
final class BetaPlusWaypoints {
    static final class Point {
        final int x, y, z;
        String name;
        boolean visible;
        Point(int x, int y, int z, String name, boolean visible) { this.x = x; this.y = y; this.z = z; this.name = name; this.visible = visible; }
    }
    final List<Point> points = new ArrayList<Point>();
    private World world;
    private int dimension;
    private File file;
    private boolean writable;

    void update(Minecraft mc) {
        if (world == mc.theWorld && dimension == mc.thePlayer.dimension) return;
        world = mc.theWorld; dimension = mc.thePlayer.dimension; points.clear(); file = null; writable = false;
        if (mc.isMultiplayerWorld() || world.saveHandler == null) return;
        file = world.saveHandler.func_28113_a("betaplus-waypoints-" + dimension);
        writable = true;
        if (!file.isFile()) return;
        Properties data = new Properties();
        try {
            FileInputStream in = new FileInputStream(file);
            try { data.load(in); } finally { in.close(); }
            int count = Integer.parseInt(data.getProperty("count"));
            if (count < 0 || count > 256) throw new IllegalArgumentException("invalid waypoint count");
            for (int i = 0; i < count; i++) {
                String[] values = data.getProperty("point." + i, "").split(",");
                if (values.length != 3) throw new IllegalArgumentException("invalid waypoint");
                String name = data.getProperty("name." + i, "Waypoint " + (i + 1));
                if (name.trim().length() == 0 || name.length() > 32) throw new IllegalArgumentException("invalid waypoint name");
                points.add(new Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), name, Boolean.parseBoolean(data.getProperty("visible." + i, "true"))));
            }
        } catch (Exception e) {
            points.clear(); writable = false;
            System.err.println("[BetaPlus] Waypoints could not be read; file left untouched: " + e);
        }
    }
    boolean available() { return writable; }
    boolean add(Minecraft mc, String name) {
        update(mc);
        if (!writable || points.size() >= 256 || name.trim().isEmpty()) return false;
        points.add(new Point(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.boundingBox.minY), MathHelper.floor_double(mc.thePlayer.posZ), name.trim(), true));
        if (!save()) { points.remove(points.size() - 1); return false; }
        return true;
    }
    boolean rename(int index, String name) {
        if (!writable || name.trim().isEmpty()) return false;
        Point p = points.get(index); String old = p.name; p.name = name.trim();
        if (!save()) { p.name = old; return false; } return true;
    }
    boolean toggle(int index) {
        if (!writable) return false;
        Point p = points.get(index); p.visible = !p.visible;
        if (!save()) { p.visible = !p.visible; return false; } return true;
    }
    boolean delete(int index) {
        if (!writable) return false;
        Point p = points.remove(index);
        if (!save()) { points.add(index, p); return false; } return true;
    }
    double distance(Minecraft mc, Point p) {
        double dx = mc.thePlayer.posX - (p.x + 0.5), dy = mc.thePlayer.boundingBox.minY - p.y, dz = mc.thePlayer.posZ - (p.z + 0.5);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    private boolean save() {
        Properties data = new Properties(); data.setProperty("count", Integer.toString(points.size()));
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i); data.setProperty("point." + i, p.x + "," + p.y + "," + p.z);
            data.setProperty("name." + i, p.name); data.setProperty("visible." + i, Boolean.toString(p.visible));
        }
        File temp = new File(file.getParentFile(), file.getName() + ".tmp");
        try {
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(temp);
            try { data.store(out, "BetaPlus single-player waypoints"); out.getFD().sync(); } finally { out.close(); }
            java.nio.file.Files.move(temp.toPath(), file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) { System.err.println("[BetaPlus] Could not save waypoints: " + e); return false; }
    }
}
