# Claude Prompt — SameView Website / Network Permission Wording V1 — Commit and Tag

Repository: `C:\data\work\privat\git-repos\sameview-website`

The approved ACCESS_NETWORK_STATE Privacy wording fix has been implemented and verified.

Expected baseline was `main` at `07b81caea384fcfab9780d23ceba013968c6dff7`, tagged `v0.0.6`.

Expected modified files only:
- `src/pages/en/privacy/_privacy.md`
- `src/pages/de/privacy/_privacy.md`

## Task

First verify that the working tree contains exactly the approved two-file diff.

If and only if that is true:
1. Commit the existing approved changes.
2. Create stable production tag `v0.0.7`.
3. Push the commit to `origin/main`.
4. Push tag `v0.0.7`.

Do not edit any file.

Commit message:
`docs: correct network permission disclosure`

## Safety
- Do not amend.
- Do not force-push.
- Do not delete or move tags.
- Do not create another tag.
- Do not touch Android or `sameview-release`.
- If `v0.0.7` already exists locally or remotely, STOP and report it.
- If anything beyond the two approved Privacy files is changed, or the diff materially differs from the approved ACCESS_NETWORK_STATE fix, STOP.

## Final verification
Report:
- new commit hash and message;
- local branch status;
- confirmation `origin/main` contains the commit;
- confirmation `v0.0.7` points to that commit locally and remotely;
- final `git status --short`;
- whether anything remains modified/untracked.

Do not rerun the build: the exact approved diff already passed `astro build` immediately before this commit.

Then STOP.
