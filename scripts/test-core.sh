#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source /workspace/toolchains/activate.sh
mkdir -p build/core-test-classes
javac -cp /workspace/toolchains/junit.jar -d build/core-test-classes \
    src/main/java/dev/createport/kinetics/KineticNetwork.java \
    src/main/java/dev/createport/equipment/WrenchRotation.java \
    src/main/java/dev/createport/equipment/KineticReadout.java \
    src/main/java/dev/createport/trains/TrackCurve.java \
    src/main/java/dev/createport/trains/TrackPlacement.java \
    src/main/java/dev/createport/trains/CasingCT.java \
    src/main/java/dev/createport/glue/GlueSelection.java \
    src/main/java/dev/createport/trains/RailPath.java \
    src/main/java/dev/createport/trains/RailNavigator.java \
    src/main/java/dev/createport/trains/TrainMotion.java \
    src/main/java/dev/createport/trains/ObjGeometry.java \
    src/main/java/dev/createport/glue/GlueGraph.java \
    src/test/java/dev/createport/kinetics/KineticNetworkTest.java \
    src/test/java/dev/createport/equipment/EquipmentMathTest.java \
    src/test/java/dev/createport/trains/TrackCurveTest.java \
    src/test/java/dev/createport/trains/TrackPlacementTest.java \
    src/test/java/dev/createport/trains/CasingCTTest.java \
    src/test/java/dev/createport/glue/GlueSelectionTest.java \
    src/test/java/dev/createport/trains/RailNavigatorTest.java \
    src/test/java/dev/createport/trains/TrainMotionTest.java \
    src/test/java/dev/createport/trains/ObjGeometryTest.java \
    src/test/java/dev/createport/glue/GlueGraphTest.java
java -jar /workspace/toolchains/junit.jar execute --class-path build/core-test-classes:src/main/resources --scan-class-path --fail-if-no-tests --disable-ansi-colors
