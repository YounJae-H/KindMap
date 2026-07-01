# Convenience Fabric Mod Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a client-only Minecraft 26.1.2 Fabric mod with persistent gamma toggle and user-created macro keybinds.

**Architecture:** Create a small Fabric client project with separate config, gamma, macro, and UI packages. Keep gamma and macro logic testable through plain Java adapters, then wire them into Fabric client events and Mod Menu.

**Tech Stack:** Java 25, Gradle 9.6.1 wrapper, Fabric Loom 1.17.13, Fabric Loader 0.19.3, Fabric API 0.154.0+26.1.2, Mojang official mappings, Mod Menu 18.0.0-beta.1, Cloth Config 26.1.154+fabric, Gson, JUnit 5.

---

## File Structure

- Create `settings.gradle`: plugin management, project name, Foojay Java toolchain resolver.
- Create `build.gradle`: Fabric Loom build, dependencies, Java 25 toolchain, test setup.
- Create `gradle.properties`: version constants and JVM flags.
- Create `.gitignore`: Gradle/build/run outputs.
- Create `src/main/resources/fabric.mod.json`: client entrypoint and optional Mod Menu entrypoint.
- Create `src/main/resources/assets/convenience_tools/lang/en_us.json`: user-facing labels.
- Create `src/main/java/com/lama/conveniencetools/ConvenienceToolsClient.java`: Fabric client entrypoint.
- Create `src/main/java/com/lama/conveniencetools/ModConstants.java`: shared mod id/name.
- Create `src/main/java/com/lama/conveniencetools/config/ModConfig.java`: root config model.
- Create `src/main/java/com/lama/conveniencetools/config/GammaConfig.java`: gamma config model.
- Create `src/main/java/com/lama/conveniencetools/config/MacroConfig.java`: macro config model.
- Create `src/main/java/com/lama/conveniencetools/config/ConfigManager.java`: JSON load/validate/save.
- Create `src/main/java/com/lama/conveniencetools/gamma/BrightnessAccess.java`: testable brightness adapter.
- Create `src/main/java/com/lama/conveniencetools/gamma/MinecraftBrightnessAccess.java`: Minecraft brightness implementation.
- Create `src/main/java/com/lama/conveniencetools/gamma/GammaController.java`: gamma state machine.
- Create `src/main/java/com/lama/conveniencetools/macro/MacroAction.java`: macro action enum.
- Create `src/main/java/com/lama/conveniencetools/macro/MacroMode.java`: macro mode enum.
- Create `src/main/java/com/lama/conveniencetools/macro/ChatExecutor.java`: testable chat/command adapter.
- Create `src/main/java/com/lama/conveniencetools/macro/MinecraftChatExecutor.java`: Minecraft chat implementation.
- Create `src/main/java/com/lama/conveniencetools/macro/MacroManager.java`: macro key matching and scheduling.
- Create `src/main/java/com/lama/conveniencetools/ui/ModMenuIntegration.java`: Mod Menu config hook.
- Create `src/main/java/com/lama/conveniencetools/ui/ConvenienceConfigScreen.java`: main settings screen.
- Create `src/main/java/com/lama/conveniencetools/ui/MacroEditScreen.java`: macro add/edit screen.
- Create `src/test/java/com/lama/conveniencetools/config/ConfigManagerTest.java`: config tests.
- Create `src/test/java/com/lama/conveniencetools/gamma/GammaControllerTest.java`: gamma tests.
- Create `src/test/java/com/lama/conveniencetools/macro/MacroManagerTest.java`: macro tests.

## Task 1: Bootstrap Fabric Project

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `.gitignore`
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/resources/assets/convenience_tools/lang/en_us.json`

- [ ] **Step 1: Create Gradle settings**

Create `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        maven { url = 'https://maven.fabricmc.net/' }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '1.0.0'
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = 'https://maven.fabricmc.net/' }
        maven { url = 'https://maven.terraformersmc.com/releases/' }
        maven { url = 'https://maven.shedaniel.me/' }
        mavenCentral()
    }
}

rootProject.name = 'convenience-tools'
```

- [ ] **Step 2: Create Gradle properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

minecraft_version=26.1.2
loader_version=0.19.3
fabric_version=0.154.0+26.1.2
loom_version=1.17.13

mod_version=0.1.0
maven_group=com.lama
archives_base_name=convenience-tools

modmenu_version=18.0.0-beta.1
cloth_config_version=26.1.154
```

- [ ] **Step 3: Create build script**

Create `build.gradle`:

```groovy
plugins {
    id 'fabric-loom' version "${loom_version}"
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        convenience_tools {
            sourceSet sourceSets.client
        }
    }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"

    modImplementation "com.terraformersmc:modmenu:${project.modmenu_version}"
    modImplementation "me.shedaniel.cloth:cloth-config-fabric:${project.cloth_config_version}+fabric"

    testImplementation platform('org.junit:junit-bom:5.13.4')
    testImplementation 'org.junit.jupiter:junit-jupiter'
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
    options.release = 25
}

test {
    useJUnitPlatform()
}

processResources {
    inputs.property 'version', project.version
    filesMatching('fabric.mod.json') {
        expand 'version': project.version
    }
}
```

- [ ] **Step 4: Create ignore file**

Create `.gitignore`:

