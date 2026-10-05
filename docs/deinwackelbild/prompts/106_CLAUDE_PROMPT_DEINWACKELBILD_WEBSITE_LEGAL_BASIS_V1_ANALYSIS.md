# Claude Prompt — SameView Website / DeinWackelbild Legal Basis V1 — TARGETED ANALYSIS

## Repository

`C:\data\work\privat\git-repos\sameview-website`

## Purpose

Resolve exactly ONE remaining question before STEP 3 implementation:

**What GDPR legal basis, if any, should SameView state for the user-initiated transfer of the two prepared images to DeinWackelbild?**

This is analysis only. Do not modify any file.

## Already approved scope / facts

Do not reopen the rest of STEP 2.

Established facts:

- SameView is a paid Android app purchased through Google Play.
- Normal SameView use is local/offline.
- DeinWackelbild is a separate optional feature.
- No transfer occurs merely by using SameView.
- The user must explicitly start the DeinWackelbild order flow.
- Immediately before transfer, the UI discloses that the two images are sent to DeinWackelbild.de to create/configure the print and that the order is completed there.
- Exactly two prepared JPEGs are transferred: reference + capture.
- They are derived/temp files, not stored originals.
- They contain no EXIF/GPS metadata.
- If enabled, a date may be rendered into the pixels.
- Transfer is via HTTPS.
- Recipient/operator/seller:
  `O.Schulze / M.Monka GbR (DeinWackelbild), Wolfener Str. 32-34, 12681 Berlin, Germany`.
- The physical print order is completed with the external DeinWackelbild provider under its own terms.
- SameView receives a 10% commission on orders attributable to this handoff.
- If no order is completed, DeinWackelbild retains the transferred images for 24 hours and then deletes them.
- No analytics, telemetry, tracking, automatic upload or background upload is part of the integration.
- SameView does not receive the transferred images back.
- Do not claim that SameView receives no other order/attribution information because that has not been established.
- Do not characterize DeinWackelbild as SameView's processor unless evidence supports that relationship.

## Relevant official guidance to consider

Use current authoritative GDPR sources and German/EU supervisory guidance, not blogs.

At minimum distinguish:

### Art. 6(1)(b) GDPR
Processing necessary for performance of a contract with the data subject or steps at the data subject's request before entering into a contract.

The key question is whether SameView's transfer to an independent external seller is genuinely **necessary for performance of SameView's own contract with the user / a contractual SameView feature**, or whether that stretches Art. 6(1)(b) too far because the physical-product contract is with DeinWackelbild.

### Art. 6(1)(a) GDPR
Consent.

The key question is whether the existing explicit button + immediate disclosure constitutes a valid GDPR consent mechanism. Do not assume that a user action is automatically consent. Check requirements for informed, specific, freely given and unambiguous consent and the need to demonstrate/withdraw consent.

### Art. 6(1)(f) GDPR
Legitimate interests.

The key question is whether this could support the optional handoff and commercial partner flow, including the 10% commission, and what balancing/objection implications would follow.

Do not choose a basis merely because it is convenient.

## Required analysis

1. Read the current DE/EN Privacy “Legal bases / Rechtsgrundlagen” section and the relevant current Terms wording.
2. Inspect only the minimum Android integration/UI code or project documentation needed to establish exactly what the user sees and what action triggers transfer.
3. Determine whether the existing interaction is designed as:
   - contractual request,
   - GDPR consent,
   - or merely an explicit feature action/disclosure.
4. Compare Art. 6(1)(a), (b), and (f) for this exact flow.
5. State which basis is best supported by the actual implementation and why.
6. State whether using that basis would require an Android UI/code change:
   - separate consent checkbox;
   - consent wording;
   - withdrawal mechanism;
   - logging/proof;
   - or no UI change.
7. If Art. 6(1)(b) is proposed, identify precisely which contract/contractual step makes the transfer necessary and explain why the fact that the final print seller is DeinWackelbild does or does not matter.
8. If Art. 6(1)(a) is proposed, explain whether the current explicit order button and disclosure satisfy consent requirements or whether they do not.
9. If Art. 6(1)(f) is proposed, identify the legitimate interest and the principal balancing/Art. 21 implications; do not perform a fake formal LIA if evidence is insufficient.
10. Consider the 10% commission when assessing transparency and legitimate-interest implications.
11. Do not infer special-category data merely because photographs are involved. Note only any real risk that ordinary photos can contain personal data.
12. Recommend the exact **legal-basis strategy**, not final polished Privacy prose.
13. If no basis can responsibly be selected without legal counsel, say exactly why and identify the minimum unresolved legal question.

## Scope consequence

At the end, state one of:

- **A — No Android change needed; Privacy can state [basis].**
- **B — Android UI must change before Privacy can rely on [basis].**
- **C — Legal review required before selecting a basis; do not publish a basis yet.**

If A, provide a short DE and EN factual legal-basis sentence suitable for later STEP 3.

If B, STOP after describing the minimal required Android change. Do not implement it and do not expand the website scope.

If C, provide the exact question that should be put to legal counsel.

## Verification / workflow

- Do not edit files.
- Do not stage, commit or push.
- Do not run full builds/tests.
- No unrelated website audit.
- No changes to the already-approved Privacy/Terms/README/PRODUCT_SUMMARY scope.
- Report sources used with URLs/titles.
- Clearly distinguish authoritative-source conclusions from your legal inference.

Then STOP.
