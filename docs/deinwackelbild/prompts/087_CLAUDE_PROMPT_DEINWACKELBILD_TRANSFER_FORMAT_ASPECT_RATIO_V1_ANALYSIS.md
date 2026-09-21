# Claude Prompt — DeinWackelbild Transfer Format / Aspect Ratio Analysis — STEP 1 ONLY

## Problem

A real end-to-end test exposed a likely format/aspect-ratio issue in the DeinWackelbild handoff.

Observed live behavior:

- SameView shows the date badge correctly in its own Wackelbild preview.
- On deinwackelbild.de, the default product size appears to be `10.5 × 14.9 cm`.
- With that default size, the transferred image is cropped/reframed such that the date badge may disappear from the visible print area.
- When the user manually selected `18 × 24 cm`, the date became visible on at least one of the two images.
- Therefore the current transferred image aspect ratio and the product format selected by deinwackelbild.de are likely not aligned.
- A second, separate date issue still exists because one image can still lack the date even when the format is changed. **Do not solve that second date issue in this prompt.**

This prompt investigates only the **format / aspect-ratio / API capability** problem.

Do not implement anything.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Goal

Determine:

1. whether the DeinWackelbild API/handoff supports passing a target print size, format, or aspect ratio;
2. if not, which product formats are available on deinwackelbild.de;
3. how SameView currently determines the outgoing transfer image dimensions/aspect ratio;
4. whether both transferred images are guaranteed to have identical dimensions/aspect ratio;
5. what the safest minimal product strategy would be so transferred images fit a supported DeinWackelbild format without losing important edge content such as the bottom-right date badge.

This is analysis only.

---

# Source-of-Truth pre-check

Read all relevant specs first:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- any DeinWackelbild API/client/handoff specification
- any image preparation / print renderer / dimension resolver specification

Explicitly identify any Source-of-Truth requirement covering:
- outgoing image dimensions;
- common aspect ratio;
- print size assumptions;
- cropping behavior;
- handoff parameters;
- partner-side configurator defaults.

If no such requirement exists, say so.

---

# Repository checks

1. Run `git status --short`.
2. Run `git log --oneline --decorate -n 30`.
3. Inspect:
   - `WackelbildDimensionResolver`
   - `WackelbildPrintRenderer`
   - `WackelbildPrintPair`
   - `WackelbildHandoffOrchestrator`
   - DeinWackelbild request models
   - API client methods for session creation / configuration / upload / handoff URL
   - any tests around transfer dimensions/aspect ratio
4. Identify exactly where outgoing pixel dimensions are chosen today.

Do not modify files.

---

# A. API capability analysis

Inspect the actual request/response models and API client.

Answer explicitly:

1. Does SameView currently send any of the following to DeinWackelbild?
   - print width/height;
   - product size;
   - format ID;
   - aspect ratio;
   - orientation;
   - crop mode;
   - print variant;
   - configurator preset.

2. Does the API response expose any field that allows SameView to select or preconfigure those values?

3. Search repository docs/code for any known partner API contract, examples, comments, payload samples, endpoint docs, or OpenAPI fragments that mention:
   - size;
   - format;
   - dimensions;
   - product;
   - variant;
   - layout;
   - crop.

4. If local project material does not answer this, inspect the actual partner API documentation available to the repository/developer environment if present.

5. Do not invent unsupported API parameters.

Return a clear conclusion:
- **API can set format** — exact field/endpoint/value;
- **API cannot set format** — based on available contract;
- **unknown from available material** — and state exactly what external documentation would be needed.

---

# B. Current SameView output geometry

Trace the exact current dimension logic.

For both transfer images determine:

1. source dimensions;
2. orientation handling;
3. rotation handling;
4. scaling mode;
5. crop behavior;
6. final output width/height;
7. final aspect ratio;
8. whether both outputs are forced to one common geometry;
9. whether Reference and Capture can still end up with different dimensions/aspect ratios.

Use concrete examples where useful.

If there is a dimension resolver, explain exactly how it chooses the pair geometry.

---

# C. Live product formats observed on deinwackelbild.de

The user observed the following selectable sizes in the configurator:

```text
A6 · 14.9 × 10.5 cm
A5 · 21 × 14.9 cm
10 × 15 cm
15 × 20 cm
A4 · 21 × 29.7 cm
18 × 24 cm
15 × 15 cm
20 × 20 cm
30 × 30 cm
A3 · 29.7 × 42 cm
30 × 40 cm
40 × 40 cm
50 × 50 cm
60 × 40 cm
```

Treat this as observed UI evidence, not as a guaranteed complete API contract.

Group these by aspect ratio, approximately:

- A-series ≈ `1 : √2`
- `10 × 15` / `60 × 40` = `2:3` or `3:2` depending orientation
- `15 × 20`, `18 × 24`, `30 × 40` = `3:4` or `4:3`
- square formats = `1:1`

Verify these ratios numerically and note portrait/landscape orientation variants.

---

# D. Root cause analysis for the cropping symptom

Explain whether the following hypothesis is consistent with the current code:

> SameView sends an image with one aspect ratio, while deinwackelbild.de defaults to a different print format. Their configurator then uses a crop/cover fit, causing edge content such as the bottom-right date badge to fall outside the visible print area.

Determine whether:
- this is consistent with the actual outgoing geometry;
- the `18 × 24 cm` observation supports a `3:4` match;
- a different default such as A6 (`~1.419`) would necessarily crop a `3:4` (`1.333`) image;
- orientation mismatches also contribute.

Do not overstate certainty. Separate proven code facts from inference from the observed configurator.

---

# E. Minimal product strategy options

Analyze, but do not implement, the safest options.

At minimum compare:

## Option 1 — Pass format through API
If supported:
- preselect the exact partner format matching SameView output;
- preserve current image pixels;
- avoid partner-side crop mismatch.

## Option 2 — Constrain SameView transfer images to one supported aspect ratio
If API cannot control product format:
- choose a supported aspect ratio intentionally;
- render both images to exactly that ratio before upload;
- ensure date badge placement remains inside the final safe area.

Evaluate candidate ratios:
- `3:4`
- `2:3`
- `1:1`
- A-series ratio

Consider:
- compatibility with typical portrait/landscape source sessions;
- how much crop/letterbox would be needed;
- whether SameView currently permits adding padding/letterboxing;
- whether crop would violate current Source of Truth;
- whether both Reference and Capture must share identical final framing.

## Option 3 — Safe-area inset only
Evaluate whether merely moving the date badge further inward would be robust enough across all supported partner formats.

Be critical: if arbitrary product format changes can crop significantly, a larger inset may still be unreliable.

## Option 4 — Let user choose format in SameView
Analyze whether this is warranted or excessive:
- UX cost;
- API support required;
- whether product-format choice belongs on partner site instead.

Do not recommend scope expansion unless necessary.

---

# F. Tests and regression protection

Inspect current tests for:

- final transfer dimensions;
- common pair dimensions;
- aspect ratio;
- crop/fit behavior;
- date badge safe-area placement;
- handoff/product-size parameters.

Explain what is currently covered and what is missing.

Do not add tests yet.

---

# Important scope boundary

Do NOT investigate or fix the separate problem that one of the two images may still have no date.

That is a different defect and must be handled in a later prompt.

Do NOT modify:
- date renderer;
- date selection logic;
- ridge overlay;
- preview UI;
- transfer disclosure;
- API behavior;
- product code;
- docs;
- tests.

Analysis only.

---

# Required output

Return:

1. `git status --short`
2. relevant Source-of-Truth rules
3. exact current outgoing transfer dimension/aspect-ratio logic
4. whether both output images are guaranteed identical geometry
5. exact current API/handoff fields related to size/format/orientation/crop
6. whether the API can preselect a print format — proven conclusion or explicit unknown
7. numeric grouping of the observed partner formats by aspect ratio
8. whether the observed crop/date-disappearance behavior is consistent with the current geometry
9. whether `18 × 24 cm` is a meaningful match to the current output ratio
10. exact root cause hypothesis, clearly separating proven facts from inference
11. comparison of Options 1–4
12. recommended minimal strategy
13. likely files affected in a future STEP 2
14. existing relevant tests
15. missing regression coverage
16. risks to image crop, framing, date visibility, orientation, and partner compatibility
17. whether real-device/end-to-end partner validation will still be required
18. any external partner API information still needed

Then STOP.

Do not proceed to scope confirmation or implementation.