```gitignore
.gradle/
build/
run/
out/
.idea/
*.iml
```

- [ ] **Step 5: Create Fabric metadata**

Create `src/main/resources/fabric.mod.json`:

```json
{
  "schemaVersion": 1,
  "id": "convenience_tools",
  "version": "${version}",
  "name": "Convenience Tools",
  "description": "Client-side gamma and macro keybind tools.",
  "authors": ["lama0"],
  "contact": {},
  "license": "MIT",
  "icon": "assets/convenience_tools/icon.png",
  "environment": "client",
  "entrypoints": {
    "client": [
      "com.lama.conveniencetools.ConvenienceToolsClient"
    ],
    "modmenu": [
      "com.lama.conveniencetools.ui.ModMenuIntegration"
    ]
  },
  "depends": {
    "fabricloader": ">=0.19.3",
    "minecraft": "26.1.2",
    "fabric-api": ">=0.154.0+26.1.2",
    "java": ">=25"
  },
  "suggests": {
    "modmenu": "*",
    "cloth-config": "*"
  }
}
```

- [ ] **Step 6: Create language labels**

Create `src/main/resources/assets/convenience_tools/lang/en_us.json`:

```json
{
  "key.categories.convenience_tools": "Convenience Tools",
  "key.convenience_tools.toggle_gamma": "Toggle Gamma",
  "text.convenience_tools.title": "Convenience Tools",
  "text.convenience_tools.gamma": "Gamma",
  "text.convenience_tools.macros": "Macros",
  "text.convenience_tools.gamma.enabled": "Gamma Enabled",
  "text.convenience_tools.gamma.enabled_value": "Enabled Gamma",
  "text.convenience_tools.add_macro": "Add Macro",
  "text.convenience_tools.edit_macro": "Edit Macro",
  "text.convenience_tools.macro.name": "Name",
  "text.convenience_tools.macro.key": "Key",
  "text.convenience_tools.macro.content": "Content",
  "text.convenience_tools.macro.action": "Action",
  "text.convenience_tools.macro.mode": "Mode"
}
```

- [ ] **Step 7: Generate Gradle wrapper**

Run:

```powershell
$zip = "$pwd\.gradle-bootstrap\gradle-9.6.1-bin.zip"
New-Item -ItemType Directory -Force .gradle-bootstrap | Out-Null
Invoke-WebRequest https://services.gradle.org/distributions/gradle-9.6.1-bin.zip -OutFile $zip
Expand-Archive $zip .gradle-bootstrap -Force
.\.gradle-bootstrap\gradle-9.6.1\bin\gradle.bat wrapper --gradle-version 9.6.1
```

Expected: `BUILD SUCCESSFUL` and new `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`.

- [ ] **Step 8: Run initial Gradle tasks**

Run:

```powershell
.\gradlew.bat tasks --no-daemon
```

Expected: Gradle downloads Fabric Loom and, when Java 25 is not installed locally, downloads a Java 25 toolchain through Foojay. The command then prints available Gradle tasks.

- [ ] **Step 9: Commit bootstrap**

Run:

```powershell
git add .gitignore settings.gradle build.gradle gradle.properties gradlew gradlew.bat gradle/wrapper src/main/resources
git commit -m "chore: bootstrap fabric mod project"
```

Expected: commit succeeds.

## Task 2: Config Model And Persistence

**Files:**
- Create: `src/main/java/com/lama/conveniencetools/ModConstants.java`
- Create: `src/main/java/com/lama/conveniencetools/config/GammaConfig.java`
- Create: `src/main/java/com/lama/conveniencetools/config/MacroConfig.java`
- Create: `src/main/java/com/lama/conveniencetools/config/ModConfig.java`
- Create: `src/main/java/com/lama/conveniencetools/config/ConfigManager.java`
- Create: `src/test/java/com/lama/conveniencetools/config/ConfigManagerTest.java`

- [ ] **Step 1: Write failing config tests**

Create `src/test/java/com/lama/conveniencetools/config/ConfigManagerTest.java`:

```java
package com.lama.conveniencetools.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class ConfigManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void createsDefaultsWhenFileIsMissing() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir.resolve("convenience-tools.json"));

        ModConfig config = manager.load();

        assertFalse(config.gamma.enabled);
        assertEquals(1500.0, config.gamma.enabledValue);
        assertEquals("key.keyboard.g", config.gamma.toggleKey);
        assertTrue(Files.exists(tempDir.resolve("convenience-tools.json")));
    }

    @Test
    void clampsInvalidGammaValues() throws Exception {
        Path file = tempDir.resolve("convenience-tools.json");
        Files.writeString(file, """
            {
              "gamma": {
                "enabled": true,
                "enabledValue": 999999.0,
                "toggleKey": "",
                "minValue": -10.0,
                "maxValue": 1.0
              },
              "macros": []
            }
            """);
        ConfigManager manager = new ConfigManager(file);

        ModConfig config = manager.load();

        assertTrue(config.gamma.enabled);
        assertEquals(1500.0, config.gamma.enabledValue);
        assertEquals("key.keyboard.g", config.gamma.toggleKey);
        assertEquals(0.0, config.gamma.minValue);
        assertEquals(1500.0, config.gamma.maxValue);
    }

    @Test
    void savesUnicodeMacroContent() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir.resolve("convenience-tools.json"));
        ModConfig config = ModConfig.defaults();
        MacroConfig macro = MacroConfig.defaults();
        macro.id = "ender";
        macro.name = "Ender";
        macro.key = "key.keyboard.grave.accent";
        macro.content = "/엔더";
        config.macros.add(macro);

        manager.save(config);
        ModConfig reloaded = manager.load();

        assertEquals("/엔더", reloaded.macros.getFirst().content);
    }
}
```

