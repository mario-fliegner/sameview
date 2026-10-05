# Claude Prompt — SameView Website / DeinWackelbild Legal Consistency V1 — STEP 2 SCOPE CONFIRMATION

## Repository

`C:\data\work\privat\git-repos\sameview-website`

## Workflow

This is **STEP 2 — SCOPE CONFIRMATION ONLY**.

Do NOT modify files.
Do NOT stage, commit or push.
Do NOT implement wording yet.
Do NOT refactor or reformat unrelated content.

Use the STEP 1 findings already established in this session. Re-check current git status before scoping.

## Additional facts now confirmed by the project owner

Treat these as authoritative SameView integration facts:

- DeinWackelbild ordering is optional.
- Nothing is uploaded merely by using SameView normally.
- Transfer happens only after the user explicitly starts the DeinWackelbild order flow.
- SameView receives **10% commission** for orders attributable to this partner flow.
- If the transferred images do **not** result in a completed order, they are retained by DeinWackelbild for **24 hours** and then deleted.
- The Android app transfers exactly two prepared JPEGs (reference + capture).
- These transferred JPEGs are derived/temp files, not the stored originals.
- They contain no EXIF/GPS metadata.
- If the date option is enabled, the date is rendered into the image pixels.
- Transfer is via HTTPS.
- The handoff contains technical partner/configuration values including partner identifier, format, orientation and direction.
- No SameView session ID or external reference is currently sent.
- No analytics, telemetry, tracking, automatic upload or background upload is introduced by this integration.
- The external checkout/configurator is opened after handoff.
- The app has INTERNET permission because of this approved explicit online flow.

Important correction to STEP 1:
Do **not** classify homepage statements such as “without photo uploads” / “ohne Foto-Uploads” or “Your photos stay on your device” as automatically false merely because the separate optional DeinWackelbild order exists. They describe SameView's normal/local core use. Scope them out unless their immediate context makes an unconditional promise that clearly includes explicitly initiated third-party ordering.

Likewise, do not automatically rewrite `README.md` or `docs/PRODUCT_SUMMARY.md`. First distinguish the local/core privacy position from the explicit optional order exception.

## Externally verified DeinWackelbild facts

The following official pages have now been checked:

- Imprint: `https://deinwackelbild.de/impressum/`
- Privacy: `https://deinwackelbild.de/datenschutz/`
- Terms: `https://deinwackelbild.de/agb/`
- Payment/shipping: `https://deinwackelbild.de/zahlung-versand/`

Use only these official pages if you need to re-verify the facts below.

### Operator / seller

Official DeinWackelbild imprint identifies:

**O.Schulze / M.Monka GbR**
spectrum/lentiprint
Wolfener Str. 32-34 – Haus C
12681 Berlin
Germany

The official DeinWackelbild Terms state that orders in the online shop are placed with **O.Schulze / M.Monka GbR, Wolfener Str. 32-34, 12681 Berlin**.

Therefore the physical print order is with the external DeinWackelbild shop/operator, not a Google Play purchase from SameView.

### DeinWackelbild privacy

The official privacy page identifies the responsible entity for data processing on the website as:

**O.Schulze-M.Monka GbR**
Olaf Schulze
Wolfener Str. 32-34
12681 Berlin

Do not infer from this alone a processor/controller relationship for the SameView handoff beyond what is safe to state factually.

### External purchase terms

The official DeinWackelbild Terms govern orders in their online shop. Their current AGB also contain their own purchase, payment, delivery, withdrawal, warranty and liability provisions.

SameView's Terms should therefore not duplicate or reinterpret those external commercial terms. The narrow SameView clarification should identify the external ordering relationship and point users to the provider's applicable terms rather than importing their rules.

## Legal-basis caution

Do NOT invent a GDPR legal basis.

The app performs the transfer only after an explicit user action and provides an immediate pre-transfer disclosure, but that fact alone is not permission to make an unsupported legal conclusion in this task.

For STEP 2:
- scope the Privacy change so the factual transfer is accurately disclosed;
- identify the exact existing “Legal bases” passage that may need amendment;
- if a legal-basis sentence cannot safely be finalized from the existing project/legal text, mark that sentence as a **legal-review/owner-decision item**, rather than blocking correction of the objectively false technical statements.

Do not claim an Art. 6 basis unless already supported by the project's existing legal approach or clearly justified and explicitly flagged for owner approval.

## Required scope decision

Produce ONE minimal, targeted scope.

### A. Privacy — MUST

Expected files:

- `src/pages/de/privacy/_privacy.md`
- `src/pages/en/privacy/_privacy.md`

Scope only the lines necessary to:

1. remove/correct absolute statements that are now technically false, especially:
   - no INTERNET permission;
   - no server/network communication of any kind;
   - no transfer to third parties;
   - no photo/image upload under any circumstances;

2. preserve the strong local/offline privacy position for normal SameView use;

