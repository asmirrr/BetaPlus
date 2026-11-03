package net.minecraft.src;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Bounded north-up surface map, keyed by world column. */
final class BetaPlusMap extends Gui {
    private static final int GRID = 64, SAMPLE_GRID = 66, SLOTS = 262144, BUDGET = 96;
    private final int[] xs = new int[SLOTS], zs = new int[SLOTS], colors = new int[SLOTS], heights = new int[SLOTS];
    private final boolean[] valid = new boolean[SLOTS];
    private final BetaPlusConfig config;
    private World world;
    private long lastTick = Long.MIN_VALUE;
    private long tickStarted;
    private int cursor;
    BetaPlusMap(BetaPlusConfig config) { this.config = config; }

    void update(Minecraft mc) {
        if (world != mc.theWorld) { world = mc.theWorld; Arrays.fill(valid, false); cursor = 0; lastTick = Long.MIN_VALUE; }
        long tick = world.getWorldTime();
        if (tick == lastTick) return;
        lastTick = tick;
        tickStarted = System.nanoTime();
        int cx = align(MathHelper.floor_double(mc.thePlayer.posX), config.zoom);
        int cz = align(MathHelper.floor_double(mc.thePlayer.posZ), config.zoom);
        int used = 0;
        // Fill missing visible columns first, then refresh existing ones over time.
        for (int i = 0; i < SAMPLE_GRID * SAMPLE_GRID && used < BUDGET; i++) {
            int cell = (cursor + i) % (SAMPLE_GRID * SAMPLE_GRID);
            int x = cx + (cell % SAMPLE_GRID - SAMPLE_GRID / 2) * config.zoom;
            int z = cz + (cell / SAMPLE_GRID - SAMPLE_GRID / 2) * config.zoom;
            int s = slot(x, z);
            if ((!valid[s] || xs[s] != x || zs[s] != z) && world.blockExists(x, 64, z)) {
                sample(x, z, s); used++;
            }
        }
        for (int i = 0; i < BUDGET - used; i++) {
            int cell = cursor++ % (SAMPLE_GRID * SAMPLE_GRID);
            int x = cx + (cell % SAMPLE_GRID - SAMPLE_GRID / 2) * config.zoom, z = cz + (cell / SAMPLE_GRID - SAMPLE_GRID / 2) * config.zoom;
            if (!world.blockExists(x, 64, z)) continue;
            sample(x, z, slot(x, z));
        }
    }
    private void sample(int x, int z, int s) {
        Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
        int lx = x & 15, lz = z & 15;
        int y = Math.min(127, chunk.getHeightValue(lx, lz) + 12);
        while (y > 0) {
            int id = chunk.getBlockID(lx, y, lz);
            Block block = id >= 0 && id < Block.blocksList.length ? Block.blocksList[id] : null;
            if (block != null && block.blockMaterial.materialMapColor != MapColor.airColor) {
                xs[s] = x; zs[s] = z; heights[s] = y;
                colors[s] = block.blockMaterial.materialMapColor.colorValue; valid[s] = true;
                return;
            }
            y--;
        }
        // Empty columns have a stable background color rather than flashing invalid.
        xs[s] = x; zs[s] = z; heights[s] = 0; colors[s] = 0x202020; valid[s] = true;
    }
    private static int align(int value, int zoom) { return MathHelper.floor_double((double)value / zoom) * zoom; }
    private int slot(int x, int z) { return (x & 511) | ((z & 511) << 9); }
    void draw(Minecraft mc) {
        ScaledResolution screen = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int size = Math.min(config.size, Math.min(screen.getScaledWidth() - 20, screen.getScaledHeight() - 20));
        if (size < 32) return;
        int x = config.position.endsWith("right") ? screen.getScaledWidth() - size - 8 : 8;
        int y = config.position.startsWith("bottom") ? screen.getScaledHeight() - size - 8 : 8;
        int cx = align(MathHelper.floor_double(mc.thePlayer.posX), config.zoom), cz = align(MathHelper.floor_double(mc.thePlayer.posZ), config.zoom);
        double partial = Math.max(0.0, Math.min(1.0, (System.nanoTime() - tickStarted) / 50000000.0));
        double playerX = mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * partial;
        double playerZ = mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * partial;
        double cellSize = size / (double)GRID;
        double offsetX = (playerX - cx) / config.zoom * cellSize;
        double offsetZ = (playerZ - cz) / config.zoom * cellSize;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix();
        try {
            mc.entityRenderer.func_905_b(); GL11.glDisable(GL11.GL_DEPTH_TEST); GL11.glDisable(GL11.GL_CULL_FACE);
            drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 0xD0202020);
            drawRect(x, y, x + size, y + size, 0xC0101010);
            GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glDisable(GL11.GL_LIGHTING); GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            int left = x * mc.displayWidth / screen.getScaledWidth();
            int right = (x + size) * mc.displayWidth / screen.getScaledWidth();
            int bottom = (screen.getScaledHeight() - y - size) * mc.displayHeight / screen.getScaledHeight();
            int top = (screen.getScaledHeight() - y) * mc.displayHeight / screen.getScaledHeight();
            GL11.glEnable(GL11.GL_SCISSOR_TEST); GL11.glScissor(left, bottom, right - left, top - bottom);
            Tessellator t = Tessellator.instance; t.startDrawingQuads();
            for (int row = 0; row < SAMPLE_GRID; row++) for (int col = 0; col < SAMPLE_GRID; col++) {
                int wx = cx + (col - SAMPLE_GRID / 2) * config.zoom, wz = cz + (row - SAMPLE_GRID / 2) * config.zoom, s = slot(wx, wz);
                if (!valid[s] || xs[s] != wx || zs[s] != wz) continue;
                int north = slot(wx, wz - config.zoom);
                int shade = valid[north] && xs[north] == wx && zs[north] == wz - config.zoom
                    ? (heights[s] > heights[north] ? 255 : heights[s] < heights[north] ? 180 : 220) : 220;
                int color = colors[s];
                t.setColorRGBA((color >> 16 & 255) * shade / 255, (color >> 8 & 255) * shade / 255, (color & 255) * shade / 255, (int)(config.opacity * 255));
                double x0 = x + size / 2.0 + (col - SAMPLE_GRID / 2 - 0.5) * cellSize - offsetX, x1 = x0 + cellSize;
                double y0 = y + size / 2.0 + (row - SAMPLE_GRID / 2 - 0.5) * cellSize - offsetZ, y1 = y0 + cellSize;
                t.addVertex(x0, y1, 0); t.addVertex(x1, y1, 0); t.addVertex(x1, y0, 0); t.addVertex(x0, y0, 0);
            }
            t.draw();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            int mx = x + size / 2, my = y + size / 2;
            GL11.glPushMatrix();
            GL11.glTranslatef(mx, my, 0); GL11.glRotatef(mc.thePlayer.rotationYaw + 180.0F, 0, 0, 1);
            String[] marker = {"....#....", "...###...", "...#W#...", "..##W##..", "..#WWW#..", ".##WWW##.", ".#WWWWW#.", "##WW#WW##", "##W###W##", ".###.###."};
            for (int row = 0; row < marker.length; row++) for (int col = 0; col < 9; col++) {
                char pixel = marker[row].charAt(col);
                if (pixel != '.') drawRect(col - 4, row - 5, col - 3, row - 4, pixel == '#' ? 0xFF000000 : 0xFFFFFFFF);
            }
            GL11.glPopMatrix();
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glColor4f(1, 1, 1, 1);
            mc.fontRenderer.drawStringWithShadow("N", mx - 3, y + 2, 0xFFFFFF); mc.fontRenderer.drawStringWithShadow("S", mx - 3, y + size - 10, 0xFFFFFF);
            mc.fontRenderer.drawStringWithShadow("W", x + 2, my - 4, 0xFFFFFF); mc.fontRenderer.drawStringWithShadow("E", x + size - 8, my - 4, 0xFFFFFF);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix(); GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }
}