- [ ] **Step 2: Run config tests and verify failure**

Run:

```powershell
.\gradlew.bat test --tests com.lama.conveniencetools.config.ConfigManagerTest --no-daemon
```

Expected: FAIL because `ConfigManager`, `ModConfig`, `GammaConfig`, and `MacroConfig` do not exist.

- [ ] **Step 3: Add shared constants**

Create `src/main/java/com/lama/conveniencetools/ModConstants.java`:

```java
package com.lama.conveniencetools;

public final class ModConstants {
    public static final String MOD_ID = "convenience_tools";
    public static final String MOD_NAME = "Convenience Tools";
    public static final String CONFIG_FILE_NAME = "convenience-tools.json";

    private ModConstants() {
    }
}
```

- [ ] **Step 4: Add config model classes**

Create `src/main/java/com/lama/conveniencetools/config/GammaConfig.java`:

```java
package com.lama.conveniencetools.config;

public final class GammaConfig {
    public boolean enabled = false;
    public double enabledValue = 1500.0;
    public String toggleKey = "key.keyboard.g";
    public double minValue = 0.0;
    public double maxValue = 1500.0;

    public static GammaConfig defaults() {
        return new GammaConfig();
    }
}
```

Create `src/main/java/com/lama/conveniencetools/config/MacroConfig.java`:

```java
package com.lama.conveniencetools.config;

import com.lama.conveniencetools.macro.MacroAction;
import com.lama.conveniencetools.macro.MacroMode;

import java.util.UUID;

public final class MacroConfig {
    public String id = UUID.randomUUID().toString();
    public String name = "New Macro";
    public String key = "";
    public String content = "";
    public MacroAction action = MacroAction.SEND;
    public MacroMode mode = MacroMode.SIMPLE;
    public long delayMs = 0L;
    public long intervalMs = 1000L;
    public boolean enabled = true;

    public static MacroConfig defaults() {
        return new MacroConfig();
    }
}
```

Create `src/main/java/com/lama/conveniencetools/config/ModConfig.java`:

```java
package com.lama.conveniencetools.config;

import java.util.ArrayList;
import java.util.List;

public final class ModConfig {
    public GammaConfig gamma = GammaConfig.defaults();
    public List<MacroConfig> macros = new ArrayList<>();

    public static ModConfig defaults() {
        return new ModConfig();
    }
}
```

- [ ] **Step 5: Add enum dependencies for config**

Create `src/main/java/com/lama/conveniencetools/macro/MacroAction.java`:

```java
package com.lama.conveniencetools.macro;

public enum MacroAction {
    SEND,
    TYPE
}
```

Create `src/main/java/com/lama/conveniencetools/macro/MacroMode.java`:

```java
package com.lama.conveniencetools.macro;

public enum MacroMode {
    SIMPLE,
    DELAYED,
    REPEATING,
    TOGGLE
}
```

- [ ] **Step 6: Implement config manager**

Create `src/main/java/com/lama/conveniencetools/config/ConfigManager.java`:

```java
package com.lama.conveniencetools.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;

    public ConfigManager(Path configPath) {
        this.configPath = Objects.requireNonNull(configPath, "configPath");
    }

    public ModConfig load() throws IOException {
        if (!Files.exists(configPath)) {
            ModConfig defaults = ModConfig.defaults();
            save(defaults);
            return defaults;
        }

        String json = Files.readString(configPath, StandardCharsets.UTF_8);
        ModConfig loaded = GSON.fromJson(json, ModConfig.class);
        ModConfig validated = validate(loaded);
        save(validated);
        return validated;
    }

    public void save(ModConfig config) throws IOException {
        Files.createDirectories(configPath.getParent());
        Files.writeString(configPath, GSON.toJson(validate(config)), StandardCharsets.UTF_8);
    }

    private static ModConfig validate(ModConfig config) {
        ModConfig result = config == null ? ModConfig.defaults() : config;
        result.gamma = validateGamma(result.gamma);
        if (result.macros == null) {
            result.macros = new ArrayList<>();
        }
        result.macros.removeIf(Objects::isNull);
        result.macros.forEach(ConfigManager::validateMacro);
        return result;
    }

    private static GammaConfig validateGamma(GammaConfig gamma) {
        GammaConfig result = gamma == null ? GammaConfig.defaults() : gamma;
        result.minValue = result.minValue < 0.0 ? 0.0 : result.minValue;
        result.maxValue = result.maxValue < 2.0 ? 1500.0 : result.maxValue;
        if (result.maxValue < result.minValue) {
            result.maxValue = 1500.0;
            result.minValue = 0.0;
        }
        if (result.enabledValue < result.minValue || result.enabledValue > result.maxValue) {
            result.enabledValue = 1500.0;
        }
        if (result.toggleKey == null || result.toggleKey.isBlank()) {
            result.toggleKey = "key.keyboard.g";
        }
        return result;
    }

    private static void validateMacro(MacroConfig macro) {
        if (macro.id == null || macro.id.isBlank()) {
            macro.id = java.util.UUID.randomUUID().toString();
        }
        if (macro.name == null || macro.name.isBlank()) {
            macro.name = "New Macro";
        }
        if (macro.key == null) {
            macro.key = "";
        }
        if (macro.content == null) {
            macro.content = "";
        }
        if (macro.action == null) {
            macro.action = com.lama.conveniencetools.macro.MacroAction.SEND;
        }
        if (macro.mode == null) {
            macro.mode = com.lama.conveniencetools.macro.MacroMode.SIMPLE;
        }
        if (macro.delayMs < 0L) {
            macro.delayMs = 0L;
        }
        if (macro.intervalMs < 50L) {
            macro.intervalMs = 1000L;
        }
    }
}
```

