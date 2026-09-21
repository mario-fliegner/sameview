# Claude Prompt — DeinWackelbild Handoff Configuration V1 — STEP 2 SCOPE CONFIRMATION

## Workflow state

STEP 1 discovery is complete and approved.

This prompt is **STEP 2 only**. Do not implement anything.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` and the project Source-of-Truth documents strictly.

## Approved behavior

Implement only this narrowly defined V1 behavior in the later STEP 3:

- For a rendered transfer pair with an exact **3:4 / 4:3 aspect ratio**, preselect DeinWackelbild format **`18x24`**.
- Derive `orientation` from the **actual rendered transfer image dimensions**:
  - width < height -> `portrait`
  - width > height -> `landscape`
- Send `direction = "horizontal"` explicitly.
- For any other aspect ratio, **omit `format`**. Do not invent mappings for 16:9, square, A-series, 2:3, or other ratios.
- Add **no UI**.
- Do not change rendering, cropping, date rendering, preview behavior, navigation, or user-visible workflow.
- The separate issue where one transferred image may still lack its date is explicitly out of scope.

For a landscape 4:3 pair, `format` remains `18x24` and `orientation` is `landscape`; do not invent a separate `24x18` format slug.

## Live API facts already proven in STEP 1

The create endpoint accepts these optional top-level JSON fields:

- `format`
  - proven: `a6`, `18x24`
  - omitted -> null/server default
  - invalid -> HTTP 400 `dwb_handoff_format_invalid`
- `orientation`
  - `portrait`
  - `landscape`
  - omitted -> inferred by configurator
  - invalid -> HTTP 400 `dwb_handoff_orientation_invalid`
- `direction`
  - `horizontal` = Seitlich kippen
  - `vertical` = Oben/unten kippen
  - omitted -> current default `horizontal`
  - invalid -> HTTP 400 `dwb_handoff_direction_invalid`

A combined live request with `18x24 + portrait + horizontal` was verified end to end in the configurator.

The server does not protect against image/configuration geometry mismatches, so SameView must not deliberately send contradictory configuration.

## Mandatory Source-of-Truth check

Before defining scope, read the current versions of at least:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `IMPLEMENTATION_NOTES.md` if present/relevant
- any current DeinWackelbild data-minimization/network addendum referenced by the project instructions

Also inspect the exact current code involved in:

- `CreateHandoffRequest`
- JSON serialization
- `WackelbildHandoffOrchestrator`
- `WackelbildPrintPair` / print renderer result
- existing relevant unit tests

Run `git status --short` first.

The known untracked prompt files under `docs/deinwackelbild/prompts/` are not implementation changes. Report the actual current status and do not modify/delete them.

## STEP 2 task

Produce the exact minimal implementation scope for the approved behavior.

### Important design constraint

Before proposing that `WackelbildPrintPair` or `WackelbildPrintRenderer.kt` be changed merely to carry width/height, inspect whether the orchestrator can reliably obtain the actual rendered JPEG dimensions from data already available at handoff time.

Prefer the **smallest change surface**.

Only include renderer/result-model changes if they are genuinely necessary. Explain why if so.

Likewise, do not create a new mapper file unless a separate file is actually justified by the existing architecture. A tiny deterministic mapping should stay in the smallest appropriate existing location if that avoids unnecessary structural change.

### Ratio matching

The implementation must recognize only exact 3:4 / 4:3 geometry from integer rendered pixel dimensions.

Prefer an integer/cross-multiplication comparison such as the conceptual relationship `width * 4 == height * 3` (portrait) or its landscape equivalent, rather than an arbitrary floating-point tolerance, provided overflow is handled safely for the app's supported dimensions.

Do not generalize into a complete format-selection system.

### DTO/response scope

The API now returns a `configuration` object, but SameView V1 does not need to consume it for this feature.

Do **not** add response parsing/model fields solely because the server now exposes them unless existing production logic actually requires them for the approved behavior.

### Documentation scope

Identify the exact Source-of-Truth sections that must change because the create request contract now permits these three optional fields.

Update documentation in STEP 3 only where required to keep the implemented contract consistent.

Do not document the unrelated status endpoint merely because STEP 1 discovered it, unless an existing statement would become directly false because of this implementation. Keep unrelated API discoveries out of this fix.

## Tests

Define the **smallest relevant verification scope**.

Expected unit coverage should prove, as applicable:

- exact JSON field names: `format`, `orientation`, `direction`;
- null/omitted optional behavior;
- portrait 3:4 -> `18x24`, `portrait`, `horizontal`;
- landscape 4:3 -> `18x24`, `landscape`, `horizontal`;
- a non-3:4 ratio -> no `format`, while orientation and horizontal direction remain correct;
- the orchestrator sends these values in the create request.

Reuse existing test classes where possible.

Do **not** prescribe the full test suite for this isolated change.

Do **not** run tests in STEP 2.

For later STEP 3, identify only the affected test classes/commands. State separately that one real-device end-to-end check should still confirm that a 3:4 transfer opens with 18×24 and the correct orientation/Seitlich selection.

## Required STEP 2 output

Return:

1. current `git status --short`;
2. relevant Source-of-Truth findings and any current contract conflicts;
3. exact production files that would be modified;
4. exact test files that would be modified;
5. exact documentation files that would be modified;
6. for each file, precisely what would change;
7. whether `WackelbildPrintRenderer.kt` / `WackelbildPrintPair` actually needs modification, with evidence;
8. whether a new mapper file is actually necessary;
9. exact 3:4 / 4:3 mapping rule;
10. how orientation will be derived from the actual rendered transfer dimensions;
11. confirmation that `direction` will always be explicitly `horizontal`;
12. confirmation that all other ratios omit `format`;
13. confirmation that no response `configuration` parsing is needed;
14. smallest relevant test commands/classes for STEP 3;
15. real-device validation still required;
16. risks/regression considerations;
17. confirmation that no unrelated code/docs will be touched;
18. confirmation that STEP 2 modified nothing.

Then STOP and wait for explicit approval.

Do not implement, edit, stage, commit, push, or run tests in this STEP 2.
