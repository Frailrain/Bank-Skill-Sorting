# Running the RuneLite dev client on Linux (Bolt + Silverblue + podman)

*Documented 2026-07-09, after being cracked end-to-end for the first time —
three failed launches, one root(!) cause. If a future session (human or
Claude) needs to launch the dev client hands-off, start here and don't
re-derive any of it.*

## The one-liner

From the VS Code Flatpak sandbox (or any non-interactive shell):

```sh
flatpak-spawn --host podman exec --user mhersee \
  -e DISPLAY=:0 \
  -e XAUTHORITY=/run/user/1000/xauth_nBCeBf \
  -e XDG_RUNTIME_DIR=/run/user/1000 \
  -e WAYLAND_DISPLAY=wayland-0 \
  -e LD_LIBRARY_PATH=/home/mhersee/.cache/skillbank-nvidia-libs \
  -e __GLX_VENDOR_LIBRARY_NAME=nvidia \
  -e __EGL_VENDOR_LIBRARY_FILENAMES=/run/host/usr/share/glvnd/egl_vendor.d/10_nvidia.json \
  -w /var/home/mhersee/dev/skillbank-plugin \
  dev ./gradlew run
```

**GPU passthrough (117HD / GPU plugins):** the host runs the proprietary
NVIDIA driver; the container only has Mesa, so without the three
`LD_LIBRARY_PATH`/glvnd variables GL silently falls back to llvmpipe
(symptom: `HdPlugin - GPU: llvmpipe` in the log, 1–2 fps under 117HD).
`~/.cache/skillbank-nvidia-libs` is a symlink farm pointing at
`/run/host/usr/lib64/*nvidia*.so*` + `libcuda*` — regenerate it after a
host driver update:

```sh
d=~/.cache/skillbank-nvidia-libs; mkdir -p $d && rm -f $d/*
for f in /run/host/usr/lib64/*nvidia*.so* /run/host/usr/lib64/libcuda.so*; do
  ln -sf "$f" "$d/$(basename $f)"
done
```

From an interactive terminal inside the `dev` container, plain
`./gradlew run` works — the session env is already correct.

## Why each piece is load-bearing

| Piece | What breaks without it |
|---|---|
| `--user mhersee` | **The big one.** `podman exec` enters the container as root. Java resolves `user.home` from passwd, so the client reads an empty `/root/.runelite` — no credentials, no plugins, a fresh `default` profile. Symptom looks exactly like broken creds; it isn't. |
| `DISPLAY` + `XAUTHORITY` | `java.awt.HeadlessException` → `error_game_crash` at boot. On this KDE Wayland session, XWayland is `:0` and the xauth cookie is `/run/user/1000/xauth_nBCeBf` (check `ls /run/user/1000 | grep xauth` — the suffix can change). |
| `dev` container | The host (Silverblue) has no JDK; the `dev` podman container carries Java 11 and shares the home dir. Builds too: `podman exec -w <repo> dev ./gradlew build`. |

## Credentials & profiles (Jagex account via Bolt)

- The game runs through **Bolt**; its RuneLite state lives at
  `~/.var/app/com.adamcake.Bolt/data/bolt-launcher/.runelite/`.
- `~/.runelite/credentials.properties` is a **symlink** into Bolt's copy —
  the [documented dev flow](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts)
  (`--insecure-write-credentials` written by a Bolt-launched client, then
  shared with the dev client). Regenerate by adding that flag to
  `clientArguments` in Bolt's `settings.json` and launching once.
- The main profile ("Active Play") is **cloud-synced** (`sync: true`), so
  the dev client also needs `~/.runelite/session` (the RuneLite-account
  session file) or it silently falls back to a fresh local profile.
  `profiles2/`, `repository2/` (hub plugin jars), and the per-plugin data
  dirs were copied over from Bolt's `.runelite` on 2026-07-09.

## Gotchas

- **Log out of the live Bolt client first** — the account can't be in-game
  twice.
- **Never broad-`pkill` inside the `dev` container** — it shares the host
  PID namespace and can see (and would kill) the live RuneLite session.
- Headless startup-check (no display needed): launch, then grep the log for
  `Loaded plugin SkillBankPlugin` + `Loaded N item metadata entries`; the
  GUI crash after those lines is expected without a display.
- Success signature in the log:
  `SessionManager - Loaded session for …` and
  `ConfigManager - Using profile: Active Play (…)`.
