# Claude Prompt — DeinWackelbild Transfer Date Safe Margin V1 — STEP 1 ANALYSIS ONLY

## Context
The print-format-matched Wackelbild implementation is complete. Targeted tests passed: 146/146 JVM and 137/137 instrumented on Samsung SM-S911B. Nothing is staged, committed or pushed.

Real-device validation confirmed:
- 9:16 portrait -> SameView 2:3 crop -> DeinWackelbild 10×15 portrait, matching composition.
- 16:9 landscape -> SameView 3:2 crop -> DeinWackelbild 10×15 landscape, matching composition.

The format/crop implementation is NOT the problem here.

## Exact problem
With date enabled, the date badge embedded in the transferred JPEG appears visually too close to the RIGHT and BOTTOM edges in the DeinWackelbild configurator. This occurs in both portrait and landscape.

The SameView in-app preview badge itself looks acceptable.

Previous analysis observed a small additional partner inner crop/inset even for an exact-ratio upload. The intended fix is therefore to investigate a slightly larger safe margin for the TRANSFER IMAGE badge only.

## Workflow
This is STEP 1 — ANALYSIS ONLY.

Do NOT edit files, output implementation code, run tests, make live API calls, alter format selection/crop/preview geometry, stage, commit or push.

## Mandatory Source-of-Truth check
Read current working-tree versions of:
- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `docs/IMPLEMENTATION_NOTES.md`
- any directly governing date-badge/rendering spec.

Run `git status --short` read-only and confirm the current uncommitted baseline. Report any code/spec conflict explicitly.

## Analysis

### 1. Transfer JPEG badge positioning
Trace the complete rendered-transfer date path. Identify exact class/function, current right margin, bottom margin, units/formula, scaling with resolution, portrait/landscape behavior, HQ/fallback behavior, badge-size interaction and clipping risk. Give exact values/formulas and file/line references.

### 2. SameView preview badge
Trace the in-app preview separately. Determine its right/bottom margins, whether independent from transfer margins, whether transfer-only adjustment is technically possible, and whether Source-of-Truth requires exact margin equality or only equivalent date/content/composition.

Product intent:
**Keep SameView preview visually unchanged if contract-safe. Move only the badge embedded in the transfer JPEG farther inward.**

If this conflicts with an explicit spec, report it.

### 3. Quantify existing partner risk
Use ONLY established observations:
- partner inner preview inset approximately 10 px in the observed configurator;
- approximately ~1% per side in that preview;
- previous analysis described current rendered badge margin as roughly 2.2% of short edge.

No new API/network calls.

Using actual current code values, estimate effective right/bottom breathing room after plausible ~1% partner cover crop/inset. Analyze portrait and landscape separately if materially different.

### 4. Compare minimal safe-margin rules
Compare 2–3 small transfer-only alternatives, such as:
- modestly increase existing proportional right/bottom margin;
- add a small partner-specific proportional increment;
- one slightly larger proportional margin for both axes.

For each give exact proposed value/formula, portrait/landscape effect, resolution scaling, preview divergence, test impact and regression risk.

Do not redesign badge. Prefer one deterministic rule rather than orientation-specific magic numbers unless justified.

### 5. Recommend ONE fix
Recommend exactly one minimal rule with a concrete value/formula.

Goal: comfortable safe margin after known partner inset without making badge look detached from corner.

It must, if technically/spec-wise possible:
- affect only embedded transfer badge;
- work portrait + landscape;
- preserve badge style/text;
- preserve format/crop;
- preserve SameView preview;
- preserve toggle;
- preserve HQ/fallback consistency;
- preserve source files;
- add no setting.

### 6. Later file scope
Identify exact smallest likely file set:
- production;
- tests;
- docs only if precise behavior requires documentation update.

Do not include files merely because they are already modified.

### 7. Smallest verification
Propose the smallest targeted later verification. No full suite.

Prefer only the affected renderer instrumentation test class/tests if sufficient, plus one manual portrait and one manual landscape partner check.

State explicitly whether SameView preview tests need changes. If not, say so.

## Explicit exclusions
Do not expand into format selection, crop logic, reframing, partner formats, tilt/ridges/perspective, API fields/networking, privacy, camera, Compare, storage, date redesign, localization, or dispatcher injection. The optional orchestrator IO-dispatcher change is OUT OF SCOPE.

## Required output
Return:
1. `git status --short` summary;
2. Source-of-Truth findings;
3. exact transfer-badge positioning path;
4. exact current right/bottom margin formula/value;
5. preview-badge path/margin;
6. whether transfer-only adjustment is contract-safe;
7. quantified partner-inset impact from existing observations;
8. 2–3 candidate margin rules;
9. ONE recommended concrete rule;
10. exact likely production/test/docs scope;
11. smallest later test command/class;
12. manual portrait/landscape validation required;
13. regression risks;
14. confirmation no files modified and no tests/API calls run.

Then STOP. Do not implement.
