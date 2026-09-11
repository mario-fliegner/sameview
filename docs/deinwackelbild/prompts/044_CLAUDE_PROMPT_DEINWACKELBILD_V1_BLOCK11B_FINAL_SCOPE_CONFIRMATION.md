# BLOCK 11B — FINAL SCOPE CONFIRMATION / IMPLEMENTATION APPROVAL GATE

## Context

We are continuing the SameView Android project and the DeinWackelbild integration.

Block 11A analysis is complete. Do **not** re-open already resolved product decisions unless repository evidence or a Source-of-Truth document proves a real contradiction.

Repository baseline from Block 11A:
- Branch: `main`
- HEAD: `41b0e4f`
- Working tree was clean at the start of Block 11A
- Block 10 is committed
- No Block 11 implementation exists yet

The Block 11A analysis found an important implementation/documentation divergence:

- `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` §14.3/§14.4 describes a more elaborate `WackelbildOperationPhase` / `CustomTabAwaitState` design that was never actually implemented in Block 8.
- The actual production implementation uses `WackelbildOperationState` with:
  - `Idle`
  - `Preparing`
  - `AwaitingFallbackConfirmation`
  - `CreatingHandoff`
  - `UploadingSlot(slot)`
  - `Ready(checkoutUrl, usedFallback)`
  - `Failed(failure)`
- Block 11 must build on the **actual shipped state model** and add only the minimum Custom-Tab bookkeeping required for the product behavior.
- Do **not** resurrect or introduce a second parallel operation state machine merely to match stale plan text.
- The affected plan text must instead be corrected to match the actual architecture.

This gate is **scope confirmation only**. Do not implement anything yet.

---

## Mandatory Source-of-Truth Review

Before confirming scope, re-read the relevant repository documentation and current implementation, especially:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `docs/IMPLEMENTATION_NOTES.md`

And inspect the actual current code at minimum:

- `WackelbildViewModel.kt`
- `WackelbildScreen.kt`
- `WackelbildOperationState.kt`
- `WackelbildHandoffOrchestrator.kt`
- `WackelbildCustomTabLauncher.kt`
- existing Wackelbild ViewModel/unit tests
- existing Wackelbild screen/instrumentation tests
- EN/DE string resources

If documentation and current code conflict, explicitly identify the conflict. The authoritative UX/product behavior comes from the integration specification unless the user decisions locked below explicitly resolve an ambiguity.

---

# Locked User Decisions From Block 11A

These decisions are now approved and MUST NOT remain open.

## 1. Consent Model

There is **no separate initial consent dialog**.

The approved sequence is:

1. User opens the Wackelbild screen.
2. The transfer disclosure is persistently visible on the screen.
3. User taps:
   - DE: `Bestelle dein Wackelbild`
   - EN: `Order your lenticular print`
4. That CTA tap itself is the explicit consent and immediately starts `viewModel.startOperation()`.
5. Local preparation/rendering occurs.
6. Only if fallback output is required, show the fallback confirmation dialog.
7. Network Create may begin only after the CTA consent and, where applicable, fallback confirmation.

Do not add an additional "you are leaving SameView", external-site warning, or pre-transfer confirmation dialog.

---

## 2. Final Fallback Warning Copy

The previously unresolved fallback message is now explicitly approved.

### German

Title:
`Originalqualität nicht verfügbar`

Message:
`Für diesen Vergleich sind die Originalbilder nicht verfügbar. Dein Wackelbild kann trotzdem mit den vorhandenen Bildern erstellt werden – möglicherweise mit geringerer Druckqualität.`

Buttons:
- `Abbrechen`
- `Trotzdem fortfahren`

### English

Title:
`Original quality not available`

Message:
`The original images aren't available for this comparison. Your lenticular print can still be created using the available images, possibly at lower print quality.`

Buttons:
- `Cancel`
- `Continue anyway`

This wording is now locked. Do not rewrite or "improve" it.

---

## 3. Back While Fallback Confirmation Is Visible

Approved behavior:

- Back while `AwaitingFallbackConfirmation` is active behaves exactly like the fallback dialog's `Abbrechen` / `Cancel`.
- It cancels the operation via the existing cancellation path.
- Do **not** stack the generic transfer-cancellation dialog on top of the fallback dialog.

This decision is now locked.

---

## 4. `HANDOFF_FAILED` User-Facing Mapping

Approved mapping:

