# Claude Prompt — DeinWackelbild Print-Format-Matched Preview & Rendering — STEP 1 ANALYSIS ONLY

## Corrected product goal

The previous analysis established that SameView sessions are commonly 9:16 / 16:9, while DeinWackelbild only offers fixed physical formats. Merely sending the nearest `format` is insufficient because DeinWackelbild would still crop the transferred JPEGs and its preview would differ from SameView.

Required UX:

1. Determine the best matching supported DeinWackelbild format from stable SameView session/render geometry.
2. Determine that format's exact target aspect ratio.
3. SameView's Wackelbild preview must show that exact final crop/aspect ratio BEFORE the order CTA.
4. The two JPEGs sent to DeinWackelbild must be rendered/cropped to exactly the same target aspect ratio and composition.
5. The date badge, when enabled, must be rendered inside that final target image area.
6. Handoff sends matching `format`, correct `orientation`, and `direction = horizontal`.
7. DeinWackelbild should therefore receive images already matching the selected format and introduce no additional avoidable crop.

Example:
`9:16 portrait session -> nearest family 2:3 -> 10x15 -> SameView preview shows 2:3 crop -> transfer JPEGs use same 2:3 crop -> date is inside it -> handoff format=10x15, orientation=portrait, direction=horizontal`.

This is STEP 1 ANALYSIS ONLY. Do not implement or modify anything. Follow `CLAUDE_PROJECT_INSTRUCTION.md`.

## Current baseline

There is an uncommitted implementation that already adds optional `format`, `orientation`, `direction`, reads final JPEG dimensions, derives orientation, sends horizontal direction, and maps only approximate 3:4/4:3 to `18x24`. Treat it as the current baseline. Do not modify/revert it.

Run `git status --short` first.

## Current partner format families

Observed formats:
- A6 14.9×10.5, A5 21×14.9, A4 21×29.7, A3 29.7×42
- 10×15, 60×40
- 15×20, 18×24, 30×40
- 15×15, 20×20, 30×30, 40×40, 50×50

Analyze these canonical family choices:
- 2:3 -> `10x15`
- A-series -> `a6`
- 3:4 -> `15x20`
- 1:1 -> `15x15`

Do not implement them yet.

## Mandatory Source-of-Truth review

