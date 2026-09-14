# Claude Prompt — DeinWackelbild Portrait Layout Density — VERIFY AND COMMIT

## Goal

Close the already-implemented portrait-layout-density iteration cleanly **before any new Wackelbild UI changes are started**.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

The previous STEP 3 implementation changed the Wackelbild responsive layout so portrait source images no longer push the order CTA below the fold on normal compact-phone layouts. The user has now visually tested that implementation on the real Samsung device and confirms that the portrait layout / CTA visibility is good enough to accept as the baseline.

Do **not** implement any of the newly discussed follow-up changes in this prompt.

In particular, do NOT yet:
- move ridges above the date badge;
- remove `See your lenticular print in action.`;
- reposition/restyle `Tilt your phone`;
- change transfer-disclosure copy;
- change Privacy Policy;
- make any other Wackelbild visual adjustment.

This prompt exists only to verify and commit the already-approved portrait-density fix.

---

# 1. Inspect the current working tree first

Report the complete current `git status --short`.

Identify the exact uncommitted changes belonging to the portrait-density iteration.

The expected approved files for that iteration are exactly:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
3. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Prompt/archive `.md` files outside the repository's intended tracked documentation must not be staged or committed.

If there are unexpected tracked modifications that are not part of the approved density fix, STOP before staging or committing and report them.

---

# 2. Audit the density implementation against the approved scope

Confirm, without expanding scope, that the current working-tree implementation contains exactly the approved behavior:

- `WackelbildPreview` carries `Modifier.weight(1f, fill = false)` in the outer layout.
- The lower controls column no longer carries the previous `weight(1f)`.
- The lower controls column remains vertically scrollable.
- Preview height remains bounded by:
  1. width/aspect-derived natural height;
  2. remaining incoming height;
  3. the existing 62% content-height ceiling.
- `PREVIEW_HEIGHT_FRACTION_OF_CONTENT` remains `0.62f`.
- `effectiveWidth` continues to preserve the source aspect ratio.
- No crop/distortion behavior was introduced.
- Exactly one `16.dp` spacer separates the date-control group from the current interaction-hint group.
- Current copy remains unchanged in this iteration.
- Existing Expanded/contentMaxWidth behavior remains unchanged.
- No minimum preview-height floor was added.
- §44 of `DEINWACKELBILD_INTEGRATION_V1.md` contains only the approved responsive-layout amendment.
- `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` remains unchanged.

Also confirm the tests added/changed by this density iteration are limited to the previously approved responsive/CTA/spacing/Expanded regression coverage.

Do not “improve” anything discovered during this audit.

---

# 3. Real-device acceptance already supplied by the user

Record the following as the user's manual Samsung acceptance of this iteration:

- portrait-source layout is now substantially improved;
- order CTA is visible without routine scrolling in the tested normal portrait-phone case;
- preview sizing is acceptable enough to establish this as the next baseline.

Do not claim that unrelated follow-up UX issues are accepted. They are explicitly separate:
- ridge/date-badge z-order;
- excessive hint/subtitle copy;
- interaction-hint placement/style;
- transfer disclosure / privacy-policy follow-up.

Do not claim tablet-height behavior was physically validated unless it actually was.

---

# 4. Verification

Run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```

Important:
- The user has explicitly approved proceeding with this verification.
- Do not suppress failures.
- Do not disable tests.
- Do not add lint baselines.
- Do not modify unrelated code to make a failing command green.
- If a failure is caused by the density fix and can be corrected strictly within the same three approved files, report the exact cause before making any correction and keep it surgical.
- If a fourth production/test/doc file would be required, STOP and report instead of expanding scope.
- If a failure appears unrelated or environmental, do not fix it in this iteration; report it and do not commit unless the repository is demonstrably in the agreed clean/verifiable state.

Report the exact connected test count and failure/error/skip counts.

---

# 5. Commit gate

Only if all of the following are true:

- working-tree audit matches the approved density scope;
- no unexpected tracked changes are mixed in;
- all required verification commands pass;
- the user-supplied Samsung acceptance above is recorded;
- only the three approved density files are staged;

then create exactly one commit for this iteration.

Use this commit message:

`Fix Wackelbild portrait layout density`

Do not amend previous commits.
Do not squash.
Do not push.

After committing, run:

```text
git status --short
git show --stat --oneline HEAD
```

Confirm that tracked working tree is clean apart from any intentionally untracked prompt/archive files.

---

# 6. Explicitly forbidden follow-up changes

Even if they are obvious from the current screen, do not touch any of these yet:

- ridge/date badge child order;
- ridge alpha/spacing;
- preview border/radius;
- `Tilt your phone` typography/color/location;
- `See your lenticular print in action.` string or rendering;
- transfer disclosure;
- EN/DE copy;
- Privacy Policy;
- upload/API/handoff;
- sensor/blend/perspective behavior;
- date badge style;
- navigation/storage/permissions.

These will be handled in subsequent one-fix iterations from the newly completed analysis.

---

# Required final report

Return:

1. initial `git status --short`;
2. exact density-fix files found modified;
3. audit result for each approved behavior;
4. confirmation no follow-up UI change was included;
5. `testDebugUnitTest` result;
6. `assembleDebug` result;
7. `lintDebug` result;
8. `connectedDebugAndroidTest` result with exact test/failure/error/skip counts;
9. exact files staged;
10. commit hash and message, if commit gate passed;
11. `git show --stat --oneline HEAD`;
12. final `git status --short`;
13. confirmation no push occurred;
14. remaining manual tablet/Expanded caveat;
15. list the next three independent follow-up fixes, without implementing them:
    - ridge over date badge;
    - subtitle removal;
    - `Tilt your phone` reposition/restyle.

Do not start the next fix in this prompt.
