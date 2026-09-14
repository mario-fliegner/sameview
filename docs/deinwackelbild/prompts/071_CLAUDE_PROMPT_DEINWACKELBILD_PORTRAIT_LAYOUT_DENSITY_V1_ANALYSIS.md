# Claude Prompt — DeinWackelbild Portrait Layout Density / CTA Visibility — STEP 1 ANALYSIS ONLY

Continue from the current committed DeinWackelbild implementation. Follow `CLAUDE_PROJECT_INSTRUCTION.md` and the strict project workflow.

## Real-device finding

Manual Samsung testing shows:

- Landscape source image: broadly good. Full primary screen fits and CTA is visible.
- Portrait source image: bad. The preview becomes so tall that lower content and the order CTA require scrolling.
- This is worse when no reference date exists because `Add a reference date to show the date.` appears below `Show date`.
- That helper then sits visually too close to `Tilt your phone`, making the hierarchy cluttered.
- User considers the screen too text-heavy and unclear.
- Requirement: on a normal phone in portrait screen orientation, the complete normal primary Wackelbild interaction should fit in one viewport, including the order CTA. A portrait source preview may therefore be displayed smaller.
- The image must remain fully visible, preserve aspect ratio, and must not be cropped or distorted.
- Landscape-source behavior is already good and must not be broken.
- In the landscape-source case, the gap between `Show date` and `Tilt your phone` could even be slightly larger if a consistent grouping system warrants it.
- Keep scroll as a safe fallback where fitting is genuinely impossible, e.g. accessibility font scaling / unusually short height; do not clip or shrink touch targets.

## Source of Truth — inspect first

Read current committed versions of:
- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- relevant EN/DE strings
- relevant Wackelbild UI/instrumentation tests
- `RESPONSIVE_LAYOUT_SYSTEM_V1.md` if applicable

If code/spec conflict, identify it explicitly. MD Source of Truth remains authoritative unless this new user decision explicitly supersedes a documented behavior.

# STEP 1 — ANALYSIS ONLY

No code. No file modifications. No implementation. No refactor. No STEP 2 yet.

Treat this as ONE problem: **Wackelbild phone-screen vertical density / CTA visibility**, manifested especially by portrait source images.

## 1. Reconstruct exact current vertical layout

Report the actual Compose hierarchy and vertical stack:
- top app bar
- preview
- preview spacing
- `Show date` row
- conditional no-reference-date helper
- `Tilt your phone`
- `See your lenticular print in action.`
- transfer/order disclosure
- CTA
- bottom/system padding
- scroll behavior

Give actual dp paddings/spacers and relevant typography from code, not screenshot guesses.

## 2. Exact root cause

Trace current preview sizing:
- `effectiveWidth` / `effectiveHeight` or equivalent
- aspect-ratio calculations
- max width/height
- portrait-source vs landscape-source behavior
- whether the preview can consume most available viewport height
- whether sizing reserves space for controls/content below
- whether top/system/bottom insets are accounted for

Identify the exact failing point.

## 3. Minimal responsive sizing strategy

Compare only minimal solutions compatible with current structure:
- fixed portrait-preview max height
- viewport-relative max height
- remaining-height-aware bound after required UI
- a smaller existing-layout solution if present

Recommend ONE solution.

Requirements:
- no crop
- no distortion
- full image remains visible
- preserve aspect ratio
- portrait source preview becomes smaller only as needed
- CTA visible without routine scrolling on normal compact phones
- landscape source remains effectively unchanged where it already fits
- portrait/landscape screen orientation remains supported
- avoid one-device hardcoding where a robust height constraint is possible
- do not redesign architecture

## 4. Text hierarchy / redundancy

Analyze each current line against the Source of Truth.

### `Add a reference date to show the date.`
Determine:
- whether spec requires it
- whether disabled toggle semantics already communicate enough
- whether it should stay, shorten, or disappear
- accessibility/usability consequence

### `Tilt your phone` + `See your lenticular print in action.`
Determine:
- whether both are necessary
- whether they duplicate meaning
- whether one concise line can replace the two-line permanent block

### Transfer/order disclosure
Determine:
- what is required for privacy/informed transfer
- what must remain visible before CTA
- do not remove privacy-relevant copy merely to save height

The goal is to remove only genuinely redundant text.

## 5. Spacing/grouping

Inspect actual spacing between:
- preview → date group
- date row → optional helper
- date group → tilt instruction
- tilt instruction → transfer disclosure
- transfer disclosure → CTA

Assess whether the current optional helper destroys grouping.

Do not globally compress all spacing. The likely priority is:
1. constrain portrait preview height
2. remove/shorten redundant copy if justified
3. establish clearer group spacing

Confirm/reject based on code.

Because landscape-source layout already fits, assess whether `Show date` → tilt group can gain slightly more separation without reintroducing overflow.

## 6. CTA visibility / spec amendment

Check whether current Source of Truth explicitly guarantees CTA visibility without routine scrolling.

If not, identify the exact section needing a narrow amendment for this new approved requirement:

> On normal compact phone layouts, preview height must be bounded so the complete primary interaction stack, including the order CTA, fits in the viewport without routine scrolling for portrait source images. The image remains fully visible with preserved aspect ratio and no crop.

Distinguish normal-device behavior from safe scroll fallback for large font / unusually short heights. Do not write or apply the amendment yet.

## 7. Accessibility

Analyze:
- TalkBack impact of helper removal/shortening
- disabled toggle semantics with no reference date
- large font scaling
- need for scroll fallback
- touch-target preservation

Do not reduce touch targets.

## 8. Regression boundaries

Eventual fix must not change:
- continuous blend
- ridge rendering
- white rounded preview border
- perspective tilt
- image crop/content
- date badge when date exists
- manual Reference/Capture behavior
- upload/API
- transfer bytes
- order flow
- navigation
- storage
- permissions

Landscape-source layout remains visually equivalent except an explicitly justified spacing adjustment.

## 9. Test implications

Inspect existing coverage for:
- portrait source
- landscape source
- CTA visibility
- no-reference-date state
- orientation
- large font/accessibility

Identify later test changes/additions only. Do not edit tests.

Later verification must consider:
```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew lintDebug
./gradlew connectedDebugAndroidTest
```
Real-device Samsung validation remains mandatory.

# Required response

Return:
1. exact vertical hierarchy + spacing
2. exact root cause
3. current sizing algorithm
4. ONE recommended minimal sizing strategy
5. each text line: keep / shorten / remove, with reason
6. recommended grouping/spacing
7. required Source-of-Truth amendment and location, if any
8. accessibility/font-scaling fallback
9. likely production/test/doc files implicated — analysis only, NOT approved STEP 2 scope
10. regression risks
11. verification requirements
12. any genuine user decision needed before STEP 2

STOP after STEP 1. No implementation, docs edit, commit, or push.
