# KindMap

Client-only Fabric mod for Minecraft Java `26.1.2`, intended to work in Feather Client profiles shown as `26.1.2-FB`.

## Features

- Gamma toggle, default key `G`.
- Gamma on/off state persists between client restarts.
- Gamma uses client brightness only. It does not use night vision, potion effects, status effects, or server-side behavior.
- Configurable enabled gamma value, default `1500.0`.
- User-created macros with configurable key, content, action, and mode.
- Macro actions: `SEND` immediately sends chat/commands, `TYPE` opens chat with the content prefilled.
- Macro modes: `SIMPLE`, `DELAYED`, `REPEATING`, `TOGGLE`.
- Macros pause while a Minecraft screen is open, including chat and settings fields.

## Build

Use Java 25.

```powershell
.\gradlew.bat build --no-daemon
```

The mod jar is written under `build/libs/`. Use it with a Minecraft `26.1.2` Fabric profile or a Feather `26.1.2-FB` profile that has Fabric Loader and Fabric API `0.152.1+26.1.2` or newer available.

## Settings

Install Mod Menu to open the KindMap settings screen in-game. The settings screen supports:

- Gamma enabled state and enabled gamma value.
- Macro add/delete/select.
- Macro enabled state, name, key, content, action, mode, delay, and interval.
- Key capture for macro keys.

Config is stored at:

```text
config/kindmap.json
```

## Manual Checklist

- The mod loads on Minecraft `26.1.2`.
- `G` toggles brightness with no chat/actionbar/toast message.
- Gamma on/off state persists after closing and reopening the client.
- Mod Menu opens the KindMap settings screen.
- Enabled gamma value can be edited and persists.
- A macro with key backtick and content `/엔더` can be stored.
- `SEND` sends `/엔더` immediately.
- `TYPE` opens chat prefilled with `/엔더`.
- Macros do not fire while chat or settings text fields are focused.
