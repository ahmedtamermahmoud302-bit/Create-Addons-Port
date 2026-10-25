#!/usr/bin/env bash
set -euo pipefail
TOOLS=/workspace/toolchains
mkdir -p "$TOOLS"
fetch() {
    local url="$1" target="$2" algorithm="$3" expected="$4"
    if [[ ! -f "$target" ]]; then curl --fail --location --silent --show-error "$url" --output "$target.part"; mv "$target.part" "$target"; fi
    python3 - "$target" "$algorithm" "$expected" <<'VERIFY'
import hashlib,sys
with open(sys.argv[1],'rb') as stream: actual=hashlib.file_digest(stream,sys.argv[2]).hexdigest()
if actual!=sys.argv[3]: raise SystemExit('Checksum mismatch: '+sys.argv[1])
VERIFY
}
fetch 'https://services.gradle.org/distributions/gradle-9.8.1-bin.zip' "$TOOLS/gradle.zip" sha256 'dce76f55f8e251a3a1f130eb120f30b3d271de2b76c9b0729d316b5a1b6dc01f'
fetch 'https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_x64_linux_hotspot_25_36.tar.gz' "$TOOLS/jdk25.tar.gz" sha256 'ee04de95ab9da7287d40bd2173076ecc2a6dd662f007bedfc6eb0380c0ef90e8'
fetch 'https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u462-b08/OpenJDK8U-jdk_x64_linux_hotspot_8u462b08.tar.gz' "$TOOLS/jdk8.tar.gz" sha256 '5d64ae542b59a962b3caadadd346f4b1c3010879a28bb02d928326993de16e79'
fetch 'https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar' "$TOOLS/junit.jar" sha512 'd8ba605f8f223d94b6a5a82debd641da9d5755cd8d68178afba45e3146d574f7b70dcc9d08864ca0abc0379dba055a26e0cdbef6499b862fcb4e54f1448d6bb8'
python3 - <<'EXTRACT'
from pathlib import Path
import zipfile,tarfile
r=Path('/workspace/toolchains')
if not (r/'gradle-9.8.1/bin/gradle').exists():
    with zipfile.ZipFile(r/'gradle.zip') as z:z.extractall(r)
if not (r/'jdk8u462-b08/bin/java').exists():
    with tarfile.open(r/'jdk8.tar.gz') as t:t.extractall(r,filter='data')
if not (r/'jdk-25+36/bin/java').exists():
    with tarfile.open(r/'jdk25.tar.gz') as t:t.extractall(r,filter='data')
EXTRACT
chmod +x "$TOOLS/gradle-9.8.1/bin/gradle" "$TOOLS/jdk-25+36/bin/"* "$TOOLS/jdk8u462-b08/bin/"*
export JAVA_HOME="$TOOLS/jdk-25+36"
export GRADLE_USER_HOME=/workspace/.gradle
mkdir -p "$GRADLE_USER_HOME"
# Use the supplied egress proxy and its CA without disabling TLS validation.
python3 - <<'PROXY'
import os,urllib.parse,pathlib
proxy=os.environ.get('HTTPS_PROXY') or os.environ.get('https_proxy')
if proxy:
    u=urllib.parse.urlparse(proxy)
    if u.username or u.password:raise SystemExit('Credential-bearing proxy requires platform configuration')
    p=pathlib.Path('/workspace/.gradle/gradle.properties')
    old=p.read_text() if p.exists() else ''
    keys=('systemProp.https.proxyHost=','systemProp.https.proxyPort=','systemProp.http.proxyHost=','systemProp.http.proxyPort=')
    lines=[line for line in old.splitlines() if not line.startswith(keys)]
    for scheme in ('https','http'):
        lines += [f'systemProp.{scheme}.proxyHost={u.hostname}',f'systemProp.{scheme}.proxyPort={u.port or 80}']
    p.write_text('\n'.join(lines)+'\n')
PROXY
python3 - <<'JDK_PATHS'
from pathlib import Path
p=Path('/workspace/.gradle/gradle.properties')
lines=p.read_text().splitlines() if p.exists() else []
lines=[line for line in lines if not line.startswith('org.gradle.java.installations.paths=')]
lines.append('org.gradle.java.installations.paths=/workspace/toolchains/jdk-25+36,/workspace/toolchains/jdk8u462-b08')
p.write_text('\n'.join(lines)+'\n')
JDK_PATHS
if [[ -n "${CODEX_PROXY_CERT:-}" && -f "$CODEX_PROXY_CERT" ]]; then
    mkdir -p "$TOOLS/trust"
    if [[ ! -f "$TOOLS/trust/cacerts" ]]; then cp "$JAVA_HOME/lib/security/cacerts" "$TOOLS/trust/cacerts"; fi
    if ! "$JAVA_HOME/bin/keytool" -list -alias codex-egress -keystore "$TOOLS/trust/cacerts" -storepass changeit >/dev/null 2>&1; then
        "$JAVA_HOME/bin/keytool" -importcert -noprompt -trustcacerts -alias codex-egress -file "$CODEX_PROXY_CERT" -keystore "$TOOLS/trust/cacerts" -storepass changeit
    fi
fi
cat > "$TOOLS/activate.sh" <<'ACTIVATE'
export JAVA_HOME=/workspace/toolchains/jdk-25+36
export GRADLE_USER_HOME=/workspace/.gradle
export PATH="$JAVA_HOME/bin:/workspace/toolchains/gradle-9.8.1/bin:$PATH"
if [[ -f /workspace/toolchains/trust/cacerts ]]; then
    export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:+$JAVA_TOOL_OPTIONS }-Djavax.net.ssl.trustStore=/workspace/toolchains/trust/cacerts"
fi
ACTIVATE
printf '%s\n' 'Toolchain verified. Run: source /workspace/toolchains/activate.sh'
