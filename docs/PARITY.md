# Exactness and remaining implementation

The acceptance target is the supplied Create 6.0.8, with original add-on names grouped in one mod. This is an initial implementation. No visual or gameplay parity certification has occurred.

| Area | Reference established | Implementation / missing behavior |
| --- | --- | --- |
| Creative motor | `CreativeMotorBlockEntity`: default 16, limits ±256; `AllBlocks`: 16384 capacity per RPM | Values implemented; six directions and one shaft output |
| Speed board | `KineticScrollValueBehaviour`: two rows, max 256, milestones 32, minimum magnitude 1 | Native GUI adapter uses the original atlas and coordinate formula; hold/release and Shift selection implemented; feedback sounds, fade animation, precise in-world value box targeting and clipboard support missing |
| Direction | `KineticBlockEntity.convertToDirection` flips negative-facing axes | Applied to generator RPM |
| Stress | `KineticNetwork`: sum capacity × absolute generated speed and impact × absolute theoretical speed | Math tested; no gameplay stress-consuming block yet; unloaded stress persistence and stress effects missing |
| Shaft rendering | `KineticBlockEntityRenderer`: render time × RPM × 0.3 degrees, positional 22.5-degree parity offset | Formula and original shaft/half-shaft models adapted to TESR; visual, lighting, damage overlay and performance parity unverified |
| Rotation propagation | Axial shafts, cog ratios, source arbitration, breakage, belts and gearboxes | Axial shafts only. Solver supports signed ratios but no cogwheel block is registered. Source conflict stops the prototype network; upstream arbitration and destructive conflict behavior are not yet ported. Periodic component discovery is an initial implementation, not upstream's event-driven propagation |
| Placement / collision | Source block-specific placement helpers and voxel shapes | Basic face placement and shaft bounds implemented; Wrench rotation and survival pickup now implemented for native blocks; motor placement preference and exact compound collision shapes remain incomplete |
| Trains | Create train graph, curved tracks, bogeys, assemblies, carriages, collision, controls, stations, schedules and signals | 0.3.0 adds station-based world-block assembly/disassembly, Train Casing bogeys, glued controls and body palettes, two-bogey curve alignment, punch protection, original casing/rail assets and flat Bezier track placement. Connected carriages, schedules, signals, seats, machine actors and complete track graphs remain unported; see TRAINS.md |
| Equipment | Source Wrench pickup/axis rotation, head-slot Goggles and kinetic readouts | Native Wrench and Goggles implemented for available blocks. Original model geometry/textures included; gear animation, all machine tooltips and exact HUD appearance remain incomplete |
| Train add-ons | Steam 'n' Rails, Bells & Whistles, Interiors and other declared integrations | Inspected metadata/linkage only; not implemented |
| Other add-ons | Uploaded mods have distinct dependencies and integration targets | Not ported; declared dependencies and Create linkage recorded in reference-inventory.json |
| Packaging | One Create mod; original add-on names retained as internal feature groups | Single mod entry point in place; no fake add-on entries or merged modern binaries |

## Required validation before claiming the kinetic slice works

1. Complete the Cleanroom Gradle build and run its JUnit suite.
2. Load a dedicated server to verify no client-only classes load on the server.
3. In a client, place motors in all six orientations and attach axial shafts. Verify only the output face transmits rotation, saves retain settings, and disconnecting or unloading a component stops or restores it correctly.
4. Compare positive and negative RPM, 1/16/32/256 settings, board layout, arrows, Shift snapping, release confirmation, rotation phase, lighting and model orientation against the supplied 1.20.1 reference.
5. Verify a second client receives changes and rejects out-of-range or unauthorized control packets.
6. Replace the listed approximations and extend validation to stress consumers, gears and other transmissions before claiming full kinetic parity.

Unit tests validate the independent algorithm only. They do not validate a Minecraft client, train functionality, add-on compatibility or exact Create source arbitration.

## Verified in this environment

- `gradle --no-daemon clean build`: passed, including reobfuscation and JUnit (11 executed, zero failed, zero skipped).
- Actual Cleanroom 0.7.4-alpha dedicated server: loaded the Create mod and a playable world under Java 25.
- `python scripts/smoke-server.py`: six assertions passed against live tile NBT through local RCON: two shafts at 16 RPM, no power from the back of the motor, two shafts at -32 RPM after reversal, and a stopped disconnected shaft.
- After a graceful stop and restart, `--verify-persistence` confirmed the motor retained -32 RPM and the disconnected shaft remained unpowered.
- The packaged JAR contains the Create mod entry point, metadata, selected original models/textures, and the speed-settings atlas. The Gradle wrapper also launches successfully with the installed tools.

The kinetic speed-settings GUI and all six motor orientations have **not** been compared in a graphical client. Train meshes, attached body rendering, mounting, driver packets and braking were checked in a real client for 0.2.0; this does not establish exact Create visual or gameplay parity.

The current Cleanroom/Foundation development launch reports a JLine console class-loader linkage error and a Maven library-directory layout warning. The game server and loopback RCON still run; functional commands and graceful shutdown through RCON were tested. The ordinary interactive development console is not verified usable. Keep the standard separate `build/classes/java/main` and `build/resources/main` output layout: Cleanroom's development discovery explicitly pairs those directory names; merging or renaming them can cause the mod to disappear from discovery.

## 0.2.0 train and glue validation

- Full Gradle build/reobfuscation passed with 35 JUnit tests executed, zero failures/errors/skips. The independent test runner also executed all 35.
- The native server loop fixture passed 13 assertions: four connected corner shapes, entity registration, route retention, acceleration, travel around multiple corners, coasting, reversing and end-of-track stopping. Seven saved cursor/control fields survived restart.
- A software-rendered Minecraft client connected to the local server. The original bogey meshes and axle textures loaded. A real player mounted and drove a carriage; vanilla vehicle prediction interference and the platform's legacy renderer rotation were found and corrected.
- Real Super Glue item clicks created two saved face bonds among three wooden blocks. After saving and restarting the server, using the Train Carriage item captured all three source blocks. The source positions became air and the entity held all three block states. The client rendered the attached body.
- The player mounted that glued carriage, drove it with Forward, and stopped it with Jump. Server velocity stayed within the rail speed limit. The three-block body and stopped location survived a later server restart.
- Original upstream license/asset notices are packaged in META-INF in the mod JAR.

These are historical 0.2.0 results. Station assembly and flat Bezier placement were added in 0.3.0; see the current guide and validation below. Connected carriages, schedules/signals, full contraption actors, add-ons, seats and exact visual parity remain incomplete. Glue still uses adjacent-block clicks rather than the modern selection-volume interface.

## 0.3.0 validation

See [the current validation record](VALIDATION-0.3.md) for the 50-test build and native station, curve, equipment and persistence checks. These supersede the historical 0.2.0 feature-gap list above, without establishing full Create/add-on parity.
