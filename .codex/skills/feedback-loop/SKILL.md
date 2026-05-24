---
name: feedback-loop
description: After every implementation or development task, append development-improvement feedback to `task/Feedback.md`. Do not replace or delete existing entries. In the final chat report, only state briefly that feedback was appended; do not duplicate the full feedback text.
---

# Post-Task Development Feedback

Use this format:

```md
# YYYY/M/D HH:mm フィードバック

## 作業内容
- ...

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: ...
- 改善した方がよいルール・手順: ...
- 追加した方がよいルール・手順: ...
- docs/README/タスクメモ/テストなどへ反映した方がよい点: ...

## 分類
- タスク固有: ...
- 恒久対応候補: ...

## 更新先候補
- AGENTS.md: ...
- .codex/skills/md-doc-viewer/SKILL.md: ...
- docs/README/task/tests など: ...
```

# Requirements

- Always start each entry with a heading in the exact form `# YYYY/M/D HH:mm フィードバック`, using the write timestamp.
- Always include a short summary of the task.
- Do not write only "なし". If there are no candidates, briefly state which areas were checked and why no update is needed.
- Treat "rules" as both `AGENTS.md` and this `SKILL.md`, plus any files referenced from them.
- Also consider broader development improvements, including `docs/`, README files, task notes, tests, fixtures, verification steps, local setup, and developer workflow.
- Classify each candidate as task-specific or permanent.
- Task-specific candidates usually belong in this `SKILL.md`, `docs/`, README, `task/Feedback.md`, tests, or verification notes.
- Permanent candidates usually belong in `AGENTS.md` or a rule file referenced from it.
- Do not update rule or documentation files from feedback unless the user explicitly approves that update.
