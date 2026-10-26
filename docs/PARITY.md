# Create 6.0.8 parity tracking

The supplied Create 6.0.8 is the reference, with original add-on names grouped under one Create artifact as the eventual packaging target. Version 0.5.0 implements the areas below. It is not a complete port or a certification of exact visual/gameplay parity.

| Area | Implemented | Remaining differences |
| --- | --- | --- |
| Creative Motor | Default 16 RPM, ±256 limits, 16384 capacity per absolute RPM, six facing directions, one output face; settings persist and synchronize | Full upstream source arbitration and event-driven propagation |
| Speed control | Original board atlas, source row/milestone coordinates, hold/release confirmation, Shift snapping, active-side in-world value target and hover outline | Sound feedback, clipboard integration and every upstream value-box animation |
| Kinetic information | Compact Generator Stats / Kinetic Stress Capacity tooltip requires worn Goggles; original icon, vanilla border and source offset/fade; motor value remains usable without Goggles | Unported machine, network and fluid information; no certification of pixel identity |
| Shaft rendering | Original shaft/half-shaft geometry, RPM rotation and phase; explicitly restored block atlas, white color and lighting for the motor shaft | Exact old/new engine lighting, damage overlays and rendering performance |
| Transmission / stress | Native cogs (small/large/bevel), gearbox signs, crank, split clutches, gearshift and analog chain ratios; solver covers overload/overspeed/conflicts | Processing consumers and upstream destructive conflicts; full event-driven update behavior |
| Station | Original normal/assembly atlases and icon coordinates, names, flags, direction marker, highlighted bogey slots, saved door-mode selection, assemble/disassemble and obstruction refusal | Schedules, complete track graphs, moving doors and multiple carriages; manual hold-Space approach/stop is implemented |
| Train construction | Real world blocks and glued seats/controls transfer into a persistent carriage palette; one/two bogeys; restores blocks/glue at its station; punch protection; prototype inventory item removed | Arbitrary block entities, inventories, machine actors and full upstream contraption physics; carried block walking and bounded solid-block placement are implemented |
| Belts | Parallel-shaft connector, flat/45-degree/vertical lines, end pulleys, original model parts/scrolling textures, dropped-item and entity transport; selection-order-independent direction; other pulley shafts restore on break | Transported-stack inventory, processing, tunnels/funnels, middle pulleys, covers/dyes and exact incline collision |
| Train control | Standing/seated console activation, scroll throttle, source unpowered speeds/acceleration, original HUD atlases and hold-Space station assistance | Complete junction routing and all modern HUD animations |
| Bogeys | Standard small/large casing cycling and original rotating wheel/drive/piston/belt geometry | Add-on bogey styles, coupling and full upstream physics |
| Moving collision / visibility | Per-carried-block support, walker carrying around yaw turns, entity impacts, full-body render bounds and offscreen connected-track rendering; curve rail support | Bounding-box support for carried blocks, approximate slope support and complete source contraption collision/portal clipping |
| Dimension travel | Automatic existing Nether portals plus saved operator links for arbitrary registered IDs; palette/glue/driver/seated or standing travellers transfer | Whole-carriage teleport; progressive portal clipping, automatic destination construction and mod-specific portal APIs |
| Seats | All 16 original models/textures; stationary seats and seats carried by trains; nearby controls can be activated without leaving the seat | Multiple passengers and complete upstream passenger management |
| Train Casing | Original 8 × 8 connected-texture sheets and omnidirectional lookup; context from world neighbors or the carried block palette | No certification of all old-engine lighting/model combinations |
| Tracks | Source flat placement rules, original held/item assets and rail/tie meshes, Bezier segments, shared red/green hints, Sprint-key maximum curves, two-quarter-turn half-circle | Full slopes, banking, junction routing, paving, girders and all upstream placement UI details |
| Super Glue | Opposite-corner volumes, connectivity validation, green preview/persisted outlines and face texture, left-click removal; capture/restoration and legacy bond support | Full upstream selection entity, every block movement rule and arbitrary contraptions |
| Wrench / Goggles | Native rotation/pickup, preserved motor RPM, supported vanilla pickup targets, equip/swap/dispenser and original worn Goggles mesh | Wrench gear animation, clipboard and tools for unported machines |
| Add-ons | Metadata and linkage inventoried for all 38 uploaded artifacts | Feature implementation, integration and compatibility testing remain unfinished |

## Validation

See [0.5.0 results](VALIDATION-0.5.md), [train instructions](TRAINS.md), [track geometry](TRACK-MATH.md) and [equipment](EQUIPMENT.md). The build executes 84 independent tests; native client/server checks exercise the implemented interactions. Passing algorithm tests or matching resource bytes does not establish full Create/add-on parity.

[0.3.0](VALIDATION-0.3.md) is retained as a historical validation record. Its feature-gap descriptions apply to that version.

## Development launch notes

The Cleanroom/Foundation development console reports a JLine class-loader error and a Maven library-directory warning. Loopback RCON commands and graceful shutdown work. Check loaded mods, world readiness and functional assertions instead of relying only on the Gradle launch exit code. Keep `foundation.loadsall=true` and separate `build/classes/java/main` and `build/resources/main` directories: Foundation's development discovery pairs those names.

The cloud client uses a dummy Xorg display and llvmpipe. Disable the splash in ignored client configuration if the SDL splash thread races Foundation's transformer list. This software-rendered environment is not a performance benchmark.
