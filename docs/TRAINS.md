# Trains in 0.2.0-alpha

This build adds a first driveable train carriage to Minecraft 1.12.2 on Cleanroom 0.7.4-alpha and Java 25. It is a native entity with its own distance-based track movement, not a renamed minecart. It does not yet reproduce Create's full train system.

## Try it

1. In Creative mode, open Transportation and get **Railway Track** and **Train Carriage**. Or use `/give @p create:track 64` and `/give @p create:train_carriage`.
2. Lay track on solid support. Adjacent pieces connect into straight lines, corners and slopes. Leave room for the wide bogey and platform beside and above the track.
3. Use the Train Carriage item on a track to place a carriage, then right-click the carriage to ride it.
4. Use the Forward and Back keys (normally W/S) to accelerate or reverse. Reversing first brakes through zero. Release the throttle to coast; hold Jump (normally Space) to brake. Sneak (normally Shift) dismounts.
5. Left-click the carriage to remove it. Outside Creative it returns the carriage item.

The speed display appears while riding. The server accepts control packets only from that carriage's driver. Losing driver input triggers braking. Carriages stop at disconnected tracks, at solid obstructions and before another carriage; they do not use Create's collision or derailment system yet.

## Included

- Straight, curved and sloping block tracks.
- A predefined bogey/platform carriage using the supplied original Create frame, wheel and axle meshes and textures. Wheels rotate from distance travelled using Create's small-bogey wheel radius of 6.5/16 blocks.
- Acceleration, reversing, coasting and braking with server-owned movement and client position interpolation.
- Track position, direction, speed and wheel rotation saved with the entity.
- One bounded 3×3 Forge chunk ticket around each carriage so unattended trains can continue ticking, subject to the world's Forge chunk-loading limits.

Tracks and carriage items currently come from Creative inventory or commands; survival crafting and Create station assembly are not implemented.

## Differences still to port

The block-track adapter uses tight single-block arcs and vanilla-style rail placement. Create's multi-block Bezier curves, junction steering, track graph and track placement tools are not ported. The base platform is predefined and can carry a limited glued solid-block body as described below. Full contraption capture, connected carriages, stations/assembly GUI, coupling, schedules, signals, cargo, portals and Steam 'n' Rails/add-on integration remain unported. Models are adapted to the 1.12 renderer; exact visual parity is not claimed.

## Developer checks

`gradle --no-daemon build` runs kinetic, track/path, driving and original OBJ mesh tests.

With the prepared local development server running, `python scripts/smoke-trains.py` reserves x=6..20, y=7..11, z=6..22 and builds a test loop plus an end-of-track fixture. It tests native registration, travel around the loop, acceleration, coasting, reversing and stopping, and saves the stopped train. After restarting the server, `python scripts/smoke-trains.py --verify-persistence` checks the saved track cursor, speed and control state. It reads the ignored loopback RCON configuration without logging its password.

## Super Glue and a custom solid-block body

Get **Super Glue** from Creative Tools or `/give @p create:super_glue`. Use it on one block, then an adjacent block, to save a bond between their shared faces. Repeat to connect the body. To remove a bond, select one block normally, then Sneak-use its neighbour. Sneak-use without a selection clears the selection. The glue has the reference 99-use durability outside Creative.

For this first assembly path, build the body above a **straight** track with its first block directly above the track where you will use the Train Carriage item. Glue its ordinary solid blocks together, then use the Train Carriage item on that track. A connected body of up to 64 blocks moves into the carriage. The supported body envelope is 9 blocks wide, 13 long and 5 high above the selected track; leave clearance around it. Tile entities, inventories, unbreakable blocks and non-solid decorations are refused before transfer. There is no station GUI or full contraption capture yet.

The attached body moves and saves with the carriage. The initial rider sits above the body. Breaking a carriage outside Creative returns its carriage item and captured block items; station-based disassembly and proper seat/control blocks are future work. Bond data is saved per world dimension; player-broken blocks lose their bonds.

See [track construction and maths](TRACK-MATH.md) for straight lines, right-angle corners, slopes, and the remaining work for long Create-style Bezier curves.
