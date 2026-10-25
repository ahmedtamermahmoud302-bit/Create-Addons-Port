# Straight track, turns and slopes

## Building in this alpha

Place Railway Track on solid blocks. Pieces next to each other align automatically into a straight run. Connect a run at a right angle to form a corner. Raise the next supported piece by one block for a slope. The current placement adapter uses Minecraft's block rail shapes; it does not yet have Create's two-endpoint placement tool or long multi-block Bezier connections.

## The implemented paths

A straight section interpolates between endpoints A and B:

`P(t) = A + t (B − A)`, with `0 ≤ t ≤ 1`.

A slope uses the same equation with different endpoint heights. Its physical length is `sqrt(dx² + dy² + dz²)`.

The initial right-angle block corner is a quarter circle:

`x(t) = cx + r cos(θ0 + Δθ t)`

`z(t) = cz + r sin(θ0 + Δθ t)`

Here `r = 0.5` blocks and `Δθ = ±π/2`. Its length is `r × |Δθ|`. These are tight block corners, not the large-radius turns in modern Create.

`RailPath.java` supplies position, normalized tangent and length. `RailNavigator.java` spends a requested travel distance along the current section, carries any remainder into its connected neighbour, and preserves the carriage's forward orientation when reversing. Missing or disconnected neighbours stop it at the endpoint. A higher endpoint searches neighbouring track one block above so slopes join correctly.

Heading follows the track tangent: `yaw = atan2(−dx, dz)`. Wheel rotation follows distance: `Δangle = −360 × distance / (2π × wheelRadius)`. The small bogey uses the reference radius `6.5/16` blocks.

## Create-style long smooth turns

A full placement tool connects two selected endpoints and their direction tangents using a cubic Bezier curve:

`B(t) = (1−t)³ A + 3(1−t)²t C1 + 3(1−t)t² C2 + t³ B`

`C1 = A + h1 × startTangent`

`C2 = B − h2 × endTangent`

The handles h1 and h2 control curvature while the tangents make the curve meet the straight pieces smoothly. For even train speed, a graph edge needs an arc-length table: sample the curve, accumulate travelled distance, then invert that table to find t for each requested distance. Advancing by a constant t would make speed vary through the turn.

Those multi-block Bezier edges, their placement tool, junction choices and two-bogey carriage alignment remain porting work. They are not included merely because this alpha has a working single-block curve adapter.
