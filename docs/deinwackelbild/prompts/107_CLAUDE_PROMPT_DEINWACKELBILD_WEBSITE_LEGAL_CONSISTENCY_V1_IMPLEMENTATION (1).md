# Claude Prompt — SameView Website / DeinWackelbild Legal Consistency V1 — STEP 3 IMPLEMENTATION

## Repository

`C:\data\work\privat\git-repos\sameview-website`

## Workflow status

STEP 1 analysis and STEP 2 scope confirmation are complete and explicitly approved.

This is **STEP 3 — IMPLEMENTATION**.

Implement exactly the approved fix. Do not expand scope.

## Approved modification scope — exactly 6 files

Modify only:

1. `src/pages/en/privacy/_privacy.md`
2. `src/pages/de/privacy/_privacy.md`
3. `src/pages/en/terms/_terms.md`
4. `src/pages/de/terms/_terms.md`
5. `README.md`
6. `docs/PRODUCT_SUMMARY.md`

Do not modify any other file.

Before editing, verify:
- branch and HEAD;
- `git status --short`;
- that there are no unexpected user changes.

If the working tree is not clean or the baseline differs materially from the approved STEP 2 baseline (`main`, HEAD `39fd8caa1173558360389105a0327ffda1745e5e`), STOP and report it rather than overwriting anything.

## Explicit NO-CHANGE scope

Do NOT modify:

- `src/i18n/home/en.ts`
- `src/i18n/home/de.ts`
- any Imprint Markdown or wrapper
- Privacy/Terms `index.astro` wrappers
- cookie-settings pages
- routing
- layouts
- footer/legal components
- consent system
- analytics
- `docs/PROJECT_INSTRUCTION.md`
- Android repository
- `sameview-release`
- Play Console
- DeinWackelbild website
- tests

Do not refactor, reformat or clean up unrelated text.

## Authoritative implementation facts

Use these facts exactly:

- SameView's normal/core use remains local/offline.
- DeinWackelbild is a separate optional feature.
- No image transfer occurs merely by using SameView normally.
- Transfer occurs only when the user explicitly starts the DeinWackelbild order flow.
- Immediately before transfer, the app tells the user that the two images are sent to DeinWackelbild.de to create/configure the print and that the order is completed there.
- Exactly two prepared JPEGs are transferred: reference + capture.
- These are derived/temp prepared files, not the stored originals.
- The transferred JPEGs contain no EXIF/GPS metadata.
- If the date option is enabled, the date is rendered into the image pixels.
- Transfer uses HTTPS.
- Recipient/operator/seller:
  **O.Schulze / M.Monka GbR (DeinWackelbild), Wolfener Str. 32-34, 12681 Berlin, Germany.**
- The physical print order is completed with DeinWackelbild under its own applicable terms.
- The physical print purchase is separate from the SameView app purchase through Google Play.
- SameView receives **10% commission** on orders attributable to this handoff.
- Do NOT state what the 10% is calculated from.
- If transferred images do not lead to a completed order, DeinWackelbild retains them for **24 hours and then deletes them**.
- Do NOT invent a retention period for completed orders. Refer to DeinWackelbild's privacy notice for its subsequent processing/retention.
- The handoff includes technical configuration values such as partner identifier, format, orientation and direction.
- No SameView session ID or external reference is sent.
- No analytics, telemetry, tracking, automatic upload or background upload is introduced.
- SameView does not receive the transferred images back.
- Do NOT state that SameView receives no other individual order/attribution information; this is not established.
- Checkout/configurator opens in a browser tab / Chrome Custom Tab after handoff.
- The app declares INTERNET permission for this approved optional flow.

## Legal-basis decision

A targeted legal-basis analysis was completed.

For this implementation:

- Do **NOT** state Art. 6(1)(a), (b), or (f) GDPR for the DeinWackelbild transfer.
- Do **NOT** add a placeholder legal-basis sentence.
- Do **NOT** characterize SameView as sole controller, joint controller, or processor for the transfer.
- Do **NOT** characterize DeinWackelbild as SameView's processor.
- Leave the existing `Legal bases` / `Rechtsgrundlagen` section unchanged where it is already explicitly scoped to the processing activities it describes.
- The transfer-specific legal basis / role question remains a separate legal-review item and is NOT to be solved in this implementation.

Do not modify Android UI.

## 1. Privacy — EN + DE

Files:

- `src/pages/en/privacy/_privacy.md`
- `src/pages/de/privacy/_privacy.md`

Preserve the existing document structure and style. Mirror substantive changes between languages.

### Date

Update:
- EN: `Last updated: September 21, 2026`
- DE: `Zuletzt aktualisiert: 21. September 2026`

Use the files' existing date formatting/style if punctuation differs.