- `HANDOFF_FAILED` → the same generic preparation/output failure copy used for a non-recoverable creation failure.
- DE: `Wackelbild kann nicht erstellt werden`
- EN: `This lenticular print can't be created`
- No dedicated retry button.
- No HTTP status, server message, handoff terminology, token, request detail, or other technical information may be exposed.

This decision is now locked.

---

# Required Block 11 Behavior

Confirm that the proposed implementation scope supports all of the following without expanding beyond Block 11.

## Persistent disclosure

Always visible in the Wackelbild screen:

DE:
`Deine beiden Bilder werden zur Gestaltung an DeinWackelbild.de übertragen. Die Bestellung schließt du dort ab.`

EN:
`Your two images are sent to DeinWackelbild.de to create your print. You complete the order there.`

## CTA

DE:
`Bestelle dein Wackelbild`

EN:
`Order your lenticular print`

CTA is visible/enabled only when appropriate and disappears/replaces its area during the active operation.

## Busy UI

All active preparation/network phases collapse into one user-visible busy state:

DE:
`Wackelbild wird vorbereitet …`

EN:
`Preparing your lenticular print …`

Do not expose:
- Preparing vs Create vs upload-one vs upload-two
- percentages
- retry counts
- HTTP information
- handoff IDs
- internal slot terminology

Tilt/swipe preview interaction remains active while busy.

The date toggle becomes non-editable from CTA tap until:
- a final error occurs, or
- the user returns from the successfully opened Custom Tab.

Its value must be retained.

## Transfer cancellation

For `Preparing`, `CreatingHandoff`, and `UploadingSlot(*)`, Back opens the approved cancellation confirmation.

DE:
- `Übertragung abbrechen?`
- `Die Bilder werden gerade an DeinWackelbild.de übertragen.`
- `Weiter übertragen`
- `Abbrechen`

EN:
- `Cancel transfer?`
- `Your images are currently being sent to DeinWackelbild.de.`
- `Keep transferring`
- `Cancel`

`AwaitingFallbackConfirmation` is excluded from this generic dialog as locked above.

## Failure UI

Approved mappings:

- `NETWORK_UNAVAILABLE`
  - DE: `Keine Internetverbindung`
  - EN: `No internet connection`
  - explicit retry action:
    - DE: `Erneut versuchen`
    - EN: `Try again`

- `SERVER_TEMPORARY`
  - DE: `Übertragung nicht möglich`
  - EN: `Transfer not possible`
  - explicit retry action:
    - DE: `Erneut versuchen`
    - EN: `Try again`

- `INTEGRATION_UNAVAILABLE`
  - DE: `DeinWackelbild.de ist derzeit nicht verfügbar. Bitte versuche es später erneut.`
  - EN: `DeinWackelbild.de is currently unavailable. Please try again later.`
  - no dedicated retry button

- `PREPARATION_FAILED`
- `INVALID_LOCAL_OUTPUT`
- `HANDOFF_FAILED`
  - DE: `Wackelbild kann nicht erstellt werden`
  - EN: `This lenticular print can't be created`
  - no dedicated retry button

The normal CTA may become available again after a final failure according to the approved state mapping.

## Custom Tab success

When `Ready(checkoutUrl, usedFallback)` is reached:

- No intermediate success screen.
- No normal-path "Open" button.
- Launch the exact server-returned checkout URL automatically.
- Never reconstruct or modify the URL.
- Launch at most once for a given Ready result.
- Recomposition must not relaunch it.
- Rotation must not relaunch an already-consumed launch event.
- Do not launch while the screen is backgrounded.
- If Ready is reached while backgrounded, defer launch until the screen is active again.
- Do not persist checkout/handoff state to DataStore or `SavedStateHandle`.

Use the smallest architecture compatible with the actual existing code. The Block 11A proposal was:
- a ViewModel-owned buffered one-shot event/Channel for launch requests,
- minimal in-memory bookkeeping for whether a Custom Tab has been launched and is awaiting return,
- a pending checkout URL only when Ready occurs while backgrounded,
- reuse of the existing `onScreenActive()` / `onScreenInactive()` lifecycle hooks.

Do not introduce a second operation state machine if this can be implemented safely with the actual `WackelbildOperationState`.

## Custom Tab launch failure

Approved copy:

DE:
- `DeinWackelbild.de konnte nicht geöffnet werden.`
- action: `DeinWackelbild.de öffnen`

EN:
- `DeinWackelbild.de couldn't be opened.`
- action: `Open DeinWackelbild.de`