- [ ] **Step 7: Run config tests and verify pass**

Run:

```powershell
.\gradlew.bat test --tests com.lama.conveniencetools.config.ConfigManagerTest --no-daemon
```

Expected: PASS.

- [ ] **Step 8: Commit config layer**

Run:

```powershell
git add src/main/java/com/lama/conveniencetools src/test/java/com/lama/conveniencetools/config
git commit -m "feat: add config persistence"
```

Expected: commit succeeds.

## Task 3: Gamma Controller

**Files:**
- Create: `src/main/java/com/lama/conveniencetools/gamma/BrightnessAccess.java`
- Create: `src/main/java/com/lama/conveniencetools/gamma/GammaController.java`
- Create: `src/main/java/com/lama/conveniencetools/gamma/MinecraftBrightnessAccess.java`
- Create: `src/test/java/com/lama/conveniencetools/gamma/GammaControllerTest.java`

- [ ] **Step 1: Write failing gamma tests**

Create `src/test/java/com/lama/conveniencetools/gamma/GammaControllerTest.java`:

```java
package com.lama.conveniencetools.gamma;

import com.lama.conveniencetools.config.GammaConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class GammaControllerTest {
    @Test
    void appliesSavedEnabledStateOnStartup() {
        FakeBrightness brightness = new FakeBrightness(0.5);
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 1500.0;
        GammaController controller = new GammaController(config, brightness, () -> {});

        controller.initialize();

        assertEquals(1500.0, brightness.value);
    }

    @Test
    void togglesOnAndPersistsState() {
        FakeBrightness brightness = new FakeBrightness(0.5);
        GammaConfig config = GammaConfig.defaults();
        int[] saves = {0};
        GammaController controller = new GammaController(config, brightness, () -> saves[0]++);

        controller.initialize();
        controller.toggle();

        assertTrue(config.enabled);
        assertEquals(1500.0, brightness.value);
        assertEquals(1, saves[0]);
    }

    @Test
    void togglesOffRestoringOriginalBrightnessAndPersistsState() {
        FakeBrightness brightness = new FakeBrightness(0.7);
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 1500.0;
        int[] saves = {0};
        GammaController controller = new GammaController(config, brightness, () -> saves[0]++);

        controller.initialize();
        controller.toggle();

        assertFalse(config.enabled);
        assertEquals(0.7, brightness.value);
        assertEquals(1, saves[0]);
    }

    private static final class FakeBrightness implements BrightnessAccess {
        double value;

        FakeBrightness(double value) {
            this.value = value;
        }

        @Override
        public double getBrightness() {
            return value;
        }

        @Override
        public void setBrightness(double value) {
            this.value = value;
        }
    }
}
```

- [ ] **Step 2: Run gamma tests and verify failure**

Run:

```powershell
.\gradlew.bat test --tests com.lama.conveniencetools.gamma.GammaControllerTest --no-daemon
```

Expected: FAIL because gamma classes do not exist.

- [ ] **Step 3: Implement brightness interface and controller**

Create `src/main/java/com/lama/conveniencetools/gamma/BrightnessAccess.java`:

```java
package com.lama.conveniencetools.gamma;

public interface BrightnessAccess {
    double getBrightness();

    void setBrightness(double value);
}
```

Create `src/main/java/com/lama/conveniencetools/gamma/GammaController.java`:

```java
package com.lama.conveniencetools.gamma;

import com.lama.conveniencetools.config.GammaConfig;

import java.util.Objects;

public final class GammaController {
    private final GammaConfig config;
    private final BrightnessAccess brightness;
    private final Runnable saveCallback;
    private double normalBrightness;
    private boolean initialized;

    public GammaController(GammaConfig config, BrightnessAccess brightness, Runnable saveCallback) {
        this.config = Objects.requireNonNull(config, "config");
        this.brightness = Objects.requireNonNull(brightness, "brightness");
        this.saveCallback = Objects.requireNonNull(saveCallback, "saveCallback");
    }

    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        normalBrightness = brightness.getBrightness();
        if (config.enabled) {
            applyEnabledValue();
        }
    }

    public void toggle() {
        initialize();
        config.enabled = !config.enabled;
        if (config.enabled) {
            normalBrightness = brightness.getBrightness();
            applyEnabledValue();
        } else {
            brightness.setBrightness(normalBrightness);
        }
        saveCallback.run();
    }

    public void applyCurrentState() {
        initialize();
        if (config.enabled) {
            applyEnabledValue();
        }
    }

    private void applyEnabledValue() {
        double clamped = Math.max(config.minValue, Math.min(config.maxValue, config.enabledValue));
        brightness.setBrightness(clamped);
    }
}
```

