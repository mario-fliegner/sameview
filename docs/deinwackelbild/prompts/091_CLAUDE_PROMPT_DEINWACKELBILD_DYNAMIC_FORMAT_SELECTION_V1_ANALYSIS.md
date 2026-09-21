# Claude Prompt — DeinWackelbild Dynamic Format Selection — STEP 1 ANALYSIS ONLY

## Problem correction

The previous implementation was too narrow.

The actual product goal is NOT merely:

> if SameView happens to render 3:4, select 18x24.

The goal is:

> SameView already has the final two rendered JPEG files. It must inspect their actual dimensions/aspect ratio and preselect the DeinWackelbild print format whose aspect ratio best matches those transferred images, so that the configurator starts with the least/no avoidable crop.

The real-device test has now demonstrated why this matters:

- SameView portrait session -> DeinWackelbild correctly receives portrait + horizontal tilt, but format remains A6.
- Another transferred session is landscape and also remains A6.
- Therefore the current 3:4-only mapping does not solve the original problem for arbitrary SameView output geometry.

This prompt is **STEP 1 analysis only**. Do not implement or modify anything.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and the relevant Source-of-Truth documents.

## Current implemented baseline

The current uncommitted implementation already:

- reads the actual dimensions of both rendered JPEGs;
- only trusts them when both are readable and identical;
- derives `orientation` from those dimensions;
- explicitly sends `direction = "horizontal"`;
- sends `format = "18x24"` only when the dimensions are approximately 3:4 / 4:3 using the approved integer tolerance;
- otherwise omits `format`.

The two targeted unit-test classes currently pass 79/79.

Do not revert or change this implementation during STEP 1.

## Live partner facts already discovered

The create-handoff API supports top-level:

- `format`
- `orientation`
- `direction`

Known public configurator format slugs/options:

- `a6` -> 14.9 × 10.5 cm
- `a5` -> 21 × 14.9 cm
- `10x15` -> 10 × 15 cm
- `15x20` -> 15 × 20 cm
- `a4` -> 21 × 29.7 cm
- `18x24` -> 18 × 24 cm
- `15x15`
- `20x20`
- `30x30`
- `a3` -> 29.7 × 42 cm
- `30x40`
- `40x40`
- `50x50`
- `60x40`

The API itself does not expose a canonical format list. Treat these as the currently observed partner formats, not an immutable API contract.

Orientation is a separate parameter, so compare format ratios independent of portrait/landscape ordering.

## Goal of this analysis

Design the smallest deterministic rule that chooses the **currently available DeinWackelbild format with the closest aspect ratio to the actual rendered SameView JPEGs**.

The selection must be based on geometry, not on assumptions about camera/session types.

No UI should be added.

## Mandatory analysis

### 1. Inspect current repo state

Run:

```powershell
git status --short
git diff -- app/src/main/java/com/isardomains/sameview/net/deinwackelbild/DeinWackelbildDtos.kt
git diff -- app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildHandoffOrchestrator.kt
```

Read the relevant current specs and tests.

Do not modify anything.

### 2. Establish actual SameView output geometry

Trace the complete path that determines the final JPEG width and height:

- session/reference geometry;
- visible/cropped composition;
- print render target;
- HQ path;
- fallback/size-loop path;
- any rounding/even-dimension normalization.

Determine whether the two final rendered JPEGs are the definitive geometry source for format selection. Identify any case where selecting from their actual pixel ratio would be wrong.

### 3. Analyze the real-device examples

Using the implementation/runtime information available in the repo, determine how to obtain or log/inspect the actual final JPEG dimensions for the two real-device cases shown by the user.

Do not add logging yet.

If exact dimensions from those already-completed handoffs are no longer recoverable, say so explicitly and identify the smallest runtime observation needed during the next handoff.

### 4. Build the partner format ratio table

For each currently observed non-square partner format, calculate its orientation-independent ratio using:

`shortSide / longSide`

Include:

- A6 10.5/14.9
- A5 14.9/21
- 10x15
- 15x20
- A4 21/29.7
- 18x24
- A3 29.7/42
- 30x40
- 60x40

For square formats, ratio = 1.

Group formats that are effectively the same ratio.

Important examples:

