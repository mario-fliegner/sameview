# Claude Prompt — DeinWackelbild Release / Privacy Consistency V1 — STEP 1 ANALYSIS ONLY

## Context

The DeinWackelbild integration is now functionally working and the print-format/crop/date-margin work has been validated on a real device.

The latest completed block includes:
- deterministic partner print-target selection;
- print-format-matched center crop for preview and transferred JPEGs;
- matching handoff format/orientation;
- metadata-stripped transfer images;
- transfer date badge safe margin;
- targeted automated tests passing;
- manual portrait and landscape validation passing.

This task addresses ONE remaining release concern only:

**Are SameView's current privacy/offline/user-facing claims and release documentation still accurate now that the DeinWackelbild flow can intentionally upload two prepared images to an external partner after explicit user action?**

This is not an implementation task yet.

## Strict workflow

This is **STEP 1 — ANALYSIS ONLY**.

Do NOT:
- edit any file;
- output implementation code;
- change Android permissions;
- change networking behavior;
- change the Wackelbild UI;
- change the partner integration;
- change the website;
- stage, commit or push;
- run build/test suites.

The goal is to identify exact contradictions and the smallest later remediation scope.

## Mandatory baseline check

1. Run `git status --short`.
2. Report the current branch and HEAD commit.
3. Confirm whether the previously validated Wackelbild print-format/date-margin work is now committed and whether the working tree is clean or contains unrelated changes.
4. If unexpected tracked changes exist, report them but do not modify them.

## Mandatory Source-of-Truth review

Read the current project specifications relevant to privacy, release behavior, networking and DeinWackelbild, including at minimum:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/IMPLEMENTATION_NOTES.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `RELEASE_HARDENING_AUDIT_V2.md` if present in the repository
- any privacy/security/release specification referenced by those documents.

Also inspect the current Android implementation/configuration needed to establish facts, including:
- `AndroidManifest.xml` variants relevant to release;
- Gradle/build configuration controlling the partner integration/key;
- the DeinWackelbild handoff/network path;
- the UI text immediately before the external transfer;
- any in-app About/Privacy/Settings text or links that make offline/no-upload/privacy claims;
- localized German and English strings where applicable.

Search the repository for claims/terms such as, but not limited to:
- offline / works offline;
- no internet / no network;
- no upload;
- photos/images never leave the device;
- third parties / external services;
- privacy / Datenschutz;
- INTERNET permission;
- DeinWackelbild / deinwackelbild.de / LENTIPRINT;
- metadata / EXIF;
- share / transfer / upload.

Do not assume a claim exists merely because an older discussion said it did. Quote/report only what is actually present in the current repository.

## External website boundary

Do NOT modify or research the SameView website in this step.

If the Android repository contains links to an external privacy policy/terms page, identify the exact URLs/route references and state that their live contents require a separate website-project check. Do not infer their current wording from the Android repository.

Likewise, do not browse or edit DeinWackelbild's website in this analysis unless the repository's Source-of-Truth explicitly requires verification of a specific partner privacy fact. This task is primarily repository consistency analysis.

## Questions to answer

### 1. What data actually leaves the device?

Trace the implemented flow and establish precisely:
- what images are transferred;
- whether exactly two images are transferred;
- whether they are derived/temp render outputs rather than stored originals;
- whether date text may be rendered into them;
- whether EXIF/metadata is stripped;
- destination/service;
- whether transfer occurs only after an explicit user action;
- whether the transfer is optional;
- whether the external configurator is then opened;
- whether any other session metadata is sent by this feature;
- whether partner format/orientation/direction fields are sent and whether they constitute personal/session data.

Distinguish verified implementation facts from assumptions.

### 2. INTERNET permission and release behavior

Determine the exact current state:
- Is `android.permission.INTERNET` present?
- In which manifest/source set?
- Does it ship in release?
- Is the real partner integration enabled in release?
- How is the partner key injected without exposing the secret?
- Does any build variant remain offline-only?
- Is there any fallback/demo/mock behavior relevant to privacy claims?

Do not print or expose the partner key or any secret value.

### 3. Find every contradictory or stale claim

Create a precise inventory of current repository text/docs that are no longer accurate or are potentially misleading because of the optional DeinWackelbild upload.

