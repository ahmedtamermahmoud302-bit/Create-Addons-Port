# Upstream references

Create source reference: https://github.com/Creators-of-Create/Create/tree/mc1.20.1-6.0.8

Copyright (c) The Create Team / The Creators of Create. See CREATE-LICENSE for the upstream MIT code license and separate asset license. The settings-board geometry and kinetic behavior constants were adapted using this reference. Original selected model JSON and texture PNG files under src/main/resources/assets/create were obtained from the user-supplied Create 6.0.8 JAR.

Gradle wrapper files originated from CleanroomMC/CleanroomModTemplate and Gradle. The wrapper uses Gradle 9.8.1 with its published SHA-256 checksum. Cleanroom development configuration follows the current CleanroomModTemplate.

All other uploaded mod artifacts are reference inputs outside this checkout. Their metadata and SHA-256 hashes appear in docs/reference-inventory.json; their licenses are not replaced or unified by this project.

The train slice additionally uses the original Create standard bogey frame/wheel OBJ meshes, their material files and textures, bogey item model and straight track models/textures. Their upstream asset license is preserved in CREATE-LICENSE. The OBJ reader and rail cursor are new compatibility code. The 0.3.0 track adapter additionally ports the flat placement rules and Bezier segment transforms using the upstream source, and includes original diagonal, ascending, rail-segment and tie OBJ assets. Legacy single-block corners remain for save compatibility.

The Super Glue item uses the original Create item model and texture from the supplied JAR, under the same upstream asset license. Version 0.4.0 adapts the opposite-corner volume workflow with new compatibility code, retaining the older face-bond graph for saves. The full upstream selection entity and contraption assembler have not been ported.

The Wrench, Engineer's Goggles, Train Casing, Train Controls and Train Station use selected original model JSON, PNG textures and the assembly-screen atlas from Create 6.0.8. Model references and rendering are adapted for 1.12; the Wrench item merges original gear geometry into its static native model. These assets retain the separate upstream asset license.

Version 0.4.0 additionally includes selected original station flag and track-overlay models/textures, all 16 seat models/textures, connected Train Casing texture sheets, station/speed GUI icon/widget atlases, the Train Door item image used in the station GUI, and glue/checkerboard highlight textures. The station layouts, flag transforms, motor targeting, track hints and omnidirectional connected-texture lookup adapt the corresponding upstream source. Their upstream code and asset licenses remain as described in CREATE-LICENSE.

Version 0.5.0 additionally uses the original cogwheel, gearbox, hand crank, clutch, gearshift, chain drive, belt and large standard bogey models/textures. Resource identifiers and vanilla log textures are adapted for Minecraft 1.12. Belt geometry, transmission signs, train control/approach constants and generator tooltip layout adapt the corresponding upstream source.

The direction-indicator atlas `assets/create/textures/gui/placement_indicator.png` was extracted from Ponder-Forge-1.20.1-1.0.91, embedded in the user-supplied Create JAR. Ponder's embedded metadata declares MIT and credits simibubi, Pepper and zelophed. Project: https://github.com/Creators-of-Create/Ponder. No Ponder executable JAR is bundled.
