# Claude Prompt — CompareScreen Empty Locale Fix — STEP 3 Implementation

## Approval

**Approved for STEP 3 implementation.**

Implement exactly the minimal one-file fix established by the completed root-cause analysis.

Follow `CLAUDE_PROJECT_INSTRUCTION.md` strictly and re-check the relevant Source-of-Truth documents before editing. If you discover a specification conflict, STOP and report it instead of changing scope.

## Exact approved file

Modify exactly:

- `app/src/main/java/com/isardomains/sameview/ui/compare/CompareScreen.kt`

Do not modify any other file.

## Proven root cause

`CompareScreen.kt` obtains the current locale from `LocalConfiguration.current.locales.get(0)` and passes it to `CountryCatalog.resolveDisplayName(...)`, whose locale parameter is non-null.

A bare Android `Configuration()` can contain an empty `LocaleList`. In that case, `locales.get(0)` returns null and the non-null Kotlin call produces the observed `NullPointerException`.

This is the root cause of the four failing landscape `CompareScreenTest` cases.

## Required implementation

Make the locale lookup at the affected `CompareScreen.kt` location minimally null-safe.

Requirements:

- Preserve the current locale when `LocalConfiguration.current.locales` contains one.
- If the locale list is empty / index 0 resolves to null, use the smallest sensible existing/default locale fallback available to this code.
- Ensure `CountryCatalog.resolveDisplayName(...)` always receives a non-null `Locale`.
- Do not alter `CountryCatalog`.
- Do not change the UI structure.
- Do not change country-display behavior for normal non-empty locale configurations.
- Do not modify the four failing instrumentation tests to hide the production edge case.
- Do not modify `CompareScreenTest.kt`.
- Do not modify `EditSessionScreenTest.kt`.
- Do not modify any Wackelbild file.
- Do not refactor or clean up unrelated code.
- Do not rename unrelated symbols.
- Do not reformat unrelated code.
- Do not modify documentation in this iteration.

## Regression safety

This is a surgical null-safety fix only.

The existing behavior for a normal configuration with a locale must remain unchanged.

Do not make any broader localization, configuration, Compose, lifecycle, navigation, or test-harness changes.

If the fix unexpectedly requires a second file, STOP before modifying it and report why scope expansion would be necessary.

## Required verification

After the one-file implementation, run all three required verification commands:

```text
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew assembleDebug
```

`connectedDebugAndroidTest` must be treated as passed only if the complete connected suite actually passes. Do not substitute `compileDebugAndroidTestKotlin` for it.

Do not suppress, disable, skip, rewrite, or hide failing tests.

If `connectedDebugAndroidTest` cannot be executed because no device/emulator is available in your environment, report that explicitly. Do not claim it passed.

If the four Compare failures disappear but any of the nine EditSession failures remain, report those exact failures rather than modifying them in this iteration.

## Required final report

Return:

1. Exact modified file.
2. Exact minimal change made.
3. Confirmation that no other file was modified.
4. Result of `./gradlew testDebugUnitTest`.
5. Result of `./gradlew connectedDebugAndroidTest`.
6. Result of `./gradlew assembleDebug`.
7. Exact remaining failures, if any.
8. Whether real-device validation or any separate follow-up is still required.
9. Any deviation from the approved one-file scope.

Do not make any further fixes beyond the approved locale null-safety change.

STOP after implementation and verification.
