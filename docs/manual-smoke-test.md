# Manual Smoke Test

## Practice Hub and Reader Mode

1. Run `./gradlew assembleDebug`.
2. Launch the app and confirm it opens to the Practice Hub.
3. Confirm the Practice Hub shows cards for `Sight Reading` and `Scales, Arpeggios & Cadences`.
4. Tap `Sight Reading` and confirm a score renders automatically.
5. Tap `New` a few times and confirm the score changes.
6. Select a different difficulty in portrait and tap `New`.
7. Open the Sight Reading bottom sheet and confirm it shows a `Targeted practice` input with the placeholder `e.g. left hand, accidentals, small jumps`.
8. Leave the field empty, tap `New`, and confirm the normal read-ahead coach behavior remains.
9. Type `left hand`, tap `New`, and confirm the coach tip mentions left hand stability.
10. Type `accidentals`, tap `New`, and confirm the coach tip mentions accidentals.
11. Type `small jumps`, tap `New`, and confirm the coach tip mentions small leaps.
12. Type `chords`, tap `New`, and confirm the coach tip does not give chord-specific instructions.
13. Rotate to landscape.
14. Confirm the same score remains visible and the screen shows only the score surface plus the compact next button.
15. Rotate back to portrait.
16. Confirm the same score, selected difficulty, and targeted practice text are still preserved.
17. Return to the Practice Hub and tap `Scales, Arpeggios & Cadences`.
18. Confirm the screen opens directly into a practice page with a single paper score surface and a compact options button in the top-right.
19. Open the bottom sheet and confirm `Mode` and `Key` controls live there, not in the header.
20. Confirm only the currently curated combinations are enabled: `Major` mode with `C`, `F`, and `G` keys.
21. Change between supported keys and confirm the score refreshes automatically.
22. Confirm the score page contains `Scale`, `Arpeggio`, and `Cadence` together on the same rendered sheet.
23. Confirm the `Scale` is shown as a 2-octave quarter-note pattern with visible fingering in both hands and no duplicated top turnaround note.
24. Confirm the `Arpeggio` is shown for two hands over two octaves using quarter notes with visible fingering in both hands, and that the highest note is not repeated at the turnaround.
25. Confirm the technical-practice page does not show placeholder rests just to pad out short bars.
26. Confirm the `Cadence` shows right-hand chords, left-hand single bass notes, no fingering numbers, and begins as a clearly separate lower block on the same score page.
27. Confirm measure numbers are not shown on the technical-practice page.
28. Confirm the technical-practice page also omits the printed `4/4` indication.
29. Confirm the bottom of the score has a bit more breathing room above the bottom sheet.

Expected result:
- The app starts at Practice Hub and navigation between the three screens is clean.
- Rotation does not regenerate the exercise.
- Portrait keeps the existing controls and bottom sheet.
- Landscape shows a clean score-only reading mode.
- Targeted practice text affects only the next generated Sight Reading exercise and does not create chord-specific behavior.
- The Scales screen opens directly into a practical notation-first workflow without extra setup clicks.
- The technical-practice notation is stable and workbook-like, using curated quarter-note scale/arpeggio patterns, hidden filler spacing instead of visible placeholder rests, and controlled cadence spacing.
