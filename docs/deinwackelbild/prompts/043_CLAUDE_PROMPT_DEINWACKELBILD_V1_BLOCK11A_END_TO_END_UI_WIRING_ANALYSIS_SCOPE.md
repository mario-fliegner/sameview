# CLAUDE PROMPT — DEINWACKELBILD V1 — BLOCK 11A: END-TO-END UI WIRING + FIRST PILOT FLOW — ANALYSIS + SCOPE CONFIRMATION ONLY

## Purpose

Blocks 1–10 are implemented and committed.

Block 11 is the first block that connects the existing Wackelbild UI to the real operation and Custom Tab infrastructure.

According to the current authoritative plan, Block 11 owns:
- real order CTA
- consent/disclosure UX
- operation-state rendering/spinner
- fallback-warning confirmation
- Back-confirmation during active transfer
- actual `startOperation()` / `confirmFallbackAndContinue()` / `cancelOperation()` wiring
- actual `WackelbildCustomTabLauncher.launch(...)` wiring
- deterministic fake-client UI/instrumentation coverage
- the first controlled manual happy-path run against the real pilot endpoint

This prompt is **ANALYSIS + SCOPE CONFIRMATION ONLY**.

Do not modify files.
Do not implement code.
Do not make a real API request in this gate.
Do not launch the real pilot Custom Tab.
Do not expose or print the real partner key.
Do not begin Block 12.
Do not modify release/privacy audit docs assigned to Block 14.

---

# 1. Mandatory source review

Read fully:
- `docs/CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `docs/IMPLEMENTATION_NOTES.md`
- `docs/RESPONSIVE_LAYOUT_SYSTEM_V1.md`
- `docs/SETTINGS_UX_V1.md`

Inspect current production code fully:
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildScreen.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildViewModel.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildOperationState.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildHandoffOrchestrator.kt`
- `app/src/main/java/com/isardomains/sameview/ui/wackelbild/WackelbildCustomTabLauncher.kt`
- `app/src/main/java/com/isardomains/sameview/image/wackelbild/WackelbildPrintRenderer.kt`
- `app/src/main/java/com/isardomains/sameview/net/deinwackelbild/DeinWackelbildApiClient.kt`
- `app/src/main/java/com/isardomains/sameview/net/deinwackelbild/OkHttpDeinWackelbildApiClient.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-de/strings.xml`

Inspect relevant UI precedents:
- `CreateVideoScreen.kt` — busy spinner + Back-confirmation
- `ShareComparisonScreen.kt` — action/footer/error layout
- existing AlertDialog patterns
- external-link/browser launch patterns
- accessibility test conventions.

Inspect relevant tests:
- `WackelbildScreenTest.kt`
- `WackelbildViewModelTest.kt`
- `WackelbildHandoffOrchestratorTest.kt`

Report:
- branch
- HEAD
- `git status --short`

Expected:
- Block 10 committed
- no Block-11 changes yet.

If materially different, STOP.

---

# 2. Reconfirm the exact Block-11 product contract

Extract the exact authoritative rules for:
- CTA wording
- transfer disclosure
- consent timing
- consent dialog text/buttons, if specified
- fallback warning timing/text/buttons
- busy-state wording
- whether phase text/percentage is forbidden
- retry visibility
- Back/cancel behavior
- success behavior
- Custom Tab launch behavior
- Custom Tab open failure behavior
- browser-return behavior
- date-toggle state on return
- Reference/Capture reset on return
- error copy
- accessibility.

If any visible copy is still explicitly unresolved in the spec, identify it as a blocker rather than inventing text.

---

# 3. Critical consent boundary

The governance rule requires no network activity from:
- app start
- screen open
- preview interaction
- date toggle
- tilt/swipe.

The first API request must occur only after explicit user action.

Determine and lock the exact intended sequence.

Candidate sequence to verify:
1. User opens Wackelbild screen.
2. User taps `Bestelle dein Wackelbild`.
3. Consent/disclosure dialog appears if required by the authoritative spec.
4. User chooses `Weiter zu DeinWackelbild`.
5. Only now does `viewModel.startOperation()` run.
6. Local render/preparation runs.
7. If renderer fallback was used:
   - state becomes `AwaitingFallbackConfirmation`
   - fallback warning dialog appears
   - Continue calls `confirmFallbackAndContinue()`
   - Cancel calls `cancelOperation()`
8. Network Create begins only after every required approval gate is satisfied.

Verify whether the persistent disclosure + CTA tap alone is the approved consent mechanism, or whether a separate initial dialog is mandatory.

Do not duplicate consent unnecessarily.

---

# 4. Separate disclosure, consent and fallback confirmation

Lock the role of each:

## Persistent disclosure
Plain screen text describing external transfer/order flow.

## Initial consent
If required:
- before `startOperation()`
- explicit Continue/Cancel
- no API call before Continue.

## Fallback confirmation
Only when `usedFallback=true`:
- after local preparation
- before network Create
- separate from privacy consent
- Continue → `confirmFallbackAndContinue()`
- Cancel → `cancelOperation()`.

No ambiguity may remain.

---

# 5. Operation state → UI mapping

Read the actual `WackelbildOperationState`.

Create an exact mapping table for:
- `Idle`
- `Preparing`
- `AwaitingFallbackConfirmation`
- `CreatingHandoff`
- `UploadingSlot(ONE)`
- `UploadingSlot(TWO)`
- `Ready(checkoutUrl, usedFallback)`
- `Failed(failure)`

For each state determine:
- CTA visible/enabled?
- spinner visible?
- loading copy?
- date toggle enabled?
- tilt/swipe enabled?
- Back intercepted?
- dialog?
- error?
- side effect?

Verify the current spec's rule that loading is one undifferentiated state:
- spinner
- one copy string
- no phase names
- no percentage.

Do not expose request stage, retry count, handoff ID or technical state names.

---

# 6. Busy-state definition

Define exactly which states count as busy for:
- CTA disabling
- Back interception
- date-toggle disabling
- preview interaction.

Explicitly decide behavior for `AwaitingFallbackConfirmation`.

Avoid overlapping dialogs.

---

# 7. CTA / retry / re-entry rules

Lock behavior for:
- Idle
- busy
- Failed
- Ready
- Custom Tab open failure.

Determine:
- whether spinner replaces CTA or accompanies it
- how a failed operation is retried
- whether retry resets to Idle first
- whether repeated taps are ignored.

Block 8 already rejects concurrent starts, but UI must still be deterministic.

---

# 8. Exactly-once Custom Tab launch

This is critical.

Determine the smallest configuration/recomposition-safe mechanism.

Requirements:
- `Ready` launches exactly once
- recomposition cannot relaunch
- rotation cannot relaunch an already-consumed Ready
- launch success prevents auto-reopen on browser return
- launch failure keeps the exact checkout URL available for retry-open
- retry-open does not rerun render/upload.

Inspect whether current ViewModel needs minimal APIs such as:
- consume Ready
- record launch success
- record launch failure
- retry open.

Do not hide this with fragile local `remember` state.

---

# 9. Custom Tab open failure / retry-open

Lock exact behavior if launcher returns false.

Determine:
- approved error copy
- action label
- same checkout URL reused
- no network restart
- no raw URL shown
- URL/token not persisted to SavedStateHandle/DataStore
- URL not logged or exposed in accessibility text.

If the plan defines `OpenFailedWithCheckoutUrl` or equivalent, align to it.

---

# 10. Browser return behavior

No app-link return and no order-status callback.

Lock exact expected state when user returns:
- no automatic re-launch
- no order-success assumption
- no polling
- preview resets to Reference if specified
- tilt recalibrates via existing lifecycle
- date toggle reset/retention exactly per spec.

Determine whether additional Block-11 ViewModel logic is necessary.

---

# 11. Date toggle during operation

The renderer freezes the current setting at operation start.

Determine exact UI rule while operation is active.

Avoid allowing UI to suggest the uploaded output can still change after render has begun.

Lock whether date toggle is disabled throughout busy/awaiting-confirmation states.

---

# 12. Tilt/swipe during operation

Use the spec to determine whether local preview switching remains enabled during transfer.

Do not change Block-3 threshold/hysteresis values.

Lock:
- sensor active/inactive
- swipe enabled/disabled
- accessibility image-switch action behavior.

---

# 13. Back/cancellation

Lock exact behavior:

- Idle Back → normal `onBack`
- initial consent dialog Back → dismiss dialog
- fallback dialog Back → exact rule from spec
- busy transfer Back → cancel-transfer confirmation
- "Keep waiting" → dismiss
- "Cancel transfer" → `cancelOperation()`
- Failed → normal Back unless spec says otherwise
- launch-failure state → exact rule.

Reuse `CreateVideoScreen` precedent only where it matches the Wackelbild contract.

---

# 14. Failure category → visible copy

Read:
- `WackelbildOperationFailureCategory`
- authoritative error-copy table.

Map exactly:
- `PREPARATION_FAILED`
- `NETWORK_UNAVAILABLE`
- `SERVER_TEMPORARY`
- `INTEGRATION_UNAVAILABLE`
- `HANDOFF_FAILED`
- `INVALID_LOCAL_OUTPUT`

Rules:
- no HTTP codes
- no raw server message
- no "handoff" terminology
- no retry count
- no secret/key detail.

