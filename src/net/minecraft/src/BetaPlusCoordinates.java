package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Minimal, independently toggleable navigation readout. */
final class BetaPlusCoordinates {
    void draw(Minecraft mc, BetaPlusConfig config) {
        int x = MathHelper.floor_double(mc.thePlayer.posX);
        int y = MathHelper.floor_double(mc.thePlayer.boundingBox.minY);
        int z = MathHelper.floor_double(mc.thePlayer.posZ);
        int direction = MathHelper.floor_double(mc.thePlayer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        String[] names = {"S", "W", "N", "E"};
        ScaledResolution screen = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int mapSize = Math.min(config.size, Math.min(screen.getScaledWidth() - 20, screen.getScaledHeight() - 20));
        int lineY = config.enabled && config.position.equals("top-left") && mapSize >= 32 ? 12 + mapSize : 6;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix();
        try {
            mc.entityRenderer.func_905_b();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            mc.fontRenderer.drawStringWithShadow("X: " + x + "  Y: " + y + "  Z: " + z + "  " + names[direction], 6, lineY, 0xFFFFFF);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }
}
