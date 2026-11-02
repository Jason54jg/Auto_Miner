# Regression scenarios

Run from the repository root with Java 25 available:

```bash
bash tests/regression/run.sh
```

The runner reuses the Kotlin compiler, standard library and Gson bundled with the
repository's Gradle distribution. The first run may download that distribution
through the wrapper. Configure Java proxy and CA settings as usual if required
by your environment. No dependencies or configuration files are written into the
source tree: compiled classes and test configuration live in an isolated temporary
directory which is removed on exit. A failing assertion produces a nonzero exit.

The 47 scenarios compile and execute the actual mining, configuration, selection,
statistics, alert and client event registration sources against the small doubles
in `stubs/`. These doubles model the specific contracts required by each test:
interaction distance to the block bounding box, destruction stages, solid support,
fluid replacement and connection callbacks. The source files are not copied or
rewritten by the runner.

Coverage includes all six audit findings, ordinary mining controls, slow legitimate
mining, refused targets, deferred destruction, extended interaction reach, unsafe
floors, blocked landings, dimension changes and attempts to activate from a menu.
They also cover smart versus forward-only navigation, sidestepping, single-step
descents and configuration migration preserved when integrating into master.

These are logic regression tests, **not Minecraft integration tests**. The compiler
bundled with Gradle can differ from the project's Kotlin plugin. To check the real
Minecraft/Fabric signatures, run the normal version builds separately:

```bash
./gradlew :26.1.2:build :26.2:build :26.3:build
```

The custom scenarios do not run through Gradle's `test` task. Rendering, actual
server prediction/acknowledgment, sound playback, mouse handling and Mod Menu
must still be checked in a real game client.
