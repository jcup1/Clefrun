# Contributing to Clefrun

This repo uses **feature branches** + **Conventional Commits** to keep work reviewable and history clean.

Reference: https://www.conventionalcommits.org/en/v1.0.0/

---

## Workflow

### 1) Create a branch for every change
Do **not** commit directly to `main`. Always branch.

**Branch naming**
- `feat/<short-description>` — new functionality
- `fix/<short-description>` — bug fixes
- `chore/<short-description>` — tooling, build, docs chores
- `docs/<short-description>` — documentation-only changes (optional)
- `refactor/<short-description>` — refactors without behavior changes (optional)

**Examples**
- `feat/m2-core-writer`
- `feat/m3-beginner-generator`
- `fix/webview-js-escaping`
- `chore/add-commitlint`
- `docs/readme-smoke-test`

**Commands**
```bash
git checkout main
git pull
git checkout -b feat/m2-core-writer
```
