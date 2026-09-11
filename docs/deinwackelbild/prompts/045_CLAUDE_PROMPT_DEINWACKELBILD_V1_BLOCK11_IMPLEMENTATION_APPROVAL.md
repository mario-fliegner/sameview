# BLOCK 11 --- IMPLEMENTATION APPROVAL

## Objective

Implement **exactly** the Block 11 scope confirmed by Block 11B for the
SameView Android DeinWackelbild integration.

Block 11B is approved. There are no remaining product decisions.

This is now the implementation gate. Do not perform another architecture
redesign or reopen resolved decisions.

------------------------------------------------------------------------

## Repository Baseline

Expected baseline from Block 11B:

-   Branch: `main`
-   HEAD: `41b0e4f`
-   Working tree: clean

Before editing:

1.  Verify branch, HEAD, and `git status --short`.
2.  Re-read the relevant Source-of-Truth documents:
    -   `CLAUDE_PROJECT_INSTRUCTION.md`
    -   `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
    -   `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
    -   `docs/IMPLEMENTATION_NOTES.md`
3.  Re-read the current versions of all eight authorized files.
4.  Confirm there is no material drift from the approved Block 11B
    scope.

If HEAD or repository contents have materially changed such that the
approved implementation is no longer safe, **STOP without implementation
and report the conflict**.

Do not treat automatically archived prompt files as implementation
drift.

------------------------------------------------------------------------

# Approved Architecture

Build on the **existing** `WackelbildOperationState` model:

-   `Idle`
-   `Preparing`
-   `AwaitingFallbackConfirmation`
-   `CreatingHandoff`
-   `UploadingSlot(slot)`
-   `Ready(checkoutUrl, usedFallback)`
-   `Failed(failure)`

Do **not** introduce the stale/unimplemented `WackelbildOperationPhase`
architecture from the old plan text.

Do **not** create a second parallel operation state machine.

The stale §14.3/§14.4 documentation must be corrected to describe the
implementation that actually exists after Block 11.

Use only the minimum additional in-memory bookkeeping required for
Custom Tab launch/return behavior.

The approved Block 11B architecture is:

-   ViewModel-owned buffered one-shot event/Channel for checkout launch
    requests.
-   Minimal in-memory state recording whether a Custom Tab was
    successfully launched and SameView is awaiting its return.
-   A pending checkout URL only when `Ready` occurs while the Wackelbild
    screen is backgrounded.
-   Reuse the existing `onScreenActive()` / `onScreenInactive()`
    lifecycle hooks.
-   A retained in-memory checkout URL only after Custom Tab launch
    failure so the same URL can be retried.
-   No DataStore.
-   No `SavedStateHandle`.
-   No persistent handoff/checkout state.
-   No polling.
-   No order-status inference.

Use the project's existing one-shot event conventions where applicable.

------------------------------------------------------------------------

# Locked Product Decisions

These are final. Do not reinterpret them.

## 1. No initial consent dialog

There is **no separate consent dialog before preparation/upload**.

Approved sequence:

1.  Wackelbild screen is open.
2.  Persistent disclosure is visible.
3.  User taps the CTA.
4.  The CTA tap itself is explicit consent.
5.  `viewModel.startOperation()` starts immediately.
6.  Local preparation/rendering runs.
7.  Only if fallback output is required, the fallback confirmation
    dialog appears.
8.  Network Create may start only after CTA consent and, where
    necessary, fallback confirmation.

Do not add: - an initial consent dialog, - a generic external-site
warning, - an additional pre-transfer confirmation.

## 2. Back during fallback confirmation

When `AwaitingFallbackConfirmation` is active:

-   Back behaves exactly like the fallback dialog's Cancel action.
-   It cancels via the existing operation-cancellation path.
-   It must **not** open the generic transfer-cancellation dialog.

## 3. `HANDOFF_FAILED`

