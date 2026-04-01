# Manual Smoke Test

## Orientation and Reader Mode

1. Run `./gradlew assembleDebug`.
2. Launch the app in portrait.
3. Confirm a score renders automatically.
4. Tap `New` a few times and confirm the score changes.
5. Select a different difficulty in portrait and tap `New`.
6. Rotate to landscape.
7. Confirm the same score remains visible and the screen shows only the score surface.
8. Rotate back to portrait.
9. Confirm the same score and selected difficulty are still preserved.

Expected result:
- Rotation does not regenerate the exercise.
- Portrait keeps the existing controls and bottom sheet.
- Landscape shows a clean score-only reading mode.
