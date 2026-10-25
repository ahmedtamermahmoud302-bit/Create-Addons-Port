#!/usr/bin/env bash
set -euo pipefail
# Optional software display for cloud client checks. Keep this process running while using DISPLAY=:99.
mkdir -p /workspace/toolchains
cat > /workspace/toolchains/create-xorg.conf <<'CONFIG'
Section "ServerFlags"
    Option "AutoAddDevices" "false"
    Option "AutoAddGPU" "false"
    Option "AllowMouseOpenFail" "true"
EndSection
Section "Device"
    Identifier "DummyDevice"
    Driver "dummy"
    VideoRam 256000
EndSection
Section "Monitor"
    Identifier "DummyMonitor"
    HorizSync 5.0-1000.0
    VertRefresh 5.0-200.0
    Modeline "1280x720" 74.50 1280 1344 1472 1664 720 723 728 748 -hsync +vsync
EndSection
Section "Screen"
    Identifier "DummyScreen"
    Device "DummyDevice"
    Monitor "DummyMonitor"
    DefaultDepth 24
    SubSection "Display"
        Depth 24
        Modes "1280x720"
    EndSubSection
EndSection
CONFIG
exec /usr/lib/xorg/Xorg :99 -config /workspace/toolchains/create-xorg.conf -logfile /workspace/toolchains/create-Xorg.log -nolisten tcp -noreset