Read at least:
- `CLAUDE_PROJECT_INSTRUCTION.md`
- `CAMERA_WORKFLOW_UX_V1.md`
- `COMPARE_SESSION_RENDERING_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `docs/IMPLEMENTATION_NOTES.md`
- any spec directly governing Wackelbild preview/rendering/date overlay.

Identify any contract that assumes Wackelbild preview/transfer uses the full session viewport and explicitly report conflicts.

## A. Current preview geometry

Trace exactly how `WackelbildScreen` sizes/draws:
- preview container;
- reference/capture;
- ridge overlay;
- perspective/tilt transform;
- date badge;
- border/corners.

Determine current aspect ratio and source, image scaling/crop semantics, whether changing only the container ratio would match JPEG rendering, and portrait/landscape behavior. Cite exact files/functions/lines.

## B. Current transfer geometry

Trace `WackelbildPrintRenderer`, dimension resolver, session viewport/crop transforms, HQ/fallback paths, size loop, date renderer and alignment transforms.

Find the smallest correct point to introduce a target output aspect ratio while preserving:
- identical pair dimensions;
- reference/capture alignment;
- HQ/fallback behavior;
- file-size limits;
- selected partner ratio.

## C. Exact crop semantics

Define mathematically how a session/source rectangle is center-cropped to the selected print ratio without stretching.

For 9:16 portrait -> 2:3 portrait, crop the long axis (top/bottom). For 16:9 landscape -> 3:2 landscape, crop the long axis (left/right).

Determine whether centered crop is correct under existing SameView alignment semantics or whether existing transforms require another anchor.

Reference and capture MUST receive the exact same target crop in session coordinates. No independent per-image crop decisions.

## D. Single geometry source of truth

Determine the smallest shared pure representation/helper so preview and renderer use the same selected format and normalized crop geometry. Avoid duplicate calculations.

Answer explicitly:
How do we guarantee that the composition visible in SameView immediately before ordering corresponds to the composition sent to DeinWackelbild, despite different preview/JPEG resolutions?

## E. Selection timing

The target must be known before CTA. It therefore cannot depend on temporary JPEG dimensions generated after CTA.

Determine which stable session geometry available to the screen should select the family once, and how that exact selection is passed/used by the renderer/handoff so rounding cannot select another family later.

## F. Date badge

Trace date placement in preview and transfer renderer. Determine the minimal geometry change so enabled date:
- appears inside the final selected print crop in SameView;
- renders at the equivalent normalized location in the JPEG;
- is not subsequently cropped by DeinWackelbild when format matches.

Do not change styling/text/date source/toggle semantics.

## G. Lenticular behavior

Changing preview geometry must not alter continuous alpha blend, neutral calibration, tilt, ridges, perspective, manual endpoints, accessibility, date toggle, navigation or CTA.

Identify only the geometry-sensitive pieces.

## H. Dynamic selector

Analyze a deterministic nearest-family selector using orientation-independent `shortSide/longSide` and relative crop loss against:
- 2/3 -> `10x15`
- approximately 1/sqrt(2) -> `a6`
- 3/4 -> `15x20`
- 1 -> `15x15`

Confirm mappings for 9:16/16:9, 2:3, A-series, 3:4, 4:5 and square.

## I. Scope isolation

Verify that this Wackelbild-specific crop must NOT alter stored session metadata, source originals, Camera, normal Compare, original export, or unrelated share/export behavior.

## J. Orientation/square

Analyze portrait, landscape, square and EXIF assumptions. For square, determine whether orientation should remain omitted unless partner behavior proves a value is required.

## K. Failure behavior

Analyze safe behavior for missing/invalid geometry, renderer inconsistency, inability to honor target ratio, or partner rejection of a slug.

Do not implement retry/fallback. Recommend minimum behavior that never silently shows one crop and sends another.

## L. Minimal change surface

Identify the smallest production/test/doc file set. Prefer one shared pure selector/geometry helper and minimal preview/renderer changes. No unrelated architecture/refactor.

## Testing analysis

Define the smallest later verification covering:
- selector boundaries;
- 9:16 -> 2:3/10x15;
- 16:9 -> 2:3/10x15;
- 3:4 -> 15x20;
- A-series -> a6;
- square -> 15x15;
- preview target ratio;
- renderer output ratio;
- identical normalized crop for reference/capture;
- date inside target output;
- portrait/landscape;
- handoff receives exactly the preview/render selection.

No full suite by default. Identify only affected unit/instrumentation classes. Real-device visual validation remains required. Do not run tests now.

## Exclusions

Do NOT edit, stage, commit or push. Do NOT add a format selector, redesign UI, alter Camera/Compare/stored sessions/originals, change badge styling, privacy/network behavior, status handling, or unrelated fallback/retry logic.

## Required output

Return:
1. `git status --short`;
2. confirmation no files modified;
3. Source-of-Truth findings/conflicts;
4. exact current preview geometry path;
5. exact current print-render geometry path;
6. mathematical target-crop rule;
7. how identical crop is guaranteed for reference/capture;
8. recommended shared source of truth for selected format/ratio;
9. where selection happens before CTA;
10. how renderer consumes/verifies the same selection;
11. how preview displays the final crop;
12. how date remains inside it;
13. geometry-sensitive vs untouched lenticular behavior;
14. recommended family algorithm/canonical slugs;
15. representative mappings;
16. confirmation Camera/Compare/sessions/originals stay untouched;
17. portrait/landscape/square behavior;
18. safe failure behavior;
19. exact likely production files for STEP 2;
20. exact likely test files;
21. exact docs requiring updates;
22. smallest later test commands/classes;
23. real-device validation requirements;
24. regression risks;
25. confirmation nothing staged/committed/pushed.

Then STOP. Do not proceed to STEP 2 or implementation.
