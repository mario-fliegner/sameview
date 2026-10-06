# RELEASE_HARDENING_AUDIT_RELEASE_2.md

## SameView — Release-Hardening-Audit für Release 2

**Art:** Read-only-Analyse. Einzige im Repository angelegte/geänderte Datei: dieses Dokument.
**Analysedatum:** 2026-10-06
**Vorgänger (historisch, unverändert):** `RELEASE_HARDENING_AUDIT_V1.md` (2026-05-29), `RELEASE_HARDENING_AUDIT_V2.md` (2026-07-08, Nachtrag 2026-09-21)

**Verdict:** **GO AFTER FIXES** — 1 BLOCKER, 3 HIGH (davon 2 Prozess/Extern). Kein Crash- oder Datenschutz-Showstopper im Code gefunden. Details in Abschnitt 13.

---

## Inhalt

1. Analysegrundlage und geprüfte Baseline
2. Executive Summary / Release-Verdict
3. Wesentliche Änderungen seit Release 1
4. Findings-Gesamttabelle
5. Findings nach Themen (mit Belegen)
6. DeinWackelbild-Integration — Prüfung gegen die aktuelle Spec
7. Status der Findings aus V1/V2
8. Positive, konkret geprüfte Befunde
9. Test-, Lint- und Build-Ergebnisse
10. Notwendige Real-Device-/Pilot-Prüfungen
11. Muss vor Release 2 behoben werden
12. Bewusst nach Release 2 verschiebbar
13. Abschluss: GO / GO AFTER FIXES / NO-GO

---

## 01 — Analysegrundlage und geprüfte Baseline

### 1.1 Release-1-Baseline

| Merkmal | Befund | Beleg |
|---|---|---|
| Veröffentlichter Release 1 | versionCode **100**, versionName **"1.0"** | `sameview-release/04_PlayConsole/ReleaseNotes_Internal_*.txt`; `05_Releases/2026-07-24_PlayStoreSubmission.docx` |
| Go-Live | 2026-08-26 (Managed Publishing, Review ab 2026-08-21, Release „Add from library“) | Submission-Dokument |
| Git-Stand von Release 1 | Tag `v1.0.100-rc` → Commit `a106450` („Track About screen website links with UTM parameters“, 2026-07-23 08:11) | `git tag`, `git show v1.0.100-rc` |
| Archiviertes Upload-Artefakt | `sameview-release/05_Releases/v1.0.0/app-release.aab`, erstellt 2026-07-23 08:24 (13 min nach Tag-Commit) | Dateizeitstempel |
| Inhaltlicher Abgleich | Das archivierte AAB enthält die UTM-Parameter (`utm_content=about_privacy`, `about_website` …), die erst mit `a106450` eingeführt wurden; Manifest ohne INTERNET | `resources.pb`/Manifest des AAB |
| Erster Commit nach R1 | `573a6e9` (2026-08-19) | `git log` |

**Bewertung:** Baseline = `v1.0.100-rc` / `a106450` mit **hoher, aber nicht byte-verifizierter Sicherheit**.

Offene Unklarheiten (dokumentiert, nicht geraten):

- Der Tag heißt „-rc“, ein separater Release-Tag existiert nicht.
- `05_Releases/v1.0.0/Checksums.txt` (2026-07-21) passt **nicht** zum archivierten AAB (SHA-256 abweichend). Die Prüfsumme stammt offenbar von einem früheren Build. Ein reproduzierbarer Byte-Vergleich (Rebuild des Tags) wurde nicht durchgeführt.
- Ob das in der Play Console verwendete Bundle („Add from library“) byte-identisch mit dem archivierten AAB ist, lässt sich aus dem Repository nicht belegen.

Für dieses Audit ist die Unklarheit unkritisch: Zwischen `a106450` und dem ersten Folge-Commit liegen keine weiteren Code-Commits.

### 1.2 Aktueller Stand

| Merkmal | Wert |
|---|---|
| HEAD | `210b980` („Updated prompt docs“), Branch `main`, synchron mit `origin/main`, Working Tree sauber bei Auditbeginn |
| Commits seit R1 | 30 |
| versionCode / versionName | **100 / "1.0" (unverändert gegenüber R1)** |
| minSdk / targetSdk / compileSdk | 29 / **36** / **36** (R1: 29 / 35 / 35) |
| AGP / Gradle / Kotlin | AGP 9.x, Gradle 9.3.1, Kotlin 2.2.x |
| Neue Dependencies | `com.squareup.okhttp3:okhttp:4.12.0`, `androidx.browser:browser:1.10.0` |
| Neue Permission | `android.permission.INTERNET` (+ transitiv `ACCESS_NETWORK_STATE` aus Media3, bereits in R1 vorhanden) |

### 1.3 Gelesene Source-of-Truth-Dokumente

`CLAUDE_PROJECT_INSTRUCTION.md` (inkl. Addenda Hosted Comparison 2026-08-19 und DeinWackelbild 2026-08-28), `IMPLEMENTATION_NOTES.md`, `deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md` (vollständig), `deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md` (§11–§32 vollständig, übrige Abschnitte gezielt), `RELEASE_HARDENING_AUDIT_V1.md` und `_V2.md` (vollständig), relevante Teile von `SESSION_METADATA_V1.md`, `SESSION_METADATA_EDITOR_V1.md`, `GUIDE_TIPS_UX_V1.md`, `FIRST_RUN_WALKTHROUGH_GUIDE_V1.md`, `SHARE_COMPARISON_IMAGE_V1.md`.

Zusätzlich (nur lesend, außerhalb dieses Repos): `sameview-release` (Play-Console-Referenzen, Store-Texte, Release-Archiv), `sameview-website` (Privacy/Terms im Repo-Stand), GitHub-Issues #1–#7.

### 1.4 Geprüfter Code

Kotlin/Compose (Schwerpunkt alle seit R1 geänderten Dateien sowie `net/deinwackelbild/*`, `ui/wackelbild/*`, `image/wackelbild/*`), `AndroidManifest.xml` (main + debug), gemergtes Release-Manifest, `app/build.gradle.kts`, `libs.versions.toml`, `proguard-rules.pro`, `backup_rules.xml`, `data_extraction_rules.xml`, `values/strings.xml`, `values-de/strings.xml`, erzeugte `BuildConfig` (debug/release), Release-AAB/-APK.

---

## 02 — Executive Summary / Release-Verdict

