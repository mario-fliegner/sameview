# Claude Prompt — Clean Up Failed Group B Attempt, Audit Uncommitted Work Since 048, Verify, Then Commit Once

## Context

This is a controlled cleanup + verification + commit step.

Important project rule: do not lose any already-successful uncommitted work.

The working tree has not been committed since prompt 048. Therefore multiple independent, already-verified changes are currently uncommitted together.

Known successful changes since prompt 048 include at least:

- Wackelbild Block 3 implementation
- CompareScreen empty-locale fix
- EditSession Group A country-picker test-host fix

Known unsuccessful change that must **not** be kept:

- the most recent Group B synchronization attempt from prompt 064 inside `EditSessionScreenTest.kt`
- this added:
  - one `onChildren` import
  - one `composeRule.waitUntil(...)` block using `onNodeWithTag("edit_session_country_picker_list").onChildren()...`
- this attempted fix failed 3/10 targeted real-device runs with the same `RootViewWithoutFocusException`

Group C remains unresolved and must not be modified.

The user now wants the tree cleaned up, verified, and then **committed once** if everything is clean.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly.

---

# Task overview

Do this in the following order:

1. Inspect the full current working tree.
2. Remove **only** the failed Group B attempt from prompt 064.
3. Preserve all successful prior uncommitted changes.
4. Audit every remaining modified/untracked file and map it to the approved work since prompt 048.
5. Verify there are no accidental/unrelated changes.
6. Run verification commands appropriate for the remaining tree.
7. If and only if the tree is clean and verification is acceptable, create **one commit** containing the successful work since 048.
8. Report exact commit hash and contents.

Do not make any new product/test fix beyond removing the failed Group B attempt.

Do not touch Group C.

Do not continue solving Group B in this prompt.

---

# Step A — Inspect before changing anything

First run and capture:

```text
git status --short
git diff --stat
git diff
```

Also inspect untracked files.

Before editing, explicitly identify:

- every modified file
- every new/untracked file
- which approved work item each belongs to
- whether anything is unexpected/unrelated

If anything cannot be confidently mapped to the approved work since prompt 048, STOP before modifying or committing and report it.

Do not assume a whole file can be reverted just because it contains the failed Group B attempt.

---

# Step B — Remove only the failed Group B attempt

In:

`app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

remove only the changes introduced by prompt 064:

1. remove the newly added import:
   `androidx.compose.ui.test.onChildren`

2. remove only this added wait block from:
   `countryPicker_cancelViaBackPress_preservesOriginalValue`

Conceptually the failed block was:

```kotlin
composeRule.waitUntil(timeoutMillis = 5_000) {
    composeRule.onNodeWithTag("edit_session_country_picker_list")
        .onChildren()
        .fetchSemanticsNodes()
        .isNotEmpty()
}
```

Do **not** remove the earlier successful Group A changes in the same file, especially the `setCountryPickerSheetContent()` host-window flags.

Do **not** modify any other line in that test method.

Do **not** use a whole-file restore command.

Do not reformat unrelated code.

After removing the failed Group B attempt, run:

```text
git diff -- app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt
```

Confirm that:

- Group A fix remains
- Group B method is back to its pre-064 state
- no unrelated hunk changed

---

# Step C — Audit the entire remaining working tree

After cleanup, run:

```text
git status --short
git diff --stat
git diff
```

Map every remaining file to its approved work item.

Expected approved work since prompt 048 may include:

## Wackelbild Block 3

Expected modified/created files from the approved Block 3 scope:

- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
- `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapper.kt`
- `app/src/test/java/com/isardomains/sameview/ui/wackelbild/TiltBlendMapperTest.kt`

## CompareScreen empty-locale fix

Expected:

- `app/src/main/java/com/isardomains/sameview/ui/compare/CompareScreen.kt`

## EditSession Group A test-host fix

Expected:

- `app/src/androidTest/java/com/isardomains/sameview/ui/compare/EditSessionScreenTest.kt`

There may also be documentation changes from prompts 049–051 if they are still uncommitted. If present, verify they match those already-approved Source-of-Truth updates and were not later superseded.

Do not silently include any file that does not map cleanly to an approved prompt/change.

If any unexpected file exists, STOP and report before committing.

---

# Step D — Verification before commit

Because this commit contains UI/test/camera-adjacent work already developed across several approved iterations, run the relevant verification again on the cleaned tree.

At minimum run:

```text
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

Interpretation of the connected suite:

- Group B is expected to still fail because the failed 064 attempt is intentionally removed.
- Group C is expected to still fail.
- No other failures are acceptable.

Expected connected result:

- 1079 total tests
- exactly 2 failures:
  - `EditSessionScreenTest#countryPicker_cancelViaBackPress_preservesOriginalValue`
  - `EditSessionScreenTest#referenceDate_laterThanCapture_showsOrderErrorText_de`
- no new/unexpected failures

If the connected suite shows any additional failure, STOP and do not commit.

If unit tests or assembleDebug fail, STOP and do not commit.

If practical and already part of the project's release-safety convention for this scope, also run:

```text
./gradlew lintDebug
```

If not run, state that explicitly.

Do not suppress any failure.

---

# Step E — Commit once, only if clean

Only if:

- failed Group B attempt is removed
- every remaining file maps to approved work
- no unexpected changes remain
- `testDebugUnitTest` passes
- `assembleDebug` passes
- connected suite has exactly the two known Group B + Group C failures and nothing else

then create **one commit** containing all remaining successful work since prompt 048.

Before committing, show:

```text
git status --short
git diff --stat
```

Then commit once.

Use a concise commit message that reflects the successful work actually included. For example, only if accurate:

```text
Implement lenticular preview and stabilize compare/edit tests
```

If documentation updates are included, the message may still remain concise.

Do not create multiple commits in this prompt.

Do not amend an existing commit.

Do not push.

After committing, run:

```text
git status --short
git rev-parse --short HEAD
git show --stat --oneline --summary HEAD
```

The final working tree should be clean unless there are intentionally excluded files, in which case explain exactly why they remain.

---

# Explicitly forbidden

Do not:

- continue fixing Group B
- touch Group C
- add window-focus polling
- add sleeps/retries
- modify production behavior
- refactor unrelated code
- rename files/classes/variables
- reformat unrelated code
- restore `EditSessionScreenTest.kt` wholesale
- discard the successful Group A fix
- discard Wackelbild Block 3
- push to remote
- create more than one commit

---

# Required final report

Return:

1. **Initial `git status --short`**
2. **Exact failed Group B changes removed**
3. **Confirmation Group A change remains**
4. **Complete list of remaining modified/new files before commit**
5. **Mapping of each file to the approved work item**
6. **Any documentation files included and why**
7. **`./gradlew testDebugUnitTest` result**
8. **`./gradlew assembleDebug` result**
9. **`./gradlew connectedDebugAndroidTest` result**
10. **Exact connected-test failure list**
11. **Whether `lintDebug` was run and result**
12. **Confirmation no unexpected file/change remained**
13. **Commit message**
14. **Commit hash**
15. **`git show --stat --oneline --summary HEAD` result**
16. **Final `git status --short`**
17. **Confirmation no push was performed**
18. **Any remaining unresolved items**
   - Group B
   - Group C

If any prerequisite fails, do not commit and report exactly where/why you stopped.
