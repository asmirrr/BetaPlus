package net.minecraft.src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Compact heading cues for the nearest visible single-player waypoints. */
final class BetaPlusWaypointHud {
    void draw(final Minecraft mc, BetaPlusWaypoints waypoints) {
        List<BetaPlusWaypoints.Point> visible = new ArrayList<BetaPlusWaypoints.Point>();
        for (BetaPlusWaypoints.Point p : waypoints.points) if (p.visible) visible.add(p);
        if (visible.isEmpty()) return;
        Collections.sort(visible, new Comparator<BetaPlusWaypoints.Point>() {
            public int compare(BetaPlusWaypoints.Point a, BetaPlusWaypoints.Point b) {
                return Double.compare(distanceSquared(mc, a), distanceSquared(mc, b));
            }
        });
        ScaledResolution screen = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix();
        try {
            mc.entityRenderer.func_905_b();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glColor4f(1, 1, 1, 1);
            float scale = 0.75F;
            int count = Math.min(5, visible.size());
            for (int i = 0; i < count; i++) {
                BetaPlusWaypoints.Point p = visible.get(i);
                double dx = p.x + 0.5 - mc.thePlayer.posX, dz = p.z + 0.5 - mc.thePlayer.posZ;
                double bearing = Math.toDegrees(Math.atan2(-dx, dz));
                double delta = ((bearing - mc.thePlayer.rotationYaw + 540.0) % 360.0 + 360.0) % 360.0 - 180.0;
                String arrow = delta < -15 ? "< " : delta > 15 ? "> " : "^ ";
                String suffix = " " + (long)Math.round(Math.sqrt(distanceSquared(mc, p))) + "m";
                String name = p.name;
                int maxWidth = Math.max(40, screen.getScaledWidth() - 30);
                while (name.length() > 1 && mc.fontRenderer.getStringWidth(arrow + name + suffix) * scale > maxWidth) name = name.substring(0, name.length() - 1);
                String label = arrow + name + suffix;
                float labelWidth = mc.fontRenderer.getStringWidth(label) * scale;
                GL11.glPushMatrix();
                GL11.glTranslatef((screen.getScaledWidth() - labelWidth) / 2.0F, 6 + i * 9, 0);
                GL11.glScalef(scale, scale, 1);
                mc.fontRenderer.drawStringWithShadow(label, 0, 0, p.death ? 0xFF7777 : 0xFFFF77);
                GL11.glPopMatrix();
            }
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }
    private static double distanceSquared(Minecraft mc, BetaPlusWaypoints.Point p) {
        double dx = p.x + 0.5 - mc.thePlayer.posX, dy = p.y - mc.thePlayer.boundingBox.minY, dz = p.z + 0.5 - mc.thePlayer.posZ;
        return dx * dx + dy * dy + dz * dz;
    }
}
