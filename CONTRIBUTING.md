# Contributing

This repository accepts small, focused PRs. Follow these guidelines to keep the project healthy:

- Create a branch from `main` named `audit-fixes/<short-description>` for code changes.
- Keep PRs small (<= 10 files) for code-style or bulk non-behavioral changes.
- Run `./gradlew assembleDebug` locally and ensure the build passes before pushing.
- Run `ktlint` and `detekt` if added to the project.
- Add unit tests for new business logic and UI tests for composable screens when appropriate.
- For bulk comment removal or formatting, do files in batches of 5 and open a PR per batch.
- CI will run on PRs; don't merge until CI is green and at least one reviewer approves.

If you need help, open an issue describing the change.