Block 12 may later refine real-world behavior, but Block 11 must have a complete baseline mapping.

---

# 15. Localization table

Enumerate every new/changed string needed.

At minimum check:
- CTA
- transfer disclosure
- consent title/body/buttons
- loading copy
- fallback warning title/body/buttons
- cancel-transfer title/body/buttons
- failure strings
- retry-open action
- accessibility/supporting text.

Return:

| Key | EN | DE | New/Existing | Source |

If English wording is still explicitly unresolved in source-of-truth, mark Block 11A blocked rather than inventing it.

---

# 16. Layout / no-default-scroll regression check

Inspect current `WackelbildScreen.kt`.

Block 4 intentionally reduced preview size so normal phone portrait can show the important controls without scrolling.

Block 11 adds:
- disclosure
- CTA/state area
- error text.

Determine exact order:
1. preview
2. date toggle
3. interaction hint
4. disclosure
5. CTA/loading/error

Requirements:
- do not enlarge preview
- preserve date badge inside image
- normal portrait should keep core CTA visible without scrolling where practical
- scrolling remains a compact-height/landscape safety net, not the default experience.

If current layout cannot accommodate new content without regression, identify the smallest layout adjustment.

Do not redesign unrelated UI.

---

# 17. Consent timing vs local rendering

Determine whether `startOperation()` is invoked only after initial consent and therefore both local render + network follow that action.

Do not start expensive HQ preparation merely from screen open.

Do not pre-render just to discover fallback unless the source-of-truth explicitly requires that behavior.

---

# 18. Custom Tab test seam

Block 10 intentionally left the raw launcher untested.

Block 11 must provide deterministic caller-side testing.

Choose the smallest seam:
- launcher lambda passed to screen
or
- injectable launcher object.

Requirements:
- exact checkout URL asserted unchanged
- fake success/failure
- no real browser in automated tests
- retry-open uses same URL
- no upload restart.

Avoid a new generic interface if a lambda is sufficient.

---

# 19. ViewModel vs Screen ownership

Lock exact ownership.

ViewModel should own:
- operation state
- start/cancel/fallback confirm
- state surviving recomposition
- checkout retry-open state if needed
- consume/launch-result transitions if needed.

Screen should own:
- Android `Context`
- calling launcher
- Compose dialogs/BackHandler
- visible strings.

Do not put Android Custom Tabs/Context into ViewModel.

---

# 20. Double-action safety

Plan exact behavior for:
- double CTA tap
- double consent Continue
- recomposition while consent visible
- `startOperation()` while active
- double fallback Continue
- duplicate launcher side effect
- retry-open double tap.

No duplicate transfer or browser launch.

---

# 21. Accessibility

Inspect existing Wackelbild semantics.

Plan:
- CTA semantics
- indeterminate progress semantics
- loading announcement if needed
- dialog semantics
- error visibility
- retry-open semantics
- disabled date-toggle supporting text
- no raw checkout URL in descriptions.

Avoid announcing hidden per-stage changes if the UI intentionally collapses them.

---

# 22. Automated test scope

## ViewModel tests
Only add tests for new state APIs, e.g.:
- Ready consumption
- launch success
- launch failure retains URL
- retry-open state
- reset after browser return if ViewModel owns it.

Do not duplicate Block-8 retry tests.

## WackelbildScreen instrumentation
At minimum:

### CTA / consent
- CTA visible in Idle
- screen open does not start operation
- date toggle/preview interaction does not start operation
- consent Cancel → no start
- consent Continue → exactly one start

### Loading
- spinner + one approved copy
- no phase text
- no percentage
- CTA unavailable while busy
- Back-confirm dialog only in busy states

### Fallback
- dialog only for `AwaitingFallbackConfirmation`
- not on screen open
- Continue exactly once
- Cancel calls cancelOperation

### Custom Tab
- Ready passes exact URL to fake launcher
- launch once
- no duplicate on recomposition
- forced failure shows fallback UI
- retry-open reuses same URL
- retry-open does not restart operation

### Errors
- every operation failure category maps to approved copy
- no technical status/server text

### Return/reset
- no auto-relaunch
- Reference/reset behavior
- date-toggle behavior

### Layout
- normal portrait CTA visible without scrolling if contract requires
- compact-height/landscape scroll safety
- preview cap unchanged
- date badge bounds unchanged

### Accessibility
- CTA
- loading
- errors
- retry-open.

All automated tests must use fake API/launcher behavior. No real partner network traffic.

---

# 23. First real pilot manual test plan

Define exact safe steps for after implementation; do not execute now.

