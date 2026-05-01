# PictureLogin

A small Spigot/Paper plugin that gives players a nicer login message — their head shows up as a picture in chat, with a few lines of text rendered next to it. Originally written by NathanG, then expanded by Lythrilla, and patched here for Paper 1.21.11.

## Why this fork exists

If you're running Paper 1.21.11 you've probably seen this in your console when somebody joins:

```
[PictureLogin] Plugin PictureLogin vR2-1.0.0 generated an exception while executing task 61
java.lang.ArrayIndexOutOfBoundsException: Index 8 out of bounds for length 8
    at me.Lythrilla.picturelogin.config.ConfigManager.getMessage(ConfigManager.java:96)
```

The result of the crash is exactly what it sounds like: the picture message never gets sent. It happens whenever you have eight or more lines configured under `messages` / `first-join-messages` / `leave-messages` (which is the default config), because the loop that copies your configured lines into a fixed eight-slot array uses `>` instead of `>=`. As soon as the ninth iteration runs it tries to write to `msg[8]` and throws.

This fork:

- Fixes the off-by-one in `ConfigManager.getMessage` so messages with eight lines actually send.
- Updates the build to target Paper 1.21.11 and bumps `api-version` to `1.21`.
- Replaces the old `Sound.valueOf(...)` call with the string-key `playSound` overload, which works whether `Sound` is an enum or the new registry-backed interface.
- Recompiles cleanly against modern Adventure (4.17) and `adventure-platform-bukkit` (4.3.4), still shaded into `me.Lythrilla.picturelogin.libs.*` so it doesn't collide with anything else on your server.

## Features

Same as upstream, briefly:

- Player avatar shown in chat on join (and optionally on first join / leave).
- MiniMessage support — gradients, rainbows, hex colours, the usual.
- Per-player message overrides via `users.yml` and per-permission groups via `perms.yml`.
- Optional login sound, configurable per player or per group.
- PlaceholderAPI integration (placeholders + an expansion that exposes avatar lines).
- Soft hooks for AuthMe (waits for auth before showing the message), SkinsRestorer, PremiumVanish.
- Multi-language: ships with `en_US` and `zh_CN`, drop your own YAML in `plugins/PictureLogin/lang/` to add more.

## Installation

1. Drop the jar into `plugins/`.
2. Start the server once so it generates `plugins/PictureLogin/`.
3. Edit `config.yml` to taste. The first-join / leave messages live in the same file; per-player and per-group overrides go in `users.yml` and `perms.yml`.
4. `/picturelogin reload` to pick up changes without a restart.

## Building

You'll need JDK 21 and Maven.

```
mvn clean package
```

The shaded jar lands at `target/PictureLogin-R2-1.0.1.jar`.

## Commands

| Command | What it does |
|---|---|
| `/pl reload` | Reload `config.yml`, `users.yml`, `perms.yml`, and language files. |
| `/pl version` | Show plugin version. |
| `/pl language [code]` | List available languages, or switch to one (`en_US`, `zh_CN`, ...). |
| `/pl debug <login\|firstjoin\|leave\|all> [global\|user <name>\|perm <group>]` | Preview a configured message in-game without rejoining. |

Aliases: `picturelogin`, `piclogin`, `plogin`, `pl`.

## Permissions

The defaults are sensible — `picturelogin.show` is `true` so everyone sees the message, admin operations like reload/language/debug default to `op`. The full list lives in `plugin.yml`.

## Credits

- **NathanG** — wrote the original PictureLogin.
- **Lythrilla** — the R2 rewrite (custom messages, languages, MiniMessage, PAPI expansion, etc.).
- This fork — patched the crash, retargeted Paper 1.21.11, otherwise unchanged.

## License

MIT, same as upstream.
