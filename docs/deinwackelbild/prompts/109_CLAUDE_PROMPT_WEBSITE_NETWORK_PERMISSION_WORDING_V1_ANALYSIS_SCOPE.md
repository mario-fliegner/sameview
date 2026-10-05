# Claude Prompt — SameView Website / Network Permission Wording V1 — STEP 1 + STEP 2

## Repository
`C:\data\work\privat\git-repos\sameview-website`

## Single problem
A later release-repository analysis found that the Android merged manifest contains `ACCESS_NETWORK_STATE`, apparently contributed transitively by Media3.

The website Privacy Policy was just updated for the optional DeinWackelbild integration. In §3 it now reportedly says:
- EN: `Any other network-related permission`
- DE: `Weitere netzwerkbezogene Berechtigungen`

If `ACCESS_NETWORK_STATE` is present in the actual merged release manifest, those blanket statements are inaccurate.

Handle only this one issue.

## Workflow
This prompt covers STEP 1 analysis and, if evidence is conclusive, STEP 2 scope confirmation. Do not implement anything. Do not modify, stage, commit or push files.

## Source-of-truth checks
1. Read the relevant website project instructions/specifications.
2. Read the current DE/EN Privacy files.
3. Verify the Android permission fact from `C:\data\work\privat\git-repos\sameview`, using the most authoritative available merged-manifest/build evidence rather than trusting the previous report blindly.
4. Determine whether `ACCESS_NETWORK_STATE` is absent from the source manifest but introduced transitively, and identify the contributor if this can be established without expanding scope.

Do not modify the Android repository.

## STEP 1 questions
Determine:
1. Is `android.permission.ACCESS_NETWORK_STATE` actually present in the merged Android manifest?
2. Is it absent from the app source manifest but introduced transitively?
3. Which dependency contributes it, if conclusively identifiable?
4. Does current Privacy §3 literally claim no other network-related permission is used?
5. Is that statement therefore inaccurate?
6. Does any other current DE/EN Privacy sentence become false specifically because of `ACCESS_NETWORK_STATE`?
7. Do not conflate permission presence with network behavior: does this permission itself establish any additional network communication?
8. Is any Terms, homepage, README, PRODUCT_SUMMARY, Imprint or other website file affected by this exact issue?

## Minimal-fix principle
Keep the fix surgical. Do not weaken broader privacy positioning.

Preserve:
- normal/core SameView use remains local/offline;
- optional DeinWackelbild transfer is the approved in-app image/network transfer described by the policy;
- `INTERNET` exists for that optional flow;
- no analytics, telemetry, tracking, automatic image upload or background image upload;
- homepage local-first wording remains intentional.

If only the blanket “other network-related permissions” bullet is inaccurate, prefer correcting/removing only that bullet rather than rewriting surrounding sections.

Do not invent a purpose for `ACCESS_NETWORK_STATE` unless supported by evidence.

## STEP 2 scope confirmation
If confirmed, provide:
1. all files to modify;
2. exact sentence/bullet to change;
3. intended replacement meaning;
4. explicit NO-CHANGE files/areas;
5. risks/regression implications;
6. documentation-consistency implications;
7. minimal later verification checks.

Expected scope should be no larger than necessary. If only these files need changes, say so:
- `src/pages/en/privacy/_privacy.md`
- `src/pages/de/privacy/_privacy.md`

## Restrictions
- No implementation.
- No Android changes.
- No Terms changes unless directly contradicted by this permission.
- No homepage changes.
- No release-repository changes.
- No unrelated cleanup.
- No legal-basis work.
- Do not reopen broader DeinWackelbild wording.
- No full builds/tests during analysis/scope.

## Required final report
Return:
1. website repo state;
2. Android evidence checked;
3. exact root cause;
4. exact inaccurate website wording;
5. whether anything else is affected;
6. minimal fix strategy;
7. STEP 2 exact modification list;
8. explicit NO-CHANGE list;
9. later verification plan;
10. confirmation no files were modified/staged/committed/pushed.

Then STOP and wait for explicit approval before implementation.
