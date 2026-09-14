# Claude Prompt — DeinWackelbild Ridges Over Date Badge — STEP 3 IMPLEMENTATION

## Approval

STEP 1 + STEP 2 are approved exactly as analyzed.

Implement exactly one isolated visual z-order correction:

> Lenticular ridge lines must continue across the optional date badge, while the outer preview border remains clean and unstriped.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

Do not expand scope.

---

# Exact approved file scope

Modify exactly these two files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

No test file modification is required.

If a third tracked file becomes necessary, STOP and report before modifying it.

---

# 1. `WackelbildScreen.kt`

Inside the existing inner clipped preview `Box` (`wackelbild_preview_interactive_area`):

Current effective paint order is:

```text
reference image
capture image
lenticular ridge overlay
optional date badge
```

Change only the composable child order so it becomes:

```text
reference image
capture image
optional date badge
lenticular ridge overlay
```

Implementation requirement:

- Move the existing `WackelbildLenticularRidgeOverlay(modifier = Modifier.fillMaxSize())` call, together with its directly associated explanatory comment, to immediately after the optional date-badge block.
- Do not duplicate it.
- Do not recreate it.
- Do not change any parameters.

This must be a pure reorder of existing blocks.

---

# Preserve all existing visual values

Do not change:

- ridge alpha;
- ridge spacing;
- ridge stroke width;
- ridge color;
- preview border width/color;
- corner radius;
- date badge background;
- date badge text;
- date badge position;
- date badge size;
- blend alpha;
- perspective;
- preview sizing;
- clipping;
- padding;
- sensor behavior;
- interaction hint.

The ridge overlay must remain in the same inner clipped parent so:

- it remains clipped to the rounded preview surface;
- it cannot draw into surrounding padding/background;
- it does not stripe the outer preview border.

---

# 2. `DEINWACKELBILD_INTEGRATION_V1.md`

Amend only §8.10.

Add a concise Source-of-Truth clarification that:

- lenticular ridges are the topmost visual layer of the inner lenticular preview surface;
- they continue across the optional date badge rather than stopping at its edge;
- this preserves the perception of one continuous physical lenticular print surface;
- date badge styling/position/content remain unchanged;
- the outer preview border remains a clean, unstriped edge.

Keep the wording concise and product/UX-oriented.

Do not rewrite unrelated paragraphs.

---

# Accessibility

No accessibility changes are expected or allowed.

The ridge overlay remains decorative/non-semantic.

Do not alter:
- preview semantics;
- date badge semantics/data;
- TalkBack descriptions;
- interaction actions.

---

# Explicitly out of scope

Do not change:

- `Tilt your phone` placement/style;
- subtitle/copy;
- portrait-density behavior;
- preview dimensions;
- ridge tuning values;
- border styling;
- date toggle behavior;
- date formatter;
- upload/share/video/export rendering;
- source image files;
- metadata;
- API/handoff;
- transfer disclosure;
- Privacy Policy;
- navigation/storage/permissions;
- tests unrelated to this screen;
- unrelated docs;
- refactors/cleanup.

---

# Verification — SMALL-SCOPE RULE

Do **not** run the full project suite.

Do **not** run:
- full `connectedDebugAndroidTest`;
- full `testDebugUnitTest`;
- full `assembleDebug`;
- full `lintDebug`.

Run only the relevant Wackelbild instrumentation class on the connected Samsung:

```text
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.wackelbild.WackelbildScreenTest
```

No new automated z-order test is required because the current test architecture only verifies composition/presence, not paint-layer order robustly.

If the class-scoped run fails:
- report the exact failing test(s);
- only fix failures directly caused by this approved two-file change and still within scope;
- otherwise STOP and report;
- do not escalate automatically to the full suite.

Report exact class-run counts:
- started;
- passed;
- failed;
- errors;
- skipped.

---

# Manual Samsung validation

Manual visual validation is required after the automated class-scoped run.

Check:

1. Date badge background/text/position/size still look unchanged.
2. Subtle vertical ridge lines now visibly continue across the date badge.
3. Ridge lines remain subtle and do not materially reduce badge text legibility.
4. Outer white preview border remains clean and unstriped.
5. No clipping or visual artifacts appear at either tilt extreme.
6. Check at least one light underlying photo and one dark underlying photo.

Do not claim visual acceptance until the user confirms it.

---

# Commit handling

Do not stage.
Do not commit.
Do not push.

Leave the two-file implementation available for user visual confirmation.

---

# Required final report

Return:

1. initial `git status --short`;
2. exact two files modified;
3. exact before/after child render order;
4. confirmation the ridge overlay was only relocated, not changed;
5. confirmation the date badge itself was unchanged;
6. confirmation outer border/clipping remain unchanged;
7. exact §8.10 documentation addition;
8. class-scoped `WackelbildScreenTest` result with exact counts;
9. confirmation no full project suite was run;
10. confirmation no third tracked file was modified;
11. exact manual Samsung checks still required;
12. final `git status --short`;
13. confirmation nothing was staged;
14. confirmation no commit was created;
15. confirmation no push occurred.

Then STOP.

Do not start any other task.
