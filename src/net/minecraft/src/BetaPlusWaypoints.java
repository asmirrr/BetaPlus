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
        Point(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
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
                points.add(new Point(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2])));
            }
        } catch (Exception e) {
            points.clear(); writable = false;
            System.err.println("[BetaPlus] Waypoints could not be read; file left untouched: " + e);
        }
    }
    void add(Minecraft mc) {
        update(mc);
        if (!writable || points.size() >= 256) return;
        points.add(new Point(MathHelper.floor_double(mc.thePlayer.posX), MathHelper.floor_double(mc.thePlayer.boundingBox.minY), MathHelper.floor_double(mc.thePlayer.posZ)));
        if (!save()) points.remove(points.size() - 1);
    }
    private boolean save() {
        Properties data = new Properties(); data.setProperty("count", Integer.toString(points.size()));
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i); data.setProperty("point." + i, p.x + "," + p.y + "," + p.z);
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
