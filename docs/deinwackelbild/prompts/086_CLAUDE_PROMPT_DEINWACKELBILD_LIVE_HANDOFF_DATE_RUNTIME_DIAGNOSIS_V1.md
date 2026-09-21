# Claude Prompt — DeinWackelbild Live Handoff Date Missing — STEP 1B RUNTIME DIAGNOSIS ONLY

## Context

The prior regression analysis found:

- Source of Truth requires the date to be baked into both transfer JPEGs when `Show date` is ON.
- Static code inspection shows the date overlay is propagated into `WackelbildPrintRenderer`.
- `DateBadgeRenderer.draw()` is called before JPEG compression.
- The exact returned `File` objects are then passed to the upload client.
- Targeted renderer instrumentation tests pass on the Samsung.
- No repository commit was found that obviously broke this path.

However, the user has just reproduced the issue in the live flow on the current build:

- SameView preview clearly shows the date badge.
- After tapping the CTA and arriving on deinwackelbild.de, the visible image does not show the date badge.
- The bottom-right image area is visibly present on the partner site, so a simple partner-side crop is not an adequate explanation by itself.

Therefore static analysis is insufficient.

This prompt is a **runtime diagnosis only**. Do not implement a fix yet.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Goal

Prove, from the **actual live handoff run on the Samsung**, whether the exact JPEG bytes prepared and uploaded by SameView contain the date badge.

We need to isolate the fault boundary:

```text
SameView rendered temp JPEG
→ SameView upload request body
→ DeinWackelbild server
→ DeinWackelbild configurator
```

The key question:

> Does the exact `image_one.jpg` / `image_two.jpg` produced for the user's live CTA press already contain the date badge before upload?

---

# Mandatory pre-check

1. Read:
   - `CLAUDE_PROJECT_INSTRUCTION.md`
   - `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
   - any relevant implementation-plan sections for temp files, print rendering, upload/handoff, cleanup.
2. Run:
   - `git status --short`
   - `git log --oneline --decorate -n 20`
3. Confirm the working tree is clean except for the still-uncommitted ridge-over-date-badge change, if it has not yet been committed.
4. Do not modify production code during this diagnosis.

If unexpected tracked changes exist, report and STOP.

---

# Diagnostic strategy

Use the current real Samsung and the current installed app build.

Prefer the least invasive method that can inspect the actual per-operation temp JPEGs from a real handoff.

Acceptable approaches include:

- `adb shell run-as <package> ...`
- copying the operation temp directory/files out with `run-as`/`cat`
- strategically pausing the app before cleanup
- debugger breakpoint in the orchestrator or temp-file cleanup path
- inspecting the exact local `File` paths passed into `uploadImage(...)`
- temporarily using debugger evaluation only

Do **not** add logging, debug UI, persistence changes, or code instrumentation unless absolutely necessary.

If runtime file cleanup happens too quickly to inspect the files, first determine whether a debugger breakpoint can safely pause before cleanup. Only if that is impossible should you propose a tiny diagnostic-only code change — but do not implement it without a separate approval gate.

---

# What to inspect in the live run

Use a real session where:

- the preview visibly shows a valid date badge;
- `Show date` is ON;
- the user taps `Order your lenticular print`;
- the handoff completes to deinwackelbild.de.

At runtime capture:

1. `dateOverlayEnabled`
2. `referenceDateBadgeText`
3. `captureDateBadgeText`
4. `currentDateOverlayInput()` result
5. exact operation directory path
6. exact rendered `referenceFile` path
7. exact rendered `captureFile` path
8. file sizes
9. exact `File` objects passed to `apiClient.uploadImage(...)`
10. whether those paths/files are identical to the rendered pair
11. whether any later copy/re-encode/overwrite happens before request body creation

---

# Critical byte-level proof

Extract the exact rendered JPEGs from the live operation **before they are deleted**.

Open/inspect both files and answer:

- Does `image_one.jpg` visibly contain its expected date badge?
- Does `image_two.jpg` visibly contain its expected date badge?
- Is the badge position/style correct?
- Are the dimensions/aspect ratio those expected for the print pair?

If possible, also compute hashes (e.g. SHA-256) of:

- rendered `image_one.jpg`
- rendered `image_two.jpg`

Then verify the exact same files/bytes are what `uploadImage()` reads for SLOT_ONE and SLOT_TWO.

Do not infer from object identity alone if the bytes can be proven directly.

---

# Upload boundary inspection

Inspect the actual call into:

`OkHttpDeinWackelbildApiClient.uploadImage(...)`

For each slot confirm:

- exact file path
- exact file length
- if practical, SHA-256 before `asRequestBody`
- upload URL/slot mapping
- no alternate file is substituted
- no second transform occurs in the client

If request-body interception is available without code changes, verify the multipart payload corresponds to the extracted JPEG bytes.

Do not expose or print secrets such as:
- partner key
- auth token
- signed upload URL query secrets
- handoff token

Redact secrets in the report.

---

# Fault-boundary conclusions

At the end classify the live issue into exactly one of these buckets:

## A. SameView rendering fault
The live rendered JPEG does not contain the date despite the preview showing it.

Then identify where live runtime state diverges from tests/static assumptions.

## B. SameView upload-source fault
The rendered JPEG contains the date, but `uploadImage()` receives a different/replaced file.

Then identify the exact substitution point.

## C. SameView upload-bytes fault
The correct file is passed, but the multipart request body differs from that file.

Then identify the exact client/request-body problem.

## D. Partner-side processing/display fault
The extracted live JPEG contains the date, and the exact same bytes are sent by SameView, but deinwackelbild.de displays/returns an image without it.

Then explicitly state that the fault boundary lies beyond SameView's upload request.

## E. Inconclusive
Only if the actual bytes cannot be inspected/proven. State precisely what blocked proof.

Do not choose a bucket without evidence.

---

# Existing test comparison

If the live result contradicts the green renderer tests, explain exactly why those tests are insufficient.

Examples:
- test injects overlay directly while live state produces null;
- live operation cleanup/path differs;
- test uses fallback render branch while live uses HQ branch;
- test asserts changed bytes but not visible badge;
- different dimensions/date availability/runtime path;
- another concrete difference.

Do not speculate — compare the actual live branch and test branch.

---

# No implementation

Do NOT:

- modify app behavior;
- change renderer logic;
- change date formatting;
- change upload logic;
- alter tests;
- alter docs;
- stage;
- commit;
- push;
- run the full project suite.

This is runtime diagnosis only.

---

# Required output

Return:

1. initial `git status --short`
2. exact runtime branch/path used for the live operation
3. actual `Show date` / date-overlay values at CTA press
4. operation temp directory
5. rendered reference/capture file paths and sizes
6. whether each extracted JPEG visibly contains its date badge
7. hashes of both extracted JPEGs if obtained
8. exact upload file paths/sizes/hashes for SLOT_ONE/SLOT_TWO, with secrets redacted
9. whether rendered files and upload files/bytes are identical
10. whether any overwrite/re-encode/substitution occurred
11. selected fault bucket A/B/C/D/E
12. exact evidence supporting that bucket
13. comparison with the previously green renderer test path
14. why prior tests/static analysis did or did not expose the live problem
15. minimal fix strategy concept only if bucket A/B/C is proven
16. if bucket D is proven, exact next partner-side verification to perform
17. whether any further user action is required
18. confirmation no production files were modified
19. confirmation nothing was staged/committed/pushed

Then STOP.

Do not proceed to scope confirmation or implementation.
