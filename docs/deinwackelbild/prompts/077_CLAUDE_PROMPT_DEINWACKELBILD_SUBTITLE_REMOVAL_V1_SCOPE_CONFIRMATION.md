# Claude Prompt — DeinWackelbild Redundant Subtitle Removal — STEP 2 SCOPE CONFIRMATION

## Goal

Prepare **STEP 2 — SCOPE CONFIRMATION ONLY** for one isolated fix:

> Remove the redundant subtitle `See your lenticular print in action.` from the Wackelbild interaction hint.

Do not implement anything yet.

The earlier STEP 1 analysis already concluded that this subtitle is redundant because it adds no unique instruction beyond the visible interactive preview plus the primary interaction hint (`Tilt your phone` / swipe fallback).

This iteration must remain strictly limited to subtitle removal.

---

# Mandatory pre-check

Before proposing scope:

1. Read and follow `CLAUDE_PROJECT_INSTRUCTION.md`.
2. Inspect current `git status --short`.
3. Inspect the current working-tree versions of:
   - `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
   - `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
   - `app/src/main/res/values/strings.xml`
   - `app/src/main/res/values-de/strings.xml`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
4. Confirm that the subtitle is still present in:
   - UI code;
   - EN string resources;
   - DE string resources;
   - any instrumentation assertion;
   - Source-of-Truth copy sections.
5. Confirm there are no unrelated tracked working-tree changes.

If unrelated tracked changes exist, report and STOP.

---

# Exact desired behavior

After this fix:

- The primary interaction hint remains exactly as-is:
  - sensor branch: `Tilt your phone`
  - fallback branch: existing swipe fallback wording
- The subtitle `See your lenticular print in action.` is no longer rendered.
- The German equivalent is likewise removed from resources/spec.
- The existing `Spacer(4.dp)` between hint title and subtitle is removed because it becomes unnecessary.
- No typography, color, alignment, placement, spacing to other screen sections, or hierarchy changes are made in this iteration.
- No hint repositioning is allowed yet.
- No ridge/date-badge layering change is allowed yet.
- Transfer disclosure remains unchanged.

This is intentionally a content-removal-only iteration.

---

# Source-of-Truth update

The prior analysis identified these exact documentation changes:

- `DEINWACKELBILD_INTEGRATION_V1.md` §8.6:
  - remove the supporting/subtitle copy requirement;
  - preserve the primary interaction hint behavior for sensor and swipe fallback.
- `DEINWACKELBILD_INTEGRATION_V1.md` §45:
  - remove the approved subtitle copy entry;
  - keep the primary hint wording unchanged.

Do not rewrite unrelated copy or historical sections.

Inspect `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` only to verify whether it contains the subtitle as an explicit contract. If not, leave it unchanged.

---

# Tests

The earlier analysis identified one known instrumentation assertion:

`interactionHint_isDisplayedWithoutScrolling_forTallPortraitImage`

Confirm its exact current implementation.

The expected test change is only to remove the assertion for the subtitle while preserving the assertion that the primary interaction hint remains visible.

Also search for any other references to:
- `wackelbild_hint_subtitle`
- the EN subtitle text
- the DE subtitle text

Do not add unrelated tests.

If removing the string causes any resource/lint issue, report it in scope.

---

# Accessibility

Confirm that removing the subtitle does not remove unique accessibility information because:

- the primary hint remains visible and semantically exposed;
- existing preview semantics/manual accessibility actions remain unchanged;
- no TalkBack-specific instruction depends uniquely on the subtitle.

No accessibility semantics should be changed in this iteration.

---

# Explicitly out of scope

Do not include any of the following:

- moving `Tilt your phone`;
- restyling `Tilt your phone`;
- changing hint typography/color/alignment;
- preview→hint spacing;
- hint→Show-date spacing;
- ridge/date-badge z-order;
- ridge alpha/spacing;
- border/radius;
- transfer disclosure;
- Privacy Policy;
- upload/API/handoff;
- sensor math;
- blend behavior;
- perspective;
- date badge;
- preview sizing;
- navigation/storage/permissions;
- cleanup/refactors.

---

# Expected file scope to confirm

The earlier analysis suggested exactly these likely files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `app/src/main/res/values/strings.xml`
3. `app/src/main/res/values-de/strings.xml`
4. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
5. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Confirm whether this is exact against the current repository state.

If a sixth file is genuinely required, explain why before implementation.

---

# Required STEP 2 output

Return:

1. current `git status --short`;
2. confirmation that the subtitle still exists and where;
3. exact files that will be modified;
4. exact change in each file;
5. exact test assertion(s) to change;
6. exact Source-of-Truth sections to amend;
7. confirmation whether `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` remains unchanged;
8. confirmation that no other strings/copy/behavior are touched;
9. accessibility impact;
10. regression risks;
11. verification commands required for STEP 3;
12. whether real-device validation is still required;
13. any genuine blocker requiring user input.

Then STOP and wait for explicit approval.

Do not modify files.
Do not stage.
Do not commit.
Do not push.
