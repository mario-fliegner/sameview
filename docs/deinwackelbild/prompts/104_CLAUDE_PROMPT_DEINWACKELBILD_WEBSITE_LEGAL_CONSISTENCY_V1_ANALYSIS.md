# Claude Prompt — SameView Website / DeinWackelbild Legal Consistency V1 — STEP 1 ANALYSIS ONLY

## Repository

Work in the separate SameView website repository:

`C:\data\work\privat\git-repos\sameview-website`

The relevant legal pages are Markdown files under:

- `src/pages/en/privacy/`
- `src/pages/en/terms/`
- `src/pages/en/imprint/`
- `src/pages/de/privacy/`
- `src/pages/de/terms/`
- `src/pages/de/imprint/`

Inspect the actual Markdown files in those directories. Do not assume filenames.

## Context

The Android app now has an implemented and validated optional DeinWackelbild V1 order flow.

Verified Android behavior:

- the feature is optional and explicitly user-initiated;
- immediately before ordering, the app tells the user that two images are transferred to DeinWackelbild.de and that the order is completed there;
- exactly two prepared JPEG images (reference + capture) are transferred;
- these are temporary derived/rendered files, not the stored originals;
- they are metadata-free: no EXIF/GPS is carried in the JPEGs;
- if enabled, the date badge is rendered into the image pixels;
- transfer is via HTTPS to `deinwackelbild.de`;
- after upload, the returned checkout/configurator URL is opened;
- the create request also contains technical partner/configuration values such as partner identifier, format, orientation and direction;
- no SameView session ID or external reference is sent by the current implementation;
- no analytics, telemetry or tracking was added;
- no background or automatic upload occurs;
- the Android app now declares `android.permission.INTERNET` for this approved online flow.

Do NOT expose or search for the private partner key.

The Android repository documentation has already been updated to reflect this exception. This task is now the WEBSITE legal-text consistency review.

## Critical workflow

This is **STEP 1 — ANALYSIS ONLY**.

Do NOT edit files.
Do NOT rewrite legal pages.
Do NOT stage, commit or push.
Do NOT make unrelated SEO/design/content changes.
Do NOT change website implementation/components.
Do NOT modify Android files.
Do NOT modify the separate `sameview-release` folder.
Do NOT make Play Console changes.

Analyze only.

## Mandatory website Source-of-Truth review

Before judging the legal pages:

1. Run `git status --short`.
2. Report branch and HEAD.
3. Read the website project's own instructions/specifications under `docs/` and any root project instruction files that govern:
   - legal pages;
   - privacy;
   - localization;
   - routing;
   - external services;
   - wording consistency with the Android app.
4. If code/spec and legal-page content conflict, identify it explicitly.
5. Preserve the current site's established legal-document structure and DE/EN terminology.

Do not use the Android project's docs as a substitute for the website project's own Source of Truth.

## Pages to inspect exhaustively

Read the current Markdown content under all six directories:

### English
- `src/pages/en/privacy/`
- `src/pages/en/terms/`
- `src/pages/en/imprint/`

### German
- `src/pages/de/privacy/`
- `src/pages/de/terms/`
- `src/pages/de/imprint/`

Also inspect only the minimum routing/layout/config needed to establish:
- the live route generated from each file;
- whether these Markdown pages are actually the site's current legal pages;
- whether DE and EN versions are linked correctly.

Do not change routing.

## Exact analysis questions

### 1. Privacy Policy — DE and EN

Determine whether the current privacy texts accurately cover the new optional DeinWackelbild flow.

Find and report any current claims such as:
- SameView works entirely/fully offline;
- no data leaves the device;
- no photos/images are uploaded;
- no INTERNET/network access exists;
- no third party receives photos;
- data leaves the device only through export/share actions;
- no external services/processors/recipients;
- all processing is exclusively local.

For each relevant passage:
- give full file path;
- section/heading;
- short exact excerpt;
- explain whether it is:
  - still accurate;
  - now misleading/incomplete;
  - directly false because of DeinWackelbild.

Then determine what factual information is missing for the privacy text to describe the implemented flow accurately, including:
- optional/user-initiated nature;
- recipient/service: DeinWackelbild.de / operator identity if already established by the website/legal material;
- purpose: preparing/configuring/ordering the lenticular print;
- two prepared images;
- metadata stripping/no GPS EXIF;
- technical handoff/configuration data where legally relevant;
- HTTPS transfer;
- transition to the external configurator/checkout;
- whether server retention/deletion is currently documented and actually supported by a reliable existing source.

Do NOT invent partner retention periods, legal bases, processor/controller relationships or recipient identity.

### 2. Terms — DE and EN

Read the complete Terms pages and determine whether DeinWackelbild requires a narrow addition or clarification.

Specifically inspect:
- what SameView itself sells/provides;
- Google Play purchase wording;
- third-party services/external links;
- whether orders/contracts for physical products are currently addressed;
- responsibility for external providers;
- payment/order/refund wording;
- warranties/liability wording;
- any statement that could make users reasonably think SameView is the seller of the physical lenticular print.

