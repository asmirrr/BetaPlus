# BetaPlus

BetaPlus is a planned client-side quality-of-life mod for Minecraft Java Edition Beta 1.7.3. Its first release is intended to include a minimap, waypoints, coordinates, death markers, and configurable controls.

**Status: 0.3 development preview.** A north-up surface minimap and coordinates HUD are implemented. They have been seen in a local single-player development client; control, sustained movement, performance, multiplayer, and installed-build testing remain incomplete. No release is available yet.

## Minimap and controls

The map displays sampled terrain using Beta's map colors and simple height shading, a pixel-style player arrow, and cardinal directions. The coordinates and facing readout uses Minecraft's font beneath the map (or above it when the map is at the bottom of the screen). It hides while an inventory or other screen is open or the HUD is hidden. The map uses a 64-by-64 sample grid; sampling is limited to 96 columns per world tick and only queries loaded chunks. A world-coordinate cache reuses samples as the player moves and clears on a world change. Newly revealed areas can take a little time to fill in.

Default controls are **M** to toggle the map, **=** to zoom in, **-** to zoom out, and **C** to toggle coordinates and facing. These are registered with ModLoader and can also appear in the game's control settings. Automated key testing has not yet confirmed their behavior.

Settings are created in the instance's `config/BetaPlus.properties`. Edit them with the game closed: `enabled` and `coordinates` are true or false; `size` is 64–160 GUI pixels; `zoom` is 1, 2, or 4 blocks per sample; `position` is `top-right`, `top-left`, `bottom-right`, or `bottom-left`; `opacity` is 0.2–1.0; `key` is an LWJGL 2 numeric key code (default 50, M). Changes to the file take effect on restart. Toggle and zoom changes are saved automatically.

## Installation

No tested installable release is available. Development builds produce the reobfuscated mod classes in `mcp/minecraft/reobf`; a normal-launcher installation still needs verification with the matching Beta 1.7.3 ModLoader before release.

## Development approach

The target is the original Beta 1.7.3 client using Risugami ModLoader and MCP-style mappings. The local smoke test used [RetroMCP-Java v1.2](https://github.com/MCPHackers/RetroMCP-Java/releases/tag/v1.2), an MCP rewrite supporting Beta 1.7.3, and the [Beta 1.7.3 ModLoader fix](https://github.com/coffeenotfound/ModloaderFix-b1.7.3). This is not MCP 4.3 itself. The game uses LWJGL 2 and legacy OpenGL; modern Minecraft APIs and modern Forge are not compatible. ModLoader and other jar mods can conflict if they modify the same game classes. Multiplayer support must be tested separately; the client can only display information the server sends it.

Java 8 is a practical starting point for running this legacy client. The original MCP scripts and compiler have their own historical requirements; confirm a compatible JDK and Python runtime with the particular MCP distribution before relying on a build. Do not distribute Minecraft's game jar, decompiled sources, ModLoader, or other third-party binaries in this repository.

## Environment setup

1. Obtain a legitimate Minecraft Java Edition installation and install or launch Beta 1.7.3 through a launcher that supports historical versions. Use a separate instance for testing.
2. Obtain MCP 4.3 and the matching Beta 1.7.3 ModLoader from trustworthy sources. Follow their licensing and distribution terms.
3. Set up MCP with the Beta client jar, matching LWJGL 2 libraries and platform natives. Install ModLoader into the development client before decompiling, following the instructions for that MCP distribution. Keep this environment outside the repository.
4. Verify the unmodified ModLoader development client launches before adding BetaPlus. MCP's recompile and reobfuscate steps produce the classes needed for a jar mod. The precise commands depend on the MCP distribution and platform.

Reference: [Beta 1.7.3 MCP setup](https://emberden.github.io/mcp.html) and [MCP 4.3 setup notes](https://gist.github.com/Benetekt/cec4bac99dd105a025111349c1b2a787). These are historical community instructions, not a verified BetaPlus build recipe.

### Current local smoke-test workspace

The ignored `mcp/` directory contains RetroMCP, an isolated Java 8 JDK, and a copy of the Beta client with the ModLoader fix applied. From PowerShell in that directory, copy all `../src/net/minecraft/src/*.java` into `minecraft/src/net/minecraft/src/`, then run:

```powershell
& .\jdk\jdk8u504-b01\bin\java.exe -jar .\downloads\RetroMCP-Java-CLI.jar recompile
& .\jdk\jdk8u504-b01\bin\java.exe -jar .\downloads\RetroMCP-Java-CLI.jar reobfuscate
```

For the development launch, copy `mod_BetaPlus.class` and both `BetaPlus*.class` files from `minecraft/bin/net/minecraft/src/` to `minecraft/game/mods/BetaPlus/net/minecraft/src/` and run `& .\jdk\jdk8u504-b01\bin\java.exe -jar .\downloads\RetroMCP-Java-CLI.jar start client`. The mods-folder copy is needed because ModLoader's development directory scan does not discover the class automatically. This local workspace is not a reproducible setup script or an installation package yet.

## Planned design

Terrain sampling will be bounded and cached, with unloaded chunks left unknown. Waypoints and settings will be stored locally, with separate world/server and dimension identities where these can be reliably determined. Death detection, world transitions, multiplayer identity, and OpenGL state restoration require validation against the actual Beta client before implementation claims can be made.

## Screenshots

No screenshots yet.

## Known issues

- The development smoke test used RetroMCP's development launcher; an installed, reobfuscated build has not yet been tested.
- Zoom and toggle controls, movement, turning, and performance still need thorough playtesting. Fast movement can leave temporary gaps while the cache catches up.
- The surface sampler is approximate: tall foliage, transparent blocks, caves and the Nether may not be represented accurately. Unloaded chunks remain blank.
- There is no tested installable build, configuration menu, or release yet.
- Compatibility with other mods and multiplayer is untested.
