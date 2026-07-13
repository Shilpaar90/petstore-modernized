---
name: ship
description: Verify, commit, push, and watch CI to green for the petstore-modernized repo. Use when asked to commit, push, or ship changes. Enforces the always-green trunk rule and the project's exact co-author commit trailer, and reports CI results including the Docker-only jobs that can't run locally.
---

# Ship a change (always-green)

The trunk must compile and pass tests at every commit. Never push red.

## 1. Verify locally first
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw verify
```
Expected: `BUILD SUCCESS`, `Tests run: N, Failures: 0, Errors: 0, Skipped: 1`.
The 1 skip is `PostgresCatalogParityTest` (Testcontainers needs Docker, absent locally). If
anything else fails, STOP and fix — do not commit.

## 2. Commit — with the project's exact trailer
The git history uses this trailer verbatim (no "(1M context)" suffix, matching every prior commit):

```
Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
```

```bash
git add -A
git commit -F - <<'EOF'
<summary line>

<body: what changed and why>

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
EOF
```

## 3. Push and watch CI to green
Publishing is outward-facing — only push when the user has asked to ship. Then:
```bash
git push origin main
RUN=$(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')
gh run watch "$RUN" --exit-status
gh run view "$RUN" --json conclusion,jobs \
  | python3 -c "import json,sys; d=json.load(sys.stdin); print('overall:',d['conclusion']); [print(' -',j['name'],'->',j.get('conclusion')) for j in d['jobs']]"
```

CI has two jobs, both must be green:
- **Build & test (Java 21)** — runs `mvn verify`; this is where `PostgresCatalogParityTest`
  actually runs against real PostgreSQL (Docker on the runner) and the embedded-Mongo tests run.
- **Build container image** — validates the multi-stage `Dockerfile`.

## 4. Report
Tell the user the commit sha, that CI is green, and specifically call out the results of the
Docker-only paths (Postgres/Testcontainers + image build) since those can't be verified on this
laptop — CI is the only place they run.

## Notes
- If on the default branch and the user only said "commit" (not push), commit but confirm before pushing.
- Diagnose any CI failure from `gh run view <id> --log-failed` (or fetch the failed job's log via
  `gh api repos/Shilpaar90/petstore-modernized/actions/jobs/<jobId>/logs`).
