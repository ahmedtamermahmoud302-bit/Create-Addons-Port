# Upstream references

Create source reference: https://github.com/Creators-of-Create/Create/tree/mc1.20.1-6.0.8

Copyright (c) The Create Team / The Creators of Create. See CREATE-LICENSE for the upstream MIT code license and separate asset license. The settings-board geometry and kinetic behavior constants were adapted using this reference. Original selected model JSON and texture PNG files under src/main/resources/assets/create were obtained from the user-supplied Create 6.0.8 JAR.

Gradle wrapper files originated from CleanroomMC/CleanroomModTemplate and Gradle. The wrapper uses Gradle 9.8.1 with its published SHA-256 checksum. Cleanroom development configuration follows the current CleanroomModTemplate.

All other uploaded mod artifacts are reference inputs outside this checkout. Their metadata and SHA-256 hashes appear in docs/reference-inventory.json; their licenses are not replaced or unified by this project.

The train slice additionally uses the original Create standard bogey frame/wheel OBJ meshes, their material files and textures, bogey item model and straight track models/textures. Their upstream asset license is preserved in CREATE-LICENSE. The OBJ reader and rail cursor are new compatibility code. The 0.3.0 track adapter additionally ports the flat placement rules and Bezier segment transforms using the upstream source, and includes original diagonal, ascending, rail-segment and tie OBJ assets. Legacy single-block corners remain for save compatibility.

The Super Glue item uses the original Create item model and texture from the supplied JAR, under the same upstream asset license. The two-block bond graph and limited solid-block carriage assembly are new adapter code; the modern selection-box entity and full contraption assembler have not been ported.

The Wrench, Engineer's Goggles, Train Casing, Train Controls and Train Station use selected original model JSON, PNG textures and the assembly-screen atlas from Create 6.0.8. Model references and rendering are adapted for 1.12; the Wrench item merges original gear geometry into its static native model. These assets retain the separate upstream asset license.
