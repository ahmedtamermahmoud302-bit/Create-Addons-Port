# Kinetic parts (0.5.0 alpha)

Use Creative inventory for the available parts. These are native Cleanroom blocks, sharing one saved kinetic network. Original Create 6.0.8 models and selected textures are adapted for the 1.12 renderer. This is an incomplete port; crafting progression, processing machines and complete upstream placement assists are not included.

| Part | Behavior |
| --- | --- |
| Creative Motor | Six facing directions, one output face, default 16 RPM and ±256 range; original in-world speed setting and board |
| Shaft | Axial 1:1 connection; placement follows the look axis and compatible adjacent shafts |
| Cogwheel | Adjacent small gears reverse at 1:1 |
| Large Cogwheel | Diagonal large/small mesh changes speed by 2 or 1/2 and reverses; perpendicular large gears turn a corner |
| Hand Crank | Use to generate 32 RPM, Sneak-use for reverse; stops ten ticks after the last activation; 8 SU capacity per absolute RPM |
| Gearbox | Four shafts perpendicular to its excluded axis; face-dependent rotation signs |
| Vertical Gearbox | A separate placement item for a gearbox with a horizontal excluded axis; horizontal and vertical shafts connect |
| Clutch | Redstone disconnects its two shaft halves while the powered input can continue turning |
| Gearshift | Redstone reverses the output |
| Encased Chain Drive | Adjacent perpendicular chain cases share rotation, including cases with different shaft axes |
| Adjustable Chain Gearshift | Analog redstone changes the ratio relative to adjacent chain drives; power 0 gives 1:1, power 1 gives 1.125 and power 15 gives 2 |
| Belt | Two parallel shaft pulleys share rotation and carry dropped items and standing entities along a flat/inclined belt |

## Connect a belt

1. Place two parallel Shafts in the same perpendicular plane.
2. Use the Belt item on the first Shaft, then on the second. Endpoints must form a straight, vertical or 45-degree line, at most 20 blocks apart. Clear the full path first.
3. One Belt is consumed in Survival. The complete path is validated before conversion. Sneak-use cancels the first selection.
4. Power either pulley shaft. Ordinary item travel uses RPM / 480 blocks per tick. Positive rotation moves west on X-aligned belts and south on Z-aligned belts; reversing the selection order does not reverse movement.
5. Breaking a segment removes the belt and restores the other pulley shafts. Sneak-use the Wrench for pickup. An ordinary Wrench use leaves the assembled belt orientation intact.

Vertical and sideways belts transmit rotation but do not carry ordinary items. End pulleys expose shaft connections; intermediate belt segments do not create extra external shafts. Original start/middle/end, diagonal and pulley models are used, with scrolling belt textures. This version moves ordinary dropped item entities; it does not yet implement Create's transported-stack inventory, belt funnels/tunnels, dye/casing covers, middle pulleys or processing actors. Incline collision uses stepped support on the older engine.

## Network limits

The solver tracks exposed shaft faces separately, so disconnected clutch halves and reversing gearshift halves have distinct speeds. It handles ratios, overspeed, overload and conflicting sources. The adapter rebuilds components periodically, bounded to 4096 shaft faces; the complete upstream event-driven network and destructive conflicting-source behavior are unported. No processing stress consumers are registered yet. Capacity tests in the solver do not imply unported machines exist.

Native checks: with the loopback development server running and a player near `(130, 8, 4)` to load the fixture, run `python scripts/smoke-kinetics.py`. It reserves x=110..150, y=7..11, z=-2..12 and intentionally edits that area. See [validation](VALIDATION-0.5.md).
