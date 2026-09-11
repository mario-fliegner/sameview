# DeinWackelbild Upload HTTP 400 `dwb_handoff_image_missing` — ANALYSIS ONLY

## Context

SameView's live DeinWackelbild pilot now gets past partner authentication successfully.

The previous `401 dwb_partner_unauthorized` was caused by an accidental trailing period in the locally copied live key. After removing that period and rebuilding, **Create Handoff now succeeds**.

The next failure occurs during the subsequent image upload.

Observed on the real S23 via Android Studio debugger:

- Stage is now in `uploadImage(...)`
- `expectedSuccessCode = 200`
- HTTP response: `400`
- `serverCode = dwb_handoff_image_missing`
- server message: `Die JPG-Datei fehlt.`
- request URL is a concrete partner-handoff `/files` upload URL returned by the successful Create Handoff
- therefore the Create Handoff itself is already working and this is a separate upload-stage failure

Partner contract previously confirmed by Olaf:

- Create:
  - header: `X-DWB-Partner-Key`
  - returns dynamic upload URL + handoff token
- Upload:
  - multipart text field: `slot`
  - multipart file field: `file`
  - upload auth header: `X-DWB-Handoff-Token`
- after second upload the server may return `status=ready` + `checkout_url`

The current server error strongly suggests that the live endpoint does not recognize the JPG file part in the multipart request.

I do **not** want to contact Olaf until SameView's request construction has been audited carefully.

This is **STEP 1 — ANALYSIS ONLY**.

**DO NOT modify code.**
**DO NOT change files.**
**DO NOT add logging.**
**DO NOT retry repeatedly against the live API.**
**DO NOT expose partner key, handoff token, signed upload URL, checkout URL, or raw multipart body.**

---

# Mandatory Source-of-Truth Review

Before analyzing the request, inspect the current relevant docs:

- `CLAUDE_PROJECT_INSTRUCTION.md`
- `docs/deinwackelbild/DEINWACKELBILD_INTEGRATION_V1.md`
- `docs/deinwackelbild/DEINWACKELBILD_IMPLEMENTATION_PLAN_V1.md`
- `docs/IMPLEMENTATION_NOTES.md`

Then inspect the actual current implementation, especially:

- `OkHttpDeinWackelbildApiClient.kt`
- related DTO/model files
- the upload request builder
- `WackelbildHandoffOrchestrator.kt`
- `WackelbildPrintRenderer.kt`
- temp-file manager if it is involved in upload file selection
- existing API client tests
- existing orchestrator tests
- any test fixture that inspects the multipart request

Do not trust the implementation plan blindly. Verify the live code.

---

# Exact Problem to Determine

We need to answer:

**Why does the live server return `400 dwb_handoff_image_missing` / `Die JPG-Datei fehlt.` even though SameView believes it is uploading the image?**

We need to distinguish between:

1. SameView does not actually include a multipart file part.
2. The file part exists but uses the wrong form field name.
3. The file part exists but is empty / zero bytes.
4. The file part has the wrong `Content-Disposition`.
5. The file part has the wrong `Content-Type`.
6. The filename is missing or malformed in a way the server rejects.
7. The multipart body is constructed incorrectly.
8. The `slot` field is malformed and causes server-side parsing to miss the file.
9. The wrong temp/local file is selected.
10. The renderer output file exists but is not what upload code sends.
11. OkHttp/request construction differs between tests and real runtime.
12. The live server contract differs from the contract previously confirmed by Olaf.

---

# 1. Audit Multipart Construction Exactly

Inspect the actual `uploadImage(...)` implementation and report the precise request shape.

For the multipart body, verify:

- `MultipartBody.FORM` is used
- text field name is exactly:
  - `slot`
- slot value is exactly the server-expected value for:
  - ONE
  - TWO
- file field name is exactly:
  - `file`
- a filename is included
- filename extension is `.jpg` or `.jpeg`
- file body media type is exactly/appropriately:
  - `image/jpeg`
- file part is added through the correct OkHttp multipart API
- no accidental raw-body upload is used instead of multipart
- no nested multipart or custom encoding exists
- no duplicate `file` parts exist
- no accidental alternate name such as:
  - `image`
  - `jpg`
  - `upload`
  - `files`
  - `photo`
  - `file[]`
- no request body is converted to text/string or otherwise corrupted

Give concrete file/method/line references.

---

# 2. Verify File Existence and Byte Length

Trace the exact file passed from:

renderer
→ prepared pair/result
→ orchestrator
→ API client `uploadImage(...)`.

Determine statically:

- exact type passed to upload method
- exact local file path source
- whether the file must exist before upload
- whether it is opened/read lazily by OkHttp
- whether temp cleanup can happen before request execution
- whether cancellation/finally cleanup can race with the upload
- whether the first upload file and second upload file can be swapped, deleted, replaced, or zeroed
- whether renderer can produce a zero-byte file
- whether pair-size/re-encode logic can leave an invalid/empty output

If static inspection cannot prove runtime byte length, give a no-code debugger check such as:

- `file.exists()`
- `file.length()`
- filename
- safe MIME/content-type inspection

Do not ask me to post local full filesystem paths if avoidable.

---

# 3. Verify `Content-Disposition`

This is important because `dwb_handoff_image_missing` may mean the server's multipart parser does not see a file in the expected field.

Determine the exact effective multipart disposition created by OkHttp.

It should semantically correspond to something like:

`Content-Disposition: form-data; name="file"; filename="something.jpg"`

Do not output the whole raw multipart request.

Instead verify by code inspection or safe debugger checks:

