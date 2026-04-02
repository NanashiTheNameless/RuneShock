# RuneShock

RuneShock is a RuneLite external plugin that sends OpenShock commands from in-game events.

It is meant for players who want RuneLite events like damage, death, idle time, or notifications to trigger an OpenShock device.

## Features

- Damage-based rules for low damage and high damage
- Flat or scaled intensity
- Death trigger
- Idle trigger
- RuneLite notification trigger
- Chat keyword trigger
- Global cooldown, intensity cap, and duration cap
- In-game error messages when OpenShock requests fail

## OpenShock Setup

In the plugin config, fill in:

- `Enable RuneShock`
- `API base URL`
- `API token`
- `Shocker ID`
- Optional `Request name`

The plugin sends requests to:

- `POST /2/shockers/control`

## Default Behavior

The default damage setup is:

- `1-19` damage: vibrate with scaled intensity
- `20+` damage: shock with scaled intensity

You can change every trigger in the plugin settings.

## Local Development

Run RuneLite with the plugin loaded:

```bash
./gradlew run
```

Build the plugin jar:

```bash
./gradlew jar
```

Build and install the jar into RuneLite's sideload folder:

```bash
./gradlew installSideload
```

Or use the helper script:

```bash
python3 setup.py
```

## Sideload Notes

RuneLite looks for sideloaded plugins in:

- Linux and macOS: `~/.runelite/sideloaded-plugins`
- Windows: `%USERPROFILE%\\.runelite\\sideloaded-plugins`

Important:

- RuneLite only loads sideloaded plugins in developer mode.
- Sideloaded plugins are not loaded when RuneLite is launched through the Jagex launcher.

## Safety

This plugin can send live OpenShock commands. Double-check your token, shocker ID, cooldowns, intensity limits, and duration limits before enabling it.
