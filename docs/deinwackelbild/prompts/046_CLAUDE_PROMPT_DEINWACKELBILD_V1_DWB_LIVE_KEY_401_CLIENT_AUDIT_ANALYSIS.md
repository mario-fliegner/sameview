# DeinWackelbild Live-Key 401 — ANALYSIS ONLY / VERIFY CLIENT BEFORE CONTACTING PARTNER

## Problem

The first live DeinWackelbild pilot still fails at **Create Handoff**, but the server response changed after the partner enabled live access and supplied a new live key.

Observed before the new live key / activation:
- HTTP `503`
- server code: `dwb_partner_disabled`
- message: SameView interface not yet enabled

Observed now after replacing the local key with the newly supplied live partner key and rebuilding/running Debug:
- HTTP `401`
- server code: `dwb_partner_unauthorized`
- server message says the Partner-App access key is invalid
- failure is still at **Create Handoff**, before either image upload begins

I do **not** want to contact Olaf again until we have ruled out an obvious SameView/local-build mistake.

This task is **STEP 1 — ANALYSIS ONLY**.

**DO NOT modify any file.**
**DO NOT implement a fix.**
**DO NOT change `local.properties`.**
**DO NOT print, echo, copy, log, or include the actual partner key in your response.**

---

## Mandatory Source-of-Truth / Code Review

Before drawing conclusions, inspect:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `docs/IMPLEMENTATION_NOTES.md`
- `app/build.gradle.kts`
- root Gradle files only as needed for property resolution
- `local.properties` locally only; never reproduce its secret value
- generated/current `BuildConfig` setup as applicable
- `OkHttpDeinWackelbildApiClient.kt`
- DTO/request-building code used by `createHandoff()`
- `WackelbildHandoffOrchestrator.kt`
- DI/Hilt provider/module that constructs `OkHttpDeinWackelbildApiClient`, if applicable
- tests that verify partner-key/header behavior

The current contract already requires:
- partner key supplied to `OkHttpDeinWackelbildApiClient`
- create call sends it as header `X-DWB-Partner-Key`
- partner key is only on the Create call, not uploads
- key must never be logged
- Block 9 introduced developer-local BuildConfig injection

Confirm all of this against the actual current code.

---

# Exact Questions to Answer

## 1. Is the correct local property actually used by the Debug build?

Verify the exact path:

`local.properties`
→ Gradle
→ `BuildConfig`
→ runtime construction/injection
→ `OkHttpDeinWackelbildApiClient`
→ Create request header.

Check:
- exact expected property name
- whether Debug/non-release reads `local.properties`
- whether an environment variable can override the local value
- precedence if both exist
- whether the current run/build variant uses the intended path
- whether stale generated `BuildConfig` / stale APK could still contain the previous key
- whether a Gradle sync/rebuild/reinstall is required after changing `local.properties`
- whether Android Studio's `main` / `SameView Debug` launch setup could run an unexpected variant

Do not guess. Cite concrete code/config locations.

## 2. Could the copied live key contain an accidental extra character?

The key was copied from an email where sentence punctuation may immediately follow the key.

Without reproducing the key, inspect the locally stored value and determine:
- leading/trailing whitespace?
- wrapped in quotes?
- trailing punctuation that looks like sentence punctuation?
- invisible CR/LF?
- does this implementation trim or preserve such characters?
- does the local value structurally match the expected key format?

Do **not** output the key.

Safe facts are okay, e.g.:
- `no leading/trailing whitespace`
- `not quoted`
- `last character is a period`
- `length = N`
- `starts with sv_live_`

If a trailing period exists, report it only. Do not silently remove it.

## 3. Is the runtime key the same as the intended local value?

Find the safest **no-code-change** way to prove the runtime APK uses the intended local value.

Prefer debugger/evaluate-expression checks returning only safe facts:
- boolean equality
- length
- `startsWith("sv_live_")`
- trailing-character check

Do not ask me to post the raw value.

If possible give exact debugger locations / expressions.

## 4. Is the request header built correctly?