### §1 Introduction

Correct the absolute local/no-server statement.

Preserve the strong privacy position for **normal SameView use**, but make clear that the optional DeinWackelbild order described later is the narrow exception.

Do not weaken:
- no account;
- no login;
- local storage/core use.

### §2 Android app

Preserve all accurate bullets for:
- tracking;
- advertising;
- telemetry;
- cloud sync;
- unrelated uploads/networking.

Correct only the now-false absolutes.

The resulting meaning must be:

- no automatic/background photo upload or server transmission;
- no network communication except the explicitly started DeinWackelbild order;
- INTERNET permission exists for that optional flow;
- normal SameView use causes no such transfer;
- SameView itself does not receive the two transferred images;
- third-party transfer happens only when the user explicitly starts the optional order.

Do not make claims about order-attribution information.

### §3 Permissions

Add `INTERNET` to the used permissions in the same style as the existing permission entries.

Purpose must be narrowly described as the optional DeinWackelbild order flow.

In “Permissions not used”:
- remove `INTERNET`;
- change any blanket “network-related permissions of any kind” statement to an accurate statement such as “other network-related permissions”, preserving the existing style.

Do not touch unrelated permission descriptions.

### §5 Photos / reference images

Keep the factual statement that the stored original/reference remains local and is not itself uploaded.

Add only a narrow clarification/pointer that when the user explicitly starts the optional DeinWackelbild order, prepared derived copies of the two images are transferred as described in §7.

Do not imply that stored originals are uploaded.

### §6 Retention / feature-specific local guarantees

Keep the accurate feature-specific statements for:
- metadata;
- logo;
- comparison export/share;
- video export;
- backup.

Where the general retention/server-copy wording needs qualification, add only a pointer to the optional DeinWackelbild flow in §7.

Do not weaken unrelated offline guarantees.

### §7 Data sharing — main DeinWackelbild disclosure

Rewrite the existing blanket no-sharing/no-network lead-in so it remains true for normal SameView use and explicitly introduces the exception below.

At the end of §7 add an unnumbered subsection:

EN:
`### Optional DeinWackelbild order`

DE:
`### Optionale DeinWackelbild-Bestellung`

Do not renumber subsequent sections.

The subsection must state, in the document's existing factual/non-promotional style:

- the feature is optional;
- transfer occurs only after the user explicitly starts it;
- the app shows a disclosure immediately before transfer;
- there is no automatic/background transfer;
- exactly two prepared JPEGs are transferred: reference + capture;
- they are prepared/derived copies, not the stored originals;
- they contain no EXIF/GPS metadata;
- if enabled, the date is rendered into the pixels;
- purpose: preparing/configuring and ordering the lenticular print / Wackelbild;
- recipient/operator: O.Schulze / M.Monka GbR (DeinWackelbild), Wolfener Str. 32-34, 12681 Berlin, Germany;
- HTTPS transfer to DeinWackelbild;
- technical handoff values include partner identifier, format, orientation and direction;
- no SameView session ID or external reference is sent;
- after handoff the checkout/configurator opens in a browser tab and the order is completed with DeinWackelbild;
- if no order is completed, DeinWackelbild retains the transferred images for 24 hours and then deletes them;
- for processing/retention associated with completed orders, refer to DeinWackelbild's own privacy notice without inventing a duration;
- link to the official DeinWackelbild privacy page:
  `https://deinwackelbild.de/datenschutz/`
- add a short cross-reference to SameView Terms for the commercial relationship/commission, but do not repeat the 10% figure in Privacy.

Do not state a GDPR legal basis here.

Do not state that DeinWackelbild is a processor.

Do not state that SameView receives no order/attribution data.

### §8 Website / network wording

Correct only the first blanket sentence that falsely says the app has no INTERNET permission or makes no network calls.

Meaning:
- apart from the optional DeinWackelbild order described in §7, SameView does not make in-app network requests in normal use.

Preserve the existing About-link/external-browser explanation. Do not conflate those links with the Wackelbild Custom Tab.

### §9 rights/local-content wording

Correct only statements that assume all processed content can never leave the device.

Preserve:
- local access/deletion rights for data stored by SameView;
- no SameView server-side copy.

Qualify them with the optional §7 transfer where necessary.

For content held by DeinWackelbild after the optional transfer, point users to DeinWackelbild's privacy notice rather than claiming SameView can access/delete it.

Do not invent an order-data relationship.

### Legal bases / Rechtsgrundlagen

Leave this section unchanged.

## 2. Terms — EN + DE

Files:

- `src/pages/en/terms/_terms.md`
- `src/pages/de/terms/_terms.md`

### Date

Update the effective date consistently to September 21, 2026 / 21. September 2026 using the existing formatting.

