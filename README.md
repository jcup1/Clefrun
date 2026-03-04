# Clefrun

## Milestone 1 smoke test

This milestone proves that Android Compose can host a `WebView`, load `app/src/main/assets/score.html`, and render a hardcoded 4-bar grand staff MusicXML example through OpenSheetMusicDisplay.

### Run

- Build from the project root with `./gradlew assembleDebug`
- Install/run the `app` module from Android Studio or with your usual debug install flow

### Click path

1. Launch the app.
2. Wait for the `WebView OSMD Render Test` screen to appear.
3. Tap `Render test score`.

### Expected result

- A grand staff with treble and bass clefs appears inside the embedded WebView.
- The score shows a 4-bar C major, 4/4 example with simple RH and LH material.
- Tapping `Render test score` again re-renders without crashing.

### Current limitation

`score.html` loads OpenSheetMusicDisplay from a CDN, so the device or emulator needs network access for this milestone.
