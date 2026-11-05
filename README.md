# 🧩 Navhi Patches

Morphe patches for apps I use daily. Each app gets its own section and its own
version targets — nothing here is generic.

Currently: **GreatDay HR** (`com.dataon.sunfishgo`).

## ➕ Add to Morphe

**One tap** (open on the phone that runs Morphe Manager):
[**Add to Morphe**](https://morphe.software/add-source?github=ravhirizaldi/navhi-morphe-patches)

**Manually:** Morphe Manager → patch sources **+** → **Remote** → paste
`https://github.com/ravhirizaldi/navhi-morphe-patches`

Patched builds are signed with a different key, so uninstall the store version
first. App data is wiped by that switch — back up anything local before you do it.

> GPLv3 · Not affiliated with any vendor listed here. Patches target only the
> versions below; the app's own server-side checks stay in place.

## 🩹 Patches

<!-- PATCHES_START EXPANDED -->

### GreatDay HR

| 💊 Patch | 📜 Description |
|----------|----------------|
| **Disable root detection** | Every `IRoot` check reports the device as unrooted. |
| **Disable mock location detection** | Mock-location checks answer false, and a `loc` action exposes the pinned point. |
| **Disable developer options and emulator detection** | Developer options and the emulator flag answer false. |
| **Manual location picker** | Every location read returns the pinned point instead of the provider, until cleared. |
| **Morphe Settings menu** | Overflow menu entry on the location screen: pin map, detection switches, about. |

#### ⚙️ Morphe Settings

The patch adds **Morphe Settings** to the location screen's overflow menu: a map
to set or change the attendance point, three switches (mock, developer options,
emulator — all off by default), and the about section. The pin survives restarts
once saved and can be changed at any time; nothing is picked on the app's own map
screen.

<!-- PATCHES_END -->

## 🚀 Releasing

GitHub Actions is dead on this account (billing), so releases are manual:

```bash
GITHUB_ACTOR=ravhirizaldi ./release.sh patch     # 1.0.1 -> 1.0.2
```

It bumps `gradle.properties`, rebuilds, regenerates `patches-list.json` and
`patches-bundle.json`, commits, tags, pushes and publishes the release with the
bundle attached. Merge the branch into `main` afterwards — Morphe Manager reads
the source from `main`.

## 🛠️ Building

```bash
GITHUB_ACTOR=<you> GITHUB_TOKEN=<token with read:packages> ./gradlew buildAndroid
```

The bundle lands in `patches/build/libs/patches-*.mpp` and is applied with
[Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) or Morphe Manager.
A GitHub token is required because the Morphe Gradle plugin resolves from GitHub
Packages, which authenticates even for public artefacts.

## 📜 License

GPLv3. Patches are original work; the patched apps are not included.
