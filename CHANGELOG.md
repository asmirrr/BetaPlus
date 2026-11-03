# Changelog

## Unreleased

### 0.3.0-dev

- Added a toggleable coordinates and facing HUD, interpolated map scrolling, and a black-outlined pixel player indicator. Sustained movement and keybind testing are still pending.

- Reworked minimap caching to avoid hash collisions and retain sampled columns while moving. Switched terrain to Beta's map colors and added a pixel-style directional marker.

### 0.2.0-dev

- Added a bounded surface minimap, facing marker, cardinal labels, keybindings and persistent settings. Initial development-client rendering and inventory visibility were observed; control and installed-build testing remain incomplete.

- Documented the proposed Beta 1.7.3 ModLoader and MCP development approach. No functional mod is available yet.
- Added a minimal ModLoader entry point and verified compilation, reobfuscation, and discovery on a RetroMCP development-client startup.
