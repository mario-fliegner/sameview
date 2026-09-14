# Claude Prompt — DeinWackelbild Preview Hierarchy, Copy Reduction & Ridge Layering — STEP 1 ANALYSIS ONLY

## Goal
Analyze the current implemented Wackelbild screen as a whole after the recent portrait-layout-density change. This is STEP 1 ANALYSIS ONLY: do not modify code, docs, tests, commit, or push.

The user tested the current screen on the real Samsung. The portrait layout/CTA visibility is now much better, but:
1. permanent interaction copy is too verbose and badly grouped;
2. `Tilt your phone` belongs visually to the preview, not below `Show date`;
3. `See your lenticular print in action.` appears redundant;
4. the transfer disclosure is visually heavy and must remain only if a concrete legal/privacy/product reason justifies it;
5. lenticular ridges currently appear not to cover the date badge, but the complete visible print surface including the date badge must carry the ridge texture.

Analyze all of this now, but do not implement. Later implementation may be split to obey one-fix-per-iteration.

## Mandatory Source-of-Truth review
Inspect the actual current working tree first; the previous layout fix may intentionally still be uncommitted pending real-device validation. Then inspect current:
- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `RESPONSIVE_LAYOUT_SYSTEM_V1.md`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
- relevant EN/DE strings
- every project privacy/partner-handoff/upload specification actually relevant to disclosure before transfer.

Report any code/spec drift explicitly.

## A — Interaction hint
The preferred direction is a subtle `Tilt your phone` immediately below the preview and before `Show date`. It should look like a small subordinate interaction hint, not a section heading.

Reconstruct the exact current hierarchy, typography, colors and spacing. Recommend ONE polished treatment, including:
- exact existing typography token (`bodySmall`, `labelMedium`, etc.);
- existing subdued/content color;
- exact preview→hint spacing;
- exact hint→Show-date spacing;
- horizontal alignment;
- whether `↔` improves comprehension or is needless decoration;
- permanent vs disappearing hint (prefer simplest behavior).

Use existing design conventions. No new design system or animation. It must have breathing room but waste little vertical space.

## B — `See your lenticular print in action.`
Determine whether this sentence conveys anything not already conveyed by the visible interactive preview plus `Tilt your phone`.

The user is explicitly willing to change the product spec. Do not keep it merely because current SoT says so. If redundant, recommend removal and identify every code/string/test/spec reference that later must change.

## C — Transfer disclosure
Current text:
`Your two images are sent to DeinWackelbild.de to create your print. You complete the order there.`

Do not assume the current SoT's “privacy-relevant” label means legally mandatory. Trace why this copy exists.

Establish from implementation/docs:
1. exactly when transfer occurs relative to CTA;
2. exactly what data is transferred;
3. documented role of DeinWackelbild as external party/controller/processor, if documented;
4. whether equivalent disclosure already occurs elsewhere before transfer;
5. whether CTA itself initiates transfer or another confirmation exists;
6. whether any legal/privacy document concretely requires this exact persistent pre-CTA disclosure;
7. whether it is merely an internal transparency UX rule.

Clearly distinguish legal requirement, privacy best practice, and internal SameView SoT requirement.

Verdict must be exactly one:
- REMOVE — no concrete requirement found for persistent pre-CTA copy;
- SHORTEN — disclosure is justified/required but current wording is unnecessarily long;
- KEEP — concrete documented reason current content must remain.

If SHORTEN, propose one concise EN and DE candidate. If repository material cannot establish legal necessity, say so; do not invent legal conclusions.

## D — Ridge/date-badge layering
Desired perceptual model: the complete preview is one simulated lenticular print surface. Subtle vertical ridges must therefore visually pass over everything printed on it, including the date badge.

Inspect actual Compose/draw/modifier order and report:
- image A;
- image B;
- date badge;
- ridge overlay;
- rounded clip;
- white border;
- perspective transform.

Identify exactly why the badge currently looks unaffected by ridges.

Recommend the smallest rendering-order correction so ridges are the final surface texture over both images AND date badge, while staying inside the rounded preview clip and perspective-transformed surface. The white outer border should remain a clean edge and should not itself become striped; explain the correct ordering.

Confirm this is preview-only and cannot affect source files, transfer bytes/rendered upload images, Share Image or Video Export.

Do not change ridge alpha/spacing, border values, badge styling or perspective amount.

## E — Target hierarchy and spacing
Evaluate this target:
1. Preview
2. subtle `Tilt your phone`
3. `Show date`
4. optional unavailable-date helper
5. transfer disclosure only if section C genuinely justifies it
6. order CTA

`See your lenticular print in action.` should disappear if B confirms redundancy.

Recommend exact vertical spacing using as few existing spacing values as practical. Preserve the newly-fixed CTA visibility behavior. Check portrait/landscape source images, Compact phones, Expanded/tablets, and portrait/landscape screen orientation.

## F — Accessibility
Analyze TalkBack reading order, whether the visual tilt hint needs accessibility exposure, existing manual swipe/reference/capture accessibility behavior, disabled Show-date explanation, large font, scroll fallback and touch targets.

Do not weaken accessibility to remove visible clutter. If visible copy can disappear but equivalent semantic guidance is genuinely required, identify the smallest mechanism.

## G — Tests/docs/files
For EACH independent recommended later fix, list likely:
- production files;
- EN/DE string files;
- instrumentation/unit tests;
- SoT sections;
- implementation-plan sections only if genuinely inconsistent.

Identify existing tests encoding hint title/subtitle, transfer disclosure, date helper, ridge presence/order, CTA visibility/layout. State which should later change/remain/be added.

Do NOT produce STEP 2 scope confirmation yet.

## H — Explicitly out of scope
Do not propose changes to continuous blend, tilt mapping/thresholds/smoothing, sensor lifecycle, manual Reference/Capture, perspective amount, ridge spacing/alpha, border thickness/color/radius, newly implemented preview sizing/aspect ratio, date badge content/style/position, upload protocol/API contract, partner key, order URL/handoff, storage, permissions, navigation, or print-renderer output. No cleanup/refactor.

## Required output
Return:
1. current exact hierarchy/spacing;
2. recommended target hierarchy;
3. exact `Tilt your phone` treatment/spacing;
4. verdict on subtitle;
5. transfer investigation + REMOVE/SHORTEN/KEEP;
6. ridge/date-badge root cause;
7. smallest ridge correction;
8. border/rounding/perspective confirmation;
9. accessibility implications;
10. phone/tablet implications;
11. SoT conflicts/amendments;
12. likely files grouped by independent later fix;
13. risks;
14. verification;
15. whether implementation must be split under one-fix-per-iteration and safest order;
16. genuine user decisions remaining.

STOP. No files modified.
