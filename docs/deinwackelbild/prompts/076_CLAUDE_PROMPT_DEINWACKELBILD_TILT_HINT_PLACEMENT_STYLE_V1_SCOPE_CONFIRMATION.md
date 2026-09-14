# Claude Prompt — DeinWackelbild `Tilt your phone` Placement & Styling — STEP 2 SCOPE CONFIRMATION

## Goal

Prepare **STEP 2 — SCOPE CONFIRMATION ONLY** for the final remaining Wackelbild preview-cleanup item:

> Move the interaction hint so it visually belongs to the preview, and restyle it as a small, subtle instruction.

Do not implement anything yet.

The previous analysis already established the desired product direction:

- `Tilt your phone` should sit **directly below the preview** and **before `Show date`**.
- It should no longer look like a section heading.
- It should use a small existing typography style and subdued existing text color.
- Recommended spacing:
  - preview → hint: **8.dp**
  - hint → Show date group: **16.dp**
- Alignment: centered relative to the preview/content lane.
- No `↔` glyph.
- Keep the hint permanently visible.
- Do not add animation or interaction state.
- Preserve the swipe-fallback wording when the device uses that branch.
- The redundant subtitle `See your lenticular print in action.` is assumed to have already been handled separately if that prior fix has already been completed. If it is still present in the current working tree, report that fact and STOP rather than silently bundling its removal into this scope.

This is the **last of the three independent UI cleanup fixes** identified in the earlier analysis. Keep it isolated.

---

# Mandatory pre-check

Before proposing scope:

1. Read and follow `CLAUDE_PROJECT_INSTRUCTION.md`.
2. Inspect current `git status --short`.
3. Inspect the actual current working-tree versions of:
   - `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
   - `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
   - relevant EN/DE string resources only to confirm whether wording changes are required
4. Confirm whether the earlier two independent fixes are already present:
   - lenticular ridges drawn over the date badge;
   - redundant subtitle removed.
5. Confirm the portrait-density baseline is already committed and not mixed into the current diff.

If unrelated tracked changes are present, report them before proposing scope.

---

# Source-of-Truth conflict to resolve in this fix

The earlier analysis identified a direct Source-of-Truth conflict:

- `DEINWACKELBILD_INTEGRATION_V1.md` §9.9 currently says the interaction hint follows **below the date row**.
- The new approved product decision is the opposite:
  - preview
  - subtle interaction hint
  - Show date

This documentation must be updated in the same implementation iteration if the code is changed.

Also inspect §8.6 and §45 only to ensure the remaining single-line hint wording is still consistent after the previous subtitle-removal iteration.

Do not rewrite unrelated historical/spec content.

---

# Exact desired behavior

The target visual hierarchy for this fix is:

1. `WackelbildPreview`
2. **8.dp**
3. single-line interaction hint:
   - sensor branch: `Tilt your phone`
   - fallback branch: existing swipe fallback wording
4. **16.dp**
5. `Show date`
6. optional unavailable-date helper
7. rest of the order screen unchanged

The hint should be:

- `bodySmall`
- `SameViewSettingsSecondaryText`
- centered
- permanently visible
- no icon/glyph
- no animation
- no new state
- no new string wording
- no accessibility-only replacement unless current code genuinely requires one

The preferred structural direction from the prior analysis was to make the hint a sibling in the **outer non-scrolling Column**, between the weighted preview and the lower scrollable controls Column. Confirm whether that is still the smallest/safest implementation against the current code.

The existing responsive behavior must remain intact:

- preview retains `weight(1f, fill = false)`;
- lower controls remain vertically scrollable;
- CTA visibility improvement remains;
- `PREVIEW_HEIGHT_FRACTION_OF_CONTENT = 0.62f` remains;
- no minimum preview floor;
- Expanded/contentMaxWidth behavior unchanged;
- no source-aspect-ratio behavior change.

---

# Tests to inspect

The prior analysis identified that two spacing tests from the portrait-density fix become conceptually obsolete once the hint moves above the date group:

- `dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperPresent`
- `dateGroupToHintGroup_spacingIsApproximatelySixteenDp_helperAbsent`

Confirm their exact current names/content.

The implementation should later replace/rework only the assertions necessary to cover the new structure, ideally validating:

- preview → interaction hint gap is approximately 8.dp;
- interaction hint → date group gap is approximately 16.dp;
- interaction hint remains visible without routine scrolling for tall portrait source;
- date row remains visible/usable;
- CTA visibility regression coverage remains intact;
- swipe fallback still renders the correct hint;
- Expanded-width composition still works.

Do not add screenshot/golden infrastructure.

---

# Accessibility constraints

Confirm that moving the hint before the date row improves/maintains TalkBack reading order.

Preserve:
- existing preview semantics;
- existing manual Reference/Capture accessibility behavior;
- Show-date semantics;
- optional date-unavailable explanation;
- minimum touch targets;
- scroll fallback;
- large-font robustness.

No accessibility regression is allowed.

---

# Out of scope

Do not include any of the following in this fix:

- ridge/date badge z-order;
- ridge opacity/spacing;
- border/radius;
- subtitle removal if not already completed;
- transfer disclosure wording/removal;
- Privacy Policy;
- upload/API/handoff;
- sensor math;
- blend smoothing;
- perspective amount;
- date badge styling;
- preview sizing formula;
- strings wording changes;
- navigation/storage/permissions;
- cleanup/refactors.

---

# Required STEP 2 output

Return:

1. current `git status --short`;
2. confirmation of the current baseline and whether prior two fixes are already complete;
3. exact files that would be modified for this fix;
4. exact change in each file;
5. exact tests to update/replace and why;
6. exact Source-of-Truth section(s) to amend;
7. confirmation that no string wording changes are needed;
8. confirmation that no unrelated code/docs will be touched;
9. risks:
   - CTA/remaining-height interaction;
   - large font;
   - TalkBack order;
   - tablet/Expanded;
10. exact verification commands to run in STEP 3;
11. whether real-device validation is still required;
12. any genuine blocker requiring user input.

Then STOP and wait for explicit approval.

Do not modify files.
Do not stage.
Do not commit.
Do not push.
