# Claude Prompt — Re-Verify Connected Suite After VideoExport Runner Anomaly, Then Commit Once If Clean

## Context

This continues the previous cleanup/audit/verification step.

No code change is approved in this prompt.

The failed Group B experiment from prompt 064 has already been removed.

All previously successful uncommitted work since prompt 048 is still preserved.

The previous verification produced:

- `./gradlew testDebugUnitTest` → BUILD SUCCESSFUL
- `./gradlew assembleDebug` → BUILD SUCCESSFUL
- `./gradlew connectedDebugAndroidTest` → BUILD FAILED after 1h 6m 16s

The connected run reported:

- 1079 expected tests
- 3 named failures
- `Expected 1079 tests, received 1078`
- one anomalous test:
  `VideoExportPipelineTest#t_i_03_highQuality_landscape_producesValidMp4WithSupportedResolution`
- that test ran for about 3038.6 seconds (~50.6 minutes)
- its `<failure>` body was empty
- the corresponding production/test file was not modified in the current working tree

The other two failures are the already-known unresolved issues:

1. `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue` (Group B)
2. `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de` (Group C)

The unexpected VideoExport result may be a runner/device-state anomaly, but that is **not proven**. Therefore do not diagnose or fix it in this prompt. Re-run verification only.

The user wants the already-approved successful work committed **once**, but only after the working tree is verified clean.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Task

Perform a clean verification-only rerun of the connected instrumentation suite.

Do not modify any file.

Do not attempt to fix Group B, Group C, or VideoExport.

Do not add retries, sleeps, ignores, suppressions, or test changes.

Do not reset or discard any successful uncommitted work.

---

# Step 1 — Confirm working tree before rerun

Run:

```text
git status --short
git diff --stat
```

Confirm that the tree is unchanged from the cleaned post-065 state:

- failed Group B `onChildren` experiment is absent
- Group A host-window fix remains
- Wackelbild Block 3 remains
- CompareScreen locale fix remains
- approved docs/strings changes remain
- no unexpected new code changes exist

If any unexpected code change appeared, STOP and do not run toward a commit.

Prompt-archive `.md` files may remain untracked if they were already present as conversation artifacts; do not silently include them unless they are intended project documentation.

---

# Step 2 — Re-run the full connected suite

Run:

```text
./gradlew connectedDebugAndroidTest
```

Do not change the code before or during the run.

Record:

- total runtime
- expected test count
- received test count
- failure count
- exact failing test names
- whether the run completed normally
- whether `VideoExportPipelineTest#t_i_03_highQuality_landscape_producesValidMp4WithSupportedResolution` passed, failed, timed out, hung, or disappeared
- whether any `Expected N tests, received M` anomaly appears

---

# Decision rule

## Case A — Clean expected result

If the rerun completes normally and the only failures are exactly:

- `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`
- `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de`

with:

- no VideoExport failure
- no missing test result
- no new unexpected failure

then continue to Step 3.

## Case B — VideoExport or runner anomaly recurs

If the VideoExport test fails/hangs/times out again, or the runner again reports a missing test/result-count mismatch:

- do not modify code
- do not commit
- stop and report the exact evidence
- treat it as a separate issue requiring its own analysis iteration

## Case C — Any different unexpected failure

If any other unexpected test fails:

- do not modify code
- do not commit
- stop and report exactly what failed

---

# Step 3 — Lint before commit

Only after Case A, run:

```text
./gradlew lintDebug
```

If `lintDebug` fails:

- do not suppress or baseline anything
- do not commit
- stop and report the lint failure

If `lintDebug` passes, continue.

The previous successful results for:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

may be reused because no file has changed since those runs.

Do not rerun them unless needed because of an unexpected working-tree change.

---

# Step 4 — Final audit before commit

Run:

```text
git status --short
git diff --stat
git diff
```

Confirm every remaining code/doc change maps to previously approved work since prompt 048.

Do not include unrelated prompt-archive conversation files in the commit unless they are genuinely intended repository files.

If any unexpected file/change remains, STOP and do not commit.

---

# Step 5 — Create exactly one commit

Only if all of the following are true:

- no code change was made in this prompt
- connected suite completed normally
- only Group B + Group C remain
- VideoExport passed and no runner-count anomaly remained
- `lintDebug` passed
- `testDebugUnitTest` is already green on this exact tree
- `assembleDebug` is already green on this exact tree
- all remaining tracked/untracked project changes are approved and understood

then create exactly **one commit** containing the successful work since prompt 048.

Do not create multiple commits.

Do not amend.

Do not push.

Use a concise message that accurately reflects the included work, for example:

```text
Implement lenticular preview and stabilize compare/edit tests
```

Use a different concise message only if the actual final diff warrants it.

Before commit, ensure prompt-archive conversation files are not accidentally staged unless they are intentionally part of project documentation.

After commit, run:

```text
git status --short
git rev-parse --short HEAD
git show --stat --oneline --summary HEAD
```

---

# Required final report

Return:

1. **Pre-rerun `git status --short`**
2. **Pre-rerun `git diff --stat`**
3. **Full connected-suite runtime**
4. **Expected vs. received test count**
5. **Exact failure count**
6. **Exact failure names**
7. **VideoExport test result**
8. **Whether any runner completion anomaly remained**
9. **`lintDebug` result, if reached**
10. **Confirmation previous `testDebugUnitTest` result remains applicable**
11. **Confirmation previous `assembleDebug` result remains applicable**
12. **Final pre-commit file list**
13. **Confirmation no unexpected changes remained**
14. **Whether prompt-archive files were excluded from staging**
15. **Commit message, if committed**
16. **Commit hash, if committed**
17. **`git show --stat --oneline --summary HEAD`**
18. **Final `git status --short`**
19. **Confirmation no push was performed**
20. **Remaining unresolved issues**
    - Group B
    - Group C

If any stop condition occurs, do not commit and report exactly where and why you stopped.
