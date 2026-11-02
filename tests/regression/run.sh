#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
# Bootstrap the repository-pinned Gradle distribution, then reuse its Kotlin compiler.
bash ./gradlew --version >/dev/null
gradle_version=$(sed -n 's/.*gradle-\(.*\)-bin.zip/\1/p' gradle/wrapper/gradle-wrapper.properties)
gradle_home=$(find "${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-${gradle_version}-bin" -mindepth 2 -maxdepth 2 -type d -name "gradle-${gradle_version}" -print -quit)
if [[ -z "$gradle_home" ]]; then
    echo "Gradle ${gradle_version} distribution not found" >&2
    exit 1
fi
gradle_lib="$gradle_home/lib"
stdlib=("$gradle_lib"/kotlin-stdlib-[0-9]*.jar)
gson=("$gradle_lib"/gson-[0-9]*.jar)
annotations=("$gradle_lib"/annotations-[0-9]*.jar)
classpath="${stdlib[0]}:${gson[0]}:${annotations[0]}"
run_dir=$(mktemp -d "${TMPDIR:-/tmp}/autominer-regression.XXXXXXXX")
trap 'rm -rf "$run_dir"' EXIT
mkdir -p "$run_dir/classes" "$run_dir/config"
java_cmd="${JAVA_HOME:+$JAVA_HOME/bin/}java"
"$java_cmd" -cp "$gradle_lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
    -no-stdlib -no-reflect -jvm-target 21 -classpath "$classpath" -d "$run_dir/classes" \
    tests/regression/stubs/*.kt tests/regression/Scenarios.kt \
    src/client/kotlin/com/jason54jg/autominer/AutoMiner.kt \
    src/main/kotlin/com/jason54jg/autominer/{AutoMinerModConfig,CustomOreSelection,ToolSelection,OreBlocks,MiningStats}.kt \
    src/client/kotlin/com/jason54jg/autominer/client/{AlertManager,ScreenFlash,AutoMinerModClient}.kt
"$java_cmd" -Dautominer.test.configDir="$run_dir/config" -cp "$run_dir/classes:$classpath" ScenariosKt
