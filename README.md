# Create — Cleanroom 1.12.2 port

Work in progress. One `create` mod/artifact is the packaging target, with add-on features grouped internally under their original names. This checkout implements an initial kinetic foundation, Wrench and Engineer's Goggles, Bezier track placement, and world-built trains assembled/disassembled at a Train Station. It is not a complete or exact port of Create or its add-ons. See [train controls and limitations](docs/TRAINS.md).

Latest build: [create-0.3.0-alpha.jar](create-0.3.0-alpha.jar). See [0.3.0 validation](docs/VALIDATION-0.3.md). Replace the older Create port JAR in `mods`; keep only one version installed.

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

To test the independent kinetic math without resolving the Minecraft toolchain:

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

Use Railway Track, Train Station, Train Casing, Train Controls and Super Glue from Creative inventory. [The train guide](docs/TRAINS.md) explains how to build, assemble, drive and disassemble a carriage. [Track maths](docs/TRACK-MATH.md) explains straight extensions and Bezier turns. [Equipment](docs/EQUIPMENT.md) covers the Wrench and Engineer's Goggles.

## Initial kinetic content

`create:creative_motor` and `create:shaft` are registered. The motor has six facing directions and a single output face; shafts connect axially. Default motor speed is 16 RPM; configurable range is -256 to +256. Hold use on a motor's side to open the two-row speed board; release use to confirm. Shift snaps selection to 32-RPM milestones. Negative values use the clockwise row; positive values use the counterclockwise row. The settings board enforces a minimum magnitude of 1, matching the reference board; the internal motor setting also supports 0.

Speed packets are applied by the server. Tile settings and network state are saved and synchronized. The independent solver includes speed ratios, stress accounting, overload, overspeed, and conflicting-source detection. Only axial 1:1 connections are currently wired into the Minecraft blocks; ratio and stress-consumer tests do not mean cogwheels or processing machines exist.

## Reference assets

`CREATE-LICENSE` preserves the upstream license overview. The selected original Create models, textures, and GUI atlas retain their upstream asset license (All Rights Reserved); the upstream code license is MIT. This project does not grant new rights to those assets. Uploaded third-party JARs are retained outside the checkout for reference and are not bundled into the output.
