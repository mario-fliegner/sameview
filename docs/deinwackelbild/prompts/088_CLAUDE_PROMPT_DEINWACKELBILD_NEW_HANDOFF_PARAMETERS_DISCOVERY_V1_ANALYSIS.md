# Claude Prompt — DeinWackelbild New Handoff Parameters Discovery — STEP 1 ANALYSIS ONLY

## Context
Olaf from DeinWackelbild apparently extended the partner integration after Mario asked whether SameView could pass:
- print format,
- portrait/landscape,
- tilt direction.

Olaf's complete reply was: **"Servus Mario. Probier mal, es sollte jetzt funktionieren."**

No parameter names, allowed values, or updated API documentation were supplied. The previously known SameView contract did not expose these fields.

This is discovery/analysis only. Do not implement anything. Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

## Goal
Determine from the current live partner API whether and how SameView can pass:
1. print format/size;
2. portrait vs. landscape;
3. tilt direction: side-to-side vs. top-to-bottom.

Do not guess parameter names or values.

## Mandatory pre-check
Read:
- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- all locally available DeinWackelbild API contract/examples/docs.

Run `git status --short`. Inspect the existing create-session request model, serialization, API client, handoff response/URL handling, and secure partner-key injection. Identify the exact live create-session endpoint.

Do not modify files. If unexpected tracked changes exist, STOP and report.

## Security
Never print, document, commit, or copy the real partner key into tests/docs. Use the existing secure local injection. Redact partner keys, tokens, signed URL secrets and credentials in the report.

## Discovery approach
First check for updated schemas/docs, response fields, OPTIONS/OpenAPI/Swagger, or useful server validation. If unavailable, use the smallest safe number of live requests necessary. Do not brute-force large parameter dictionaries.

Establish that the existing request still succeeds unchanged and record its non-secret shape and resulting configurator defaults.

### Format
Discover with evidence:
- exact field name;
- exact accepted representation (ID/slug/dimensions/enum/etc.);
- omitted/default behavior;
- invalid-value behavior;
- whether the resulting configurator actually opens with that format selected.

Observed configurator formats include:
`A6 14.9×10.5`, `A5 21×14.9`, `10×15`, `15×20`, `A4 21×29.7`, `18×24`, `15×15`, `20×20`, `30×30`, `A3 29.7×42`, `30×40`, `40×40`, `50×50`, `60×40` cm.

Use only one or two representative live checks, preferably A6 and 18×24. Do not test every format unless the API exposes the complete list itself.

### Orientation
Discover:
- exact field name;
- accepted portrait and landscape values;
- omitted/default and invalid behavior;
- proof that the configurator opens with the requested selection.

### Tilt direction
Discover:
- exact field name;
- accepted values for `Seitlich kippen` and `Oben / unten kippen`;
- omitted/default and invalid behavior;
- proof that the configurator opens with the requested selection.

SameView normally wants side-to-side, but do not implement that yet.

### Combined verification
After discovering all three, perform at most one coherent combined verification, e.g. 18×24 + portrait + side-to-side, using the actual discovered names/values. Verify all three selections survive together.

Also determine whether uploaded image geometry causes the server/configurator to reject or override combinations, especially a portrait 3:4 image with 18×24 portrait. Confirm whether both images still need identical dimensions.

## Scope boundary
Do not change request models, production code, image dimensions/cropping, date rendering, preview UI, tests, or docs. Do not stage, commit, push, or run the full test suite.

This prompt does NOT investigate the separate issue where one of the two images may still lack its date.

If live discovery cannot be done without modifying production code, STOP and explain why.

## Required output
Return:
1. `git status --short`;
2. baseline create-session request shape, secrets redacted;
3. backward compatibility result;
4. exact new format field and proven values;
5. omitted/invalid format behavior;
6. exact orientation field and proven values;
7. omitted/invalid orientation behavior;
8. exact tilt-direction field and proven values;
9. omitted/invalid tilt behavior;
10. evidence each field actually changes the configurator selection;
11. combined-request result;
12. interaction with image dimensions/aspect ratio;
13. whether API exposes the complete valid-format list;
14. discrepancy with current SameView Source of Truth;
15. minimal implementation strategy concept only;
16. likely files for STEP 2;
17. smallest relevant verification plan;
18. anything still requiring Olaf;
19. confirmation no files modified/staged/committed/pushed.

Then STOP. Do not proceed to STEP 2 or implementation.