- [ ] **Step 4: Implement Minecraft brightness adapter**

Create `src/main/java/com/lama/conveniencetools/gamma/MinecraftBrightnessAccess.java`:

```java
package com.lama.conveniencetools.gamma;

import net.minecraft.client.Minecraft;

public final class MinecraftBrightnessAccess implements BrightnessAccess {
    private final Minecraft client;

    public MinecraftBrightnessAccess(Minecraft client) {
        this.client = client;
    }

    @Override
    public double getBrightness() {
        return client.options.gamma().get();
    }

    @Override
    public void setBrightness(double value) {
        client.options.gamma().set(value);
    }
}
```

- [ ] **Step 5: Run gamma tests and verify pass**

Run:

```powershell
.\gradlew.bat test --tests com.lama.conveniencetools.gamma.GammaControllerTest --no-daemon
```

Expected: PASS.

- [ ] **Step 6: Commit gamma layer**

Run:

```powershell
git add src/main/java/com/lama/conveniencetools/gamma src/test/java/com/lama/conveniencetools/gamma
git commit -m "feat: add persistent gamma controller"
```

Expected: commit succeeds.

## Task 4: Macro Runtime

**Files:**
- Create: `src/main/java/com/lama/conveniencetools/macro/ChatExecutor.java`
- Create: `src/main/java/com/lama/conveniencetools/macro/MacroManager.java`
- Create: `src/main/java/com/lama/conveniencetools/macro/MinecraftChatExecutor.java`
- Create: `src/test/java/com/lama/conveniencetools/macro/MacroManagerTest.java`

- [ ] **Step 1: Write failing macro tests**

Create `src/test/java/com/lama/conveniencetools/macro/MacroManagerTest.java`:

```java
package com.lama.conveniencetools.macro;

import com.lama.conveniencetools.config.MacroConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class MacroManagerTest {
    @Test
    void sendsSimpleMacroImmediately() {
        RecordingChat chat = new RecordingChat();
        MacroManager manager = new MacroManager(chat);
        MacroConfig macro = MacroConfig.defaults();
        macro.key = "key.keyboard.grave.accent";
        macro.content = "/엔더";
        macro.action = MacroAction.SEND;
        macro.mode = MacroMode.SIMPLE;
        manager.setMacros(List.of(macro));

        manager.onKeyPressed("key.keyboard.grave.accent", 0L, false);

        assertEquals(List.of("SEND:/엔더"), chat.events);
    }

    @Test
    void typesSimpleMacroInsteadOfSendingWhenConfigured() {
        RecordingChat chat = new RecordingChat();
        MacroManager manager = new MacroManager(chat);
        MacroConfig macro = MacroConfig.defaults();
        macro.key = "key.keyboard.grave.accent";
        macro.content = "/엔더";
        macro.action = MacroAction.TYPE;
        manager.setMacros(List.of(macro));

        manager.onKeyPressed("key.keyboard.grave.accent", 0L, false);

        assertEquals(List.of("TYPE:/엔더"), chat.events);
    }

    @Test
    void ignoresMacrosWhileTyping() {
        RecordingChat chat = new RecordingChat();
        MacroManager manager = new MacroManager(chat);
        MacroConfig macro = MacroConfig.defaults();
        macro.key = "key.keyboard.grave.accent";
        macro.content = "/엔더";
        manager.setMacros(List.of(macro));

        manager.onKeyPressed("key.keyboard.grave.accent", 0L, true);

        assertTrue(chat.events.isEmpty());
    }

    @Test
    void toggleMacroRepeatsUntilPressedAgain() {
        RecordingChat chat = new RecordingChat();
        MacroManager manager = new MacroManager(chat);
        MacroConfig macro = MacroConfig.defaults();
        macro.id = "toggle";
        macro.key = "key.keyboard.g";
        macro.content = "/spawn";
        macro.mode = MacroMode.TOGGLE;
        macro.intervalMs = 100L;
        manager.setMacros(List.of(macro));

        manager.onKeyPressed("key.keyboard.g", 0L, false);
        manager.tick(0L);
        manager.tick(100L);
        manager.onKeyPressed("key.keyboard.g", 150L, false);
        manager.tick(200L);

        assertEquals(List.of("SEND:/spawn", "SEND:/spawn"), chat.events);
    }

    private static final class RecordingChat implements ChatExecutor {
        final List<String> events = new ArrayList<>();

        @Override
        public void send(String content) {
            events.add("SEND:" + content);
        }

        @Override
        public void type(String content) {
            events.add("TYPE:" + content);
        }
    }
}
```

- [ ] **Step 2: Run macro tests and verify failure**

Run:

```powershell
.\gradlew.bat test --tests com.lama.conveniencetools.macro.MacroManagerTest --no-daemon
```

Expected: FAIL because macro runtime classes do not exist.

- [ ] **Step 3: Implement chat executor and manager**

Create `src/main/java/com/lama/conveniencetools/macro/ChatExecutor.java`:

```java
package com.lama.conveniencetools.macro;

public interface ChatExecutor {
    void send(String content);

    void type(String content);
}
```

Create `src/main/java/com/lama/conveniencetools/macro/MacroManager.java`:

```java
package com.lama.conveniencetools.macro;

import com.lama.conveniencetools.config.MacroConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MacroManager {
    private final ChatExecutor chatExecutor;
    private final List<MacroConfig> macros = new ArrayList<>();
    private final Map<String, Long> nextRunById = new HashMap<>();
    private final Map<String, MacroConfig> delayedById = new HashMap<>();
    private final Map<String, MacroConfig> toggledById = new HashMap<>();

    public MacroManager(ChatExecutor chatExecutor) {
        this.chatExecutor = Objects.requireNonNull(chatExecutor, "chatExecutor");
    }

    public void setMacros(List<MacroConfig> macros) {
        this.macros.clear();
        if (macros != null) {
            this.macros.addAll(macros);
        }
        toggledById.clear();
        delayedById.clear();
        nextRunById.clear();
    }

    public void onKeyPressed(String key, long nowMs, boolean typingFocused) {
        if (typingFocused || key == null || key.isBlank()) {
            return;
        }
        for (MacroConfig macro : macros) {
            if (canRun(macro) && key.equals(macro.key)) {
                handlePress(macro, nowMs);
            }
        }
    }

    public void tick(long nowMs) {
        for (MacroConfig macro : List.copyOf(delayedById.values())) {
            long nextRun = nextRunById.getOrDefault(macro.id, Long.MAX_VALUE);
            if (nowMs >= nextRun) {
                execute(macro);
                delayedById.remove(macro.id);
                nextRunById.remove(macro.id);
            }
        }
        for (MacroConfig macro : toggledById.values()) {
            long nextRun = nextRunById.getOrDefault(macro.id, 0L);
            if (nowMs >= nextRun) {
                execute(macro);
                nextRunById.put(macro.id, nowMs + macro.intervalMs);
            }
        }
    }

    public void clearRuntimeState() {
        toggledById.clear();
        delayedById.clear();
        nextRunById.clear();
    }

    private void handlePress(MacroConfig macro, long nowMs) {
        switch (macro.mode) {
            case SIMPLE -> execute(macro);
            case DELAYED -> {
                nextRunById.put(macro.id, nowMs + macro.delayMs);
                delayedById.put(macro.id, macro);
            }
            case REPEATING -> {
                execute(macro);
                nextRunById.put(macro.id, nowMs + macro.intervalMs);
            }
            case TOGGLE -> {
                if (toggledById.containsKey(macro.id)) {
                    toggledById.remove(macro.id);
                    nextRunById.remove(macro.id);
                } else {
                    toggledById.put(macro.id, macro);
                    nextRunById.put(macro.id, nowMs);
                }
            }
        }
    }

    private boolean canRun(MacroConfig macro) {
        return macro != null
            && macro.enabled
            && macro.key != null
            && !macro.key.isBlank()
            && macro.content != null
            && !macro.content.isBlank();
    }

    private void execute(MacroConfig macro) {
        if (macro.action == MacroAction.TYPE) {
            chatExecutor.type(macro.content);
        } else {
            chatExecutor.send(macro.content);
        }
    }
}
```

- [ ] **Step 4: Implement Minecraft chat executor**

Create `src/main/java/com/lama/conveniencetools/macro/MinecraftChatExecutor.java`:

```java
package com.lama.conveniencetools.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public final class MinecraftChatExecutor implements ChatExecutor {
    private final Minecraft client;

    public MinecraftChatExecutor(Minecraft client) {
        this.client = client;
    }

    @Override
    public void send(String content) {
        if (client.player == null || client.player.connection == null) {
            return;
        }
        if (content.startsWith("/")) {
            client.player.connection.sendCommand(content.substring(1));
        } else {
            client.player.connection.sendChat(content);
        }
    }

    @Override
    public void type(String content) {
        client.setScreen(new ChatScreen(content));
    }
}
```

- [ ] **Step 5: Run macro tests**

Run:

```powershell
.\gradlew.bat test --tests com.lama.conveniencetools.macro.MacroManagerTest --no-daemon
```

Expected: PASS.

- [ ] **Step 6: Commit macro runtime**

Run:

```powershell
git add src/main/java/com/lama/conveniencetools/macro src/test/java/com/lama/conveniencetools/macro
git commit -m "feat: add macro runtime"
```

Expected: commit succeeds.

## Task 5: Fabric Client Wiring

**Files:**
- Create: `src/main/java/com/lama/conveniencetools/ConvenienceToolsClient.java`
- Modify: `src/main/java/com/lama/conveniencetools/macro/MacroManager.java`

- [ ] **Step 1: Add held-key support to MacroManager**

Modify `MacroManager` so `REPEATING` can be driven from client tick while a key remains down:

```java
public void onKeyHeld(String key, long nowMs, boolean typingFocused) {
    if (typingFocused || key == null || key.isBlank()) {
        return;
    }
    for (MacroConfig macro : macros) {
        if (canRun(macro) && macro.mode == MacroMode.REPEATING && key.equals(macro.key)) {
            long nextRun = nextRunById.getOrDefault(macro.id, 0L);
            if (nowMs >= nextRun) {
                execute(macro);
                nextRunById.put(macro.id, nowMs + macro.intervalMs);
            }
        }
    }
}
```

