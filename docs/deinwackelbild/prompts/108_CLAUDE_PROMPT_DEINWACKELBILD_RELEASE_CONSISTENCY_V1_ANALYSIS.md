# Claude Prompt — SameView Release / DeinWackelbild Release Consistency V1 — STEP 1 ANALYSIS

## Repository

`C:\data\work\privat\git-repos\sameview-release`

## Task

Perform **STEP 1 — ANALYSIS ONLY** for the release repository after the Android app gained the optional DeinWackelbild ordering integration.

Do not modify files.

The goal is to determine exactly which release / Google Play / legal-copy materials in this repository have become stale, false, incomplete, or potentially inconsistent because SameView can now, only after an explicit user action, transfer two prepared images to DeinWackelbild for an external physical-print order.

This is ONE problem: **release-material consistency for the DeinWackelbild integration**.

Do not turn this into a general release audit.

## Mandatory workflow

Before analysing individual files:

1. Verify current branch, HEAD and `git status --short`.
2. Read the repository's own source-of-truth/instruction Markdown files first.
3. Identify any repo-specific workflow, release, legal, Play Console, privacy or Data Safety specifications.
4. Treat those documents as authoritative unless the facts below explicitly supersede an old technical statement.
5. Report conflicts; do not silently resolve them.
6. Do not edit, stage, commit or push anything.

## Established product facts

Treat these as authoritative for this analysis:

- SameView's normal/core comparison, camera and session use remains local/offline.
- There is no analytics, telemetry, tracking, automatic image upload or background image upload in the Android app.
- DeinWackelbild is a **separate optional feature**.
- Nothing is transferred merely because the user uses SameView normally.
- The transfer starts only when the user explicitly taps the DeinWackelbild order action.
- Immediately before transfer, the app tells the user that the two images are sent to DeinWackelbild.de to create/configure the print and that the order is completed there.
- Exactly two prepared JPEGs are transferred: reference + capture.
- They are derived/prepared files, not the stored originals.
- They contain no EXIF/GPS metadata.
- If the date option is enabled, the date is rendered into the image pixels.
- Transfer is via HTTPS.
- The Android app now declares `INTERNET` permission for this optional flow.
- Recipient/operator/seller:
  **O.Schulze / M.Monka GbR (DeinWackelbild), Wolfener Str. 32-34, 12681 Berlin, Germany.**
- The physical lenticular-print/Wackelbild order is completed with DeinWackelbild under its own terms.
- The physical print purchase is separate from the SameView app purchase through Google Play.
- SameView receives **10% commission** on orders attributable to this handoff.
- Do not assume or invent the calculation base of that commission.
- If no order is completed, DeinWackelbild retains the transferred images for **24 hours and then deletes them**.
- Do not invent a retention period for completed orders.
- SameView does not receive the transferred images back.
- Do not claim that SameView receives no other order/attribution information; that has not been established.
- The handoff includes technical configuration values such as partner identifier, format, orientation and direction.
- No SameView session ID or external reference is sent.
- Checkout/configurator opens externally in a Chrome Custom Tab/browser flow.
- The real DeinWackelbild partner key is secret and must never be copied into release files, reports, prompts or commits.

## Current legal-status decision

The website consistency pass intentionally did **not** invent a transfer-specific GDPR legal basis.

The remaining legal-review questions are:

- whether SameView is a sole controller, joint controller, or neither for the transmission step;
- which Art. 6 GDPR basis applies to that step;
- whether Art. 26 implications exist.

For this release-repo analysis:

- do not decide those questions;
- do not introduce Art. 6 wording;
- do not characterize DeinWackelbild as a processor;
- do not characterize SameView as sole/joint controller for this transfer;
- flag any release material that would require such a conclusion before it can safely be finalized.

## Important interpretation: preserve the local-first positioning

Do **not** automatically classify general marketing language such as:

- “without photo uploads”;
- “ohne Foto-Uploads”;
- “Your photos stay on your device”;
- local/offline/privacy-first wording

as false merely because the optional DeinWackelbild feature exists.

The intended product distinction is:

- normal SameView use remains local/offline;
- stored originals remain on-device;
- only two prepared derived JPEGs leave the device;
- only after the user explicitly starts the optional external print-order flow.

However, absolute technical/legal claims such as these are stale if present:

- SameView has no `INTERNET` permission;
- SameView never performs network communication;
- SameView can never transfer an image to a third party;
- no data ever leaves the device under any circumstances.

Distinguish marketing shorthand about normal use from literal technical/legal absolutes.

## Repository areas to inspect

Inspect the whole release repository for relevance, but pay particular attention to files/areas corresponding to:

- Google Play Data Safety documentation, especially something like:
  `04_PlayConsole/DataSafety.txt`
- release checklists / Play Console submission notes;
- internal EN/DE release notes;
- EN/DE short/full Play Store descriptions;
- legal/privacy-policy copies, especially something like:
  `06_Legal/PrivacyPolicy_*.txt`
- permission declarations or permission checklists;
- offline/network/privacy claims;
- screenshots/captions only if their text contains relevant claims;
- release metadata that says the app has no network access, no INTERNET permission, or no third-party transfer.

Do not assume the filenames above exist exactly. Inspect the actual repository structure.

## Google Play Data Safety — analysis requirements

This is especially important.

