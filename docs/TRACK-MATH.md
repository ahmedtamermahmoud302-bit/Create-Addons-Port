# Track geometry

Reference: Create 6.0.8 `TrackPlacement`, `BezierConnection.Runtime`, `SegmentAngles` and `TrackRenderer.getModelAngles`.

## Straight and diagonal track

Each ordinary track block has a path between its two boundary endpoints. Horizontal north–south/east–west paths have length 1; flat diagonal paths have length √2. The server advances its cursor by distance, then transfers the remainder to a matching endpoint. A missing connection stops the carriage. Legacy 0.2.0 tight block corners remain available for save compatibility; new turns use multi-block Bezier connections.

## Placement

For a flat connection, the selected axes are oriented toward each other. The tool intersects their horizontal lines to distinguish a turn from a straight line or S bend.

For turns, the source's minimum intersection distances are 7 for 90-degree turns and 3.25 for other allowed turns, with additional clearance for elevation. Extra distance becomes straight extensions on both ends, rounded down to block counts. Sprint keeps the largest available curve. Level S bends need a forward distance of at least twice their lateral offset (or 3 when the offset is at most 1); excess distance is split between the two straight extensions. Collinear level connections fill ordinary track rather than storing a curve.

Preview and placement share `TrackPlacement.Plan`. Placement validates permissions, loaded chunks, obstructions and survival inventory before changing the world. Curve specifications are saved in per-dimension `create_tracks` data; both endpoint tiles synchronize them for rendering.

## Cubic curves

With endpoints A/B and outward unit axes U/V, control points are C₁ = A + hU and C₂ = B + hV:

`P(t) = (1−t)³A + 3(1−t)²tC₁ + 3(1−t)t²C₂ + t³B`.

Create's circular handle rule is `h = r × 4/3 × tan(θ/4)`, where r comes from intersecting the perpendicular endpoint axes and θ is the angle between U and −V. For a quarter circle, `h/r ≈ 0.55228475`. Parallel-axis S bends use the source's special handle rule; the fallback is endpoint distance / 3.

The source estimates length with 16 samples, chooses `floor(length × 2)` render segments, and constructs its step lookup table. The port uses these rules for visual segmentation. It adds a separate 512-sample distance table for server movement; it does not use render segment number as train speed.

Rails are offset from the centerline by ±0.965 along the track normal. Original rail and tie OBJ parts receive the source's translations, yaw/pitch/roll, 2.1 overlap scaling and 2.2 end scaling. The item icon and item model are original assets, not a miniature world-track mesh.

## Carriage orientation

For a two-bogey carriage, the body follows the chord between bogey positions. A bounded search finds the forward rail distance whose chord equals the built bogey spacing. Each bogey renders with its own tangent relative to the body. At the end of the available route the carriage stops before its leading bogey loses support.

## Remaining differences

Full track graphs/junction routing, tilted endpoints, slope-transition placement, banking, girders, offhand paving and the complete upstream placement UI are not ported. The old engine's lighting and rendering have not been proven pixel-identical to 1.20.1.
