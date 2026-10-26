# Wrench and Engineer's Goggles

`create:wrench` and `create:goggles` are available in Creative inventory. The selected original 6.0.8 item textures and models are included.

## Wrench

Use the Wrench on an axial shaft or Creative Motor to rotate it around the clicked face's axis. A parallel axis stays unchanged. Motor speed settings survive rotation; neighboring kinetic networks refresh. Use the top or bottom of Train Controls, a Train Station, or a bogey to turn its horizontal facing.

Sneak-use picks up the implemented Create blocks and the available 1.12 vanilla equivalents of Create's wrench-pickup tag: rails, redstone components, pistons, hoppers, pressure plates and buttons. Survival returns drops to the inventory, with overflow dropped at the player. Picked-up motors retain their configured RPM in BlockEntityTag. Creative removes the block without drops. Protection checks and a cancellable Forge break event are respected.

Attacking a vanilla minecart with the Wrench removes it immediately, following the source behavior. Assembled trains must be disassembled at their station.

The Wrench model includes the original gear geometry; its custom animated gear rendering, clipboard integration, placement assists and interactions with unported blocks remain absent.

## Engineer's Goggles

Use to equip the head slot, swapping back the previous helmet. Goggles can also be placed in the head slot or equipped by a dispenser. They add no armor or durability. A model perspective switch selects the original 3D mesh for vanilla's head equipment layer, preserving the original head transform.

While worn, look at a shaft or motor for actual synchronized speed, capacity, stress and remaining capacity. Without worn Goggles, that information HUD is hidden. Hold Sneak for theoretical speed, network size/status and orientation. This is a native 1.12 HUD adapter, not a pixel-identical recreation of every modern Create tooltip. Only implemented kinetic blocks supply information; processing-machine and fluid readouts await those ports.

## Creative Motor controls

Look at an active side of a motor to see its in-world numeric setting and hover outline. Aim at that value and hold Use to open the original two-row speed board. Choose direction/speed with the pointer and release Use to confirm; hold Shift to snap to 32-RPM milestones. This is a setting control, so it is usable without Goggles. The information HUD still requires Goggles.

The rotating shaft binds the block atlas and restores white color/lighting explicitly, preventing glue/track overlay textures from coloring it green.
