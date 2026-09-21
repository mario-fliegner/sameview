# Claude Prompt — DeinWackelbild Date Missing After Handoff Regression — STEP 1 ANALYSIS ONLY

## Problem

A regression exists in the DeinWackelbild integration:

- In SameView, the date badge is visible correctly in the Wackelbild preview when `Show date` is enabled.
- After handing the images off to deinwackelbild.de, the date is no longer visible there.
- This **used to work before**.

Treat this as a regression investigation.

Do not implement any fix yet.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Required Source-of-Truth pre-check

Before analyzing code, read all directly relevant project specifications, especially:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- any spec covering:
  - date badge rendering;
  - print preparation;
  - upload/handoff;
  - temporary files;
  - image preprocessing;
  - metadata stripping;
  - partner API behavior.

If implementation and specification differ, explicitly report the conflict.

---

# Mandatory repository checks

1. Run `git status --short`.
2. Run `git log --oneline --decorate -n 40`.
3. Identify relevant recent commits touching:
   - `WackelbildPrintRenderer`
   - `DateBadgeRenderer`
   - `WackelbildHandoffOrchestrator`
   - temp-file preparation
   - upload image preparation
   - show-date state propagation
   - DeinWackelbild API client
   - Wackelbild screen/ViewModel
4. Use `git log -p`, `git blame`, or targeted diffs where useful to determine **when the working date-overlay handoff behavior changed**.

Because the user explicitly states this worked before, do not treat the current behavior as an unspecified feature gap until history has been checked.

---

# Exact investigation goal

Trace the date from UI state all the way to the files uploaded to DeinWackelbild.

Establish the complete current pipeline:

```text
Show date toggle
→ UI / ViewModel state
→ selected/reference date source
→ badge text formatting
→ print-preparation request
→ DateBadgeRenderer / WackelbildPrintRenderer
→ temporary output files
→ upload request
→ API client
→ handoff/browser result
```

For every stage, identify:

- exact class/function;
- exact value passed;
- whether date/showDate is present;
- whether date is rendered into pixels;
- whether the resulting rendered file is preserved or replaced later.

---

# Questions that MUST be answered

## A. Preview vs upload path

1. Confirm that the on-screen preview date badge and the uploaded-image date rendering are separate code paths.
2. Identify exactly which renderer is supposed to bake the date into the outgoing image(s).
3. Confirm whether the date should be:
   - baked into both images;
   - baked into only one image;
   - or transferred separately as metadata/API data.
4. Cite the Source-of-Truth requirement for this behavior.

## B. Current runtime path

Determine what happens today when `Show date = ON`:

1. Is the boolean/state actually propagated into the print preparation path?
2. Is the formatted date string non-null at render time?
3. Does `DateBadgeRenderer` execute?
4. Does it render into the expected bitmap/file?
5. Are those rendered files the exact files passed to the upload client?
6. Is there a later step that recreates/copies/re-encodes the images from the originals and thereby discards the baked badge?
7. Does metadata stripping or JPEG re-encoding replace the rendered files?
8. Is the file ordering/reference-vs-capture mapping still correct?

## C. Regression history

Because this used to work, determine:

1. Which commit last had the working behavior, if reasonably identifiable.
2. Which later commit changed the relevant path.
3. The exact diff that introduced the regression.
4. Whether the regression came from:
   - showDate state no longer propagated;
   - renderer bypassed;
   - rendered file overwritten/replaced;
   - temp-file path changed;
   - upload source switched back to originals;
   - date value lost;
   - date applied only to a non-uploaded branch;
   - another concrete cause.

Do not guess. Use repository history and code flow.

## D. Existing tests

Inspect all existing tests covering:

- date badge rendering;
- Wackelbild print renderer;
- handoff preparation;
- temp files;
- upload request file selection;
- `showDate`;
- date presence/absence.

Explain why the regression was not caught.

If there is already a test that should have caught it but does not, identify the weakness precisely.

---

# Important distinction

Do **not** investigate the recent ridge-over-date-badge visual change as the cause unless code evidence points there.

That change affects only the on-screen Compose preview z-order and, by prior analysis, is structurally separate from file rendering/upload.

Likewise, do not alter or analyze unrelated preview styling unless required to understand data flow.

---

# No implementation

This is STEP 1 only.

Do NOT:

- modify files;
- add tests;
- change docs;
- stage;
- commit;
- push;
- run a full test suite.

You may run only targeted existing tests if needed to prove where the regression occurs, for example a specific renderer/handoff test class.

If a targeted diagnostic test is useful, explain why and run only the smallest relevant scope.

---

# Required output

Return a concise but complete regression analysis containing:

1. `git status --short`
2. relevant Source-of-Truth requirement
3. current end-to-end date handoff pipeline
4. exact failing point
5. exact root cause
6. proof that this is a regression from previously working behavior
7. relevant commit(s)/diff(s) that introduced it, if identifiable
8. whether outgoing rendered files currently contain the date before upload
9. whether upload uses those rendered files or different files
10. exact existing tests covering this path
11. why tests did not catch the regression
12. minimal fix strategy — **concept only, no code**
13. likely files that would need modification in STEP 2
14. regression risks
15. smallest relevant verification plan for a later implementation
16. whether real-device/end-to-end validation against deinwackelbild.de is still required
17. any uncertainty or blocker that prevents a proven conclusion

Then STOP.

Do not proceed to scope confirmation or implementation until the user explicitly approves the analysis.