Find the repository's current Data Safety answers/notes and determine how the optional DeinWackelbild transfer affects them.

Analyse, but **do not yet rewrite**, the following distinctions:

1. What data type(s) Google Play's current Data Safety taxonomy may treat the two user-selected/prepared images as.
2. Whether the transfer is likely relevant as **collected**, **shared**, both, or subject to an exception under Google's definitions.
3. The fact that transfer is:
   - optional;
   - user initiated;
   - feature-specific;
   - encrypted in transit;
   - to an external commercial print provider.
4. Whether Google's treatment of a transfer initiated by the user to a third party differs from ordinary background collection/sharing.
5. Whether the 24-hour retention for abandoned orders matters to a Data Safety answer.
6. Whether SameView's 10% commission affects the characterization of the third-party relationship.
7. Whether any current declaration that “no data is collected/shared” is still defensible under Google's definitions.
8. Whether deletion-related questions in Data Safety are implicated.
9. Whether any answer depends on the unresolved GDPR controller/legal-basis question. Keep GDPR and Google Play Data Safety concepts separate.

### Current-source requirement

Google Play Data Safety definitions and forms can change.

For this STEP 1 analysis, use **current official Google/Android/Play Console documentation** where necessary to evaluate the existing repository answers.

Prefer official Google sources only for Play Console/Data Safety classification.

Cite/report the exact official pages consulted.

Do not use blogs as authority for Data Safety.

If the repository merely contains a snapshot/checklist and the actual live Play Console form may differ, explicitly distinguish:
- what the repository currently says;
- what current Google guidance says;
- what still has to be checked manually in the live Play Console.

## Privacy-policy copies

Compare any privacy-policy copies in `sameview-release` against the now-established product behavior.

Do not assume they should be independently rewritten if they are intended merely as archived/snapshotted copies.

Determine first:

- whether they are authoritative submission artifacts;
- whether they should mirror the public website Privacy Policy;
- whether they are historical evidence that should remain unchanged;
- whether the release workflow expects them to be refreshed before publication/submission.

Flag exact stale statements and their significance.

## Store listing / release-note analysis

For EN and DE store materials:

- identify wording that is technically false versus wording that remains valid as normal-use marketing positioning;
- do not weaken privacy marketing unnecessarily;
- determine whether the optional print-order feature itself should be mentioned in store descriptions or release notes based on the repository's conventions and the actual release being prepared;
- do not invent promotional copy yet;
- check DE/EN parity.

## Search for stale absolutes

Search repository text for relevant terms and variants, including at least:

English:
- `INTERNET`
- `internet`
- `network`
- `offline`
- `upload`
- `photo upload`
- `server`
- `third party`
- `third-party`
- `share`
- `collect`
- `Data Safety`
- `privacy`
- `permission`

German:
- `Internet`
- `Netzwerk`
- `offline`
- `Upload`
- `hochlad`
- `Server`
- `Dritte`
- `weitergegeben`
- `übertragen`
- `Datensicherheit`
- `Datenschutz`
- `Berechtigung`

Review matches in context. Do not mechanically classify every hit as stale.

## Required output

Return a structured STEP 1 report containing:

### 1. Repo state
- branch;
- HEAD;
- working-tree status.

### 2. Source-of-truth files reviewed
For each relevant instruction/spec/release document:
- path;
- what authority it has;
- whether it conflicts with the current DeinWackelbild behavior.

### 3. Complete relevance inventory
List every repository file that contains DeinWackelbild-relevant release/privacy/network/Data-Safety/store wording.

Classify each as:
- **MUST CHANGE**
- **SHOULD CHANGE**
- **NO CHANGE**
- **HISTORICAL — DO NOT CHANGE**
- **NEEDS MANUAL PLAY CONSOLE VERIFICATION**
- **LEGAL REVIEW DEPENDENCY**

Explain why.

### 4. Exact stale/conflicting claims
For each affected file:
- quote or precisely identify the existing claim;
- explain why it is stale or why it remains valid;
- identify the smallest conceptual correction.

No implementation wording yet unless needed to explain the issue.

### 5. Google Play Data Safety analysis
Separate:
- current repository declaration;
- current official Google definition/guidance;
- likely consequence for SameView;
- uncertainty/manual verification required.

Do not make unsupported legal claims.

### 6. Privacy-policy copy analysis
State whether the release copies should be synchronized with the public website version and why.

### 7. Store listing / release notes
State exactly which EN/DE materials, if any, need changes and why.

Preserve valid local-first wording.

### 8. DE/EN parity
Identify any existing or potential mismatch.

### 9. Explicit NO-CHANGE list
Call out relevant files/areas that should remain untouched.

### 10. Risks
Include:
- Play Store compliance;
- privacy consistency;
- release sequencing;
- stale duplicated legal text;
- accidental weakening of truthful local/offline marketing;
- accidental exposure of partner secrets;
- unresolved GDPR role/legal-basis question.

### 11. Minimal fix strategy for STEP 2
Propose only the smallest coherent release-repo correction.

Do not yet finalize the modification list as implementation approval; this is still analysis.

### 12. Verification strategy for a later implementation
Recommend the smallest appropriate checks. Do not run full unrelated test suites.

### 13. State
Explicitly confirm:
- no files modified;
- nothing staged;
- nothing committed;
- nothing pushed.

Then STOP. Do not implement anything.