Map `HANDOFF_FAILED` to the same generic non-recoverable
creation-failure copy as `PREPARATION_FAILED` and
`INVALID_LOCAL_OUTPUT`.

No dedicated retry action.

Never expose HTTP/server/handoff/token/request details.

------------------------------------------------------------------------

# Exact UI / Localization Contract

Use the following final strings exactly.

## Persistent disclosure

German:

`Deine beiden Bilder werden zur Gestaltung an DeinWackelbild.de übertragen. Die Bestellung schließt du dort ab.`

English:

`Your two images are sent to DeinWackelbild.de to create your print. You complete the order there.`

Suggested key:

`wackelbild_transfer_disclosure`

## CTA

German:

`Bestelle dein Wackelbild`

English:

`Order your lenticular print`

Suggested key:

`wackelbild_cta_order`

## Busy state

German:

`Wackelbild wird vorbereitet …`

English:

`Preparing your lenticular print …`

Suggested key:

`wackelbild_loading_preparing`

All active preparation/network phases use this **same** visible busy
presentation.

Do not expose: - internal phase names, - upload slots, - retry counts, -
percentages, - HTTP state, - handoff IDs.

## Generic active-transfer cancellation

German:

Title: `Übertragung abbrechen?`

Message: `Die Bilder werden gerade an DeinWackelbild.de übertragen.`

Continue: `Weiter übertragen`

Cancel: `Abbrechen`

English:

Title: `Cancel transfer?`

Message: `Your images are currently being sent to DeinWackelbild.de.`

Continue: `Keep transferring`

Cancel: `Cancel`

Suggested keys:

-   `wackelbild_cancel_transfer_title`
-   `wackelbild_cancel_transfer_message`
-   `wackelbild_cancel_transfer_continue`
-   `wackelbild_cancel_transfer_stop`

## Fallback confirmation

German:

Title: `Originalqualität nicht verfügbar`

Message:
`Für diesen Vergleich sind die Originalbilder nicht verfügbar. Dein Wackelbild kann trotzdem mit den vorhandenen Bildern erstellt werden – möglicherweise mit geringerer Druckqualität.`

Buttons: - `Abbrechen` - `Trotzdem fortfahren`

English:

Title: `Original quality not available`

Message:
`The original images aren't available for this comparison. Your lenticular print can still be created using the available images, possibly at lower print quality.`

Buttons: - `Cancel` - `Continue anyway`

Suggested keys:

-   `wackelbild_quality_fallback_title`
-   `wackelbild_quality_fallback_message`
-   `wackelbild_quality_fallback_cancel`
-   `wackelbild_quality_fallback_continue`

Do not rewrite this copy.

## Failure mappings

### `NETWORK_UNAVAILABLE`

German: `Keine Internetverbindung`

English: `No internet connection`

Dedicated retry action:

German: `Erneut versuchen`

English: `Try again`

### `SERVER_TEMPORARY`

German: `Übertragung nicht möglich`

English: `Transfer not possible`

Dedicated retry action:

German: `Erneut versuchen`

English: `Try again`

### `INTEGRATION_UNAVAILABLE`

German:
`DeinWackelbild.de ist derzeit nicht verfügbar. Bitte versuche es später erneut.`

English:
`DeinWackelbild.de is currently unavailable. Please try again later.`

No dedicated retry button.

### `PREPARATION_FAILED`, `INVALID_LOCAL_OUTPUT`, `HANDOFF_FAILED`

German: `Wackelbild kann nicht erstellt werden`

English: `This lenticular print can't be created`

No dedicated retry button.

Suggested keys:

-   `wackelbild_error_no_internet`
-   `wackelbild_error_transfer_failed`
-   `wackelbild_error_retry`
-   `wackelbild_error_preparation_failed`
-   `wackelbild_error_integration_unavailable`

`HANDOFF_FAILED` must reuse the generic preparation-failure copy; do not
create technical error text.

## Custom Tab launch failure