Inspect `createHandoff()` / shared request execution and confirm:
- exact header name `X-DWB-Partner-Key`
- attached to Create Handoff
- value passed unchanged unless intentional normalization is specified
- no accidental `Bearer ` prefix
- no duplicate header
- no hardcoded old/test key
- correct BuildConfig field
- no upload/handoff token substituted
- expected live Create endpoint
- no interceptor rewrites/removes header
- no redirect plausibly strips auth before endpoint

Do not reveal the header value.

## 5. What does 503 → 401 prove?

Analyze:

Previous:
- `503 dwb_partner_disabled`

Now:
- `401 dwb_partner_unauthorized`

State precisely:
- whether request reaches intended server/endpoint
- whether this proves only credential rejection
- whether malformed/stale client credential can cause the same 401
- whether it distinguishes partner-side wrong key from SameView sending wrong/stale/malformed key

Do not overclaim.

## 6. Check tests for blind spots

Inspect Block 7/9/11 tests:
- do they verify only that *some* supplied key becomes `X-DWB-Partner-Key`?
- do they verify the actual BuildConfig/local.properties integration path?
- could tests pass while the real APK uses a stale/wrong/malformed key?
- does any test setup mask this?

Analysis only; do not add tests.

---

# Explicitly Rule In/Out

Check each against actual repo/local setup:

1. New key copied with accidental trailing `.`.
2. Leading/trailing whitespace or quotes in `local.properties`.
3. App still contains old key because rebuild/reinstall did not refresh it.
4. Environment variable overrides `local.properties`.
5. Wrong BuildConfig field/property name.
6. Debug build does not use the expected local-key path.
7. Header construction differs from partner contract.
8. `Bearer` vs `X-DWB-Partner-Key` mismatch.
9. Wrong endpoint/environment.
10. Redirect/interceptor strips/alters auth.
11. Partner supplied/activated a key different from the locally stored one.
12. Partner-side key registration/activation is still wrong.

---

# Safe Runtime Diagnostic Plan

If static inspection cannot prove the client is correct, provide the smallest **no-code-change debugger procedure** proving:

1. BuildConfig field used,
2. runtime value structurally looks like the new live key,
3. runtime value equals intended local value,
4. Create request contains expected header name,
5. actual header value equals runtime BuildConfig value,
6. request goes to expected endpoint,
7. response remains `401/dwb_partner_unauthorized`.

Give exact:
- file
- method
- breakpoint expression/line
- safe Evaluate Expression checks

Never instruct me to paste the raw key/request/header/token.

---

# Required Output

Return exactly:

## 1. Source-of-Truth / Current Implementation
Exact intended key flow and header contract.

## 2. Local Key Injection Audit
- property name
- resolution precedence
- build variant behavior
- suspicious quoting/whitespace/trailing punctuation?
- stale-build risk?

## 3. Runtime / Request Audit
- expected BuildConfig field?
- exact header name?
- value unmodified?
- correct endpoint/environment?
- interceptor/redirect risk?

## 4. Test Blind Spots
What existing tests prove / do not prove.

## 5. Interpretation of the 503 → 401 Change
What is proven / not proven.

## 6. No-Code Debugger Verification
Only if still needed.

## 7. Root-Cause Assessment
Rank:
- SameView/local configuration mistake
- stale build/runtime value
- malformed copied key
- client request-construction problem
- partner-side key/activation problem

For each: evidence FOR / AGAINST.

## 8. Should We Contact Olaf?
Choose exactly one:
- `NO — CLIENT/LOCAL ISSUE FOUND`
- `NOT YET — ONE LOCAL CHECK REMAINS`
- `YES — CLIENT SIDE VERIFIED, PARTNER-SIDE CHECK JUSTIFIED`

If `YES`, list only non-secret facts to send Olaf.

## 9. File Changes
`None — analysis only.`

Then STOP.

---

# Hard Constraints

- No code changes.
- No file modifications.
- No cleanup/refactor.
- No logging additions.
- No secret output.
- No live-key regeneration.
- No retries against the API unless required for one debugger verification.
- Do not contact the partner.
- Do not implement any fix.
