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
        String text = "X: " + x + "  Y: " + y + "  Z: " + z + "  " + names[direction];
        int mapX = config.position.endsWith("right") ? screen.getScaledWidth() - mapSize - 8 : 8;
        int mapY = config.position.startsWith("bottom") ? screen.getScaledHeight() - mapSize - 8 : 8;
        int lineX = config.position.endsWith("right") ? mapX + mapSize - mc.fontRenderer.getStringWidth(text) : mapX;
        lineX = Math.max(2, Math.min(lineX, screen.getScaledWidth() - mc.fontRenderer.getStringWidth(text) - 2));
        int lineY = config.enabled && mapSize >= 32 ? mapY + mapSize + 5 : 6;
        if (lineY + 9 > screen.getScaledHeight()) lineY = Math.max(2, mapY - 12);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix();
        try {
            mc.entityRenderer.func_905_b();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            mc.fontRenderer.drawStringWithShadow(text, lineX, lineY, 0xFFFFFF);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }
}
