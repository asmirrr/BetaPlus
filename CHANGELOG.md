# Changelog

## 1.1.0

- Added automatic single-player death markers to the waypoint manager and minimap. Death detection and persistence still require an in-game playtest.

- Added a single-player waypoint manager with names, distance, visibility, deletion confirmation, and off-map edge markers. In-game verification remains pending.

- Added initial single-player waypoint creation and save/dimension-specific persistence. Management and multiplayer support remain unfinished and the feature requires in-game testing.

- Added an optional FPS and in-game clock readout, toggled with V.

## 1.0.0

- First release: surface minimap and coordinates HUD for Beta 1.7.3 with ModLoader. Waypoints and death markers remain planned.

- Reduced the compass label size and removed the redundant direction from the smaller coordinate readout.

- Set the default minimap size to 70 by 70 GUI pixels.

- Reduced the default minimap to 50 by 50 GUI pixels, with configurable sizes down to 40 pixels.

- Reduced the default minimap size to 80 pixels and hide the minimap and coordinates while F3 debug information is displayed.

- Moved the coordinates readout beneath the minimap and expanded the terrain sample grid for a more detailed, wider view.

### 0.3.0-dev

- Added a toggleable coordinates and facing HUD, interpolated map scrolling, and a black-outlined pixel player indicator. Sustained movement and keybind testing are still pending.

- Reworked minimap caching to avoid hash collisions and retain sampled columns while moving. Switched terrain to Beta's map colors and added a pixel-style directional marker.

### 0.2.0-dev

- Added a bounded surface minimap, facing marker, cardinal labels, keybindings and persistent settings. Initial development-client rendering and inventory visibility were observed; control and installed-build testing remain incomplete.

- Documented the proposed Beta 1.7.3 ModLoader and MCP development approach. No functional mod is available yet.
- Added a minimal ModLoader entry point and verified compilation, reobfuscation, and discovery on a RetroMCP development-client startup.
