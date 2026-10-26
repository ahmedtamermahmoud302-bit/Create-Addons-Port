# Create 6.0.8 parity tracking

The supplied Create 6.0.8 is the reference, with original add-on names grouped under one Create artifact as the eventual packaging target. Version 0.4.0 implements the areas below. It is not a complete port or a certification of exact visual/gameplay parity.

| Area | Implemented | Remaining differences |
| --- | --- | --- |
| Creative Motor | Default 16 RPM, ±256 limits, 16384 capacity per absolute RPM, six facing directions, one output face; settings persist and synchronize | Full upstream source arbitration and event-driven propagation |
| Speed control | Original board atlas, source row/milestone coordinates, hold/release confirmation, Shift snapping, active-side in-world value target and hover outline | Sound feedback, clipboard integration and every upstream value-box animation |
| Kinetic information | RPM, capacity and stress HUD requires worn Engineer's Goggles; motor value setting remains usable without Goggles | Complete modern tooltip styling and unported machine information |
| Shaft rendering | Original shaft/half-shaft geometry, RPM rotation and phase; explicitly restored block atlas, white color and lighting for the motor shaft | Exact old/new engine lighting, damage overlays and rendering performance |
| Transmission / stress | Axial 1:1 gameplay connections; independent solver covers signed ratios, capacity, load, overload and conflicts | Cogwheels, gearboxes, belts, processing consumers and upstream destructive conflict behavior |
| Station | Original normal/assembly atlases and icon coordinates, names, flags, direction marker, highlighted bogey slots, saved door-mode selection, assemble/disassemble and obstruction refusal | Schedules, complete track graphs, automatic arrivals, moving doors and multiple carriages |
| Train construction | Real world blocks and glued seats/controls transfer into a persistent carriage palette; one/two bogeys; restores blocks/glue at its station; punch protection; prototype inventory item removed | Arbitrary block entities, inventories, machine actors, walking on trains and full upstream contraption physics |
| Seats | All 16 original models/textures; stationary seats and seats carried by trains; nearby controls can be activated without leaving the seat | Multiple passengers and complete upstream passenger management |
| Train Casing | Original 8 × 8 connected-texture sheets and omnidirectional lookup; context from world neighbors or the carried block palette | No certification of all old-engine lighting/model combinations |
| Tracks | Source flat placement rules, original held/item assets and rail/tie meshes, Bezier segments, shared red/green hints, Sprint-key maximum curves, two-quarter-turn half-circle | Full slopes, banking, junction routing, paving, girders and all upstream placement UI details |
| Super Glue | Opposite-corner volumes, connectivity validation, green preview/persisted outlines and face texture, left-click removal; capture/restoration and legacy bond support | Full upstream selection entity, every block movement rule and arbitrary contraptions |
| Wrench / Goggles | Native rotation/pickup, preserved motor RPM, supported vanilla pickup targets, equip/swap/dispenser and original worn Goggles mesh | Wrench gear animation, clipboard and tools for unported machines |
| Add-ons | Metadata and linkage inventoried for all 38 uploaded artifacts | Feature implementation, integration and compatibility testing remain unfinished |

## Validation

See [0.4.0 results and screenshots](VALIDATION-0.4.md), [train instructions](TRAINS.md), [track geometry](TRACK-MATH.md) and [equipment](EQUIPMENT.md). The build executes 58 independent tests; native client/server checks exercise the implemented interactions. Passing algorithm tests or matching resource bytes does not establish full Create/add-on parity.

[0.3.0](VALIDATION-0.3.md) is retained as a historical validation record. Its feature-gap descriptions apply to that version.

## Development launch notes

The Cleanroom/Foundation development console reports a JLine class-loader error and a Maven library-directory warning. Loopback RCON commands and graceful shutdown work. Check loaded mods, world readiness and functional assertions instead of relying only on the Gradle launch exit code. Keep `foundation.loadsall=true` and separate `build/classes/java/main` and `build/resources/main` directories: Foundation's development discovery pairs those names.

The cloud client uses a dummy Xorg display and llvmpipe. Disable the splash in ignored client configuration if the SDL splash thread races Foundation's transformer list. This software-rendered environment is not a performance benchmark.