Expected: `SIMPLE`, `TYPE`, `SEND`, and `TOGGLE` tests still pass.

- [ ] **Step 2: Create Fabric client entrypoint**

Create `src/main/java/com/lama/conveniencetools/ConvenienceToolsClient.java`:

```java
package com.lama.conveniencetools;

import com.lama.conveniencetools.config.ConfigManager;
import com.lama.conveniencetools.config.MacroConfig;
import com.lama.conveniencetools.config.ModConfig;
import com.lama.conveniencetools.gamma.GammaController;
import com.lama.conveniencetools.gamma.MinecraftBrightnessAccess;
import com.lama.conveniencetools.macro.MacroManager;
import com.lama.conveniencetools.macro.MinecraftChatExecutor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class ConvenienceToolsClient implements ClientModInitializer {
    private static ConvenienceToolsClient instance;

    private ModConfig config;
    private ConfigManager configManager;
    private GammaController gammaController;
    private MacroManager macroManager;
    private KeyMapping gammaKey;
    private final Set<String> pressedMacroKeys = new HashSet<>();

    public static ConvenienceToolsClient instance() {
        return instance;
    }

    public ModConfig config() {
        return config;
    }

    public void saveConfig() {
        try {
            configManager.save(config);
        } catch (IOException ignored) {
            // Keep gameplay uninterrupted if disk write fails.
        }
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        Minecraft client = Minecraft.getInstance();
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(ModConstants.CONFIG_FILE_NAME);
        configManager = new ConfigManager(configPath);
        try {
            config = configManager.load();
        } catch (IOException exception) {
            config = ModConfig.defaults();
        }

        gammaKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.convenience_tools.toggle_gamma",
            GLFW.GLFW_KEY_G,
            "key.categories.convenience_tools"
        ));

        gammaController = new GammaController(config.gamma, new MinecraftBrightnessAccess(client), this::saveConfig);
        macroManager = new MacroManager(new MinecraftChatExecutor(client));
        macroManager.setMacros(config.macros);

        ClientLifecycleEvents.CLIENT_STARTED.register(startedClient -> gammaController.initialize());
        ClientLifecycleEvents.CLIENT_STOPPING.register(stoppingClient -> saveConfig());

        ClientTickEvents.END_CLIENT_TICK.register(tickClient -> {
            while (gammaKey.consumeClick()) {
                gammaController.toggle();
            }

            long nowMs = System.currentTimeMillis();
            boolean typingFocused = isTypingFocused(tickClient.screen);
            macroManager.tick(nowMs);
            macroManager.setMacros(config.macros);
            pollMacroKeys(tickClient, nowMs, typingFocused);
        });
    }

    private void pollMacroKeys(Minecraft client, long nowMs, boolean typingFocused) {
        long window = client.getWindow().getWindow();
        for (MacroConfig macro : config.macros) {
            int keyCode = keyboardKeyCode(macro.key);
            if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
                continue;
            }
            boolean down = InputConstants.isKeyDown(window, keyCode);
            boolean wasDown = pressedMacroKeys.contains(macro.key);
            if (down && !wasDown) {
                pressedMacroKeys.add(macro.key);
                macroManager.onKeyPressed(macro.key, nowMs, typingFocused);
            }
            if (down) {
                macroManager.onKeyHeld(macro.key, nowMs, typingFocused);
            }
            if (!down && wasDown) {
                pressedMacroKeys.remove(macro.key);
            }
        }
    }

    private int keyboardKeyCode(String keyTranslationKey) {
        if (keyTranslationKey == null || keyTranslationKey.isBlank()) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        if ("key.keyboard.grave.accent".equals(keyTranslationKey)) {
            return GLFW.GLFW_KEY_GRAVE_ACCENT;
        }
        return InputConstants.getKey(keyTranslationKey).getValue();
    }

    private boolean isTypingFocused(Screen screen) {
        return screen instanceof ChatScreen;
    }
}
```

- [ ] **Step 3: Compile macro key polling**

Run:

```powershell
.\gradlew.bat compileJava --no-daemon
```

Expected: PASS with `InputConstants.isKeyDown`, `InputConstants.getKey`, and `Minecraft#getWindow()` available under Mojang mappings.

- [ ] **Step 4: Compile client wiring**

Run:

```powershell
.\gradlew.bat compileJava --no-daemon
```

Expected: PASS.

- [ ] **Step 5: Commit client wiring**

Run:

```powershell
git add src/main/java/com/lama/conveniencetools
git commit -m "feat: wire gamma and macros into fabric client"
```

Expected: commit succeeds.

## Task 6: Settings UI

**Files:**
- Create: `src/main/java/com/lama/conveniencetools/ui/ModMenuIntegration.java`
- Create: `src/main/java/com/lama/conveniencetools/ui/ConvenienceConfigScreen.java`
- Create: `src/main/java/com/lama/conveniencetools/ui/MacroEditScreen.java`

- [ ] **Step 1: Create Mod Menu entrypoint**

Create `src/main/java/com/lama/conveniencetools/ui/ModMenuIntegration.java`:

```java
package com.lama.conveniencetools.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConvenienceConfigScreen::create;
    }
}
```

