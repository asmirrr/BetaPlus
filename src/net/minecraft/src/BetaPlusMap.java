package net.minecraft.src;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Bounded north-up surface map, keyed by world column. */
final class BetaPlusMap extends Gui {
    private static final int GRID = 32, SLOTS = 16384, BUDGET = 96;
    private final int[] xs = new int[SLOTS], zs = new int[SLOTS], tiles = new int[SLOTS];
    private final boolean[] valid = new boolean[SLOTS];
    private final BetaPlusConfig config;
    private World world;
    private long lastTick = Long.MIN_VALUE;
    private int cursor;
    BetaPlusMap(BetaPlusConfig config) { this.config = config; }

    void update(Minecraft mc) {
        if (world != mc.theWorld) { world = mc.theWorld; Arrays.fill(valid, false); cursor = 0; lastTick = Long.MIN_VALUE; }
        long tick = world.getWorldTime();
        if (tick == lastTick) return;
        lastTick = tick;
        int cx = MathHelper.floor_double(mc.thePlayer.posX), cz = MathHelper.floor_double(mc.thePlayer.posZ);
        for (int i = 0; i < BUDGET; i++) {
            int cell = cursor++ % (GRID * GRID);
            int x = cx + (cell % GRID - GRID / 2) * config.zoom, z = cz + (cell / GRID - GRID / 2) * config.zoom;
            int s = slot(x, z); valid[s] = false;
            if (!world.blockExists(x, 64, z)) continue;
            Chunk chunk = world.getChunkFromChunkCoords(x >> 4, z >> 4);
            int lx = x & 15, lz = z & 15;
            int y = Math.min(127, chunk.getHeightValue(lx, lz) + 12);
            while (y > 0 && chunk.getBlockID(lx, y, lz) == 0) y--;
            int id = chunk.getBlockID(lx, y, lz);
            Block block = id >= 0 && id < Block.blocksList.length ? Block.blocksList[id] : null;
            if (block == null) continue;
            xs[s] = x; zs[s] = z; tiles[s] = block.getBlockTextureFromSideAndMetadata(1, chunk.getBlockMetadata(lx, y, lz)); valid[s] = true;
        }
    }
    private int slot(int x, int z) { return (x * 73428767 ^ z * 912931) & (SLOTS - 1); }
    void draw(Minecraft mc) {
        ScaledResolution screen = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int size = Math.min(config.size, Math.min(screen.getScaledWidth() - 20, screen.getScaledHeight() - 20));
        if (size < 32) return;
        int x = config.position.endsWith("right") ? screen.getScaledWidth() - size - 8 : 8;
        int y = config.position.startsWith("bottom") ? screen.getScaledHeight() - size - 8 : 8;
        int cx = MathHelper.floor_double(mc.thePlayer.posX), cz = MathHelper.floor_double(mc.thePlayer.posZ);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix();
        try {
            mc.entityRenderer.func_905_b(); GL11.glDisable(GL11.GL_DEPTH_TEST); GL11.glDisable(GL11.GL_CULL_FACE);
            drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 0xD0202020);
            drawRect(x, y, x + size, y + size, 0xC0101010);
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1, 1, 1, config.opacity);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/terrain.png"));
            Tessellator t = Tessellator.instance; t.startDrawingQuads();
            for (int row = 0; row < GRID; row++) for (int col = 0; col < GRID; col++) {
                int wx = cx + (col - GRID / 2) * config.zoom, wz = cz + (row - GRID / 2) * config.zoom, s = slot(wx, wz);
                if (!valid[s] || xs[s] != wx || zs[s] != wz) continue;
                int tile = tiles[s]; if (tile < 0 || tile >= 256) continue;
                double u = (tile % 16) / 16.0, v = (tile / 16) / 16.0, d = 15.9 / 256.0;
                double x0 = x + col * size / (double)GRID, x1 = x + (col + 1) * size / (double)GRID;
                double y0 = y + row * size / (double)GRID, y1 = y + (row + 1) * size / (double)GRID;
                t.addVertexWithUV(x0, y1, 0, u, v + d); t.addVertexWithUV(x1, y1, 0, u + d, v + d);
                t.addVertexWithUV(x1, y0, 0, u + d, v); t.addVertexWithUV(x0, y0, 0, u, v);
            }
            t.draw();
            int mx = x + size / 2, my = y + size / 2;
            GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glColor4f(1, 1, 1, 1);
            double angle = Math.toRadians(mc.thePlayer.rotationYaw), dx = -Math.sin(angle), dz = Math.cos(angle);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex2d(mx + dx * 6, my + dz * 6); GL11.glVertex2d(mx - dx * 3 - dz * 3, my - dz * 3 + dx * 3);
            GL11.glVertex2d(mx - dx * 3 + dz * 3, my - dz * 3 - dx * 3); GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            mc.fontRenderer.drawStringWithShadow("N", mx - 3, y + 2, 0xFFFFFF); mc.fontRenderer.drawStringWithShadow("S", mx - 3, y + size - 10, 0xFFFFFF);
            mc.fontRenderer.drawStringWithShadow("W", x + 2, my - 4, 0xFFFFFF); mc.fontRenderer.drawStringWithShadow("E", x + size - 8, my - 4, 0xFFFFFF);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix(); GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }
}
