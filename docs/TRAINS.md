# Train construction (0.3.0 alpha)

Target: Create 6.0.8, adapted to Minecraft 1.12.2 / Cleanroom. Use the Creative inventory for the currently available content. This is a working assembly slice, not the full upstream train system.

## Build a train in the world

1. Place a straight run of Railway Track (`create:track`). Tracks face the direction you look, including 45-degree diagonals. Assembly requires horizontal north–south or east–west track.
2. Use the Train Station item on that straight track to select it. Place the station beside the line, within 16 blocks of the selected track.
3. Open the station and choose **Begin Assembly Mode**.
4. Use **Train Casing** on the track to place a bogey. Place one or two bogeys for this carriage, at least three blocks apart. Both must be on the selected straight line, within 24 blocks of the station's selected track.
5. Build the body above the bogeys using Train Casing or ordinary solid blocks. The block directly above each bogey attaches automatically. Connect the rest using Super Glue.
6. Add **Train Controls** to the glued body. Face them along the track; use the Wrench on their top to rotate them.
7. Return to the station and choose **Assemble Train**. The server validates the entire selection first, captures block states and glue bonds, removes the source blocks, and creates a persistent moving carriage. No prefab wooden deck is added.
8. Right-click the actual controls on the assembled carriage to drive. **Forward/Back** throttle, **Jump** brakes, and **Sneak** dismounts. Control orientation determines the forward direction. The carriage brakes when driver input stops arriving.
9. Stop on straight track near the original station, dismount, open the station, and choose **Disassemble Train**. The carriage returns to world blocks and its internal glue bonds are restored. Occupied or protected destinations refuse disassembly and retain the train.

Punching an assembled carriage does not delete it or drop a train item. Use the station to disassemble it. The old `train_carriage` item ID remains for existing saves, but is hidden and no longer spawns trains.

## Super Glue

The current glue adapter uses two adjacent-block clicks: select one block, then its face-adjacent neighbor. Repeat to join the body. It saves bonds per dimension. Select one block and Sneak-use its neighbor to remove that joint. Sneak-use also clears a pending selection. Survival glue has 99 uses.

This input is still different from Create's modern selection-volume interface. The original item model and texture are included.

## Track placement

Use an existing track with the Track item to select an endpoint, then use another track or a block surface for the other endpoint. The preview and server share the flat-track placement plan. Endpoint orientation is corrected toward the connection. Straight connections fill ordinary track blocks. Turns and S bends include straight extensions, with Create's minimum-size checks and standardization; hold Sprint to maximize the curve. Sneak-use clears the selection.

Curves use original Create rail/tie OBJ geometry, gauge, segment overlap and Bezier handle/spacing rules. The held/inventory Track model JSON and icon are copied from the supplied 6.0.8 JAR. This does not certify pixel-identical rendering on the older engine. See [track maths](TRACK-MATH.md).

## Current scope

- One carriage per station, one or two small bogeys, up to 256 solid block states in a 17 × 49 × 13 envelope. Bogeys and native controls are supported; inventories and machine tile entities are refused before capture.
- Two-bogey heading uses both rail positions; bogeys swivel separately through curves. Distance-based movement, forward/reverse, braking, save/reload and bounded chunk loading are implemented.
- Each carried block has its own placement/collision check. Client picking uses individual carried blocks so empty carriage space does not intercept station clicks. Collision between separate trains remains conservative.
- Existing 0.2.0 trains and block-corner track metadata remain readable.
- Coupled carriages, seats, schedules, signals, junction switching, train inventory/machine actors, derailment and full upstream contraption physics are not implemented. Walking on a moving train is not supported; drive by mounting its controls.
- Slope blocks remain readable and traversable by the rail adapter. The placement tool currently refuses slope-transition endpoints; banking, girders, paving and the complete upstream placement UI remain unported.
- Train add-ons, including Steam 'n' Rails, Bells & Whistles and Interiors, are not implemented yet. Uploaded JARs are references, not bundled modern binaries.