For each item report:
- full file path;
- line/section;
- exact relevant wording or a short faithful excerpt;
- why it conflicts with actual behavior;
- severity:
  - release-blocking contradiction;
  - should update for clarity;
  - still accurate/no change.

Pay particular attention to absolute claims such as “never”, “no data leaves the device”, “no Internet permission”, or “fully offline”.

Do not broaden this into unrelated privacy improvements.

### 4. User disclosure immediately before transfer

Inspect the actual Wackelbild UI and strings.

Determine whether the user is clearly informed, before the network transfer starts, that:
- two images will be transferred to DeinWackelbild/LENTIPRINT or an external service;
- this is necessary to continue to the external configurator/order flow;
- the action is user initiated.

Assess factual adequacy only against the project's existing privacy/release contract. Do not redesign the screen or invent legal wording.

If German and English differ materially, identify that.

### 5. Privacy policy / terms linkage

Identify:
- where the app links to Privacy Policy / Datenschutz;
- where it links to Terms/AGB if relevant;
- DE and EN routes if both exist;
- whether the app itself contains substantive privacy claims or mostly delegates to the website.

If live website content is not in this repository, explicitly separate:
1. Android-repository changes that can be resolved here;
2. website privacy/terms content that must be checked in the separate SameView website project before release.

Do not claim the website is wrong unless its content is actually available in this repository.

### 6. Google Play implications

Using only repository/project documentation and current Android behavior, identify what Play Console declarations may need re-checking because of this feature.

Do NOT make speculative legal conclusions.

At minimum analyze whether the feature could affect:
- Data safety answers;
- privacy-policy accuracy;
- disclosure of third-party image transfer;
- network/data collection versus transient processing distinctions;
- release notes/testing requirements if already documented by the project.

Clearly distinguish:
- facts established from the app;
- Play Console items that require manual verification outside the repository.

Do not change Play Console configuration.

### 7. Minimal remediation strategy

Recommend ONE minimal release-consistency strategy.

The likely principle is:
- preserve the optional DeinWackelbild feature;
- preserve explicit user initiation;
- preserve metadata stripping;
- replace only absolute offline/no-upload claims that became false;
- describe the partner transfer narrowly rather than weakening SameView's general privacy positioning;
- keep unrelated offline behavior/privacy guarantees intact.

But derive the recommendation from the actual repository evidence.

Do not propose analytics, telemetry, consent frameworks, new permissions, architectural changes, or broad privacy rewrites unless the current implementation genuinely requires them.

### 8. Exact likely STEP 2 scope

List the smallest likely set of Android repository files that would need modification later.

Separate:
- Source-of-Truth docs;
- Android strings/UI text;
- manifest/configuration, only if actually inconsistent;
- tests, only if user-visible wording/behavior tests require updates.

Also list items that belong to the separate website project or Play Console and therefore must NOT be modified in this Android task.

### 9. Verification plan

For the later implementation, propose the smallest relevant verification only.

Do NOT default to a full suite.

If the eventual changes are docs/strings only, identify the narrowest checks needed. If an Android UI test currently pins changed disclosure text, name the exact test/class.

State whether real-device validation is actually needed for the eventual remediation and why.

## Scope exclusions

Do not change or re-analyze:
- print-format selection;
- center crop;
- transfer date margin;
- tilt/ridges/perspective;
- camera;
- Compare;
- session storage;
- originals handling except where needed to establish what is uploaded;
- API retry/error handling;
- partner-key implementation except to establish release/privacy facts;
- website content itself;
- Google Play Console itself;
- unrelated legal text.

## Required output

Return:

1. branch, HEAD and `git status --short` summary;
2. relevant Source-of-Truth findings;
3. exact implemented outbound-data flow;
4. exact INTERNET/release/key-injection state without revealing secrets;
5. complete inventory of stale/contradictory repository claims with paths and line/section references;
6. assessment of the current pre-transfer user disclosure in DE and EN;
7. current in-app Privacy/Terms link routes;
8. website-project items requiring separate verification;
9. Play Console items requiring manual re-check;
10. ONE recommended minimal remediation strategy;
11. exact likely STEP 2 Android file scope;
12. files/components explicitly not requiring changes;
13. smallest later verification scope;
14. release risks if nothing is changed;
15. confirmation that no files were modified, no tests/builds/API calls were run, and nothing was staged/committed/pushed.

Then STOP. Do not implement anything.
