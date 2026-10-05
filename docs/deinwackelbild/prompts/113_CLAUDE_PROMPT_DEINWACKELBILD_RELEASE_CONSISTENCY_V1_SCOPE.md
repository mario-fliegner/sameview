# Claude Prompt — SameView Release / DeinWackelbild Release Consistency V1 — STEP 2 SCOPE CONFIRMATION

## Repository
`C:\data\work\privat\git-repos\sameview-release`

STEP 1 is complete. This is **STEP 2 — SCOPE CONFIRMATION ONLY**.

Do not modify, stage, commit or push anything.

The website work is complete (`v0.0.7`); do not reopen it.

## Single problem
Define the smallest coherent changes required in `sameview-release` so its current release / Google Play / Data Safety documentation accurately reflects the optional DeinWackelbild order flow.

Do not perform a general release audit.

## Authoritative product facts
- Normal/core SameView use remains local/offline.
- DeinWackelbild is a separate optional feature.
- Transfer starts only after explicit user action.
- Immediately before transfer the app discloses that the two images go to DeinWackelbild.de and ordering is completed there.
- Exactly two prepared JPEGs are transferred: reference + capture.
- They are derived/prepared copies, not stored originals.
- They contain no EXIF/GPS metadata.
- Enabled date text is rendered into image pixels.
- Transfer uses HTTPS.
- `INTERNET` is declared for this optional flow.
- `ACCESS_NETWORK_STATE` is present transitively through AndroidX Media3; SameView-owned code does not use it and it does not itself permit data transmission.
- Recipient/operator/seller: **O.Schulze / M.Monka GbR (DeinWackelbild), Wolfener Str. 32-34, 12681 Berlin, Germany.**
- The physical print order is completed with DeinWackelbild under its own terms and is separate from the SameView Google Play purchase.
- SameView receives **10% commission** on orders attributable to this handoff. Do not invent the calculation base.
- If no order is completed, DeinWackelbild retains the transferred images for **24 hours and then deletes them**.
- Do not invent retention for completed orders.
- SameView does not receive the transferred images back.
- Do not claim SameView receives no other order/attribution information; this is not established.
- Handoff technical values include partner identifier, format, orientation and direction.
- No SameView session ID or external reference is sent.
- No analytics, telemetry, tracking, automatic upload or background upload is introduced.
- Checkout/configurator opens externally in a Chrome Custom Tab/browser flow.
- Never expose/copy the real partner key.

## Legal boundary
Do not settle:
- SameView's GDPR controller/joint-controller role;
- transfer-specific Art. 6 basis;
- possible Art. 26 implications.

Keep GDPR questions separate from Google Play Data Safety classification.

## Before proposing scope
1. Verify branch, HEAD and `git status --short`.
2. Read repo instructions/source-of-truth docs.
3. Re-open the files identified in STEP 1.
4. Confirm STEP 1 still matches the current repo.
5. If anything materially changed, report it and adjust scope.

## Current Google Data Safety guidance
Verify STEP 1 against **current official Google/Android/Play Console documentation only**.

Determine what the later repository implementation should document about:
- whether the two transferred images are collected, shared, both, or qualify for a user-initiated-transfer exception;
- effect of optional/user-initiated transfer;
- encryption in transit;
- 24-hour retention for abandoned orders;
- whether the 10% commission/commercial relationship affects a sharing exception;
- deletion questions;
- what still requires checking in the live Play Console.

If official guidance does not support a confident answer, state the uncertainty.

## Historical material
Do not rewrite historical release evidence to describe the current app.

Classify candidates as:
- current source-of-truth — must change;
- current supporting doc — should change;
- historical snapshot — do not change;
- external/manual Play Console state.

Old v1.0 release notes should remain historical unless there is a specific current-state reason otherwise.

## Privacy copies
Determine whether Privacy Policy copies in this repo are current submission artifacts that must mirror the corrected public website, historical snapshots, or redundant reference copies.

## Store listing
Preserve truthful local-first marketing. Do not automatically treat “without photo uploads”, “ohne Foto-Uploads”, or “Your photos stay on your device” as false when describing normal use/stored originals.

Only scope store-text changes if materially misleading in context.

## Required STEP 2 report

### 1. Baseline
Branch, HEAD, working tree, changes since STEP 1.

### 2. Files that WILL be modified
For every file give:
- full repo-relative path;
- exact section/content affected;
- factual change;
- replace/append/annotate;
- why required.

### 3. Files explicitly NOT modified
Include historical release notes, store descriptions if unchanged, historical legal copies, and all other considered exclusions, with brief reasons.

### 4. Data Safety target state
State the exact intended repository-level classification/documentation grounded in current official Google guidance.

Separate:
- facts documentable now;
- live Play Console items requiring manual verification;
- unresolved GDPR questions outside Data Safety.

### 5. Privacy-copy target state
Exactly whether/how release Privacy copies synchronize with the website.

### 6. Store/release-note target state
Whether EN/DE store descriptions or release notes change.

### 7. DE/EN parity
All bilingual files that must remain mirrored.

### 8. Exact NO-CHANGE behavior
Confirm no changes to Android code, website, live Play Console, app behavior, permissions, networking, partner integration or old release history.

### 9. Risks
Cover Data Safety misclassification, historical corruption, stale duplicated Privacy, weakened privacy marketing, accidental legal conclusions, partner-key leakage, DE/EN drift.

### 10. STEP 3 verification
Smallest checks: `git status --short`, `git diff --check`, focused diffs, targeted stale-claim greps, DE/EN parity and any repo-specific validation. No Android builds/tests for documentation-only work unless required by this repo.

### 11. Manual follow-up
Exactly what must later be checked/changed in the live Google Play Console.

### 12. Confirmation
No files modified/staged/committed/pushed.

Then STOP and wait for explicit approval before STEP 3.