Determine whether the Terms should clarify, based only on established facts, that:
- SameView provides an optional transition/order handoff to an external DeinWackelbild service;
- the physical-print ordering process is completed on the external provider's site;
- any purchase contract for the physical print is not automatically a SameView/Google Play app purchase.

Do NOT invent contractual relationships, commission terms, refund rules or liability exclusions.

If the actual operator/seller or affiliate relationship cannot be established from available project sources, flag it as information required before wording can be finalized.

### 3. Imprint — DE and EN

Check whether the new integration creates any factual inconsistency in the existing imprint.

Do NOT add DeinWackelbild merely because it is a partner.

Determine whether any existing statement about:
- responsibility;
- external links/services;
- commercial relationships;
- contact/operator identity;
needs adjustment.

If no imprint change is required, say so explicitly.

### 4. Partner / affiliate / commission disclosure

Search the website repo for:
- `DeinWackelbild`
- `deinwackelbild.de`
- `LENTIPRINT`
- `partner`
- `affiliate`
- `commission`
- `Provision`
- `Vergütung`
- `Werbung`
- relevant equivalent wording.

Determine whether the repository establishes:
- who operates DeinWackelbild;
- whether SameView receives commission or other compensation;
- whether this is an affiliate/referral relationship;
- whether any disclosure is already specified.

Do not guess.

If these facts are absent, identify exactly what needs to be confirmed before deciding whether Terms/Privacy/other website text should mention compensation or a commercial relationship.

### 5. DE/EN parity

Compare the German and English versions structurally and substantively.

Report:
- sections present in one language but missing in the other;
- materially different privacy/third-party wording;
- whether any eventual DeinWackelbild amendment must be mirrored in both languages;
- terminology that should remain consistent across DE/EN.

Do not rewrite translations in STEP 1.

### 6. Existing website analytics/cookies/external services

Do not broaden this into a general privacy audit.

However, read enough of the existing privacy pages to understand their current structure. If DeinWackelbild should be added alongside an existing “external services”, “data recipients”, “links”, or similar section, identify the exact section rather than creating an unnecessary new legal architecture.

### 7. Legal uncertainty

This task is a consistency analysis, not a substitute for legal advice.

Separate:
- factual contradictions we can fix from verified implementation facts;
- legal classifications that require a decision or confirmation;
- partner facts missing from the repository.

In particular, do not independently assert GDPR controller/processor status, Art. 6 legal basis, retention periods, affiliate disclosure duties, or seller identity unless current project evidence supports it.

### 8. Minimal remediation strategy

Recommend ONE minimal website strategy.

The preferred principle, if supported by the actual pages, is:
- keep the existing Privacy/Terms structure;
- add a narrow DeinWackelbild exception/disclosure where necessary;
- correct only absolute offline/no-upload statements made false by the feature;
- leave unrelated privacy guarantees intact;
- change Imprint only if a real factual/legal reason exists;
- mirror substantive changes in DE and EN.

No broad rewrite.

### 9. Likely STEP 2 scope

List every website file that would likely require modification.

For each:
- full Windows/repo-relative path;
- why it needs a change;
- what category of wording changes;
- whether it is MUST / SHOULD / NO CHANGE.

If other website source files are not needed, explicitly exclude them.

Also separate unresolved facts that must be answered before STEP 2 can safely approve final wording.

## No external web research by default

First establish everything available from the local website repository.

If the repository itself contains a direct authoritative partner URL or legal reference whose current contents are essential to identify the operator/seller or another required legal fact, report that external verification is needed. Do NOT browse it in this STEP 1 unless explicitly instructed later.

## Verification planning

For a later legal-Markdown-only implementation, propose the smallest verification:
- focused diff;
- repository search for stale absolute claims;
- DE/EN parity review;
- whatever existing lightweight website validation is genuinely required for Markdown front matter/build integrity.

Do NOT automatically propose a full site build/test suite unless these Markdown files require it under the project's own instructions.

## Required output

Return:

1. branch, HEAD and `git status --short`;
2. website Source-of-Truth documents reviewed;
3. exact legal Markdown files found and their generated routes;
4. Privacy DE analysis;
5. Privacy EN analysis;
6. Terms DE analysis;
7. Terms EN analysis;
8. Imprint DE/EN analysis;
9. exact stale/contradictory claims inventory with paths/sections;
10. DeinWackelbild facts already established by the website repo;
11. partner/operator/affiliate/commission facts NOT established and requiring confirmation;
12. DE/EN parity findings;
13. ONE minimal remediation strategy;
14. exact likely STEP 2 modification scope with MUST / SHOULD / NO CHANGE;
15. files explicitly not requiring changes;
16. smallest later verification plan;
17. any release-critical uncertainty that blocks final legal wording;
18. confirmation no files were modified, no builds/tests/API calls were run, and nothing was staged/committed/pushed.

Then STOP. Do not implement anything.