Requirements:
- retain the same checkout URL only in memory,
- retry-open uses that exact same URL,
- no new render,
- no new Create,
- no re-upload,
- no raw URL displayed/logged,
- normal Back navigation is allowed,
- no additional Cancel button.

## Browser return

After a successfully launched Custom Tab, when SameView becomes active again:

- reset visible image to Reference,
- re-enable date-toggle editing,
- retain the date-toggle value,
- show the normal CTA again,
- do not infer order status,
- do not poll,
- do not automatically relaunch the checkout URL,
- clear the "awaiting Custom Tab return" marker so later ordinary resumes do nothing.

Ordinary background/foreground cycles unrelated to a Custom Tab must not trigger this reset.

---

# Proposed File Scope From Block 11A

Block 11A proposed exactly these eight files:

1. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
2. `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
3. `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
4. `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
5. `app/src/main/res/values/strings.xml`
6. `app/src/main/res/values-de/strings.xml`
7. `docs/IMPLEMENTATION_NOTES.md`
8. `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

Your job in this gate is to verify this list against the current repository.

Do not automatically accept it if repository evidence proves another file is genuinely required.

If another file is required:
- identify it,
- explain exactly why,
- explain why the eight-file scope is insufficient,
- STOP without implementation.

If no additional file is required:
- explicitly confirm the final eight-file scope.

Files expected to remain untouched unless repository evidence proves otherwise include:

- `WackelbildCustomTabLauncher.kt`
- `WackelbildHandoffOrchestrator.kt`
- `WackelbildHandoffOrchestratorTest.kt`
- `WackelbildOperationState.kt`
- `WackelbildPrintRenderer.kt`
- `WackelbildTempFileManager.kt`
- `net/deinwackelbild/*`
- `MainActivity.kt`
- navigation files
- `AndroidManifest.xml`
- Gradle files
- camera/session-storage code
- `RELEASE_HARDENING_AUDIT_V2.md`
- `CLAUDE_PROJECT_INSTRUCTION.md`

No unrelated cleanup, refactor, rename, formatting churn, dependency change, manifest change, permission change, or architecture rewrite is allowed.

---

# Required Test Scope Confirmation

Confirm the exact tests that implementation must add/update.

At minimum, the implementation gate must verify:

## ViewModel/unit behavior

- Ready while foregrounded emits exactly one launch event.
- Ready while backgrounded emits no launch event until `onScreenActive()`.
- Backgrounded Ready is emitted exactly once after resume.
- Successful launcher result marks the flow as awaiting browser return and returns operation UI to the normal post-launch state.
- Failed launcher result retains the exact same checkout URL for retry-open.
- Retry-open emits the same URL without calling renderer/API/orchestrator again.
- Browser return resets visible image to Reference.
- Browser return re-enables date-toggle editing without changing its value.
- Browser return clears the awaiting-return marker.
- Ordinary resume without a successful Custom-Tab launch does not perform browser-return reset.
- Existing operation/retry/cancellation tests remain unchanged and green.

## Screen/instrumentation behavior

- Persistent disclosure visible.
- CTA exact DE/EN text.
- CTA starts operation directly; no separate initial consent dialog.
- Busy spinner/copy shown for all active phases without exposing phase details.
- CTA absent/disabled while busy.
- Date toggle disabled during the complete operation/Custom-Tab-await window.
- Preview tilt/swipe remains available while busy.
- Fallback dialog exact title/message/buttons.
- Fallback Continue calls the existing confirmation path.
- Fallback Cancel cancels.
- Back on fallback behaves like Cancel and does not show the generic cancel-transfer dialog.
- Busy Back shows generic transfer-cancel dialog.
- "Weiter übertragen"/"Keep transferring" dismisses only the dialog.
- "Abbrechen"/"Cancel" cancels the operation.
- Error-category → exact approved copy mapping.
- Retry button only for network/server temporary categories.
- Custom Tab event invokes the fake launcher with the exact checkout URL.
- Recomposition does not cause a second launcher invocation.
- Launcher failure shows exact open-failure copy/action.
- Retry-open invokes launcher again with the exact same URL and performs no upload/re-render.
- No intermediate success screen.
- Browser-return UI reset behaves as specified.
- Accessibility semantics remain valid for CTA, spinner, dialogs, error actions.
- Existing responsive layout behavior is not broken.

Use a fake launcher seam. Do not open a real browser or make real network traffic in automated tests.

---

# Required Verification Commands for the Implementation Gate

