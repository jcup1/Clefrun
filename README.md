# Clefrun

## Milestone 3 smoke test

This milestone generates a beginner 4-bar grand staff exercise in `:core`, converts it to MusicXML, and renders it through the existing Android WebView + OpenSheetMusicDisplay pipeline.

### Run

- Build from the project root with `./gradlew assembleDebug`
- Install/run the `app` module from Android Studio or with your usual debug install flow

### Click path

1. Launch the app.
2. Wait for the `WebView OSMD Render Test` screen to appear.
3. Tap `New exercise`.

### Expected result

- A grand staff with treble and bass clefs appears inside the embedded WebView.
- The score is a generated 4-bar C major, 4/4 beginner exercise with RH melody and LH accompaniment.
- Tapping `New exercise` again generates and re-renders another deterministic seeded exercise without crashing.

### Current limitation

`score.html` loads OpenSheetMusicDisplay from a CDN (SRI-pinned), so the device or emulator needs network access for this milestone.