- 15x20, 18x24, 30x40 -> 0.75
- 10x15 and 60x40 -> 2/3
- A-series -> approximately 1/sqrt(2)
- square -> 1.0

Verify the exact ratios numerically.

### 5. Define closest-ratio selection

Propose a deterministic mapping from actual JPEG ratio to the closest supported ratio family.

The algorithm should:

1. normalize image geometry to `shortSide / longSide`;
2. compare it against supported ratio families;
3. choose the family with the smallest ratio error;
4. map that family to one canonical format slug to send.

Analyze whether absolute ratio difference is sufficient or whether relative error is materially better. Keep it simple unless evidence requires otherwise.

### 6. Canonical format within equal-ratio families

This is important.

Several physical sizes share the same ratio. SameView must choose one slug even though the goal is only to control crop/aspect ratio.

Analyze what canonical slug should be used for each family without pretending that SameView knows the user's desired physical print size.

Consider whether choosing a middle/default-ish size is safe, whether choosing the smallest size is less intrusive, and whether the configurator allows the user to change physical size afterward without losing the images/configuration.

Do not silently decide this if product semantics are ambiguous. State the tradeoff clearly.

The already approved 3:4 choice is `18x24`.

For other equal-ratio families, recommend a canonical slug and justify it.

### 7. Boundary behavior

Calculate the mathematical boundaries between the supported ratio families.

Determine what happens for common SameView ratios such as:

- 9:16
- 2:3
- A-series / sqrt(2)
- 3:4
- 4:5
- 1:1
- arbitrary device viewport ratios

Show which partner ratio family each would select and the resulting crop mismatch.

Identify whether there should be a maximum allowed mismatch beyond which SameView should omit `format` rather than select a poor match.

This is crucial: do not assume "closest" is always acceptable if every available format would crop heavily.

Recommend a threshold only if it is justified quantitatively.

### 8. Orientation

Confirm that orientation remains independently derived from actual JPEG width/height and that the selected format slug does not need to be transposed.

### 9. Partner behavior risk

Analyze the consequences of preselecting a concrete physical format merely to obtain the correct aspect ratio:

- Does it accidentally imply a physical print-size preference?
- Can the user still change size?
- Could changing size move them to another aspect-ratio family and reintroduce crop?
- Is there any better API behavior Olaf could expose, such as an aspect-ratio-only or closest-format mode?

Do not contact Olaf. Just identify whether we need him before implementation.

### 10. Minimal implementation strategy

Compare these approaches:

A. Static table of currently known partner ratio families + closest-ratio selection.
B. Explicit mapping only for known SameView ratios.
C. Omit format unless exact/near-exact supported ratio.
D. Any simpler robust alternative supported by the current API.

Recommend one based on the actual goal: best initial configurator match for arbitrary SameView transfer geometry.

Keep the change local. Do not propose architecture/refactoring.

## Separate issue remains excluded

Do NOT investigate or fix the separate problem where one transferred image may lack the date overlay.

Do NOT change:
- rendering;
- date badge;
- preview UI;
- navigation;
- upload bytes;
- privacy;
- status endpoint;
- response configuration parsing.

## Required output

Return:

1. current `git status --short`;
2. confirmation that STEP 1 changed nothing;
3. actual code path that determines final JPEG geometry;
4. whether final rendered JPEG dimensions are the correct source of truth;
5. whether the dimensions of the two already-observed real-device handoffs can still be recovered;
6. exact ratio table for all current partner formats;
7. grouped ratio families;
8. deterministic closest-ratio algorithm;
9. absolute-vs-relative error recommendation;
10. mathematical boundaries between ratio families;
11. mapping for 9:16, 2:3, A-series, 3:4, 4:5, 1:1 and representative arbitrary ratios;
12. expected crop mismatch for those examples;
13. whether a maximum mismatch threshold is needed and, if so, its justified value;
14. recommended canonical format slug for each ratio family and the product tradeoff;
15. confirmation orientation remains independent;
16. partner/API risks;
17. whether Olaf must be asked anything before implementation;
18. minimal implementation strategy;
19. exact likely files for STEP 2;
20. smallest relevant tests for a later implementation;
21. confirmation no files were modified/staged/committed/pushed.

Then STOP. Do not implement.
