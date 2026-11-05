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

<!-- PATCHES_END -->

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
