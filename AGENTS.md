# Contributor and agent guidelines

This file is public. Keep it free of private information.
If `AGENTS.local.md` exists in the repository root, read it too. It is ignored by Git
and holds machine-specific instructions (local paths, device-test workflow, archive
locations). Never commit it.

## Project

- `hc-lite` is a reduced fork of ColdP/HyperChanger. Package: `com.zhuhuibin.hyperchange.lite`.
  Keep the `hyperchange_lite` preference group and the `hyperchange-lite` JSON format
  so upgrades and old preset imports keep working.
- Scope: SystemUI/Control Center materials, notification behavior, volume controls,
  Personal Assistant widgets and SecurityCenter sidebar material.
- Preserve source attribution and licences. Behavioral references are described in prose;
  never redistribute third-party APKs, decompiled trees or proprietary assets.
- Keep settings in small feature-specific files. Every exposed setting needs a consumer,
  validation, reset and export/import coverage. Keep Chinese and English resources complete.
- Global class-loading callbacks keep their Java-only, callback-entry guard and defer adapter
  installation (see `design/STARTUP_SAFETY.md`). Preserve the upstream's startup ordering
  when pruning features. Keep raw ANR evidence out of commits.
- Build and test with `tools/build.sh` (see the Build section of `README.md`).
  Environment variables: `DEV_VOLUME` (required), `DEV_TOOLS_DIR`, `LITE_DEV_CACHE`,
  `APK_ARCHIVE_DIR`, `JAVA_HOME`, `ANDROID_HOME`. Host tests do not prove on-device behavior.
- Signing material never lives in the repository. Do not publish releases or open upstream
  pull requests unless explicitly asked.

## Repository hygiene (every commit)

1. **No machine-specific details.** Never commit absolute paths, user names, host names,
   drive or volume names, or personal folders. Use the documented environment variables.
2. **No process records.** Keep logs, approval or handover notes, review transcripts,
   AI prompts, session or memory notes, device dumps, screenshots and local APK receipts
   out of the repository. Store them elsewhere or in an ignored local file.
3. **Write for outside readers.** Commit subjects and documents are technical and neutral
   (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`, imperative mood). No workflow
   chatter about who asked for or approved what.
4. **Docs describe the code as it is,** not the history of how or when it was made.
   Put rationale and constraints in `design/`, not step-by-step work logs.
5. **No secrets.** No keys, tokens, passwords, keystores or personal data, in files or in messages.
6. **Self-check, then proceed.** Before each commit, run `tools/check-hygiene.sh` and read
   `git diff --cached` against these rules. Fix anything that violates them, then commit and
   push without asking for confirmation on routine hygiene. Enable the hook once per clone:
   `git config core.hooksPath tools/hooks`.