Release 2 bringt drei inhaltliche Blöcke: Android 16 / API 36, Country-Auswahl plus Referenzdatum-Validierung (Issues #2/#3) und — mit Abstand am größten und riskantesten — die **DeinWackelbild-Integration**, die erste Netzwerkfunktion der App.

Die DeinWackelbild-Integration ist in den sicherheits- und datenschutzrelevanten Punkten **sauber umgesetzt**:

- HTTPS-only mit URL-Validierung, keine Redirects, kein Logging
- Partner-Key nur im Create-Header
- keine Session-IDs auf dem Draht
- metadatenfreie Transfer-JPEGs
- Temp-Dateien nur in `cacheDir` mit Cleanup auf allen Pfaden
- kein Netzwerk vor dem CTA

Session-Dateien bleiben nachweislich unverändert.

Die Release-Risiken liegen vor allem im **Release-Prozess** und in zwei **realen Fehlerpfaden**:

1. **[BLOCKER] versionCode ist weiterhin 100** — identisch mit dem veröffentlichten R1. Play lehnt den Upload ab (R2-B01).
2. **[HIGH] Kein Schutz gegen einen Release-Build ohne Partner-Key.** Ein lokal gebautes `bundleRelease` enthält nachweislich einen **leeren** Key. Der Menüpunkt bleibt sichtbar, jede Bestellung scheitert dann mit „DeinWackelbild.de ist derzeit nicht verfügbar“ (R2-H01).
3. **[HIGH] Play Data Safety / Play Console sind für die neue Datenübertragung noch nicht aktualisiert** (offene Punkte laut `DataSafety.txt`). Der Live-Stand von Website-Privacy/Terms ist unverifiziert (R2-H02).
4. **[HIGH] Real-Device-/Pilot-Abnahme der Integration (Plan-Blöcke 12/14/15) ist nicht als abgeschlossen dokumentiert.** Ein Release-Build-Durchlauf (R8, signiert, Produktions-Key) gegen den echten Endpunkt fehlt als Nachweis (R2-H03).
5. **[MEDIUM]** Abbruch während der lokalen Vorbereitung zeigt fälschlich die Fehlermeldung „Wackelbild kann nicht erstellt werden“, weil die Renderer eine `CancellationException` verschlucken (R2-M01).
6. **[MEDIUM]** Die HTTP-Response wird auf dem Main-Thread gelesen (latentes `NetworkOnMainThreadException`/ANR-Risiko mit Fehlklassifikation) (R2-M02).
7. **[MEDIUM]** Der Play-Store-Text verspricht „Export **und Import** von Backups“. Einen Import gibt es nicht (vorbestehend seit R1) (R2-M03).

Altlasten aus V1/V2 sind überwiegend geschlossen oder bewusst verworfen. Offen und weiterhin verschiebbar sind Accessibility-Lücken (Slider/Overlay, Guide-Tip-Live-Region) und der fehlende Beschreibungstext beim GPS-Toggle.

Verifikation:

- Unit-Tests **1231/1231 grün**
- Lint **0 Errors**
- Debug-, Release-APK-, Release-AAB- und AndroidTest-APK-Build erfolgreich
- Managed-Device-Instrumentation: API 29 **1111/1111 grün**; API 36 **1110/1111** (ein Timing-Flake in `CompareScreenTest`, isolierte Wiederholung der Klasse 113/113 grün, siehe 9.1)

---

## 03 — Wesentliche Änderungen seit Release 1 (`a106450..210b980`)

| Bereich | Änderung | Commits (Auswahl) | Risiko-Einordnung |
|---|---|---|---|
| Plattform | compileSdk/targetSdk 35 → 36, Managed Device `pixel2Api36`, Ersatz von `announceForAccessibility()` durch `LiveRegionMode.Polite` | `917c66a`, `d59100e`, `73753f5`, `d077045` | Mittel — laut `IMPLEMENTATION_NOTES.md` real-device-verifiziert (Predictive Back, Insets, TalkBack) |
| Metadaten | Additives Feld `location.countryCode` (Schema bleibt v6), Country-Picker, lokalisierte Anzeige über `CountryCatalog` in Compare/Share/Video | `e24930d`, `00ba901` | Niedrig — rückwärtskompatibel |
| Edit Session | Validierung: Referenzdatum nicht nach Aufnahmedatum (präzisionsbewusst; Legacy-Werte bleiben unangetastet) | `2572f89` | Niedrig |
| Compare | Export-Menü: Divider + „Wackelbild erstellen“ | `46b5662` | Niedrig |
| DeinWackelbild | Neuer Screen mit Tilt/Swipe-Preview, Ridges, Perspektive, Datums-Badge, Print-Format-Crop; HQ/Fallback-Print-Renderer; Temp-Datei-Lifecycle; OkHttp-API-Client; Orchestrierung mit Retry/Restart; Partner-Key-Injection; INTERNET-Permission; Custom Tab | `648cbd0` … `b2ed20b` | **Hoch** — erste Netzwerkfunktion, Secret im Build, neuer Datenabfluss |
| Build | `buildConfigField DEINWACKELBILD_PARTNER_KEY` (debug: local.properties → env; release: nur env) | `2cb743c` | **Hoch** für den Release-Prozess |
| Docs | Netzwerk-Ausnahme-Addenda, Specs, Prompts | diverse | — |

---

## 04 — Findings-Gesamttabelle

Herkunft:

- **NEU** = neu seit R1
- **VORB.** = bestand bereits in R1
- **WIEDER** = wieder aufgetretenes altes Finding
- **SPEC** = Spec-/Dokumentationsabweichung

| ID | Severity | Bereich | Kurzbeschreibung | Herkunft |
|---|---|---|---|---|
| R2-B01 | **BLOCKER** | Release-Konfiguration | versionCode = 100 (= veröffentlichter R1), versionName = "1.0" | WIEDER (V1 R-04) |
| R2-H01 | **HIGH** | Secrets / Release | Release-Build ohne `DEINWACKELBILD_PARTNER_KEY`-Env-Var baut erfolgreich mit leerem Key → Feature live, aber jede Bestellung scheitert | NEU |
| R2-H02 | **HIGH** | Play Store / Privacy (extern) | Data Safety in der Play Console noch nicht für die Foto-Übertragung aktualisiert; Live-Stand von Privacy/Terms unverifiziert | NEU (Nachfolger V1 PS-02) |
| R2-H03 | **HIGH** | Verifikation | Real-Device-/Pilot-Abnahme (Plan §25/§26, Blöcke 12/14/15) nicht dokumentiert abgeschlossen; kein Nachweis eines R8-Release-Builds gegen den echten Endpunkt | NEU |
| R2-M01 | MEDIUM | DeinWackelbild / Lifecycle | Abbruch während der Vorbereitung → verschluckte `CancellationException` → Fehlermeldung statt stillem Abbruch; Race mit Folgeoperation | NEU |
| R2-M02 | MEDIUM | DeinWackelbild / Threading | Response-Body-Lesen und Datei-IO auf dem Main-Thread | NEU |
| R2-M03 | MEDIUM | Play Store Listing | Store-Text verspricht Backup-**Import**, der nicht existiert | VORB. |
| R2-L01 | LOW | DeinWackelbild / Spec | „Abbrechen“ im Übertragungs-Dialog bleibt auf dem Wackelbild-Screen statt zur CompareScreen zurückzukehren (Spec §13) | SPEC |
| R2-L02 | LOW | DeinWackelbild / Spec | `locale` wird nie gesendet; Locale-Mapping (Spec §29, Plan §17.2) nicht implementiert und nicht als Abweichung dokumentiert | SPEC |
| R2-L03 | LOW | DeinWackelbild / Netzwerk | `callTimeout` 90 s pro Upload-Request (bis 20 MiB) → schwache Uplinks werden als „Keine Internetverbindung“ gemeldet, nach bis zu ~4,6 min Spinner | NEU |
| R2-L04 | LOW | DeinWackelbild / Robustheit | Rückgabewert von `Bitmap.compress()` ungeprüft | NEU |
| R2-I01 | INFO | Build-Hygiene | `.kotlin/` (Kotlin-2.x-Build-Verzeichnis) nicht in `.gitignore` | NEU |
| R2-I02 | INFO | Release-Archiv | `Checksums.txt` von R1 passt nicht zum archivierten AAB | VORB. |
| R2-I03 | INFO | Release-Doku | Interne R1-Release-Notes behaupten „keine Internetberechtigung“; R2-Release-Notes fehlen noch | NEU |
| R2-I04 | INFO | DeinWackelbild | `OkHttpClient` wird pro ViewModel-Instanz neu erzeugt; abgebrochene Responses werden ggf. nicht geschlossen | NEU |
| R2-I05 | INFO | DeinWackelbild | Theoretisches Race: Orphan-Sweep im `init` vs. sehr schneller CTA-Tap | NEU |
| R2-I06 | INFO | Lint | `Untranslatable`: `about_privacy_policy_url` ist `translatable="false"`, aber in `values-de` überschrieben (funktional korrekt, im Release-AAB beide URLs vorhanden) | VORB. |

Anzahl nach Severity: 1 BLOCKER, 3 HIGH, 3 MEDIUM, 4 LOW, 6 INFO.

---

## 05 — Findings nach Themen

### 5.1 Release-Konfiguration

#### R2-B01 — versionCode/versionName nicht erhöht · BLOCKER · WIEDER (V1 R-04)

- **Datei:** `app/build.gradle.kts`, `defaultConfig { versionCode = 100; versionName = "1.0" }`
- **Befund:** Werte sind seit R1 unverändert (`git diff v1.0.100-rc HEAD -- app/build.gradle.kts` enthält keine Versionsänderung). Das gemergte Release-Manifest des lokal gebauten AAB trägt `versionCode="100" versionName="1.0"`.
- **Failure Path:** Upload des R2-AAB in die Play Console → Ablehnung, weil versionCode 100 bereits verwendet wurde. Selbst bei einem anderen Track ist kein Rollout möglich. Nutzer würden außerdem in About/Systeminfo weiterhin „1.0“ sehen.
- **Spec-Bezug:** V1 R-04 („versionCode muss für jeden Upload inkrementiert werden“). `ReleaseChecklist.txt` → „Version Code finalized“.
- **Release-Auswirkung:** Kein Release möglich.
- **Fix-Richtung:** versionCode > 100 (gemäß bestehendem Schema, z. B. 2xx) und versionName passend zu Release 2 setzen; danach Release-Build erneut verifizieren.

### 5.2 Secrets / Partner-Key

#### R2-H01 — Release-Build ohne Partner-Key wird nicht verhindert · HIGH · NEU

*Fix-Status: CLOSED (Build-Gate) — 2026-10-06, noch nicht committet.* Der Befund unten beschreibt den Auditstand vor dem Fix und bleibt unverändert stehen.

- **Umsetzung:** `app/build.gradle.kts` sammelt über `androidComponents.onVariants` die Artefakt-Tasks aller Varianten des Build Types `release` (`assemble…`, `bundle…`, `package…`, `package…Bundle`, `package…UniversalApk`, `sign…Bundle`, `install…`). `gradle.taskGraph.whenReady` bricht den Build mit einer `GradleException` ab, wenn einer dieser Tasks angefordert ist und `DEINWACKELBILD_PARTNER_KEY` in der Umgebung fehlt, leer ist oder nur aus Whitespace besteht. Die Prüfung läuft, bevor irgendein Task ausgeführt wird; es entsteht kein Artefakt.
- **Unverändert:** Release liest weiterhin ausschließlich die Env-Var und nie `local.properties`. Der Runtime-Guard `partnerKey.isBlank()` bleibt bestehen. Es gibt keine Format-/Längenprüfung und kein Bypass-Property. Kotlin-, Manifest-, Ressourcen- und Testdateien wurden nicht geändert.
- **Verifikation (alle Läufe ohne echten Key):**

  | Gegenprobe | Ergebnis |
  |---|---|
  | `./gradlew assembleDebug testDebugUnitTest lintDebug lintRelease assembleDebugAndroidTest --continue` ohne Env-Var | BUILD SUCCESSFUL; 1231 Unit-Tests, 0 Fehler; Lint 0 Errors (117 / 116 Warnings, unverändert) |
  | `./gradlew assembleRelease` ohne Env-Var | BUILD FAILED mit der Gate-Meldung, 0 Tasks ausgeführt |
  | `./gradlew bundleRelease` ohne Env-Var | BUILD FAILED mit der Gate-Meldung, 0 Tasks ausgeführt |
  | `./gradlew packageRelease` ohne Env-Var (direkter Aufruf des Packaging-Tasks) | BUILD FAILED |
  | `./gradlew assembleRelease bundleRelease` mit Whitespace-only-Wert | BUILD FAILED mit der Gate-Meldung, 0 Tasks ausgeführt |
  | `./gradlew assembleRelease bundleRelease` mit synthetischem Wert | BUILD SUCCESSFUL; Release-`BuildConfig` enthält genau den synthetischen Wert |
  | Release übernimmt den Key aus `local.properties` nicht | bestätigt: Release-`BuildConfig` ungleich lokalem Key; lokaler Key weder im DEX des APK noch im DEX des AAB |
  | Keine Offenlegung | Die Gate-Meldung nennt nur den Variablennamen; weder der lokale noch der synthetische Wert steht in einem der Build-Logs |

- **Hinweis zur ursprünglichen Auditprüfung:** Der DEX-Scan des AAB in Abschnitt 8, Punkt 1 lief im Audit mit einem Pfadmuster, das im AAB nichts traf (DEX liegt dort unter `base/dex/`). Die Aussage war damals nur über die Release-`BuildConfig` (Länge 0) und den APK-Scan gedeckt. Der AAB-Scan wurde jetzt mit korrektem Pfad nachgeholt, das Ergebnis ist unverändert.
- **Weiterhin offen (nicht durch den Gate gelöst):** Der Gate unterscheidet einen falschen, abgelaufenen oder synthetischen Key nicht vom Produktions-Key. Die Klärung Pilot- vs. Produktions-Key und eine echte Bestellung mit dem signierten Release-Build bleiben Teil von R2-H03. Wie die Release-Umgebung die Variable bereitstellt (z. B. systemweit für den Signier-Dialog von Android Studio), ist ein Prozessschritt außerhalb des Repos.
- **Folge für Verifikationsläufe:** `assembleRelease`, `bundleRelease` und die Aggregate `build`/`assemble` benötigen ab jetzt einen nicht leeren Wert. Die Kommandos 1 und 2 aus Abschnitt 9 schlagen ohne Env-Var deshalb künftig absichtlich fehl.

- **Datei/Funktion:** `app/build.gradle.kts` (`buildTypes.release.buildConfigField("String", "DEINWACKELBILD_PARTNER_KEY", env ?: "")`), `OkHttpDeinWackelbildApiClient.createHandoff()` (`partnerKey.isBlank()` → `INTEGRATION_UNAVAILABLE`), `CompareScreen` (Menüpunkt immer `enabled = true`).
- **Befund:** Die Release-Variante liest den Key ausschließlich aus der Umgebungsvariable. Fehlt sie, entsteht ohne Warnung ein leerer Key. Im Audit-Build (`bundleRelease` ohne Env-Var) wurde verifiziert:
  - `BuildConfig.DEINWACKELBILD_PARTNER_KEY` hat im Release die Länge 0, im Debug die Länge 56 (Key aus `local.properties`).
  - Der Build ist erfolgreich.

  Es gibt im Repo keine CI-Konfiguration und keinen dokumentierten, verifizierten Release-Schritt für die Env-Var. Plan §30 nennt dies selbst als offenen Punkt.
- **Failure Path:** Release-AAB wird lokal aus der IDE oder über ein Terminal ohne gesetzte Variable gebaut → Upload → alle Nutzer sehen „Wackelbild erstellen“ → CTA → sofort „DeinWackelbild.de ist derzeit nicht verfügbar. Bitte versuche es später erneut.“ Das Feature ist für alle Nutzer dauerhaft defekt, ohne dass es im Entwickler-Debug-Build auffällt (dort greift `local.properties`).
- **Spec-Bezug:** Integration §35/§42 („release build handling of the partner key“), Plan §15 („release builds read … only from an environment variable — the production key must be supplied externally“) und §30 (offen).
- **Release-Auswirkung:** Hohe Wahrscheinlichkeit eines unbemerkten Totalausfalls des Hauptfeatures von R2.
- **Fix-Richtung:** Den Release-Build bei leerem Key verhindern, z. B. durch einen Gradle-Check, der nur für die Release-Variante greift und Debug/Tests nicht betrifft. Alternativ oder zusätzlich: einen verbindlichen, dokumentierten Release-Schritt mit Artefaktprüfung („Key-Länge > 0“, ohne den Wert auszugeben) in die `ReleaseChecklist` aufnehmen. Klären, ob der Key in `local.properties` ein Pilot-/Test-Key ist und welcher Key in Produktion verwendet wird.

### 5.3 Play Store / Privacy / Compliance

#### R2-H02 — Data Safety / Play Console noch nicht für R2 aktualisiert · HIGH · NEU (extern)

- **Belege:**
  - `sameview-release/04_PlayConsole/DataSafety.txt` (Stand 2026-10) beschreibt den Soll-Zustand („Photos: Collected Yes, Shared No“), listet aber unter „OPEN ITEMS — VERIFY IN THE LIVE PLAY CONSOLE“ ausdrücklich offene Punkte. Dazu gehören „Update the form no later than the first rollout of that build on any track“, die Bewertung der 10-%-Provision im Kontext „Shared: No“ und die Löschfrage.
  - Das R1-Submission-Protokoll dokumentiert „No data types are selected … The app has no INTERNET permission“, also den Live-Stand für R1.
  - Die Website-Texte (`sameview-website`, Commits `07b81ca`, `619ae56`) beschreiben DeinWackelbild, die Provision (Terms §55) und die 24-h-Löschung. Ob dieser Stand **live deployed** ist, wurde nicht geprüft (kein Netzwerkzugriff im Audit).
- **Failure Path:** R2 wird mit INTERNET-Permission und Foto-Übertragung ausgerollt, während das Live-Formular weiterhin „keine Daten erhoben“ angibt. Das ist ein Verstoß gegen die User-Data-/Data-Safety-Policy und birgt das Risiko von Review-Ablehnung oder Enforcement.
- **Spec-Bezug:** `CLAUDE_PROJECT_INSTRUCTION.md` Addendum 2026-08-28 „Release/compliance consequence … These external compliance items are not yet complete“; Integration §42; Plan §27.
- **Fix-Richtung (extern):**
  - Play-Console-Formular gemäß `DataSafety.txt` ausfüllen und die offenen Punkte (Provision/„Shared“, Löschfrage, Custom-Tab-Eingaben) bewusst entscheiden.
  - Live-Privacy/-Terms (EN/DE) auf den Repo-Stand prüfen.
  - Die Privacy-URL im Listing bestätigen.

#### R2-M03 — Store-Text verspricht Backup-Import · MEDIUM · VORB.

- **Belege:**
  - `sameview-release/01_Store_Texts/FullDescription_EN.txt`: „• Export and import backups of your comparisons“
  - `FullDescription_DE.txt`: „• Backups deiner Vergleiche exportieren und wieder importieren“
  - Die App hat keinen Import: Repo-weit kein Import-/Restore-Code (bereits in V2 bestätigt). `ReleaseNotes_Internal_*.txt` nennt „Backup export is one-way“ als bekannte Einschränkung.
- **Failure Path:** Nutzer installieren in Erwartung einer Wiederherstellungsfunktion → negative Bewertungen; Angriffsfläche für eine Beanstandung wegen irreführender Store-Angaben (Play Misrepresentation Policy).
- **Release-Auswirkung:** Kein technischer Blocker, aber ein ohnehin zu überarbeitendes Listing für R2 (neues Feature) sollte nicht weiter Falsches versprechen.
- **Fix-Richtung:** Text auf „Export“ korrigieren. Bei der Gelegenheit prüfen, ob „Keine Cloud / ausschließlich lokal“ im Zusammenspiel mit der optionalen DeinWackelbild-Übertragung präzisiert werden sollte (Datenhaltung bleibt lokal, die Aussage ist aktuell nicht falsch).

#### R2-I03 — Release-Notes · INFO

`ReleaseNotes_Internal_DE/EN.txt` (R1) enthalten „SameView benötigt keine Internetberechtigung und stellt keinerlei Netzwerkverbindungen her“. Diese Aussage ist für R2 falsch. R2-Release-Notes (Store und intern) existieren noch nicht. Die R1-Texte dürfen beim Erstellen nicht wiederverwendet werden.

### 5.4 Verifikation / Abnahme

#### R2-H03 — Pilot-/Real-Device-Abnahme der Integration nicht dokumentiert · HIGH · NEU

- **Belege:**
  - `IMPLEMENTATION_NOTES.md` (Block 11, Handoff-Konfiguration 2026-09-21) listet als „Remaining“ unter anderem:
    - Real-Device-Pilot (Portrait/Landscape, Datum an/aus, Fallback-Pfad, EXIF/GPS-Stichprobe, Abbruch während Transfer, Temp-Cleanup, große Sessions)
    - Block 12 (Error-/Cancel-Polish gegen echtes API)
    - Block 14
    - visuelle Prüfung der Print-Crops (9:16 → `10x15`, 16:9, 3:4 → `15x20`) beim Partner
  - Plan §32 (Definition of Done) verlangt §25/§26 „complete, or explicitly and visibly deferred with owner and reason“.
  - Es gibt Hinweise auf Live-Läufe (Prompts 046/047/086), aber kein abschließendes Abnahmeprotokoll und keinen Nachweis eines **R8-minifizierten, signierten Release-Builds** gegen den Produktions-Endpunkt.
- **Failure Path:** Probleme, die nur im Release-Build oder nur am echten Endpunkt auftreten, bleiben bis zum Rollout unentdeckt. Beispiele: R8/Shrinking, Key-Provisionierung (R2-H01), HTTP/1.1-vs-HTTP/2-Verhalten (R2-M02), echte 409/410-Semantik, Partner-Crop/Inset, Fallback-Häufigkeit.
- **Fix-Richtung:** Die Liste in Abschnitt 10 auf mindestens einem realen Gerät mit dem signierten Release-Build abarbeiten und dokumentieren. Nicht durchgeführte Punkte explizit mit Begründung zurückstellen.

### 5.5 DeinWackelbild — Lifecycle und Fehlerpfade

#### R2-M01 — Abbruch während der Vorbereitung wird als Fehler angezeigt · MEDIUM · NEU

*Fix-Status: CLOSED — 2026-10-06, noch nicht committet.* Der Befund unten beschreibt den Auditstand vor dem Fix und bleibt unverändert stehen.

- **Umsetzung:** `WackelbildPrintRenderer.tryRenderHq()` und `renderFallback()` werfen eine `CancellationException` jetzt vor dem generischen `catch (_: Exception)` weiter. Ein Abbruch löst damit weder den Fallback aus noch wird er als `Failure(PERMANENT_NO_VALID_SOURCE)` zurückgegeben. Echte IO-, Decode- und OOM-Fehler verhalten sich unverändert.
- **Kein ViewModel-Guard:** Die unten genannte zweite Fix-Richtung (Ergebnis abgebrochener Jobs im ViewModel verwerfen) war nicht erforderlich. Weil der Renderer die Cancellation propagiert, erreicht eine abgebrochene Operation die Ergebniszuweisung in `startOperation()` nicht mehr. Damit entfällt auch die unter Schritt 6 beschriebene Überschreibung einer neu gestarteten Operation.
- **Neue Tests** in `WackelbildPrintRendererInstrumentedTest`: `cancelledJob_validHqSession_propagatesCancellation_noFallbackNoFailure` (HQ-Catch-Kette) und `cancelledJob_missingCaptureOriginal_propagatesCancellation_noFailure` (Fallback-Catch-Kette). Beide laufen in einem bereits abgebrochenen Job und sind damit deterministisch.
- **Verifikation:**

  | Lauf | Ergebnis |
  |---|---|
  | Gegenprobe ohne Fix: `WackelbildPrintRendererInstrumentedTest` auf `pixel2Api29` | 38 Tests, genau die 2 neuen fehlgeschlagen (`expected null, but was:<Failure(reason=PERMANENT_NO_VALID_SOURCE)>`), 36 bestanden |
  | `./gradlew testDebugUnitTest assembleDebug lintDebug --continue` | BUILD SUCCESSFUL; 1231 Unit-Tests, 0 Fehler; Lint 0 Errors, 117 Warnings (unverändert) |
  | `WackelbildPrintRendererInstrumentedTest` auf `pixel2Api36` | 38/38 bestanden |
  | `WackelbildPrintRendererInstrumentedTest` auf `pixel2Api29` | 38/38 bestanden (zweiter Anlauf, siehe unten) |

- **Hinweis zum ersten API-29-Lauf mit Fix:** Er brach ohne Testausführung ab (0 Tests). Der frisch gestartete Emulator meldete bei der Installation „API level=1“ (`Cannot install split APKs with API level < 21`). Die dabei geschriebene Ergebnisdatei ließ anschließend auch `mergeDebugAndroidTestTestResultProtos` im API-36-Lauf fehlschlagen, obwohl dort alle 38 Tests bestanden hatten. Das ist ein Emulator-/Infrastrukturproblem ohne Bezug zum Fix; die Wiederholung beider Läufe war erfolgreich.
- **Nicht Teil des Fixes:** R2-L01 (nach dem Abbruch bleibt der Nutzer auf dem Wackelbild-Screen). Der laufende Bitmap-Block ist weiterhin nicht unterbrechbar; der Abbruch wirkt erst, wenn dieser Block beendet ist.

- **Dateien/Funktionen:**
  - `image/wackelbild/WackelbildPrintRenderer.kt`: `tryRenderHq()` (`catch (_: Exception) { null }`) und `renderFallback()` (`catch (_: Exception) { Failure(PERMANENT_NO_VALID_SOURCE) }`)
  - `ui/wackelbild/WackelbildHandoffOrchestrator.execute()`
  - `ui/wackelbild/WackelbildViewModel.startOperation()` / `cancelOperation()`
- **Befund:** `kotlinx.coroutines.CancellationException` ist eine `Exception` (über `IllegalStateException`). Beide `catch`-Blöcke im Renderer fangen sie ab.
- **Failure Path (deterministisch nachvollziehbar):**
  1. Nutzer tippt „Bestelle dein Wackelbild“. Die Operation läuft in `renderBadgeEncodeRecycle` (`withContext(Dispatchers.Default)`, HQ-Decode/-Encode, typischerweise Sekunden).
  2. Back → „Übertragung abbrechen?“ → „Abbrechen“ → `cancelOperation()` cancelt den Job und setzt den State auf `Idle`.
  3. Bei Rückkehr aus `withContext` wird `CancellationException` geworfen. `tryRenderHq` fängt sie und liefert `null` → `renderFallback`. Dessen erstes `withContext` wirft erneut, die Exception wird gefangen → `Failure`.
  4. Der Orchestrator gibt regulär `Failed(PREPARATION_FAILED)` zurück. In `startOperation` folgt dann `_operationState.value = result` — der zuvor gesetzte `Idle`-State wird überschrieben.
  5. Weil „Abbrechen“ auf dem Screen bleibt (R2-L01), sieht der Nutzer unmittelbar nach dem eigenen Abbruch „Wackelbild kann nicht erstellt werden“.
  6. Variante: Tippt der Nutzer direkt nach dem Abbruch erneut auf den CTA (`operationJob.isActive` ist nach `cancel()` bereits `false`), läuft eine zweite Operation. Die noch auslaufende erste überschreibt deren State mit `Failed`. Das ist selbstheilend, aber verwirrend.
- **Spec-Bezug:** Integration §13 (Abbruch: „stop the active SameView operation“, kein Fehlerzustand), Plan §14.6 („Coroutine cancellation … Cancellation cleanup path, no error shown“), §18.
- **Test-Lücke:** `WackelbildViewModelTest` nutzt einen Fake-Renderer, der die Cancellation korrekt propagiert. Der reale Renderer ist für diesen Pfad nicht abgedeckt.
- **Release-Auswirkung:** Falsche, beunruhigende Fehlermeldung bei einer legitimen Nutzeraktion. Kein Datenverlust; Temp-Cleanup läuft weiterhin (`finally`/`NonCancellable`).
- **Fix-Richtung:** Cancellation im Renderer nicht als Render-Fehler behandeln, also `CancellationException` vor dem generischen `catch` weiterwerfen (analog zum V2-Fix in `ShareComparisonViewModel.onShare()`). Zusätzlich das Ergebnis eines bereits abgebrochenen Jobs im ViewModel nicht mehr in `operationState` schreiben.

#### R2-M02 — Netzwerk-Response und Datei-IO auf dem Main-Thread · MEDIUM · NEU

*Fix-Status: CLOSED für den Netzwerk-Teil — 2026-10-06, noch nicht committet. Einstufung nach Detailanalyse: PARTIALLY CONFIRMED.* Der Befund unten beschreibt den Auditstand vor dem Fix und bleibt unverändert stehen.

- **Präzisierung des Befunds (Analyse am Code und an den OkHttp-4.12.0-Quellen):**
  - **Bestätigt:** Der Response-Body wurde immer auf dem Main-Thread gelesen, geparst und geschlossen. `startOperation()` läuft auf `Dispatchers.Main.immediate`, `Call.await()` setzte dort fort, und `onResponse` liefert nur die Header; der Body wird erst beim Lesen gestreamt.
  - **Normalfall unkritisch:** `deinwackelbild.de` handelt per ALPN HTTP/2 aus. Dort macht der lesende Thread keine Socket-IO, sondern wartet auf den Reader-Thread. Für diesen Pfad wurde **kein reproduzierbarer ANR oder Crash festgestellt**; die Wartezeit liegt bei kleinen JSON-Antworten im Millisekundenbereich.
  - **Störfall A (Verbindung über HTTP/1.1, z. B. Netz ohne ALPN):** Body-Lesen auf Main kann den Socket berühren → `NetworkOnMainThreadException`, von `runCatching` geschluckt → bei 2xx `MALFORMED_RESPONSE` (nicht retrybar). Das anschließende `close()` liest erneut vom Socket (`discard()` fängt nur `IOException`) → unbehandelte Exception, also Absturz.
  - **Störfall B (HTTP/2, Body bleibt nach den Headern aus):** Main-Thread blockiert bis zum Read-Timeout (60 s); danach schreibt `close()` synchron ein `RST_STREAM` auf Main → ebenfalls unbehandelte Exception.
  - Beide Störfälle sind aus dem Code abgeleitet und **nicht auf einem Gerät reproduziert**.
- **Umsetzung:** Der unveränderte Rumpf von `OkHttpDeinWackelbildApiClient.executeAndParse()` läuft jetzt in `withContext(Dispatchers.IO)`. Fortsetzung nach `await()`, Body-Lesen, Parsing und Schließen finden damit außerhalb des Main-Threads statt. OkHttp-Konfiguration, Timeouts, `enqueue()`/`Call.cancel()`, Request-Aufbau, Key-Behandlung, `runCatching` und alle Fehlerklassen sind unverändert; Orchestrator und ViewModel wurden nicht geändert.
- **Tests** in `OkHttpDeinWackelbildApiClientTest`:
  - Neu: `responseBody_isConsumedAndClosedOffTheCallingThread` ruft den Client von einem Ein-Thread-Dispatcher (Main-Ersatz) auf und prüft, dass Body-Lesen und Schließen auf einem anderen Thread stattfinden.
  - Angepasst (nur Synchronisation, Assertions unverändert): `cancellation_propagatesAsCancellationException_andCancelsUnderlyingCall` wartet jetzt auf ein explizites Signal aus `enqueue()` statt auf `yield()`. Mit dem Start des Calls auf einem IO-Thread war die alte Annahme zeitabhängig: In 1 von 5 Läufen schlug der Test mit `UninitializedPropertyAccessException` fehl, weil der Abbruch vor dem Start des Requests ankam. Produktiv ist dieses Verhalten korrekt (kein Request, `CancellationException`).
- **Verifikation:**

  | Lauf | Ergebnis |
  |---|---|
  | Gegenprobe ohne Fix: `testDebugUnitTest --tests '*OkHttpDeinWackelbildApiClientTest'` | 35 Tests, genau der neue Test fehlgeschlagen, 34 bestanden |
  | Testklasse mit Fix, 20 Wiederholungen (`:app:testDebugUnitTest --rerun --tests '*OkHttpDeinWackelbildApiClientTest'`) | 20 × 35/35 bestanden |
  | `./gradlew testDebugUnitTest assembleDebug lintDebug --rerun-tasks --continue` | BUILD SUCCESSFUL; 1232 Unit-Tests, 0 Fehler; Lint 0 Errors, 117 Warnings (unverändert) |

- **Nicht Teil des Fixes:** die kurze lokale Datei-IO auf Main (`createOperationDir()`, `deleteOperationDir()`, Vorab-Lesen im Renderer). Sie ist nicht fatal, ein konkreter Fehlerpfad wurde nicht nachgewiesen; dieser Teil des Befunds bleibt unverändert bestehen. Ebenfalls unverändert: R2-I04 (ungeschlossene Response, wenn `onResponse` nach einem Abbruch eintrifft).
- **Weiterhin offen:** Der Nachweis am Gerät (StrictMode mit `detectNetwork` während einer echten Bestellung, Abschnitt 10, Punkt 7). Kein automatisierter Gerätetest erreicht den echten Client.

- **Dateien/Funktionen:** `WackelbildViewModel.startOperation()` (`viewModelScope.launch` → `Dispatchers.Main.immediate`), `OkHttpDeinWackelbildApiClient.executeAndParse()` (`resp.body?.string()` nach `await()`), `WackelbildHandoffOrchestrator.execute()` (`createOperationDir()`, `deleteOperationDir()`), `WackelbildPrintRenderer.tryRenderHq()`/`renderFallback()` (`readSessionViewport`, `readOverlayParams`, `readExifOrientedDimensions` vor dem ersten `withContext`).
- **Befund:**
  - `Call.await()` setzt die Coroutine im Aufrufer-Dispatcher fort, also auf **Main**. Dort wird `resp.body?.string()` synchron gelesen.
  - Bei HTTP/1.1 liegt ein kleiner JSON-Body meist schon im Okio-Puffer, das Lesen kann aber den Socket berühren → `NetworkOnMainThreadException`. Diese Exception wird von `runCatching` geschluckt → `bodyString = null` → bei 2xx `MALFORMED_RESPONSE` → `HANDOFF_FAILED` (nicht retrybar) → „Wackelbild kann nicht erstellt werden“.
  - Bei HTTP/2 blockiert der Main-Thread bis zum Eintreffen des Bodys (bis `readTimeout` 60 s) → ANR-Risiko bei langsamem Server.
  - Kleinere Datei-IO-Aufrufe laufen ebenfalls auf Main (StrictMode-Disk, keine Abstürze).
- **Evidenz-Status:** PLAUSIBEL, nicht reproduziert. Live-Läufe des Entwicklers waren offenbar erfolgreich (Body im Puffer). Kein automatisierter Test deckt den echten Client auf einem Gerät ab.
- **Spec-Bezug:** `CLAUDE_PROJECT_INSTRUCTION.md` (ERROR HANDLING „No silent broken state“), Integration §30 (korrekt klassifizierte Fehler).
- **Release-Auswirkung:** Sporadische, nicht reproduzierbare Bestellfehler oder ANRs unter realen Netzbedingungen.
- **Fix-Richtung:** Netzwerk-Operationen inklusive Body-Lesen und Parsing nicht auf Main ausführen, z. B. durch einen expliziten IO-Kontext im Client oder in der Orchestrierung. Datei-IO des Orchestrators ebenfalls auf IO legen.

#### R2-L01 — „Abbrechen“ bleibt auf dem Screen · LOW · SPEC

- **Datei:** `WackelbildScreen.kt` (Cancel-Dialog: `onCancelOperation()` ohne `onBack()`); Test `WackelbildScreenTest.cancelTransfer_invokesCancelOperation` prüft nur den Cancel-Aufruf.
- **Befund:** Spec §13 verlangt nach „Abbrechen“ die Rückkehr zur CompareScreen. Implementiert ist: Operation abbrechen und auf dem Screen bleiben (State `Idle`). Eine Product Decision zu dieser Abweichung ist in Block-11-Notes oder Spec nicht dokumentiert.
- **Release-Auswirkung:** Gering (UX), verstärkt aber die Sichtbarkeit von R2-M01.
- **Fix-Richtung:** Entweder Spec-konform zurücknavigieren oder die Abweichung als Product Decision in der Spec dokumentieren.

#### R2-L02 — Locale wird nicht übertragen · LOW · SPEC

- **Datei:** `WackelbildHandoffOrchestrator.buildCreateHandoffRequest()` setzt `locale` nie. `WackelbildLocaleMapper` (Plan §17.2) existiert nicht.
- **Befund:** Spec §29 fordert eine automatische Abbildung der App-Sprache auf eine DeinWackelbild-Locale mit Fallback `de-DE`. Es wird keine Locale gesendet. Der Partner nutzt seinen Default, vermutlich Deutsch, was faktisch dem Spec-Fallback entspricht. Die Locale-Matrix ist laut Plan §30 weiterhin offen. Die Abweichung ist nirgends als Entscheidung dokumentiert.
- **Release-Auswirkung:** Englischsprachige Nutzer landen nach englischer App-UI und englischem Disclosure in einem deutschen Shop. Das ist akzeptabel, solange es bewusst entschieden ist.
- **Fix-Richtung:** Locale-Matrix mit dem Partner klären und das Mapping umsetzen, oder „kein Locale-Feld in V1“ als bewusste Entscheidung in Spec/Plan dokumentieren.

#### R2-L03 — Timeout-Budget für große Uploads · LOW · NEU

- **Datei:** `OkHttpDeinWackelbildApiClient.createDefaultCallFactory()`: `connect/write/read` je 60 s, **`callTimeout` 90 s** für den gesamten Request (gilt auch für den Multipart-Upload bis 20 MiB).
- **Befund/Failure Path:**
  - Ein Transfer-JPEG von z. B. 8 MB braucht bei 0,7 Mbit/s Uplink etwa 90 s. Der Request bricht dann ab → `RETRYABLE_NETWORK` → zwei weitere identische Versuche (+1 s/+2 s).
  - Nach bis zu ~4,6 min Spinner erscheint „Keine Internetverbindung“, obwohl eine Verbindung besteht. „Erneut versuchen“ rendert neu und scheitert identisch.
  - Die Vertragsanforderung (≥ 60 s) ist formal erfüllt.
- **Release-Auswirkung:** Betrifft nur schwache Uplinks. Reale Dateigrößen wurden im Audit nicht gemessen.
- **Fix-Richtung:** Reale Transfer-Dateigrößen und Upload-Dauer auf gedrosseltem Netz messen (Abschnitt 10), danach entscheiden, ob `callTimeout` für Uploads entfallen bzw. größenabhängig gewählt werden soll.

#### R2-L04 — `Bitmap.compress()`-Ergebnis ungeprüft · LOW · NEU

- **Datei:** `WackelbildPrintRenderer.renderBadgeEncodeRecycle()`.
- **Befund:** Schlägt `compress` fehl (Rückgabe `false`, z. B. bei vollem Speicher in `cacheDir`), entsteht eine leere oder abgeschnittene Datei.
  - Mit Print-Target fängt `readPairDimensions` das ab → `PREPARATION_FAILED`.
  - Ohne Target (Sessions ohne vertrauenswürdige Geometrie) wird die Datei hochgeladen → 415 → „Wackelbild kann nicht erstellt werden“.
  - Es wird kein unnötiger HQ→Fallback-Wechsel ausgelöst.
- **Release-Auswirkung:** Gering, Randfall.
- **Fix-Richtung:** Fehlgeschlagenes Encoding als Render-Fehler behandeln (HQ → Fallback, Fallback → Failure), bevor ein Upload möglich ist.

#### R2-I04 / R2-I05 — Ressourcen und Rennen · INFO

- `WackelbildViewModel` erzeugt pro Screen-Besuch einen neuen `OkHttpClient` (eigener Connection-Pool/Dispatcher, nicht geschlossen). Die Threads laufen per Idle-Timeout aus; ein funktionales Problem besteht nicht.
- `Call.await()`: Trifft `onResponse` nach einer Cancellation ein, wird die `Response` nicht geschlossen (Connection-Leak bis GC).
- `sweepStaleOperationDirs()` startet im `init` parallel zur ersten möglichen Operation. Theoretisch könnte ein extrem schneller CTA-Tap ein frisch angelegtes Operationsverzeichnis wegräumen. Praktisch ist das ausgeschlossen, weil der CTA erst nach Auflösung des Print-Targets (Datei-IO) startet.

### 5.6 Build-Hygiene

#### R2-I01 — `.kotlin/` nicht ignoriert · INFO

Der Audit-Build hat im Projekt-Root `.kotlin/` (Kotlin-2.x-Compiler-/Daemon-Daten) erzeugt. Das Verzeichnis ist in `.gitignore` nicht enthalten und erscheint als untracked. Das birgt Risiko für versehentliche Commits. *(Das vom Audit erzeugte Verzeichnis wurde nach dem Audit wieder entfernt, siehe Abschnitt 9.)*

#### R2-I06 — Lint `Untranslatable` · INFO

`about_privacy_policy_url` ist `translatable="false"` und hat eine bewusste DE-Variante. Im Release-AAB sind beide URLs vorhanden (verifiziert), funktional also korrekt. Die Lint-Warnung bleibt bestehen.

---

## 06 — DeinWackelbild-Integration: Prüfung gegen die aktuelle Spec

Bewertungsmaßstab sind die **heutigen** Specs: `CLAUDE_PROJECT_INSTRUCTION.md` mit den Netzwerk-Addenda, Integration V1 und Plan V1. Die R1-Aussage „kein INTERNET“ ist durch das Addendum 2026-08-28 bewusst ersetzt und wird **nicht** als Finding gewertet.

| Spec-Anforderung | Umsetzung | Status |
|---|---|---|
| Kein Netzwerk beim Öffnen/Preview/Tilt/Swipe/Toggle (§3.2, §23, Addendum) | Netzwerk ausschließlich in `startOperation()` → Orchestrator; `init` liest nur lokale Dateien | ✅ |
| Kein Connectivity-Gate (§23, §38) | Kein `ConnectivityManager` im App-Code | ✅ |
| INTERNET nur für genehmigtes Feature (§23.1) | Einziger Netzwerkcode: `net/deinwackelbild/*`; repo-weit keine `HttpURLConnection`/`WebView`/weitere OkHttp-Nutzung | ✅ |
| HTTPS, Server-Input untrusted (Addendum, §50) | Base-URL `https://`; `upload_url`/`checkout_url` per `toHttpUrlOrNull()?.scheme == "https"` validiert; `followRedirects(false)`; Cleartext durch Plattform-Default (targetSdk ≥ 28) gesperrt, kein `usesCleartextTraffic`/NSC im gemergten Manifest | ✅ |
| Key nie in URL, nie geloggt, nicht im Repo (§35) | Nur Header `X-DWB-Partner-Key` am Create; Upload trägt nur `X-DWB-Handoff-Token`; keine Logging-Interceptor-Dependency, keine `Log`-Aufrufe in DWB-Code; Key-Wert in keinem Commit, keiner getrackten Datei und nicht in den Repos `sameview-release`/`sameview-website` (Suche per Wert, ohne Ausgabe) | ✅ |
| Release-Key-Handling (§42) | Release liest nur die Env-Var; lokaler Key gelangt nachweislich nicht ins Release-Artefakt; **leerer Key wird aber nicht verhindert** | ⚠️ R2-H01 |
| Datenminimierung (§21, §27) | Request: `partner`, optional `format`/`orientation`/`direction`; kein `external_reference`, keine Session-ID; Multipart-Dateinamen `image_one.jpg`/`image_two.jpg` | ✅ |
| Metadatenfreie Transfer-JPEGs (§21) | Ausgabe ausschließlich über `Bitmap.compress()`; kein `ExifInterface`-Schreibzugriff; Instrumented Test prüft GPS/DateTime/Make/Model/Serials/MakerNote = null | ✅ |
| Session-Dateien unverändert (§3.6, §40) | Nur lesende Zugriffe; Instrumented Test prüft SHA-256 vorher/nachher | ✅ |
| Temp-Dateien (§22) | `cacheDir/wackelbild/<uuid>/`; Löschung bei Erfolg (vor `Ready`), Fehler und Cancel (`finally` + `NonCancellable`); Sweep beim Screen-Eintritt; Containment-Check per Canonical Path; `cacheDir` ist nicht Teil von Auto Backup | ✅ |
| Kein persistenter Handoff-State (§25) | Alle Felder in-memory; keine DataStore/SavedState-Nutzung außer `sessionId` | ✅ |
| Idempotenz/Retry (§26, §37) | UUID pro Handoff-Generation, wiederverwendet über Create-Retries; 3 Versuche pro Stufe (1 s/2 s), max. 3 Generationen, max. 27 Requests (Test vorhanden); 403/409/410 → neue Generation | ✅ |
| Fehler-UX ohne Technikbegriffe (§30) | Mapping auf freigegebene Strings; `HANDOFF_FAILED` nutzt bewusst die Preparation-Copy (Product Decision, Block-11-Notes) | ✅ (Hinweis: Partner-Ausfälle mit 3xx/404 erscheinen dadurch als „kann nicht erstellt werden“, gedeckt durch die Product Decision) |
| Abbruch (§13) | Cancel des HTTP-Calls per `invokeOnCancellation { call.cancel() }` korrekt; **Vorbereitungsphase fehlerhaft**, keine Rückkehr zur CompareScreen | ⚠️ R2-M01, R2-L01 |
| Custom Tab (§12, §14, §31) | Exakte URL, Launch nur im Vordergrund (deferred bei Background), `ActivityNotFoundException` → Retry-Open ohne Re-Upload, Return-Reset nur nach echtem Launch | ✅ |
| Fallback-Warnung (§19) | Kein Netzwerk vor expliziter Bestätigung (Parameter ohne Default) | ✅ |
| Locale (§29) | Nicht implementiert | ⚠️ R2-L02 |
| Sensor-Lifecycle/Privacy (§8.8) | Stop bei `ON_PAUSE`/Dispose, keine Persistenz, kein Logging, keine Übertragung | ✅ |
| Kein Background-Upload (§24) | Kein WorkManager/Service; reine `viewModelScope`-Coroutine | ✅ |

---

## 07 — Status der Findings aus V1/V2 (heute neu geprüft)

### 7.1 Aus RELEASE_HARDENING_AUDIT_V1

| ID | Ursprünglich | Heutiger Status | Beleg / Begründung |
|---|---|---|---|
| M-01 | Camera required | **Behoben** (seit 2026-06-10) | Manifest `required="true"` |
| M-02 | Kein networkSecurityConfig | **Kein aktives Problem (neu bewertet)** | Trotz INTERNET: Cleartext ist bei targetSdk 36 per Plattform-Default gesperrt, alle URLs werden auf HTTPS validiert, Plan §27 entscheidet bewusst gegen eine NSC |
| M-03 | Unbenutztes `xmlns:tools` | Unverändert, kosmetisch | Kein Release-Risiko |
| M-06 | ACCESS_MEDIA_LOCATION erklärungsbedürftig | Unverändert, INFO | In `DataSafety.txt` begründet |
| P-01 / PS-01 | Privacy-Policy-Link | **Behoben** (R1) | `AboutScreen`, lokalisierte URLs im Release-AAB vorhanden |
| P-02 | Source-URI in metadata.json | Unverändert, akzeptiert | Nicht Teil des DeinWackelbild-Transfers (Request/JPEGs geprüft) |
| P-03 | GPS in Debug-Logs | **Kein Problem** | Alle `SameView.GPS`-Logs per `BuildConfig.DEBUG` gegated (neu geprüft) |
| P-04 | Delete-Dialog erwähnt Galerie-Foto nicht | **Offen, LOW, verschiebbar** | Text weiterhin „This comparison will be permanently deleted.“; als Known Limitation in den R1-Notes dokumentiert |
| P-05 | „Location never shared or uploaded“ | Weiterhin zutreffend | Basis ist jetzt die Transfer-Spec (keine Standortdaten im Request, metadatenfreie JPEGs), nicht mehr das Fehlen von INTERNET (vgl. V2-Nachtrag) |
| S-01 | Settings-Backup | **Verworfen (Product Decision 2026-07-09)** | — |
| S-02 | `file://`-URIs intern | Unverändert latent | Nach außen gehen nur HTTPS-URLs (Custom Tab) und MediaStore/SAF-Pfade |
| S-03 | Keine Speicher-Quota | Offen, LOW, verschiebbar | DWB-Temp-Daten liegen in `cacheDir` und werden aufgeräumt |
| S-04 | reference-original-Overhead | Akzeptiert | — |
| LC-01 | Einzel-Delete ohne Snackbar | **Behoben** | `CameraViewModel` sendet `delete_failed` auch im Einzelpfad |
| A-01 | Camera-Preview-Description | False Positive | — |
| A-02 / A-03 | Slider/Overlay ohne TalkBack-Alternative | **Offen, MEDIUM, verschiebbar** | Keine `customActions` in Compare/Camera; Known Limitation in den R1-Notes. Der neue Wackelbild-Screen hat dagegen eine Custom Action |
| A-04 / A-05 | About-Screen-A11y | Behoben (V2) | — |
| R-01 | Kein Crash-Reporting | Bewusst so (keine Crash-SDKs laut `DataSafety.txt`) | Play Vitals; Mapping wird mit dem AAB ausgeliefert |
| R-02 | Minimale ProGuard-Regeln | Build OK; Laufzeitnachweis des Release-Builds für OkHttp/DWB fehlt | → R2-H03 |
| R-03 | Hardcodierte Test-Dependency-Version | Unverändert, kosmetisch | — |
| R-04 | versionCode-Prozess | **Wieder aufgetreten** | → **R2-B01** |
| PS-02 | Data-Safety-Formular | R1 erledigt; für R2 neu offen | → **R2-H02** |
| PS-03 | Device-Targeting | Durch M-01 erledigt | — |
| PS-04 | Domain/E-Mail aktiv | Extern, R1 live | Nicht erneut geprüft |

### 7.2 Aus RELEASE_HARDENING_AUDIT_V2

| Finding | Heutiger Status |
|---|---|
| Privacy-Policy-Verlinkung (Blocker) | In-App behoben; Play-Anteil für R2 → R2-H02 |
| OOM beim HQ-Export | Behoben (V2) |
| „Clear markers“ | Verworfen (Product Decision) |
| Privacy-Mode-Fallback | Behoben (Settings-Text + Session-Hinweis) |
| `branding/` im Backup | Behoben |
| Settings-DataStore-Backup | Verworfen (Product Decision) |
| Backup-ZIP mit gemischtem Privacy-Status | Verworfen (Product Decision) |
| GPS-Toggle ohne permanente Beschreibung | **Offen, MEDIUM, verschiebbar** — kein `settings_recreation_guidance_description` vorhanden; nur reaktiver Rationale-Dialog bzw. Hinweis bei verweigerter Permission |
| Video-Dateiname mit Aufnahmezeit | Behoben |
| Guide-Tips ohne Live-Region / dynamische Learn-More-Beschreibung | **Offen, MEDIUM, verschiebbar** — `GuideTipHost.kt` ohne `liveRegion`; Spec `GUIDE_TIPS_UX_V1.md` fordert sie weiterhin |
| Tote Strings (`markers_outside_image`, `compare_screen_edit_title*`, `about_no_account_required`) | **Behoben** — Ressourcen entfernt. Lint meldet andere, harmlose ungenutzte Ressourcen (`compare_images`, `settings_grid_type_*`, `edit_session_save` …) |
| EN/DE-Delete-Dialog | Behoben |
| Walkthrough-WEBP vs. Spec | Erledigt durch Spec-Anpassung (2026-07-09) |
| Share-Label „Extras“ vs. Spec | Erledigt (Spec nutzt jetzt `share_comparison_extras_label`) |
| Nachtrag INTERNET (2026-09-21) | Durch dieses Audit fortgeschrieben (Abschnitt 6) |

---

## 08 — Positive, konkret geprüfte Befunde

1. **Secret-Hygiene:** Der 56-stellige lokale Key ist weder in der Git-Historie (`git log --all -S`) noch in getrackten Dateien, im Release-Repo oder im Website-Repo enthalten. `local.properties` ist ignoriert. Release-AAB und -APK enthalten den lokalen Key nicht (DEX-Scan).
2. **Release/Debug-Trennung des Keys** wirkt strukturell wie spezifiziert (Release-`BuildConfig` leer, Debug befüllt).
3. **Gemergtes Release-Manifest:**
   - Permissions: CAMERA, FINE/COARSE/MEDIA_LOCATION, INTERNET, ACCESS_NETWORK_STATE (transitiv, Media3), DYNAMIC_RECEIVER_NOT_EXPORTED (AndroidX-intern)
   - Exportiert sind nur `MainActivity` sowie `ProfileInstallReceiver` (geschützt durch `android.permission.DUMP`)
   - Die Test-ContentProvider aus `src/debug` sind im Release nicht enthalten
4. **Kein Klartextverkehr möglich:** Es gibt weder `usesCleartextTraffic` noch eine NSC; die Plattform-Defaults greifen, die URLs werden auf HTTPS validiert, Redirects sind deaktiviert.
5. **Netzwerk-Isolation:** Nur das DeinWackelbild-Paket nutzt Netzwerk. Lokale Funktionen (Capture, Compare, Share, Video, Backup) haben keinen Netzwerkpfad.
6. **Fehlerklassifikation** ist vollständig (13 Klassen inkl. Catch-all) und robust: Ein fehlerhafter Error-Body überschreibt keine bekannte HTTP-Klassifikation; ein HTTP-Cancel bricht den Transfer tatsächlich ab.
7. **Temp-Datei-Lifecycle** ist dreistufig (Erfolg vor `Ready`, `finally` + `NonCancellable`, Sweep beim Eintritt) und containment-gesichert.
8. **Transfer-Privacy:** metadatenfreie JPEGs, unveränderte Session-Dateien, keine Identifikatoren — alles per Instrumented Tests abgesichert.
9. **Schema-Kompatibilität:**
   - `location.countryCode` ist additiv, `SCHEMA_VERSION` bleibt 6
   - der Scanner übernimmt Werte tolerant
   - ältere Builds (R1) ignorieren das Feld, ein Downgrade bleibt also lesbar
   - `updateLocation` ist als Full-Replacement konsistent; `countryCode` geht bei unbeteiligten Änderungen nicht verloren
10. **Referenzdatum-Validierung** ist präzisionsbewusst und blockiert Legacy-Werte nicht.
11. **Backup-/Extraction-Regeln** sind unverändert korrekt (`sessions/`, `branding/` ausgeschlossen); DWB-Daten liegen außerhalb des Backups.
12. **16-KB-Page-Size:** Alle 16 nativen Libraries (4 ABIs × 4 Libs) im R2-AAB haben LOAD-Alignment 16384 (ELF-Header-Prüfung). Gleiches gilt für das R1-AAB.
13. **targetSdk 36** erfüllt die aktuelle Play-Mindestanforderung. Die Migration ist laut `IMPLEMENTATION_NOTES.md` real-device-verifiziert.
14. **Lokalisierung:** EN/DE-Key-Parität vollständig. Die einzigen EN-only-Keys sind korrekt `translatable="false"`. Alle 27 Wackelbild-Strings existieren in beiden Sprachen.
15. **Debug-Logging:** Alle GPS-Logs sind gegated, im DWB-Code gibt es keine Logs.
16. **Accessibility im neuen Screen:** Custom Action zum Bildwechsel, kein Tilt-Zwang, Swipe-Fallback, deaktivierter Toggle mit Begründungstext.
17. **Native Debug Symbols (Issue #7):** Bewusst geschlossen — es handelt sich um eine Upstream-Limitierung; die Play-Warnung ist nicht blockierend.

---

## 09 — Test-, Lint- und Build-Ergebnisse

Umgebung: Windows 11, JDK 25 (Launcher) / Daemon-JVM 21, Gradle 9.3.1. Kein physisches Gerät angeschlossen (`adb devices` leer). Managed-Device-Images API 29/33/35/36 sind installiert. **Kein `DEINWACKELBILD_PARTNER_KEY` in der Umgebung gesetzt.**

| # | Kommando | Ergebnis |
|---|---|---|
| 1 | `./gradlew clean testDebugUnitTest testReleaseUnitTest lintDebug lintRelease assembleDebug assembleRelease bundleRelease assembleDebugAndroidTest --continue` | **Fehlgeschlagen in der Konfiguration:** `Task 'testReleaseUnitTest' not found` (in dieser AGP-9-Konfiguration existieren Unit-Tests nur für Debug). Kein Code-Problem; der Lauf wurde ohne diesen Task wiederholt. |
| 2 | `./gradlew clean testDebugUnitTest lintDebug lintRelease assembleDebug assembleRelease bundleRelease assembleDebugAndroidTest --continue` | **BUILD SUCCESSFUL** (22 min 46 s) |
| 2a | — `testDebugUnitTest` | **1231 Tests, 0 Failures, 0 Errors, 0 Skipped** |
| 2b | — `lintDebug` | **0 Errors**, 117 Warnings, 6 Hints (u. a. GradleDependency 17, NewerVersionAvailable 9, ExifInterface 13, UnusedResources 15, ExportedContentProvider 2 — nur Debug-Testprovider) |
| 2c | — `lintRelease` | **0 Errors**, 116 Warnings, 6 Hints (wie oben ohne ExportedContentProvider; UnusedResources 16) |
| 2d | — `assembleDebug` | erfolgreich |
| 2e | — `assembleRelease` | erfolgreich (R8 + Resource Shrinking, **unsigned**) |
| 2f | — `bundleRelease` | erfolgreich (unsigned); versionCode 100; Partner-Key leer (→ R2-H01) |
| 2g | — `assembleDebugAndroidTest` | erfolgreich |
| 3 | `./gradlew pixel2Api36DebugAndroidTest --continue` (Managed Device, Pixel 2, API 36, x86_64) | **Task FAILED: 1111 Tests, 1110 bestanden, 1 fehlgeschlagen** (1 h 9 min) — Details in 9.1 |
| 3a | `./gradlew pixel2Api36DebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.isardomains.sameview.ui.compare.CompareScreenTest` | **BUILD SUCCESSFUL: 113/113 bestanden** (Wiederholung der betroffenen Klasse) |
| 3b | `./gradlew pixel2Api29DebugAndroidTest --continue` (Managed Device, API 29 = minSdk) | **BUILD SUCCESSFUL: 1111 Tests, 0 Failures** (45 min) |
| 4 | Artefaktprüfungen (Python/unzip, read-only): gemergtes Release-Manifest, `BuildConfig`-Key-Längen, DEX-Scan auf lokalen Key, ELF-16-KB-Alignment R1/R2-AAB | durchgeführt, Ergebnisse in Abschnitt 8 / R2-H01 |

Nicht ausgeführt bzw. nicht möglich:

- `connectedDebugAndroidTest` auf einem physischen Gerät: kein Gerät angeschlossen.
- Signierter Release-Build und Installation: keine Signing-Konfiguration im Repo, Keystore außerhalb.
- Jeglicher echter Netzwerkaufruf gegen DeinWackelbild: bewusst nicht, kein Produktions-Key, Audit ohne Netzwerk-Seiteneffekte.
- Rebuild des R1-Tags zum Byte-Vergleich.

Es wurden keine Tests deaktiviert, keine Lint-Probleme unterdrückt und keine Baseline erzeugt.

Hinweis zum Repository-Zustand: Der Gradle-Lauf erzeugte das untracked Verzeichnis `.kotlin/` (siehe R2-I01). Es wurde nach Abschluss der Läufe entfernt, damit der Working Tree bis auf dieses Dokument unverändert bleibt.

### 9.1 Nachtrag Instrumentation (Managed Device)

**API 36 (`pixel2Api36`):** 1111 Tests, 1110 bestanden, 1 fehlgeschlagen.

- Fehlgeschlagen: `CompareScreenTest.compareScreen_bothImagesUseSameViewportSurface` — `AssertionError: expected:<51.809525> but was:<41.52381>` in `assertRectEquals(viewportBounds, captureBounds)` (Zeile 548).
- Einordnung: **Timing-Flake des Tests, keine Produkt-Regression.**
  - Die unmittelbar davor liegende Assertion (`viewportBounds` gegen `referenceBounds`) war im selben Lauf erfolgreich. Die drei Bounds werden nacheinander und nicht atomar gelesen, während sich das Layout nach dem Laden der Bilder noch setzt.
  - Die isolierte Wiederholung der gesamten Klasse war vollständig grün (113/113).
  - Der Test und der betroffene Layout-Code sind seit R1 nicht inhaltlich verändert worden.
- Der Test wurde weder deaktiviert noch angepasst. Als Stabilitätsthema der Testsuite ist er nach Release 2 verschiebbar.

**API 29 (`pixel2Api29`, minSdk):** **BUILD SUCCESSFUL** (45 min), 1111 Tests, 0 Failures, 0 Errors, 0 Skipped. Der auf API 36 einmalig fehlgeschlagene Test war hier grün.

**Nicht ausgeführt:** `pixel2Api33` und `pixel2Api35` (bzw. die Gruppe `allPixel2Devices`). Mit API 29 und API 36 sind die untere und die obere Grenze des unterstützten Bereichs abgedeckt; die beiden mittleren Läufe wurden aus Zeitgründen nicht gestartet.

**Aussagegrenze der Instrumentation-Tests:** Alle Wackelbild-UI-Tests laufen gegen einen Fake-API-Client und einen Fake-Custom-Tab-Launcher. Der echte OkHttp-Client, der echte Endpunkt, der Release-Build (R8) und die Key-Provisionierung werden von keinem automatisierten Test auf einem Gerät ausgeführt (vgl. R2-H03, R2-M02).

---

## 10 — Notwendige Real-Device- und Pilot-Prüfungen

Mit einem **signierten Release-Build** (R8 aktiv, Produktions-Key per Env-Var) auf mindestens einem physischen Gerät (Android 16), möglichst zusätzlich auf einem Low-RAM-Gerät mit API 29/30:

1. **Key-Nachweis:** Bestellung erreicht den Partner (kein „derzeit nicht verfügbar“). Danach Artefaktprüfung „Key-Länge > 0“, ohne den Wert auszugeben.
2. **Happy Path** Portrait (9:16 → `10x15`, Portrait) und Landscape (16:9 → `10x15`, Landscape), jeweils mit Datum an und aus. Prüfen, dass der Custom Tab die Konfiguration „Seitlich kippen“, das passende Format und eine unveränderte Komposition zeigt und das Datum nicht im Partner-Inset verschwindet.
3. **3:4-Session** → `15x20`. Session ohne Viewport-Metadaten → Vollbild, kein Format.
4. **HQ vs. Fallback:** Bei regulären aktuellen Sessions darf **kein** „Originalqualität nicht verfügbar“-Dialog erscheinen. Eine Session mit entferntem `capture-original.jpg` muss den Dialog zeigen; Abbrechen darf keinen Netzwerkverkehr erzeugen.
5. **Abbruch** während der Vorbereitung und während des Uploads (Back → „Abbrechen“). Erwartet: keine Fehlermeldung (aktuell erscheint sie, siehe R2-M01), `cacheDir/wackelbild/` danach leer.
6. **Flugmodus vor dem CTA** → „Keine Internetverbindung“ + „Erneut versuchen“; **Flugmodus während des Uploads**; **gedrosseltes Netz** (z. B. 512 kbit/s Uplink) → Upload-Dauer und Timeout-Verhalten messen (R2-L03); dabei die realen Transfer-Dateigrößen protokollieren.
7. **StrictMode/ANR-Beobachtung** im Debug-Build mit `detectNetwork` während einer echten Bestellung (R2-M02).
8. **Background/Resume:** Home während des Uploads → Rückkehr → Custom Tab öffnet genau einmal. Rückkehr aus dem Custom Tab → Reset auf Referenz, Toggle wieder editierbar, kein Bestellstatus.
9. **Erneute Bestellung** nach Rückkehr erzeugt einen neuen Handoff.
10. **Prozess-Tod** während des Uploads (Entwickleroptionen / `am kill`) → beim Neustart kein Auto-Resume, kein Shop-Öffnen, Temp-Ordner nach erneutem Screen-Eintritt leer.
11. **Privacy-Stichprobe:** Beim Partner angekommene Bilder enthalten keine EXIF/GPS-Daten (sofern der Pilot den Zugriff erlaubt).
12. **Großes Referenzbild / Low-RAM:** kein Crash, notfalls Fallback-Dialog.
13. **Tilt-Blend/Perspektive/Ridges** (offenes Tuning laut Plan §25): kein Flackern, korrekte Richtung in Portrait/Landscape, nach Rotation und nach Wiedereintritt.
14. **Regression Kern-Workflow** im Release-Build: Capture mit Overlay, Compare, Share Image, Video Export, Backup, Edit Session mit Country-Picker und Referenzdatum-Validierung (DE/EN).
15. **Partner-Abnahmepunkte** aus Spec §51 (Idempotenz bei Verbindungsabbruch, abgelaufene Handoffs, Partner-ID `sameview` in der Testbestellung, Rechnung/E-Mail ohne Tokens) gemeinsam mit DeinWackelbild.

---

## 11 — Muss vor Release 2 behoben werden

1. **R2-B01** — versionCode erhöhen (und versionName setzen).
2. **R2-H01** — Absicherung gegen einen Release-Build ohne Partner-Key (Build-Gate und/oder verbindlicher, verifizierter Release-Schritt); Klärung Pilot- vs. Produktions-Key. *Status 2026-10-06: Build-Gate umgesetzt und verifiziert (siehe 5.2). Die Klärung Pilot- vs. Produktions-Key bleibt offen und wird unter R2-H03 geführt.*
3. **R2-H02** — Play Data Safety für R2 aktualisieren (inkl. der offenen Punkte in `DataSafety.txt`); Live-Stand von Privacy/Terms (EN/DE) verifizieren. *(extern)*
4. **R2-H03** — Real-Device-/Pilot-Abnahme gemäß Abschnitt 10 mit signiertem Release-Build durchführen und dokumentieren, oder Einzelpunkte explizit mit Begründung zurückstellen.

**Dringend empfohlen vor Release 2** (MEDIUM, geringer Aufwand, reale Nutzerwirkung):

5. **R2-M01** — Cancellation im Renderer nicht als Fehler behandeln; Ergebnis abgebrochener Jobs verwerfen. *Status 2026-10-06: behoben und verifiziert (siehe 5.5); der ViewModel-Teil war nicht erforderlich.*
6. **R2-M02** — Response-Lesen und Datei-IO vom Main-Thread nehmen. *Status 2026-10-06: Netzwerk-Teil behoben und verifiziert (siehe 5.5); die lokale Datei-IO wurde bewusst nicht geändert.*
7. **R2-M03** — Store-Text „Import“ korrigieren (im Zuge des ohnehin anzupassenden Listings / der R2-Release-Notes, vgl. R2-I03).

---

## 12 — Bewusst nach Release 2 verschiebbar

| Punkt | Begründung |
|---|---|
| R2-L01 Rückkehr zur CompareScreen nach Abbruch | UX-/Spec-Abweichung ohne Datenrisiko; alternativ Spec anpassen |
| R2-L02 Locale-Mapping | Faktisches Verhalten = Spec-Fallback `de-DE`; Partner-Matrix offen. Entscheidung sollte vor R2 dokumentiert werden, die Implementierung kann später folgen |
| R2-L03 Upload-Timeout-Budget | Erst nach Messung (Abschnitt 10, Punkt 6) entscheiden |
| R2-L04 `compress()`-Ergebnis | Randfall (Speicher voll) |
| R2-I01…I06 | Hygiene/Information |
| Flake `CompareScreenTest.compareScreen_bothImagesUseSameViewportSurface` | Test-Stabilität, keine Produkt-Regression (siehe 9.1) |
| V1 A-02/A-03 (Slider/Overlay-A11y) | Vorbestehend, als Known Limitation kommuniziert |
| V2 Guide-Tip-Live-Region, GPS-Toggle-Beschreibung | Vorbestehend, keine Regression |
| V1 P-04, S-03 | Vorbestehend, LOW |

---

## 13 — Abschluss

### **GO AFTER FIXES**

Der Code von Release 2 ist in Architektur, Privacy und Secret-Handling der neuen Netzwerkfunktion solide und spec-konform. Unit-Tests, Lint und alle Builds sind grün; die Instrumentation-Suite ist auf API 29 vollständig grün und auf API 36 bis auf einen als Flake eingeordneten Test.

Ein Release ist aber erst möglich, wenn

- der **versionCode** erhöht ist (BLOCKER),
- ein Release-Build **mit leerem Partner-Key ausgeschlossen** ist,
- **Data Safety / Privacy** extern auf den neuen Datenfluss gebracht sind,
- die **Pilot-/Real-Device-Abnahme mit dem signierten Release-Build** dokumentiert ist.

Die drei MEDIUM-Findings sollten wegen ihrer direkten Nutzerwirkung und des geringen Aufwands ebenfalls vor Release 2 behoben werden.

---

## Colophon

Read-only-Audit. Angelegt wurde ausschließlich `docs/RELEASE_HARDENING_AUDIT_RELEASE_2.md`. Es wurden kein Code, kein Manifest, keine Gradle-Datei, keine Ressource, keine Tests und keine anderen Dokumente verändert; die historischen Audits V1/V2 sind unverändert. Build-Ausgaben liegen ausschließlich in ignorierten Verzeichnissen (`build/`, `app/build/`); das vom Build erzeugte untracked `.kotlin/` wurde wieder entfernt.
