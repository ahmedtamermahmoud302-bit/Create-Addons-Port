# Uploaded reference inventory

Metadata and bytecode linkage inspection only. These are reference inputs, not bundled runtime dependencies.

| Uploaded artifact | Loader | Classes referencing Create |
| --- | --- | --- |
| AdChimneys-1.12.2-3.5.16.0-build.0654.jar | Forge 1.12-era | 0 |
| AdChimneys-1.20.1-10.1.34.0-build.2374.jar | Forge modern | 0 |
| AdPother-1.12.2-1.2.25.0-build.0673.jar | Forge 1.12-era | 0 |
| AdPother-1.20.1-8.1.52.0-build.2374.jar | Forge modern | 1 |
| architectury-9.2.14-forge.jar | Forge modern | 0 |
| bellsandwhistles-0.4.5-1.20.x-Create6.0+.jar | Forge modern | 7 |
| copycats-3.0.10+mc.1.20.1-forge.jar | Forge modern | 142 |
| create-1.20.1-6.0.8.jar | Forge modern | 0 |
| create-confectionery1.20.1_v1.1.3.jar | Forge modern | 0 |
| create-new-age-1.2.0+forge-mc1.20.1.jar | Forge modern | 106 |
| create-stuff-additions1.20.1_v2.1.2.jar | Forge modern | 7 |
| create_bb-1.0.8-1.20.1.jar | Forge modern | 304 |
| create_central_kitchen-1.20.1-for-create-6.0.8-1.5.1.jar | Forge modern | 113 |
| create_enchantment_industry-1.4.1-for-create-6.0.8.jar | Forge modern | 65 |
| create_radar-0.4.6+mc1.20.1.jar | Forge modern | 66 |
| createaddition-1.20.1-1.3.3.jar | Forge modern | 91 |
| createbigcannons-5.11.4-mc.1.20.1-forge.jar | Forge modern | 313 |
| createdieselgenerators-1.20.1-1.3.12.jar | Forge modern | 167 |
| createoreexcavation-1.20-1.6.6.jar | Forge modern | 39 |
| ForgeEndertech-1.12.2-4.5.6.1-build.0648.jar | Forge 1.12-era | 0 |
| ForgeEndertech-1.20.1-11.1.11.2-build.2374.jar | Forge modern | 0 |
| Forgelin-Continuous-2.4.20.0.jar | Forge 1.12-era | 0 |
| HadEnoughItems_1.12.2-4.36.1.jar | Forge 1.12-era | 0 |
| interiors-1.20.1-forge-0.6.0.jar | Forge modern | 12 |
| jei-1.20.1-forge-15.56.0.205.jar | Forge modern | 0 |
| kotlinforforge-4.12.0-all.jar | Other/embedded metadata | 0 |
| markdown_manual-MC1.20.1-forge-1.2.5+c3f0b88.jar | Forge modern | 0 |
| oc2r-1.20.1-forge-2.2.13-all.jar | Forge modern | 0 |
| packetfixer-3.3.2-1.18-1.20.4-merged.jar | Forge modern | 0 |
| pollutionofcreate-1.20.1-0.0.4.jar | Forge modern | 3 |
| PollutionPlus-1.12.2-3.2.1.jar | Forge 1.12-era | 0 |
| pollutionplus-1.20.1-3.0.1.0.jar | Forge modern | 0 |
| ritchiesprojectilelib-2.1.1-mc.1.20.1-forge.jar | Forge modern | 0 |
| ScalableCatsForce-3.3.1-build-0-with-library.jar | Other/embedded metadata | 0 |
| sliceanddice-forge-3.6.0.jar | Forge modern | 27 |
| Steam_Rails-1.7.3+forge-mc1.20.1.jar | Forge modern | 480 |
| tfmg-1.0.2f.jar | Forge modern | 417 |
| trading_floor-forge-1.20.1-2.0.5.jar | Forge modern | 29 |

See `reference-inventory.json` for hashes, declared dependencies, license metadata, and linkage class names. Linkage does not establish behavioral compatibility. Existing 1.12.2 versions are candidates for external integration; modern versions cannot be placed on the 1.12.2 runtime classpath.

## Packaging target

One `create` artifact and mod identity, with add-on code organized in internal modules preserving each original display name. Only the kinetic foundation exists now. No placeholder add-on mod identities are registered. Dependency/library mods in this inventory are not all Create add-ons.
