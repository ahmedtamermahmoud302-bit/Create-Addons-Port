# Create — Cleanroom 1.12.2 port

Work in progress. One `create` mod/artifact is the packaging target, with add-on features grouped internally under their original names. This checkout implements kinetic transmissions and belts, Wrench and Engineer's Goggles, Bezier tracks, Super Glue volumes, and walkable world-built trains assembled/disassembled at a Train Station. It is not a complete or exact port of Create or its add-ons. See [train controls and limitations](docs/TRAINS.md).

Latest build: [create-0.5.0-alpha.jar](create-0.5.0-alpha.jar). See [0.5.0 validation](docs/VALIDATION-0.5.md). Replace the older Create port JAR in `mods`; keep only one version installed.

0.5.0 adds small/large Cogwheels, Belts, the Hand Crank, both Gearbox orientations, Clutch, Gearshift, Encased Chain Drive and Adjustable Chain Gearshift. It also adds large standard bogeys, carried-block collisions and placement, source-style train controls with hold-Space station approach, entity impacts and dimension track links. Goggles use the compact generator-capacity tooltip; shaft/motor placement follows view and adjacent shafts. See [kinetic parts](docs/KINETICS.md) and [remaining differences](docs/PARITY.md).

Reference: Create **6.0.8 for Minecraft 1.20.1**, upstream tag `mc1.20.1-6.0.8` / commit `1a1a9a2819b4f89f78caec41b55ed8cb222fa24b`. See [parity tracking](docs/PARITY.md) and the [38 uploaded reference artifacts](docs/REFERENCES.md).

## Development

Target: Cleanroom **0.7.4-alpha**, Minecraft **1.12.2**, Java **25**, Gradle **9.8.1**, Linux x86_64. CleanroomGradle 0.22.0 requires Java 25. The reobfuscation tool additionally requires a Java 8 toolchain; the bootstrap installs it alongside Java 25. The mod itself runs on Cleanroom with Java 25.

```bash
cd /workspace/Create-Addons-Port
bash scripts/bootstrap.sh
source /workspace/toolchains/activate.sh
gradle --no-daemon build
```

The bootstrap verifies pinned distribution checksums, installs tools outside the checkout, configures the supplied proxy, and retains TLS validation with the platform CA. It does not install any uploaded reference JAR onto the game classpath. It requires Python 3.12+, curl, and access to the declared dependency hosts.

To test the independent kinetic, railway, glue and connected-texture algorithms without resolving the Minecraft toolchain:

```bash
bash scripts/test-core.sh
```

For development launch (requires a graphical display for the client):

```bash
source /workspace/toolchains/activate.sh
gradle runClient
# Or: gradle runServer
```

The user explicitly accepted the Minecraft EULA for this development server during setup. Its acceptance is retained in ignored `run/server/eula.txt`; preserve that file. A different server requires its own acceptance workflow. The development server binds to 127.0.0.1.

The local dedicated-server smoke check uses generated RCON credentials from ignored server configuration, bound to loopback. It reserves positions `(-1..2, 8, 0)` in the test world; that area must be loaded (the prepared development world spawns at `0, 8, 0`). With the development server running:

```bash
python scripts/smoke-server.py
# After stopping and restarting the server:
python scripts/smoke-server.py --verify-persistence
python scripts/smoke-server.py --stop
```

## Trains and Super Glue

Use Railway Track, Train Station, Train Casing, Train Controls, Seats and Super Glue from Creative inventory. [The train guide](docs/TRAINS.md) explains how to build, assemble, sit, drive and disassemble a carriage. [Track maths](docs/TRACK-MATH.md) explains straight extensions, Bezier turns and half-circles. [Equipment](docs/EQUIPMENT.md) covers the Wrench, Engineer's Goggles and motor controls.

## Kinetic content

`create:creative_motor` and `create:shaft` are registered. The motor has six facing directions and a single output face; shafts connect axially. Default motor speed is 16 RPM; configurable range is -256 to +256. Look at the value on an active motor side and hold use to open the two-row speed board; release use to confirm. Shift snaps selection to 32-RPM milestones. Negative values use the clockwise row; positive values use the counterclockwise row. The settings board enforces a minimum magnitude of 1, matching the reference board; the internal motor setting also supports 0. The in-world value control remains usable without Goggles; kinetic RPM/stress statistics appear only while Goggles are worn.

Speed packets are applied by the server. Settings, per-face rotation and network state persist and synchronize. Cog ratios, gearbox signs, split clutch/gearshift shafts, connected chain drives and pulley belts are wired into the native network. See [transmission behavior and belt placement](docs/KINETICS.md). Processing machines, belt processing actors and inventories are still unported.

## Reference assets

`CREATE-LICENSE` preserves the upstream license overview. The selected original Create models, textures, and GUI atlas retain their upstream asset license (All Rights Reserved); the upstream code license is MIT. This project does not grant new rights to those assets. Uploaded third-party JARs are retained outside the checkout for reference and are not bundled into the output.
