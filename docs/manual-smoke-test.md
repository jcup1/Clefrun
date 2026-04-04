# Manual Smoke Test

## Practice Hub and Reader Mode

1. Run `./gradlew assembleDebug`.
2. Launch the app and confirm it opens to the Practice Hub.
3. Confirm the Practice Hub shows cards for `Sight Reading` and `Scales, Arpeggios & Cadences`.
4. Tap `Sight Reading` and confirm a score renders automatically.
5. Tap `New` a few times and confirm the score changes.
6. Select a different difficulty in portrait and tap `New`.
7. Rotate to landscape.
8. Confirm the same score remains visible and the screen shows only the score surface plus the compact next button.
9. Rotate back to portrait.
10. Confirm the same score and selected difficulty are still preserved.
11. Return to the Practice Hub and tap `Scales, Arpeggios & Cadences`.
12. Confirm the screen opens directly into a practice page with a single paper score surface and a compact options button in the top-right.
13. Open the bottom sheet and confirm `Mode` and `Key` controls live there, not in the header.
14. Confirm only the currently curated combinations are enabled: `Major` mode with `C`, `F`, and `G` keys.
15. Change between supported keys and confirm the score refreshes automatically.
16. Confirm the score page contains `Scale`, `Arpeggio`, and `Cadence` together on the same rendered sheet.
17. Confirm the `Scale` is shown as a 2-octave quarter-note pattern with visible fingering in both hands and no duplicated top turnaround note.
18. Confirm the `Arpeggio` is shown for two hands over two octaves using quarter notes with visible fingering in both hands, and that the highest note is not repeated at the turnaround.
19. Confirm the technical-practice page does not show placeholder rests just to pad out short bars.
20. Confirm the `Cadence` shows right-hand chords, left-hand single bass notes, no fingering numbers, and begins as a clearly separate lower block on the same score page.
21. Confirm measure numbers are not shown on the technical-practice page.
22. Confirm the technical-practice page also omits the printed `4/4` indication.
23. Confirm the bottom of the score has a bit more breathing room above the bottom sheet.

Expected result:
- The app starts at Practice Hub and navigation between the three screens is clean.
- Rotation does not regenerate the exercise.
- Portrait keeps the existing controls and bottom sheet.
- Landscape shows a clean score-only reading mode.
- The Scales screen opens directly into a practical notation-first workflow without extra setup clicks.
- The technical-practice notation is stable and workbook-like, using curated quarter-note scale/arpeggio patterns, hidden filler spacing instead of visible placeholder rests, and controlled cadence spacing.