3. add one narrow, clearly separated DeinWackelbild disclosure describing:
   - optional and explicitly user-initiated flow;
   - two prepared JPEGs;
   - no EXIF/GPS metadata;
   - purpose: configuring/ordering a lenticular print;
   - recipient/operator: O.Schulze / M.Monka GbR / DeinWackelbild;
   - HTTPS transfer;
   - technical handoff/configuration values where appropriate;
   - external checkout/order;
   - 24-hour deletion when no order is completed;
   - 10% SameView commission, if Privacy is an appropriate place for this fact; otherwise explain why it belongs only in Terms/another disclosure;
   - no automatic/background transfer.

4. update the Privacy “last updated” date.

Preserve unrelated sections and feature-specific guarantees such as local GPS behavior, comparison export, video export and backup where they remain accurate.

### B. Terms — MUST/SHOULD decision

Expected files:

- `src/pages/de/terms/_terms.md`
- `src/pages/en/terms/_terms.md`

Decide whether these should now be **MUST** rather than STEP 1's SHOULD, given that operator/seller and 10% commission are now confirmed.

The intended minimal content is:
- optional external DeinWackelbild order handoff;
- the physical print is ordered from the external provider O.Schulze / M.Monka GbR / DeinWackelbild;
- the external order is separate from the SameView app purchase via Google Play;
- the external provider's applicable terms govern that physical-product order;
- SameView receives a 10% commission from attributable orders;
- no duplication of DeinWackelbild payment/refund/warranty/delivery rules.

Also decide whether the existing “Local Data” wording needs one narrow exception/pointer.

Update Terms effective date only if the project's existing convention requires it for this substantive change.

### C. Imprint — NO CHANGE unless new evidence contradicts this

Expected no changes:

- `src/pages/de/imprint/_imprint.md`
- `src/pages/en/imprint/_imprint.md`
- their `index.astro` wrappers

Do not add DeinWackelbild merely because it is a commercial partner.

### D. Homepage wording — default NO CHANGE

Reassess these STEP 1 candidates:

- `src/i18n/home/de.ts`
- `src/i18n/home/en.ts`

The project owner explicitly intends statements such as “without photo uploads” and “Your photos stay on your device” to describe SameView's normal/core use, while DeinWackelbild is a separate voluntary action.

Do not scope homepage changes merely because an optional explicit upload exists.

Only include either file if the exact surrounding wording is genuinely unconditional/misleading even with that product distinction. If you include it, quote the exact context and explain why a narrow qualifier is necessary.

### E. Website Source-of-Truth docs

Reassess:

- `docs/PRODUCT_SUMMARY.md`
- `README.md`

Do not automatically replace the local/offline product positioning.

Determine whether a minimal explicit exception note is required to prevent the documentation from contradicting the now-shipped optional DeinWackelbild feature. Prefer a narrow exception note over weakening the general local/offline positioning.

Also inspect `docs/PROJECT_INSTRUCTION.md` only to determine whether it itself contains a conflicting technical privacy contract. Do not modify it unless necessary.

## Scope discipline

No changes to:
- cookie settings pages unless a direct contradiction is demonstrated;
- legal page Astro wrappers;
- routing;
- layout;
- consent system;
- analytics;
- SEO unrelated to the exact contradiction;
- Android repo;
- sameview-release repo;
- Play Console;
- DeinWackelbild site;
- unrelated docs.

## Verification scope

For the later STEP 3, define the smallest appropriate verification.

Expected:
- focused git diff/stat;
- targeted grep for stale absolute network/INTERNET/upload claims;
- DE/EN parity check;
- `pnpm build` if required by `docs/PROJECT_INSTRUCTION.md`.

Do not require `pnpm lint` if only Markdown files are changed and the repo's lint tool does not process Markdown. If TS files are ultimately scoped, state why lint becomes relevant.

No Playwright/e2e unless an actual routed/component behavior changes.

## Required output

Return:

1. current branch, HEAD and `git status --short`;
2. exact proposed modification list;
3. for EACH proposed file:
   - full path;
   - exact sections/claims to change;
   - exact nature of change;
   - MUST/SHOULD classification;
4. explicit NO-CHANGE list;
5. explicit decision on homepage `de.ts` / `en.ts`;
6. explicit decision on `README.md`, `docs/PRODUCT_SUMMARY.md`, and `docs/PROJECT_INSTRUCTION.md`;
7. explicit decision on Imprint DE/EN;
8. exact placement strategy for the new DeinWackelbild Privacy disclosure;
9. exact placement strategy for the Terms clarification;
10. how the 10% commission disclosure will be handled;
11. how the 24-hour non-completed-order deletion fact will be handled;
12. any legal-basis sentence that remains an owner/legal-review decision;
13. DE/EN parity requirements;
14. smallest verification commands/checks for STEP 3;
15. regression/release risks;
16. confirmation that no unrelated code/content will be touched;
17. confirmation that no files were modified, staged, committed or pushed.

Then STOP and wait for explicit approval. Do not implement.
