# Train construction (0.5.0 alpha)

Target: Create 6.0.8, adapted to Minecraft 1.12.2 / Cleanroom. Use Creative inventory for the available content. This is a working carriage assembly implementation; full upstream contraption physics and the add-ons remain unfinished.

## Build a train in the world

1. Place a straight run of Railway Track (`create:track`). Tracks follow your look direction, including 45-degree diagonals. Assembly requires horizontal north–south or east–west track.
2. Use the Train Station item on that track to select it. Its marker previews the direction. Place the station beside the line, within 16 blocks of the selection.
3. Open the station and use its **Create Train** icon to enter the blue **Train Assembly** screen. The flag changes to the original assembly flag. Highlighted bogey positions extend in one direction along up to 48 continuous straight track blocks.
4. Use **Train Casing** on a highlighted track to create a bogey. The first bogey must be at the first marker, one block beyond the station's selected track. Add a second bogey at least three blocks farther along the highlighted line, if desired. Use Train Casing on the track beneath an existing bogey, or the adjacent rail, to switch its standard Small/Large size. Both sizes use their original bogey models; add-on bogey styles are not included.
5. Build the body above the bogeys with Train Casing or ordinary solid blocks. The block directly above each bogey attaches automatically. Connect the rest with Super Glue. Train Casing joins its textures across adjacent casing faces, both in the world and after assembly.
6. Add a **Seat** and **Train Controls** to the glued body. All 16 seat colors are available. Controls must face along the track; the Wrench can rotate them from the top or bottom. Put the seat near enough to use the controls while sitting.
7. Return to the station and choose **Assemble Train**. Validation happens before transfer. The server captures the block states and glue data, removes those world blocks, and creates a persistent moving carriage with those blocks. No prefab deck or inventory Train Carriage block is added.
8. Right-click the carried seat to sit. Right-click nearby Train Controls to start driving while remaining in that seat. **Forward/Back** accelerate/reverse, the mouse wheel adjusts the throttle limit, and **Sneak** releases controls/dismounts. **Jump** brakes, or, while a station approach prompt is visible, hold Jump to approach and stop at that station. Release Jump to cancel the approach. Escape also releases controls. Control orientation determines forward. A seated passenger who has not activated controls does not drive. Direct use of controls is also supported.
9. Stop with the whole carriage aligned on the original station's straight track, dismount, open the station, and choose **Disassemble Train**. The button is disabled while the carriage is moving, curved or misaligned. Disassembly snaps back to the station marker, restoring the original world blocks and glue volumes. Occupied or protected destinations refuse restoration and retain the train.

Punching an assembled carriage does not delete it or drop an item. Use its station to disassemble it. The prototype `train_carriage` inventory item is removed; existing item stacks migrate to Train Casing. The internal entity ID remains `create:train_carriage` for saved trains.

The normal station screen includes editable station/train names, a train indicator, original icon buttons, the door-control selector, and a rendered station with its flag. Door mode is saved, but moving door actors and schedules are not implemented. The schedule icon is disabled.

## Super Glue

Hold Super Glue, use the first block corner, then use the opposite corner of the desired volume. A green preview follows the selection. The opposite corner must be reachable through solid face-connected blocks inside that box, with endpoint distance below 25 blocks. Permissions are checked before saving; one new volume costs one durability in Survival. Sneak cancels the pending selection, including in the air. Changing away from the item also discards a pending selection.

While holding Glue, nearby saved volumes show their outlines and the original glue face texture. Point at a saved volume and left-click to remove it. Existing older face bonds remain readable and visible. Glue data is saved per dimension and transfers with station assembly/disassembly.

## Track placement

Use an existing track with the Track item to select an endpoint, then use another track or a block surface for the other endpoint. Green/red rail outlines and the checkerboard hint area share the server's geometry, obstruction, inventory and permission checks. Straight connections fill ordinary track blocks. Turns and S bends include straight extensions; hold the **Sprint key** to maximize the curve, even while standing still. Sneak-use clears the selection.

For a half-circle, place a perpendicular track halfway around the bend and make **two 90-degree connections**, holding Sprint when placing each. A single 180-degree connection is rejected by Create's placement rules. See [track maths and a coordinate example](TRACK-MATH.md).

The held/inventory Track model and icon, rail/tie meshes, gauge, segment overlap and Bezier handle rules use the supplied 6.0.8 reference. Rendering on the older engine has not been certified pixel-identical.

## Walking and building

Carried solid blocks provide collision on both client and server. A standing player follows the carriage around turns and can release controls to walk or jump on its deck. Right-click a carried solid block with an ordinary solid ItemBlock to add a block to the carriage; Survival consumes the stack. The palette edit and attachment bond persist and restore on disassembly. Blocks with tile entities, occupied/protected positions and placements outside the assembly envelope are refused. Arbitrary block interactions, removal and machine actors are not a complete contraption implementation.

Moving trains damage and knock back entities struck by their blocks above the source speed threshold. Riders and supported standing passengers are excluded, as are Creative/Spectator players and dropped items.

## Station approach

Right-click actual Train Controls while standing on the deck or sitting nearby. The source speed/throttle bar, direction atlas and station prompt replace the former text control panel. When a station ahead on the connected route is in range, **hold Space** (the default Jump key) to approach. The progress indicator ends with an arrival prompt and the train stops at the marker. Releasing Space cancels assistance; Forward/Back departs. This is manual station assistance, not an automatic schedule/signalling system.

## Dimension tracks

Default Nether travel looks for an existing vanilla portal immediately beyond a straight track endpoint and an existing outbound track beside its linked destination portal. Build clear, sufficiently long track on both sides. Portal generation and destination track construction are not automatic.

For installed custom dimensions, an operator can bind two straight track endpoints:

```text
/createportal link <fromDim> <x> <y> <z> <entryFacing> <toDim> <x> <y> <z> <exitFacing>
/createportal unlink <dim> <x> <y> <z> <entryFacing>
```

Facings are north/south/east/west. Entry points into the portal; exit points away from it. Dimensions must already be registered by Minecraft or an installed dimension mod. The binding saves both travel directions, validates track axes and is stored in overworld saved data. An unregistered dimension is refused.

Travel transfers the whole carriage and its palette, glue, seated riders and supported standing entities, retaining driver ownership. Destination track/body clearance is checked first. This implementation teleports the whole carriage at the boundary; Create's progressive carriage clipping and rendering across both dimensions remain unported. Custom mod-specific portal placement APIs are not integrated automatically.

## Current limits

- One carriage per station, one passenger, one or two standard small/large bogeys, and up to 256 solid blocks within a 17 × 49 × 13 envelope. Inventories and machine tile entities are refused before capture.
- Two-bogey heading follows both rail positions; bogeys swivel independently through turns. Forward/reverse, braking, save/reload and bounded 5 × 5 chunk tickets are implemented.
- Picking and server interaction validation use individual carried blocks, including controls reached from a seat. Empty carriage space does not intercept world interactions. Train-to-train collision remains conservative.
- Connected carriages, schedules/signals, automatic routing, junction switching, moving machine/door actors, derailment and the complete upstream contraption physics system remain unported. These trains are rail-constrained entities carrying actual block palettes, rather than the complete modern contraption physics engine.
- Legacy slopes remain readable and traversable. Slope-transition placement, banking, girders and paving remain unported.
- Steam 'n' Rails, Bells & Whistles, Interiors and the other referenced add-ons are not implemented. Reference JARs are not runtime dependencies or bundled modern binaries.