German:

`DeinWackelbild.de konnte nicht geöffnet werden.`

Action: `DeinWackelbild.de öffnen`

English:

`DeinWackelbild.de couldn't be opened.`

Action: `Open DeinWackelbild.de`

Suggested keys:

-   `wackelbild_custom_tab_open_failed`
-   `wackelbild_custom_tab_open_retry`

------------------------------------------------------------------------

# Exact State → UI Behavior

## `Idle`

-   Normal preview.
-   Persistent disclosure visible.
-   CTA visible and enabled when the screen's existing prerequisites
    allow it.
-   Date toggle editable according to existing usability rules.
-   Normal Back navigation.

## `Preparing`, `CreatingHandoff`, `UploadingSlot(*)`

-   CTA hidden/replaced by busy presentation.
-   Show one spinner and the generic preparation copy.
-   Date toggle disabled.
-   Date-toggle value retained.
-   Tilt/swipe preview remains usable.
-   Back opens the generic transfer-cancellation dialog.

Generic dialog actions:

-   Continue/Keep transferring: dismiss dialog only.
-   Cancel/Abbrechen: invoke the existing operation cancellation path.

## `AwaitingFallbackConfirmation`

-   Show the exact fallback dialog.
-   Date toggle remains disabled.
-   Do not start network Create until confirmation.
-   Continue invokes the existing fallback-confirmation path.
-   Cancel invokes the existing operation-cancellation path.
-   System/app Back behaves exactly like Cancel.
-   Never stack the generic transfer-cancellation dialog on top.

## `Ready(checkoutUrl, usedFallback)`

This is transient application state, not a user-facing success screen.

-   No intermediate success screen.
-   No normal-path "Open" button.
-   Automatically request Custom Tab launch with the exact
    `checkoutUrl`.
-   Never reconstruct, modify, normalize, or display the checkout URL.
-   Launch at most once for a given Ready result.
-   Recomposition must not relaunch it.
-   Rotation must not relaunch an already-consumed launch event.
-   Do not launch while the screen is backgrounded.
-   If Ready occurs while backgrounded, retain it in memory and emit the
    launch only after the screen becomes active.

## Custom Tab launch succeeds

-   Mark in memory that SameView is awaiting the browser/Custom-Tab
    return.
-   Return the operation UI to its normal post-launch state as required
    by the approved architecture.
-   Do not infer that an order was placed.
-   Do not poll.
-   Do not persist checkout/handoff state.

The date toggle must remain non-editable until the actual return to
SameView.

## Custom Tab launch fails

-   Retain the exact checkout URL in memory.
-   Show the approved open-failure copy and action.
-   Retry-open uses the same URL.
-   Retry-open must not:
    -   rerender,
    -   create another handoff,
    -   upload again.
-   Do not display/log the raw URL.
-   Date toggle remains disabled while this flow is unresolved.
-   Normal Back navigation is allowed.
-   No additional Cancel button.

## Browser/Custom-Tab return

Only when a Custom Tab was previously launched successfully and SameView
becomes active again:

-   reset visible image to Reference,
-   re-enable date-toggle editing,
-   retain the date-toggle value,
-   show the normal CTA again,
-   clear the awaiting-return marker,
-   do not infer order status,
-   do not poll,
-   do not automatically relaunch the checkout URL.

An ordinary unrelated background/foreground cycle must not trigger this
reset.

------------------------------------------------------------------------

# Authorized File Scope

Modify **only** these eight files:

1.  `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
2.  `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
3.  `app/src/test/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModelTest.kt`
4.  `app/src/androidTest/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreenTest.kt`
5.  `app/src/main/res/values/strings.xml`
6.  `app/src/main/res/values-de/strings.xml`
7.  `docs/IMPLEMENTATION_NOTES.md`
8.  `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

No ninth file is approved.

If implementation genuinely requires another file, **STOP before
touching it** and report why the approved eight-file scope is
insufficient.

------------------------------------------------------------------------

# Explicitly Forbidden / Untouched

Do not modify:

-   `WackelbildCustomTabLauncher.kt`
-   `WackelbildHandoffOrchestrator.kt`
-   `WackelbildHandoffOrchestratorTest.kt`
-   `WackelbildOperationState.kt`
-   `WackelbildPrintRenderer.kt`
-   `WackelbildTempFileManager.kt`
-   `net/deinwackelbild/*`
-   `MainActivity.kt`
-   navigation files
-   `AndroidManifest.xml`
-   Gradle files
-   version catalog
-   camera code
-   session-storage code
-   `RELEASE_HARDENING_AUDIT_V2.md`
-   `CLAUDE_PROJECT_INSTRUCTION.md`

Also forbidden:

-   unrelated refactoring,
-   renaming unrelated symbols,
-   formatting unrelated code,
-   dependency changes,
-   permission changes,
-   analytics/telemetry/tracking,
-   persistence of checkout/handoff state,
-   order-status polling,
-   release-compliance changes belonging to later blocks.

------------------------------------------------------------------------

# Implementation Discipline

Make the smallest targeted implementation that satisfies this contract.

Preserve all existing: - tilt behavior, - swipe behavior, - date-overlay
value behavior, - renderer behavior, - API behavior, - retry/backoff
behavior, - cancellation semantics, - temp-file cleanup, - session
behavior, - responsive layout behavior.

Do not "clean up" nearby code.

Do not alter Block 8 retry/orchestration logic.

Do not alter Block 10 Custom Tab launcher implementation.

------------------------------------------------------------------------

# Required Unit Tests

Update/add tests in `WackelbildViewModelTest.kt` covering at minimum:

1.  Ready while foregrounded emits exactly one checkout launch event.
2.  Ready while backgrounded emits no launch event immediately.
3.  Backgrounded Ready emits exactly once after `onScreenActive()`.
4.  Repeated active/recomposition-equivalent handling does not duplicate
    the event.
5.  Successful launcher result marks the flow as awaiting browser
    return.
6.  Successful launcher result returns operation UI to the approved
    post-launch state.
7.  Failed launcher result retains the exact checkout URL.
8.  Retry-open emits the exact same checkout URL.
9.  Retry-open does not invoke renderer/API/orchestrator again.
10. Browser return resets visible image to Reference.
11. Browser return re-enables date-toggle editing.
12. Browser return does not change the date-toggle value.
13. Browser return clears the awaiting-return marker.
14. A later ordinary resume after marker clearing does nothing.
15. Ordinary background/foreground without successful Custom Tab launch
    does not perform browser-return reset.
16. Existing operation/retry/cancellation tests remain unchanged and
    green.

Do not weaken, disable, or delete existing tests.

------------------------------------------------------------------------

# Required Screen / Instrumentation Tests

Update/add `WackelbildScreenTest.kt` coverage for at minimum:

1.  Persistent disclosure is visible.
2.  CTA uses exact German text.
3.  CTA uses exact English text.
4.  CTA starts the operation directly.
5.  No initial consent dialog appears.
6.  `Preparing` shows spinner + generic busy copy.
7.  `CreatingHandoff` shows the same spinner + copy.
8.  `UploadingSlot(ONE)` shows the same spinner + copy.
9.  `UploadingSlot(TWO)` shows the same spinner + copy.
10. Internal phase/slot details are not exposed.
11. CTA is absent/disabled while busy.
12. Date toggle is disabled during active operation.
13. Date toggle remains disabled during fallback confirmation.
14. Date toggle remains disabled while awaiting successful Custom-Tab
    return.
15. Tilt/swipe preview interaction remains available while busy.
16. Fallback dialog uses exact title/message/buttons.
17. Fallback Continue invokes the existing confirmation path.
18. Fallback Cancel invokes cancellation.
19. Back while fallback is visible behaves like Cancel.
20. Back while fallback is visible does not show the generic
    transfer-cancellation dialog.
21. Back during `Preparing` shows generic cancellation dialog.
22. Back during `CreatingHandoff` shows generic cancellation dialog.
23. Back during `UploadingSlot(*)` shows generic cancellation dialog.
24. Continue-transfer dismisses only that dialog.
25. Cancel-transfer invokes operation cancellation.
26. `NETWORK_UNAVAILABLE` exact copy + retry action.
27. `SERVER_TEMPORARY` exact copy + retry action.
28. `INTEGRATION_UNAVAILABLE` exact copy and no dedicated retry.
29. `PREPARATION_FAILED` exact generic creation-failure copy.
30. `INVALID_LOCAL_OUTPUT` same generic creation-failure copy.
31. `HANDOFF_FAILED` same generic creation-failure copy.
32. No raw technical/server error is exposed.
33. Ready invokes the fake Custom Tab launcher with the exact checkout
    URL.
34. Recomposition does not invoke the launcher a second time.
35. Launcher failure shows exact failure copy/action.
36. Retry-open invokes the launcher with the exact same URL.
37. Retry-open causes no rerender/re-upload/new handoff.
38. No intermediate success screen exists.
39. Browser-return UI resets to Reference.
40. Browser-return restores date-toggle editing while retaining its
    value.
41. Browser return restores normal CTA.
42. No automatic relaunch occurs after return.
43. Accessibility semantics remain valid for CTA, spinner, dialogs, and
    error actions.
44. Existing responsive behavior remains intact.

Use a fake launcher seam. Automated tests must not open a real browser
and must not make real network traffic.

------------------------------------------------------------------------

# Documentation Updates

## `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`

Correct the stale §14.3/§14.4 architecture so the plan matches the
implementation actually shipped after Block 11.

Specifically:

-   remove/supersede the unimplemented parallel
    `WackelbildOperationPhase` design as live guidance,
-   document the real `WackelbildOperationState` model,
-   document the minimal in-memory Custom Tab event/await-return
    bookkeeping,
-   document foreground-deferred launch,
-   document launch failure + same-URL retry-open,
-   document browser-return reset,
-   document the locked fallback Back behavior,
-   document the final `HANDOFF_FAILED` user-facing mapping.

Do not rewrite unrelated historical sections.

## `docs/IMPLEMENTATION_NOTES.md`

Add one Block 11 entry in the existing style recording: -
CTA/disclosure, - collapsed busy UI, - fallback confirmation, -
cancellation behavior, - error mapping, - Custom Tab launch, -
foreground deferral, - same-URL retry-open, - browser-return reset, -
files changed, - tests/verification, - remaining real-device validation.

Do not rewrite historical Block 1--10 entries.

------------------------------------------------------------------------

# Required Verification

After implementation, run:

``` text
./gradlew testDebugUnitTest
./gradlew compileDebugAndroidTestKotlin
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
./gradlew pixel2Api29DebugAndroidTest
git diff --check
git status --short
```

Do not silently skip instrumentation.

If `pixel2Api29DebugAndroidTest` cannot execute because of an
environmental problem, report the exact reason. Use the project's
established attached-device fallback only if one is actually available.

The five failures already A/B-proven against the Block-9 baseline during
Block 10 verification are outside Block 11:

-   four existing `CompareScreenTest` landscape metadata failures,
-   one existing `EditSessionScreenTest` reference-date failure.

If they reproduce with the **same failure mode**, report them as the
already-proven pre-existing failures.

Do **not** modify those tests or their production code in Block 11.

If their failure mode changes, or any Wackelbild test fails, treat it as
a Block 11 regression until proven otherwise.

Never suppress, disable, baseline, or weaken a failing test.

------------------------------------------------------------------------

# Physical-Device Validation Still Required

Automated success does not complete real pilot validation.

After implementation, explicitly report that physical-device validation
remains required for:

-   portrait session,
-   landscape session,
-   date overlay OFF,
-   date overlay ON,
-   normal HQ path,
-   forced/real fallback path,
-   exact fallback dialog,
-   CTA → preparation → upload → Custom Tab happy path,
-   Reference → slot `one`,
-   Capture → slot `two`,
-   generated dimensions/file sizes,
-   visual parity against frozen images,
-   EXIF/GPS privacy spot-check,
-   cancellation during active transfer,
-   retry after temporary network failure where practical,
-   Custom Tab return reset,
-   no automatic relaunch after return,
-   no order-status inference,
-   operation temp-file cleanup,
-   large-session memory/performance,
-   real DeinWackelbild pilot configurator/checkout prefill.

Do not claim automated tests replace these checks.

------------------------------------------------------------------------

# Release Boundary

Block 11 is not final Play-release approval.

Do not perform Block 14/compliance work here.

Still later:

-   Google Play Data Safety review/update,
-   Privacy Policy disclosure/update,
-   partner/commission disclosure review,
-   final release-hardening audit.

No new telemetry, analytics, tracking, permission, or persistence is
permitted.

------------------------------------------------------------------------

# Required Final Implementation Report

After implementation and verification, return exactly these sections:

## 1. Repository Baseline

-   branch
-   initial HEAD
-   initial status
-   drift result

## 2. Files Modified

-   exact paths
-   confirm exactly eight authorized files
-   if not exactly eight, explain why implementation stopped

## 3. UI Implementation

-   disclosure
-   CTA
-   busy behavior
-   date-toggle locking
-   preview interaction preservation
-   fallback dialog
-   Back/cancel behavior
-   error mapping

## 4. Custom Tab Architecture

-   one-shot launch mechanism
-   foreground/background handling
-   at-most-once behavior
-   launch-result handling
-   same-URL retry-open
-   browser-return reset
-   confirm no persistence/polling/order inference

## 5. Locked Decisions Verification

Explicitly confirm: - no initial consent dialog - exact fallback copy -
fallback Back == Cancel - `HANDOFF_FAILED` generic mapping

## 6. Tests Added / Updated

-   ViewModel tests
-   instrumentation tests
-   existing tests preserved

## 7. Documentation

-   exact plan sections corrected
-   Implementation Notes entry
-   confirm unrelated/historical docs untouched

## 8. Security / Privacy

-   no raw checkout URL displayed/logged
-   no server error exposed
-   no new telemetry
-   no persistence
-   no permission/dependency changes

## 9. Verification

For every required command: - command - result - test counts where
available - exact failures if any - classification of the five
already-proven baseline failures if reproduced unchanged

## 10. Regression Safety

Explicitly confirm unchanged: - renderer - orchestrator/retry logic -
API client - temp-file manager - operation state model - Custom Tab
launcher - navigation - camera/session storage - manifest/Gradle

## 11. Physical-Device Status

List what remains to be validated manually.

## 12. Diff Scope

-   `git diff --stat`
-   `git status --short`
-   unauthorized changes: yes/no

## 13. Remaining Work

Only genuinely remaining later blocks/compliance/pilot validation.

## 14. Gate Result

If implementation and required automated verification are complete with
no Block 11 regression:

`BLOCK 11 IMPLEMENTED — READY FOR PHYSICAL-DEVICE PILOT VALIDATION`

If implementation is complete but a required test cannot be classified
safely:

`BLOCK 11 IMPLEMENTED — VERIFICATION BLOCKED`

If implementation cannot safely remain within the approved eight-file
scope:

`BLOCK 11 BLOCKED — SCOPE REAPPROVAL REQUIRED`

------------------------------------------------------------------------

Implement now.

Do not ask for another approval unless: - repository drift invalidates
this scope, - a ninth file is genuinely required, - or a new
contradiction makes the approved behavior unsafe.