- [ ] **Step 2: Create main config screen**

Create `src/main/java/com/lama/conveniencetools/ui/ConvenienceConfigScreen.java`:

```java
package com.lama.conveniencetools.ui;

import com.lama.conveniencetools.ConvenienceToolsClient;
import com.lama.conveniencetools.config.ModConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConvenienceConfigScreen {
    private ConvenienceConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConvenienceToolsClient mod = ConvenienceToolsClient.instance();
        ModConfig config = mod.config();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("text.convenience_tools.title"))
            .setSavingRunnable(mod::saveConfig);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory gamma = builder.getOrCreateCategory(Component.translatable("text.convenience_tools.gamma"));
        gamma.addEntry(entries.startBooleanToggle(Component.translatable("text.convenience_tools.gamma.enabled"), config.gamma.enabled)
            .setDefaultValue(false)
            .setSaveConsumer(value -> config.gamma.enabled = value)
            .build());
        gamma.addEntry(entries.startDoubleField(Component.translatable("text.convenience_tools.gamma.enabled_value"), config.gamma.enabledValue)
            .setDefaultValue(1500.0)
            .setMin(config.gamma.minValue)
            .setMax(config.gamma.maxValue)
            .setSaveConsumer(value -> config.gamma.enabledValue = value)
            .build());

        ConfigCategory macros = builder.getOrCreateCategory(Component.translatable("text.convenience_tools.macros"));
        macros.addEntry(entries.startTextDescription(Component.literal("Use Add Macro to create key-bound commands or chat text.")).build());

        return builder.build();
    }
}
```

- [ ] **Step 3: Create macro edit screen shell**

Create `src/main/java/com/lama/conveniencetools/ui/MacroEditScreen.java`:

```java
package com.lama.conveniencetools.ui;

import com.lama.conveniencetools.config.MacroConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MacroEditScreen extends Screen {
    private final Screen parent;
    private final MacroConfig macro;

    public MacroEditScreen(Screen parent, MacroConfig macro) {
        super(Component.translatable("text.convenience_tools.edit_macro"));
        this.parent = parent;
        this.macro = macro;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    public MacroConfig macro() {
        return macro;
    }
}
```

- [ ] **Step 4: Compile UI**

Run:

```powershell
.\gradlew.bat compileJava --no-daemon
```

Expected: PASS.

- [ ] **Step 5: Expand macro UI**

Add entries to the Macros category for each macro:

```java
for (MacroConfig macro : config.macros) {
    macros.addEntry(entries.startBooleanToggle(Component.literal(macro.name), macro.enabled)
        .setDefaultValue(true)
        .setSaveConsumer(value -> macro.enabled = value)
        .build());
}
```

Expected: existing macros can be enabled/disabled from Mod Menu.

- [ ] **Step 6: Commit settings UI**

Run:

```powershell
git add src/main/java/com/lama/conveniencetools/ui src/main/resources
git commit -m "feat: add mod menu settings"
```

Expected: commit succeeds.

## Task 7: Full Build And Manual Checks

**Files:**
- Create: `README.md`

- [ ] **Step 1: Run unit tests**

Run:

```powershell
.\gradlew.bat test --no-daemon
```

Expected: all JUnit tests pass.

- [ ] **Step 2: Run full build**

Run:

```powershell
.\gradlew.bat build --no-daemon
```

Expected: `BUILD SUCCESSFUL` and jar appears under `build/libs/`.

- [ ] **Step 3: Create usage notes**

Create `README.md`:

```markdown
# Convenience Tools

Client-only Fabric mod for Minecraft Java 26.1.2.

## Features

- Gamma toggle, default key `G`.
- Gamma on/off state persists between launches.
- User-created macros with send/type actions.
- Macro modes: simple, delayed, repeating, toggle.

## Build

```powershell
.\gradlew.bat build
```

Use the jar from `build/libs/` in a Minecraft 26.1.2 Fabric or Feather `26.1.2-FB` profile.
```

- [ ] **Step 4: Manual runtime checklist**

Run the game from Gradle if local assets download correctly:

```powershell
.\gradlew.bat runClient --no-daemon
```

Expected manual results:

- Mod loads on Minecraft `26.1.2`.
- `G` toggles brightness with no chat/actionbar/toast.
- Gamma state persists after closing and reopening the client.
- Mod Menu opens the settings screen.
- Enabled gamma value persists after editing.
- A macro with key backtick and content `/엔더` can be stored.
- `SEND` sends `/엔더` immediately.
- `TYPE` opens chat prefilled with `/엔더`.
- Macros do not fire while chat or settings text fields are focused.

- [ ] **Step 5: Commit verification docs**

Run:

```powershell
git add README.md
git commit -m "docs: add build and usage notes"
```

Expected: commit succeeds.

## Self-Review

- Spec coverage: target environment, gamma persistence, default `G`, no night vision, no status messages, user-created macros, send/type choice, repeat/delay/toggle modes, JSON config, Mod Menu settings, and build verification are covered by tasks.
- Placeholder scan: no incomplete-work markers are intentionally left in this plan.
- Risk: Minecraft runtime behavior still needs manual verification in a 26.1.2 Fabric client because unit tests cover core config, gamma state, and macro scheduling rather than the live client UI.