Minimum sequence:
1. Use Debug build only.
2. Developer-local pilot key present; never print/inspect it.
3. Use a deliberately chosen non-sensitive test session.
4. Open Wackelbild screen.
5. Confirm Reference preview.
6. Optionally enable date.
7. Tap CTA.
8. Complete required consent.
9. Observe one loading state.
10. If fallback occurs, explicitly confirm before network continues.
11. Wait for transfer.
12. Real Custom Tab opens exact API-returned URL.
13. Verify partner page preloads:
    - Reference as slot one
    - Capture as slot two
    - matching dimensions/orientation.
14. Do not place a paid order unless explicitly intended.
15. Return to SameView.
16. Confirm no auto-relaunch/polling.
17. Confirm preview/reset behavior.
18. Confirm transfer temp files cleaned.
19. Confirm no sensitive log output.

Do not perform destructive/forced error testing in Block 11; Block 12 owns that.

---

# 24. Release safety for the pilot

Before first live request confirm:
- Debug build
- pilot/test key only
- key never printed
- intentional test images
- no analytics/logging
- Privacy/Data Safety work still pending for public release.

Pilot-test readiness is not Play-release readiness.

---

# 25. Exact file scope

After analysis, list every file required.

Likely candidates:
- `WackelbildScreen.kt`
- `WackelbildViewModel.kt`
- `WackelbildScreenTest.kt`
- `WackelbildViewModelTest.kt` only if new APIs are needed
- `values/strings.xml`
- `values-de/strings.xml`
- `docs/IMPLEMENTATION_NOTES.md`
- `DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` only if current plan must be corrected for finalized UI behavior/copy.

Normally do not change:
- `WackelbildCustomTabLauncher.kt`
- HandoffOrchestrator
- API client
- renderer
- temp manager
- MainActivity
- manifest/Gradle.

Return exact paths, no vague groups.

---

# 26. Documentation impact

Block 11 should normally append a completion entry to:
- `docs/IMPLEMENTATION_NOTES.md`

Determine whether the plan must also be updated for any newly finalized detail:
- consent mechanism
- launch-failure state
- browser return/reset
- final EN wording.

Do not touch Block-14 release/privacy docs.

---

# 27. Verification plan after implementation

At minimum:

```bash
./gradlew testDebugUnitTest
./gradlew compileDebugAndroidTestKotlin
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew bundleRelease
git diff --check
git status --short
```

Run targeted/full Wackelbild instrumentation via attached device or configured Managed Device.

The full API29 suite has 5 known A/B-proven pre-existing failures. Do not treat those as Block-11 regressions unless Block 11 touches those areas or failure modes change.

All new/modified Wackelbild tests must be green.

Current plan also requires the full instrumentation flow for Block 11; confirm exact command.

No real API traffic in automated tests.

Then perform the controlled manual pilot test separately.

---

# 28. Required final output

Return exactly:

## 1. Repository Baseline

## 2. Source-of-Truth Verification

## 3. Consent Model

## 4. Disclosure vs Consent vs Fallback Warning

## 5. Operation State → UI Mapping

Table:
| State | CTA | Spinner | Dialog | Back | Preview/toggle | Side effect |

## 6. Busy-State Definition

## 7. CTA / Retry / Re-entry Rules

## 8. Custom Tab Exactly-Once Launch Architecture

## 9. Custom Tab Failure / Retry-Open Model

## 10. Browser Return Behavior

## 11. Date Toggle / Preview During Busy

## 12. Back / Cancellation Model

## 13. Failure-to-Copy Mapping

## 14. Localization Table

| Key | EN | DE | New/Existing | Source |

If copy is unresolved, mark blocked.

## 15. Layout Impact

## 16. Accessibility

## 17. Testability / Fake Launcher Seam

## 18. Files Proposed for Modification / Creation

| File | Modify/Create | Exact change | Why |

## 19. Files Explicitly Not Touched

## 20. Tests to Add / Update

## 21. Documentation Impact

## 22. Automated Verification Plan

## 23. First Real Pilot Manual Test Plan

## 24. Release / Privacy Status

## 25. Risks / Blockers

## 26. Remaining Open Decisions

If none:
`None`

## 27. Gate Result

If everything is locked:
**BLOCK 11A SCOPE READY — WAITING FOR EXPLICIT APPROVAL**

If visible copy, consent semantics or launch-state behavior remains unresolved:
**BLOCK 11A BLOCKED — USER DECISION REQUIRED**

Then STOP.

---

# Final constraints

Analysis only.

Block 11 is the first user-reachable live-network flow.

No implementation until:
- consent semantics are exact
- fallback warning is separate and correct
- Custom Tab cannot launch twice
- launch failure is recoverable without re-upload
- visible EN/DE copy is locked
- fake-client/launcher automated tests are defined
- the real pilot test is bounded and deliberate.

Do not make a real request in this gate.
