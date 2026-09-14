# Claude Prompt — DeinWackelbild Ridge Over Date Badge — STEP 1 + STEP 2

## Goal

Handle exactly one remaining Wackelbild preview issue:

> The lenticular ridge lines must visually continue across the date badge instead of disappearing underneath it.

This prompt is **STEP 1 ANALYSIS + STEP 2 SCOPE CONFIRMATION ONLY**.

Do not implement anything yet.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Product intent

The preview should read as one physical lenticular surface.

Therefore the subtle vertical lenticular ridge lines must remain visible across:
- the blended image area;
- the optional date badge.

The date badge itself must otherwise remain unchanged.

The preview border must remain clean and must **not** be striped over.

This is a visual z-order correction only.

---

# Mandatory pre-check

Before analysis:

1. Read:
   - `CLAUDE_PROJECT_INSTRUCTION.md`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
   - any directly relevant preview/rendering specification.
2. Run `git status --short`.
3. Confirm the previous interaction-hint work is committed/clean as expected.
4. Inspect the current `WackelbildPreview` child draw order.
5. Confirm whether the current order is still effectively:
   - reference image;
   - capture image;
   - lenticular ridge overlay;
   - optional date badge;
   causing the badge to paint over and hide the ridges.
6. Inspect existing ridge-related and date-badge-related instrumentation tests.

If unexpected tracked working-tree changes exist, report and STOP.

---

# STEP 1 — Exact analysis

Determine:

1. The exact current child/render order inside the preview.
2. Why ridges disappear over the date badge.
3. Whether the minimal production fix is simply relocating the existing `WackelbildLenticularRidgeOverlay(...)` call so it is composed **after** the optional date badge.
4. Whether that yields the intended order:

```text
reference image
capture image
date badge
lenticular ridges
```

5. Confirm the outer preview border remains above/outside this inner ridge layer and therefore stays visually clean.
6. Confirm the ridge overlay remains clipped to the same preview/image shape and does not escape into padding/background.
7. Confirm no ridge alpha, spacing, stroke width, color, perspective, blend, border, radius, or badge styling needs to change.
8. Confirm this affects preview rendering only and cannot affect source images, upload images, sharing, video export, storage, metadata, or date data.
9. Check accessibility impact. The ridge overlay should remain decorative/non-semantic and the date badge semantics should remain unchanged.
10. Check whether the existing implementation already has a test that can meaningfully assert z-order. Do not invent a brittle pixel/screenshot test merely to prove draw order if the current test architecture does not support it robustly.

---

# Source-of-Truth

Inspect the current ridge/lenticular sections, especially the equivalent of §8.10 identified in the prior analysis.

Determine the smallest documentation amendment needed to state that:

- ridges are the topmost **inner lenticular-surface** visual layer;
- ridges continue across the optional date badge;
- the outer preview border remains clean/unstriped.

Do not rewrite unrelated preview behavior.

If the current Source of Truth already says this precisely, no doc change is needed; report that explicitly.

---

# Expected minimal scope

The prior analysis suggested likely scope:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
2. `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`

Tests may require **no modification** if existing instrumentation can only verify presence/render-no-crash rather than visual z-order.

Confirm the exact scope from the current repository.

If a test file genuinely must change, explain exactly why before implementation.

---

# Verification policy — SMALL-SCOPE RULE

Do **not** propose the full project test suite.

This is a tiny, isolated preview draw-order change.

For later STEP 3, use the **smallest relevant automated test scope**:
- preferably only the directly relevant `WackelbildScreenTest` test(s);
- if reliable individual filtering is awkward, the `WackelbildScreenTest` class is the maximum default scope.

Do NOT require full:
- `connectedDebugAndroidTest`;
- `testDebugUnitTest`;
- `assembleDebug`;
- `lintDebug`.

Only request anything broader if analysis identifies a concrete technical necessity, and explain why first.

Manual validation on the Samsung **is required**, because the actual acceptance criterion is visual layering:
- date badge still looks unchanged;
- subtle vertical ridges visibly continue over it;
- ridges remain subtle;
- border remains clean;
- no clipping/artifact appears during tilt/perspective.

---

# Explicitly out of scope

Do not change:

- `Tilt your phone` placement/style;
- subtitle/copy;
- preview sizing;
- portrait-density behavior;
- ridge alpha;
- ridge spacing;
- ridge stroke width;
- ridge color;
- preview border;
- corner radius;
- date badge styling/position/content;
- date toggle;
- blend fraction;
- perspective amount;
- sensor behavior;
- transfer disclosure;
- Privacy Policy;
- upload/API/handoff;
- navigation/storage/permissions;
- unrelated tests;
- unrelated docs;
- refactors/cleanup.

---

# Required STEP 1 + STEP 2 output

Return:

1. current `git status --short`;
2. exact current render order;
3. exact root cause;
4. exact minimal proposed render-order correction;
5. confirmation border remains clean;
6. confirmation clipping remains correct;
7. confirmation ridge/badge styling is unchanged;
8. accessibility impact;
9. source/upload/share/export impact;
10. exact Source-of-Truth section to amend, if any;
11. exact files to modify;
12. whether any test file needs modification and why;
13. exact minimal automated verification for STEP 3;
14. exact Samsung visual checks;
15. regression risks;
16. confirmation all interaction-hint work remains untouched;
17. any blocker requiring user input.

Then STOP and wait for explicit approval.

Do not modify files.
Do not stage.
Do not commit.
Do not push.
