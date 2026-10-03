# ChromiumClient 2.0 (logo: "cr") — Fabric, Minecraft 1.21.11

Custom ClickGUI, drag-and-drop HUD editor, 53 modules, JSON config (`config/chromiumclient.json`).

## Build
Requires JDK 21.
Safest route: generate a fresh 1.21.11 template at https://fabricmc.net/develop
(Yarn mappings), then copy this project's `src/`, `gradle.properties` version lines
and `build.gradle` over it. Then:

    ./gradlew runClient     # dev client
    ./gradlew build         # jar in build/libs/

## Controls
Right Shift = ClickGUI (left click toggle, right click settings, drag headers, scroll)
Right Ctrl  = HUD editor (drag, right-click resets, ESC saves)
C (hold)    = Zoom        |  Screenshot folder key = unbound by default

## Modules (53)
PvP (16): Keystrokes, CPS Counter, Armor HUD, Potion Effects, Durability HUD, Totem Counter,
  Item Counter, Attack Indicator, Attack Cooldown, Mace Cooldown, Crosshair Customizer,
  Hit Color, Hit Sound, Low Fire, Low Shield, Auto Sprint
Info (12): FPS Counter, Ping Display, Coordinates, Direction, Speedometer, Session Time,
  Memory Usage, Clock, Biome Display, Player Counter, Server Address, Custom HUD Editor
Performance (10): Entity Culling, Block Entity Culling, Particle Optimization, Dynamic FPS,
  Fast Rendering, Fast Chunk Loading, Animation Optimization, Weather Optimization,
  FPS Limiter, Performance Profiler
Visual (8): Fullbright, Zoom, Weather Changer, No Vignette, No Pumpkin Overlay,
  No Portal Overlay, Custom Block Outline, Custom Sky
Utility (4): Auto Respawn, Screenshot Manager, Chat Timestamps, Chat Notifications

## Version-sensitive code (1.21.9+ reworked rendering/input)
All mixins use `require = 0`, so a wrong target is skipped instead of crashing the game.
Modules that depend on mixins: No Vignette / Pumpkin / Portal, Zoom, Low Shield, Low Fire,
Custom Sky, Custom Block Outline, Entity Culling, Block Entity Culling,
Animation Optimization, Chat Timestamps, Chat Notifications (timestamps/mentions on player chat).
If one does nothing, check its method name/descriptor with the Yarn 1.21.11 docs.
Everything else uses plain Fabric API / GLFW polling and does not depend on mixins.

## Extending
Add a class extending `Module` in one of the `*Modules` files and register it in `ModuleManager`.