### New purchase-related section

Insert directly after the existing:
- EN `Purchase via Google Play`
- DE corresponding Google Play purchase section

and before `Limitation of Liability`.

Heading:

EN:
`## Optional Print Order via DeinWackelbild`

DE:
`## Optionale Bestellung über DeinWackelbild`

Keep it concise.

State:

- the DeinWackelbild order feature is optional;
- SameView provides the handoff from the app;
- the physical lenticular print / Wackelbild is ordered from:
  **O.Schulze / M.Monka GbR (DeinWackelbild), Wolfener Str. 32-34, 12681 Berlin, Germany**;
- the physical-product order is separate from the SameView app purchase via Google Play;
- the external order is governed by DeinWackelbild's applicable terms;
- link to:
  `https://deinwackelbild.de/agb/`
- SameView receives a **10% commission** on orders attributable to this handoff.

Do NOT:
- say what the 10% is calculated from;
- say the commission does not affect the price;
- duplicate DeinWackelbild payment, shipping, withdrawal, warranty, refund or liability provisions;
- invent responsibility/liability exclusions;
- characterize GDPR roles.

### Local Data section

Append one narrow sentence:

- the stored photos/sessions remain local in normal use;
- prepared copies of two images leave the device only if the user explicitly starts the optional DeinWackelbild order;
- point to the new section.

Do not weaken the existing general local-data positioning beyond this explicit exception.

## 3. README.md

Keep the existing privacy/local positioning.

Do not rewrite the existing bullets.

Add one short explicit exception note adjacent to the existing Android privacy-positioning section.

Meaning:

- exception: the optional, explicitly user-initiated DeinWackelbild order transfers two prepared, metadata-free JPEGs via HTTPS to DeinWackelbild;
- nothing is transferred merely through normal SameView use;
- no automatic/background upload.

Keep it technical and concise. This is documentation, not legal copy.

Do not add commission/retention details here unless necessary to prevent a direct contradiction. They are not necessary for this source-of-truth exception note.

## 4. docs/PRODUCT_SUMMARY.md

Same approach as README.

Preserve the existing German local/offline privacy bullets.

Add one concise exception note directly beside/under that privacy position:

- optionale, ausdrücklich vom Nutzer gestartete DeinWackelbild-Bestellung;
- two prepared metadata-free JPEGs transferred over HTTPS;
- otherwise no transfer;
- no automatic/background upload.

Do not broadly rewrite the product summary.

## Homepage and Imprint

Explicitly verify after implementation that:

- `src/i18n/home/en.ts`
- `src/i18n/home/de.ts`
- DE/EN Imprint

remain byte-for-byte untouched.

The homepage's local/no-upload positioning intentionally describes normal SameView use and is NOT part of this change.

## External-link style

Before adding the two DeinWackelbild URLs to Markdown, inspect how the existing legal Markdown represents external links and follow that exact Markdown/style convention.

Do not add tracking parameters.

Official URLs:

- Privacy: `https://deinwackelbild.de/datenschutz/`
- Terms: `https://deinwackelbild.de/agb/`

## Verification — minimal and targeted

After implementation run only:

1. `git status --short`
2. `git diff --stat`
3. focused `git diff` for the six approved files
4. targeted grep over the modified legal/docs content for stale absolute claims involving:
   - `INTERNET`
   - network / Netzwerk
   - upload / hochlad
   - third party / Dritte
   - remains local / bleiben lokal / liegen lokal
5. DE/EN parity review for the new Privacy and Terms sections
6. `pnpm build`

Do NOT run:
- Playwright/e2e
- Android Gradle tests
- full unrelated test suites
- `pnpm lint` unless a `.ts` or `.astro` file unexpectedly has to change; if that happens, STOP instead because it is outside the approved scope.

Do not suppress failures.

If `pnpm build` fails because of your changes, report and fix only the in-scope cause.
If it fails for an unrelated pre-existing reason, report it and do not expand scope.

## Final report

Return:

1. branch/HEAD baseline;
2. exact six modified files;
3. concise per-file implementation summary;
4. confirmation homepage and Imprint stayed untouched;
5. confirmation the legal-basis section was not changed and no Art. 6 basis was invented;
6. exact treatment of the 10% commission;
7. exact treatment of the 24-hour deletion;
8. DE/EN parity result;
9. stale-claim grep result;
10. `pnpm build` result;
11. commands/checks run;
12. commands/tests not run;
13. remaining legal-review item:
    - SameView's role for the transfer;
    - transfer-specific Art. 6 basis;
    - possible Art. 26 implications;
14. `git status --short`;
15. confirmation nothing was staged, committed or pushed.

Do not stage, commit or push.