- field name = `file`
- filename is present
- filename is non-empty
- file part is recognized as a multipart **file** part, not a normal text part

If current code uses `addFormDataPart(name, filename, body)`, state that explicitly.

If it uses another method, explain the effective result.

---

# 4. Verify `Content-Type`

Confirm the actual file part's media type.

Questions:

- Is it `image/jpeg`?
- Is it `application/octet-stream`?
- Is it null?
- Is the entire multipart request `multipart/form-data` with a generated boundary?
- Is some custom header overriding the multipart content type incorrectly?

If the file part MIME type is not `image/jpeg`, state whether that could plausibly explain the live server message.

---

# 5. Verify Upload Authentication and URL — But Keep Scope Narrow

The current failure is `image_missing`, not auth, so do not over-focus on auth.

Still confirm:

- header is exactly `X-DWB-Handoff-Token`
- the token from the successful Create response is used
- no partner key is used on upload
- exact server-provided upload URL is used without reconstruction
- no redirect/interceptor alters the request
- method is the expected HTTP verb
- expected success code is correct per contract

Do not reveal token or URL values.

---

# 6. Compare Against Existing Tests

Audit the current API-client multipart tests.

Answer:

- Do tests inspect the actual multipart request body?
- Do they assert exact field name `file`?
- Do they assert exact field name `slot`?
- Do they assert filename?
- Do they assert media type `image/jpeg`?
- Do they assert non-zero file bytes?
- Do they parse multipart semantically or only search request-body text?
- Could the tests pass even if the real request contains no valid file part?
- Could test fixtures differ from production temp files?

If a test has a blind spot, identify it precisely, but **do not change it yet**.

---

# 7. Safe No-Code Runtime Inspection

If static inspection is not sufficient, give the smallest Android Studio debugger procedure.

Preferred breakpoints:

- immediately before `callFactory.newCall(httpRequest).execute()`
- inside `uploadImage(...)` after multipart body construction

Safe values to inspect:

- HTTP method
- multipart body type
- number of multipart parts
- each part's headers **without secret auth header**
- whether a part has `name="file"`
- whether it has a filename
- file body content length
- file MIME type
- `slot` value
- local file `exists()`
- local file `length()`

If OkHttp allows evaluating:
- `requestBody.contentType()`
- `requestBody.contentLength()`
- multipart parts count
- part headers
- individual part body content length/type

then give exact safe expressions.

Do not instruct me to dump:
- whole request
- whole multipart body
- headers including token
- signed URL
- partner key

---

# 8. Contract Cross-Check Against Olaf's Confirmed API

Compare actual SameView request construction against the confirmed partner contract:

Upload must contain:

- header:
  - `X-DWB-Handoff-Token`
- multipart text:
  - `slot`
- multipart file:
  - `file`

State one of:

- `MATCHES CONTRACT EXACTLY`
- `CLIENT-SIDE MISMATCH FOUND`
- `STATICALLY MATCHES — RUNTIME CHECK STILL REQUIRED`

If there is a mismatch, identify only that mismatch and stop. Do not propose a broad rewrite.

---

# 9. Root-Cause Ranking

Rank these based on actual evidence:

- wrong multipart file field name
- missing multipart filename
- wrong file MIME type
- zero-byte/nonexistent temp file
- wrong multipart API usage / malformed disposition
- temp cleanup race
- wrong file object passed from orchestrator
- live API contract mismatch on partner side
- other specific cause discovered

For each:
- evidence FOR
- evidence AGAINST
- confidence

Do not speculate beyond what the code/runtime evidence supports.

---

# 10. Decision: Should We Contact Olaf?

Return exactly one:

- `NO — CLIENT-SIDE ISSUE FOUND`
- `NOT YET — RUNTIME MULTIPART CHECK REQUIRED`
- `YES — CLIENT REQUEST VERIFIED AGAINST CONTRACT`

If `YES`, list the exact **non-secret** facts we can send Olaf, for example:

- Create Handoff succeeds
- upload request uses multipart/form-data
- text field is `slot`
- file field is `file`
- filename present
- media type `image/jpeg`
- file byte length > 0
- header name `X-DWB-Handoff-Token`
- response `400 dwb_handoff_image_missing`

Do not include actual token, upload URL, partner key, or raw multipart.

---

# Required Output

Return exactly these sections:

## 1. Repository / Source-of-Truth Check
Files/docs inspected, contradictions if any.

## 2. Exact Upload Request Shape
Method, multipart type, field names, filename behavior, MIME type, auth header name, URL handling.

## 3. File-Lifecycle Audit
Exact file passed, existence/lifetime/cleanup/zero-byte risk.

## 4. Existing Test Coverage / Blind Spots
What tests prove and do not prove.

## 5. Runtime Verification Needed?
If yes, exact no-code debugger steps and safe expressions.

## 6. Contract Comparison
One of:
- `MATCHES CONTRACT EXACTLY`
- `CLIENT-SIDE MISMATCH FOUND`
- `STATICALLY MATCHES — RUNTIME CHECK STILL REQUIRED`

## 7. Root-Cause Assessment
Ranked with evidence.

## 8. Should We Contact Olaf?
One of the three exact decision strings above.

## 9. File Changes
Must say:

`None — analysis only.`

Then STOP.

---

# Hard Constraints

- Analysis only.
- No source edits.
- No test edits.
- No logging additions.
- No request-format changes.
- No retries beyond at most one controlled debugger reproduction if needed.
- No partner contact.
- No secrets in output.
- No broad Block 12 work.
