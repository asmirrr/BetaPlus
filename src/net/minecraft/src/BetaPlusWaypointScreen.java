package net.minecraft.src;

import org.lwjgl.input.Keyboard;

/** Small keyboard-friendly manager using Beta's normal GUI. */
final class BetaPlusWaypointScreen extends GuiScreen {
    private final BetaPlusWaypoints waypoints;
    private int selected = -1, page = 0;
    private String input = "", message = "";
    private boolean editing, creating, confirmDelete;
    private static final int PAGE_SIZE = 5;

    BetaPlusWaypointScreen(BetaPlusWaypoints waypoints) { this.waypoints = waypoints; }
    public void initGui() {
        controlList.clear();
        int left = width / 2 - 120, top = height / 2 - 72;
        if (editing) {
            controlList.add(new GuiButton(20, left, top + 100, 115, 20, "Save"));
            controlList.add(new GuiButton(21, left + 125, top + 100, 115, 20, "Cancel"));
        } else {
            for (int i = 0; i < PAGE_SIZE; i++) {
                int index = page * PAGE_SIZE + i;
                if (index < waypoints.points.size()) {
                    BetaPlusWaypoints.Point p = waypoints.points.get(index);
                    String prefix = (selected == index ? "> " : "") + (p.visible ? "" : "[hidden] ");
                    String suffix = "  " + (int)waypoints.distance(mc, p) + "m";
                    String name = p.name;
                    while (name.length() > 1 && fontRenderer.getStringWidth(prefix + name + suffix) > 225) name = name.substring(0, name.length() - 1);
                    String label = prefix + name + suffix;
                    controlList.add(new GuiButton(100 + i, left, top + 18 + i * 20, 240, 20, label));
                }
            }
            controlList.add(new GuiButton(1, left, top + 122, 56, 20, "Add"));
            controlList.add(new GuiButton(2, left + 60, top + 122, 56, 20, "Rename"));
            controlList.add(new GuiButton(3, left + 120, top + 122, 56, 20, "Show/hide"));
            controlList.add(new GuiButton(4, left + 180, top + 122, 60, 20, confirmDelete ? "Confirm" : "Delete"));
            controlList.add(new GuiButton(5, left, top + 145, 56, 20, "<"));
            controlList.add(new GuiButton(6, left + 60, top + 145, 56, 20, ">"));
            controlList.add(new GuiButton(7, left + 120, top + 145, 120, 20, "Done"));
            ((GuiButton)controlList.get(controlList.size() - 7)).enabled = waypoints.available() && waypoints.points.size() < 256;
            for (int i = controlList.size() - 6; i <= controlList.size() - 4; i++) ((GuiButton)controlList.get(i)).enabled = selected >= 0 && selected < waypoints.points.size() && waypoints.available();
        }
    }
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id >= 100) { selected = page * PAGE_SIZE + button.id - 100; confirmDelete = false; }
        else if (button.id == 1) { creating = true; editing = true; input = "Waypoint " + (waypoints.points.size() + 1); confirmDelete = false; }
        else if (button.id == 2 && selected >= 0) { creating = false; editing = true; input = waypoints.points.get(selected).name; confirmDelete = false; }
        else if (button.id == 3 && selected >= 0) { if (!waypoints.toggle(selected)) message = "Could not save waypoint."; confirmDelete = false; }
        else if (button.id == 4 && selected >= 0) {
            if (confirmDelete) { if (!waypoints.delete(selected)) message = "Could not save waypoint."; else { selected = -1; if (page > 0 && page * PAGE_SIZE >= waypoints.points.size()) page--; message = "Waypoint deleted."; } confirmDelete = false; }
            else { confirmDelete = true; message = "Click Confirm to delete."; }
        }
        else if (button.id == 5) { page = Math.max(0, page - 1); confirmDelete = false; }
        else if (button.id == 6) { if ((page + 1) * PAGE_SIZE < waypoints.points.size()) page++; confirmDelete = false; }
        else if (button.id == 7) { mc.displayGuiScreen(null); mc.setIngameFocus(); return; }
        else if (button.id == 20) {
            boolean ok = creating ? waypoints.add(mc, input) : waypoints.rename(selected, input);
            if (ok) { editing = false; message = "Waypoint saved."; }
            else message = "Enter a name or check the log for save errors.";
        }
        else if (button.id == 21) { editing = false; message = ""; }
        initGui();
    }
    protected void keyTyped(char character, int key) {
        if (!editing) { super.keyTyped(character, key); return; }
        if (key == Keyboard.KEY_ESCAPE) { editing = false; message = ""; initGui(); }
        else if (key == Keyboard.KEY_BACK && input.length() > 0) input = input.substring(0, input.length() - 1);
        else if (key == Keyboard.KEY_RETURN) actionPerformed(new GuiButton(20, 0, 0, "Save"));
        else if (character >= 32 && character < 127 && input.length() < 32) input += character;
    }
    public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();
        int left = width / 2 - 120, top = height / 2 - 72;
        drawCenteredString(fontRenderer, "Waypoints", width / 2, top, 0xFFFFFF);
        if (editing) {
            drawString(fontRenderer, creating ? "Name new waypoint:" : "Rename waypoint:", left, top + 30, 0xFFFFFF);
            drawRect(left, top + 48, left + 240, top + 66, 0xFF000000);
            drawString(fontRenderer, input + "_", left + 4, top + 53, 0xFFFFFF);
        } else {
            if (!waypoints.available()) drawCenteredString(fontRenderer, "Single-player only or waypoint file unavailable", width / 2, top + 55, 0xFF7777);
            else if (waypoints.points.isEmpty()) drawCenteredString(fontRenderer, "No waypoints yet", width / 2, top + 55, 0xAAAAAA);
            drawString(fontRenderer, "Page " + (page + 1), left + 6, top + 110, 0xAAAAAA);
        }
        if (!message.isEmpty()) drawCenteredString(fontRenderer, message, width / 2, top + 169, 0xFFFF77);
        super.drawScreen(mouseX, mouseY, partial);
    }
}
