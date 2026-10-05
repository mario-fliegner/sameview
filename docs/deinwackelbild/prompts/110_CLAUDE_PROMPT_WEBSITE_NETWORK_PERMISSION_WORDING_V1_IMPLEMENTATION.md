# Claude Prompt — SameView Website / Network Permission Wording V1 — STEP 3 IMPLEMENTATION

## Repository
`C:\data\work\privat\git-repos\sameview-website`

## Approved scope
STEP 1 analysis and STEP 2 scope are approved.

Implement exactly ONE fix: correct the Privacy Policy treatment of the transitive `ACCESS_NETWORK_STATE` permission.

Modify only:
1. `src/pages/en/privacy/_privacy.md`
2. `src/pages/de/privacy/_privacy.md`

Do not modify any other file.

## Baseline safety
Before editing:
- verify branch/HEAD and `git status --short`;
- expected baseline is `main` at `07b81ca` (or the same committed/tagged website state if the full hash is shown), with a clean working tree;
- if there are unexpected changes, STOP.

Do not modify the Android or release repositories.

## Authoritative evidence
The Android merged release manifest contains:
`android.permission.ACCESS_NETWORK_STATE`

The manifest-merger report attributes it to AndroidX Media3:
- added from `androidx.media3:media3-common:1.5.1`
- merged from `androidx.media3:media3-exoplayer:1.5.1`

It is absent from SameView's source manifests.

A code search found no SameView-owned use of `ConnectivityManager`, `NetworkCapabilities`, `getActiveNetwork`, `NetworkInfo`, `NetworkCallback`, `ACCESS_NETWORK_STATE`, or `ACCESS_WIFI_STATE`.

Do not infer any additional purpose beyond this evidence.

## Exact implementation

In both Privacy files, §3 permissions:

### 1. Remove the inaccurate bullet
Remove only:

EN:
`- Any other network-related permission`

DE:
`- Weitere netzwerkbezogene Berechtigungen`

from the existing “Permissions not used” list.

Do not otherwise rewrite that list.

### 2. Add ACCESS_NETWORK_STATE entry
Directly after the existing `INTERNET` permission entry and before “Permissions not used”, add an entry matching the existing Markdown/heading/style conventions.

EN heading:
`### ACCESS_NETWORK_STATE`

EN meaning:
- the permission is not requested by SameView's own code;
- it is added automatically to the app package by AndroidX Media3, which SameView uses for video preview;
- it allows an app to read whether a network connection is available;
- the permission by itself does not allow data to be sent or received;
- SameView's own code does not use this permission.

Use concise natural wording consistent with the existing Privacy Policy. Do not add claims beyond those facts.

DE heading:
`### Netzwerkstatus (ACCESS_NETWORK_STATE)`

DE meaning:
- die Berechtigung wird nicht vom eigenen Code von *SameView* angefordert;
- sie wird durch AndroidX Media3, das *SameView* für die Videovorschau verwendet, automatisch zum App-Paket hinzugefügt;
- sie erlaubt einer App festzustellen, ob eine Netzwerkverbindung verfügbar ist;
- sie erlaubt für sich genommen nicht das Senden oder Empfangen von Daten;
- der eigene Code von *SameView* verwendet diese Berechtigung nicht.

Use the document's existing informal/style conventions.

## Preserve everything else
Do not change:
- Privacy date;
- `INTERNET` entry;
- any other Privacy section;
- DeinWackelbild disclosure;
- Legal bases / Rechtsgrundlagen;
- Terms;
- homepage;
- README;
- PRODUCT_SUMMARY;
- PROJECT_INSTRUCTION;
- Imprint;
- cookie pages;
- wrappers/layouts/tests;
- Android;
- `sameview-release`.

No unrelated cleanup, reformatting, heading renumbering, wording improvements, or link changes.

## Verification
After editing, run only:
1. `git status --short`
2. `git diff --stat`
3. focused `git diff` for the two Privacy files
4. grep confirming:
   - `Any other network-related permission` is gone;
   - `Weitere netzwerkbezogene Berechtigungen` is gone;
   - `ACCESS_NETWORK_STATE` appears in the intended permission entry in both languages
5. DE/EN heading/parity check
6. local Astro build, using the repository's available command. If `pnpm` is unavailable, using the local Astro binary is acceptable.

Do not run Playwright, Android tests, or unrelated suites.

## Final report
Report:
1. baseline;
2. exact two modified files;
3. exact implemented wording/meaning;
4. confirmation no other Privacy sections changed;
5. confirmation all protected files/repositories stayed untouched;
6. grep/parity results;
7. build result;
8. tests/checks run and not run;
9. final `git status --short`;
10. confirmation nothing was staged, committed or pushed.

Then STOP.
