package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Minimal, independently toggleable navigation readout. */
final class BetaPlusCoordinates {
    void draw(Minecraft mc, BetaPlusConfig config) {
        int x = MathHelper.floor_double(mc.thePlayer.posX);
        int y = MathHelper.floor_double(mc.thePlayer.boundingBox.minY);
        int z = MathHelper.floor_double(mc.thePlayer.posZ);
        ScaledResolution screen = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int mapSize = Math.min(config.size, Math.min(screen.getScaledWidth() - 20, screen.getScaledHeight() - 20));
        String text = config.coordinates ? "X: " + x + "  Y: " + y + "  Z: " + z : "";
        if (config.status) {
            long time = ((mc.theWorld.getWorldTime() % 24000L) + 24000L) % 24000L;
            int minutes = (int)((time * 60L / 1000L + 360L) % 1440L);
            String fps = mc.debug == null ? "" : mc.debug;
            int end = fps.indexOf(" fps");
            fps = end > 0 ? fps.substring(0, end) + " FPS" : "FPS --";
            String clock = (minutes / 60 < 10 ? "0" : "") + minutes / 60 + ":" + (minutes % 60 < 10 ? "0" : "") + minutes % 60;
            text += (config.coordinates ? "  " : "") + fps + "  " + clock;
        }
        int mapX = config.position.endsWith("right") ? screen.getScaledWidth() - mapSize - 8 : 8;
        int mapY = config.position.startsWith("bottom") ? screen.getScaledHeight() - mapSize - 8 : 8;
        float textScale = 0.75F;
        int textWidth = (int)Math.ceil(mc.fontRenderer.getStringWidth(text) * textScale);
        int lineX = config.position.endsWith("right") ? mapX + mapSize - textWidth : mapX;
        lineX = Math.max(2, Math.min(lineX, screen.getScaledWidth() - textWidth - 2));
        int lineY = config.enabled && mapSize >= 32 ? mapY + mapSize + 5 : 6;
        if (lineY + 9 > screen.getScaledHeight()) lineY = Math.max(2, mapY - 12);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix();
        try {
            mc.entityRenderer.func_905_b();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glTranslatef(lineX, lineY, 0.0F);
            GL11.glScalef(textScale, textScale, 1.0F);
            mc.fontRenderer.drawStringWithShadow(text, 0, 0, 0xFFFFFF);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }
}