Confirm that the later implementation gate must run, at minimum:

```text
./gradlew testDebugUnitTest
./gradlew compileDebugAndroidTestKotlin
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
./gradlew pixel2Api29DebugAndroidTest
git diff --check
git status --short
```

If the Managed Device cannot execute for an environmental reason, report that explicitly and use the project's already-established attached-device fallback only if available. Do not silently omit instrumentation.

The five failures already A/B-proven against the Block-9 baseline during Block 10 verification are not to be "fixed" in Block 11:
- four existing `CompareScreenTest` landscape metadata failures,
- one existing `EditSessionScreenTest` reference-date test failure.

If those same failures occur unchanged, report them as the already-proven pre-existing failures. If their failure mode changes, or any Wackelbild test fails, treat that as a Block 11 problem until proven otherwise.

---

# Manual Real-Device Validation to Require After Implementation

Confirm that Block 11 completion will still require physical-device pilot validation, including at least:

- portrait session,
- landscape session,
- date overlay OFF,
- date overlay ON,
- normal HQ path,
- forced/real fallback path and confirmation dialog,
- CTA → preparation → network → Custom Tab happy path,
- exact Reference → slot `one`,
- exact Capture → slot `two`,
- generated image dimensions/file sizes,
- visual parity against SameView's frozen pair,
- EXIF/GPS privacy spot-check,
- cancellation during active transfer,
- retry after temporary network failure where practical,
- Custom Tab return reset,
- no automatic relaunch after return,
- no order-status inference,
- temporary operation-file cleanup,
- large-session memory/performance,
- real checkout/configurator prefill on the DeinWackelbild pilot environment.

Do not claim these are automated substitutes for physical-device validation.

---

# Release / Privacy Boundary

Block 11 is still **not** final Play-release approval.

Do not pull Block 14 work into this gate.

The following remain later release/compliance work unless current Source-of-Truth explicitly says otherwise:

- Google Play Data Safety review/update
- Privacy Policy disclosure
- partner/commission disclosure review
- final release-hardening audit

No telemetry, analytics, tracking, new permissions, or persistence may be introduced.

---

# Required Output — STOP BEFORE IMPLEMENTATION

Return one report with exactly these sections:

## 1. Repository Baseline
- branch
- HEAD
- working-tree status
- whether anything drifted since Block 11A

## 2. Source-of-Truth Recheck
- documents inspected
- code inspected
- contradictions found
- whether the stale §14.3/§14.4 plan issue is confirmed

## 3. Locked User Decisions
Explicitly confirm:
- no initial consent dialog
- final EN/DE fallback copy
- Back on fallback == fallback Cancel
- `HANDOFF_FAILED` mapping

## 4. Final Architecture
Describe the minimum implementation architecture against the actual current code.
Explicitly confirm that no parallel replacement operation state machine will be introduced.

## 5. Final State → UI Mapping
Give the complete state mapping, including Custom-Tab-open-failure and browser-return handling.

## 6. Final File Scope
List **every file** proposed for modification/creation.
State whether the eight-file Block-11A scope is sufficient.
If not, explain and STOP.

## 7. Files Explicitly Untouched
List the important files/subsystems that will not change.

## 8. Exact UI / Localization Contract
List all final EN/DE strings that will be added/used.
There must be **no unresolved wording** remaining.

## 9. Test Scope
List the exact unit/instrumentation behavior that will be added or updated.
Confirm existing tests will not be weakened/disabled.

## 10. Verification Plan
List exact Gradle/git commands.
State what real-device validation remains required.

## 11. Risks
Only concrete Block 11 regression/release risks.
No unrelated improvements.

## 12. Documentation Impact
State exactly which docs change and why.
Confirm historical/unrelated docs remain untouched.

## 13. Remaining Open Decisions
Expected result: `None`.
If anything is genuinely still unresolved, identify it and STOP.

## 14. Gate Result

If and only if everything is fully specified and the file scope is complete, end exactly with:

`BLOCK 11B SCOPE CONFIRMED — READY FOR IMPLEMENTATION APPROVAL`

Otherwise:

`BLOCK 11B BLOCKED — USER DECISION REQUIRED`

---

# Hard Stop

This is a scope-confirmation gate only.

**DO NOT IMPLEMENT.**
**DO NOT MODIFY FILES.**
**DO NOT RUN IMPLEMENTATION EDITS.**
**DO NOT CREATE PRODUCTION CODE.**

After producing the report, STOP and wait for explicit user approval.
