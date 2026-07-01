# Convenience Fabric Mod Design

## Goal

Build a client-only Fabric mod for Minecraft Java 26.1.2 that combines two convenience features:

- Gamma control for quickly switching between normal brightness and a configured high-brightness value.
- User-configurable macro keybinds for sending commands/messages or placing them into chat.

The mod should work in a Feather Client Fabric profile shown as `26.1.2-FB`, which maps to Minecraft `26.1.2` with Fabric.

## Target Environment

- Minecraft: `26.1.2`
- Fabric Loader: `0.19.3`
- Fabric API: `0.154.0+26.1.2`
- Mapping strategy: Mojang official mappings
- Runtime side: client only
- Optional UI integrations:
  - Mod Menu for opening the config screen
  - Cloth Config for settings UI widgets

Yarn mappings are not the primary choice because current Fabric metadata showed `26.1.2` as a stable game target while the Yarn listing did not expose matching `26.x` mappings during verification.

## Gamma Feature

Gamma is a local client brightness control only. It must not use night vision, potion effects, status effects, or server-side behavior.

Default behavior:

- Default toggle key: `G`
- Initial default state: off
- If the user turns gamma on and exits Minecraft, the next launch starts with gamma on.
- If the user turns gamma off and exits Minecraft, the next launch starts with gamma off.
- Turning gamma on applies the configured enabled gamma value.
- Turning gamma off restores the user's normal brightness value.
- The default enabled gamma value should represent maximum brightness.
- The enabled gamma value can be changed in the settings screen.
- No chat message, actionbar message, toast, or other status message is shown when gamma changes.

Config fields:

- `gamma.enabled`: persisted boolean for last on/off state.
- `gamma.enabledValue`: brightness/gamma value used while on.
- `gamma.toggleKey`: default `G`.
- `gamma.minValue` and `gamma.maxValue`: bounds used by the settings UI and validation.

Runtime behavior:

- On client initialization, load config.
- Store the user's non-gamma brightness value before applying enabled gamma.
- If `gamma.enabled` is true, apply `gamma.enabledValue` after options load.
- On toggle:
  - Flip `gamma.enabled`.
  - Apply the correct brightness immediately.
  - Save config immediately so launcher/client restarts preserve the state.

## Macro Feature

Macros are managed by the mod's own settings screen rather than fixed slots in Minecraft's vanilla keybind menu. Users can add, edit, and delete macros with no hardcoded slot names.

Each macro contains:

- `id`: stable generated identifier.
- `name`: display name in the config screen.
- `key`: captured keyboard input, such as backtick.
- `content`: text to send or insert, such as `/ender` or `/엔더`.
- `action`: execution behavior.
- `mode`: simple, delayed, repeating, or toggle.
- `delayMs`: used for delayed mode.
- `intervalMs`: used for repeating/toggle mode.
- `enabled`: whether the macro is active.

Actions:

- `SEND`: immediately send the configured content as chat or command.
- `TYPE`: open chat with the configured content pre-filled, leaving Enter to the user.

Modes:

- `SIMPLE`: perform the action once when the key is pressed.
- `DELAYED`: perform the action once after `delayMs`.
- `REPEATING`: repeat while the key is held, using `intervalMs`.
- `TOGGLE`: first press starts repeating, second press stops.

Input behavior:

- Macro key input is captured in the mod's macro edit screen.
- The macro runner listens on client tick/key input and ignores macro execution while text fields, chat input, or other typing screens are focused.
- Duplicate macro keys are allowed only if the UI clearly marks them as conflicts; the recommended default is to warn and keep the macro disabled until resolved.

Safety behavior:

- Macro execution is client initiated only.
- The mod does not bypass chat signing, permissions, cooldowns, or server-side command restrictions.
- Commands/messages are sent through normal Minecraft client APIs.

## Settings UI

Mod Menu opens the main config screen. The screen has two sections:

1. Gamma
   - Toggle current on/off state.
   - Enabled gamma value slider/input.
   - Min/max bounds if exposed.
   - Keybind capture for gamma toggle, default `G`.

2. Macros
   - List macros.
   - Add macro.
   - Edit macro.
   - Delete macro.
   - Enable/disable macro.
   - Capture key.
   - Choose action: send or type.
   - Choose mode: simple, delayed, repeating, toggle.
   - Configure delay/interval where relevant.
   - Show duplicate key conflicts.

The UI should avoid status text overlays during gameplay. Settings labels can be visible inside the settings screen.

## Config Storage

Use one JSON config file:

`config/convenience-tools.json`

Example shape:

```json
{
  "gamma": {
    "enabled": false,
    "enabledValue": 1500.0,
    "toggleKey": "key.keyboard.g",
    "minValue": 0.0,
    "maxValue": 1500.0
  },
  "macros": [
    {
      "id": "example-ender",
      "name": "Ender",
      "key": "key.keyboard.grave.accent",
      "content": "/엔더",
      "action": "SEND",
      "mode": "SIMPLE",
      "delayMs": 0,
      "intervalMs": 1000,
      "enabled": true
    }
  ]
}
```

Config handling:

- Missing config creates defaults.
- Invalid fields fall back to safe defaults.
- Unknown fields are ignored where practical.
- Config is saved after user changes and after gamma toggle changes.

## Architecture

Suggested package layout:

- `client/ConvenienceToolsClient`: Fabric client entrypoint.
- `config/ModConfig`: serializable config model.
- `config/ConfigManager`: load, validate, save.
- `gamma/GammaController`: apply/toggle gamma and restore normal brightness.
- `macro/Macro`: macro model.
- `macro/MacroAction`, `MacroMode`: enums.
- `macro/MacroManager`: key matching, scheduling, repeat/toggle state.
- `ui/ConfigScreenFactory`: Mod Menu integration.
- `ui/GammaConfigScreen` or Cloth Config category builder.
- `ui/MacroListScreen` and `ui/MacroEditScreen`: macro management.

The gamma and macro systems should not depend on each other except through shared config loading/saving.

## Error Handling

- If config loading fails, rename or ignore the broken data in memory and continue with defaults.
- If a macro has no key or no content, skip execution.
- If a macro key conflicts, show the conflict in UI and do not execute disabled/conflicting macros.
- If Minecraft client/player/network objects are unavailable, skip macro execution for that tick.
- If a command/message send call fails, do not retry automatically.

## Testing And Verification

Build verification:

- `./gradlew build`

Manual runtime checks:

- The mod loads in a Minecraft `26.1.2` Fabric client.
- `G` toggles gamma with no chat/actionbar/toast message.
- Gamma on/off state persists after client restart.
- Enabled gamma value can be changed in settings and persists.
- A macro can be created with backtick and `/엔더`.
- Macro `SEND` sends immediately.
- Macro `TYPE` opens chat with text pre-filled.
- Macro execution does not trigger while typing in chat/settings fields.
- Repeating/toggle macros can be stopped and do not continue after leaving a world.

## Out Of Scope

- Night vision or any potion/status effect.
- Server-side commands or server-side mod behavior.
- Predefined fixed macro slots.
- Chat/actionbar/toast status messages for gamma toggles.
- Bypassing server permissions, chat signing, spam limits, or command cooldowns.
