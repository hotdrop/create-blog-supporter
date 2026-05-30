# AGENTS.md

このリポジトリの常時ルールだけを定義する。詳細な実装ルールは `.codex/skills/` に移し、対象タスクに応じて必要なスキルを読む。

## 恒久ルール
- このアプリは、ユーザー自身の言葉でテックブログを書くための執筆支援アプリである。
- LLMは本文生成の主体ではなく、構成、提案、校正を支援する補助機能として扱う。
- LLMの出力を保存済み本文へ自動反映しない。
- `content` はユーザーの明示的な保存操作でのみ更新し、編集中本文は `draftContent` として扱う。
- `phase2` に遷移した記事を `phase1` に戻してはならない。
- MVPでは、外部API連携、インターネット検索、クラウド同期、認証、ブログサービスへの直接投稿、Git連携など外部連携を追加しない。必要に見える場合は先にユーザーへ相談する。

## スキル利用
- Android全体設計、DI、テスト、検証方針を変更する場合は `.codex/skills/android-app-architecture/SKILL.md` を読む。
- 記事フェーズ、`content` / `draftContent`、LLM提案、MVP境界に関わる場合は `.codex/skills/article-writing-domain/SKILL.md` を読む。
- Compose UI、Flow/Coroutines、Room、LiteRT-LM、Markdown出力に関わる場合は、該当する `.codex/skills/*/SKILL.md` を読む。
- Android CLIは常用せず、最新Android仕様、SDK/エミュレータ/実行、レイアウト調査、Android Studio連携が必要な場合に限って `.codex/skills/android-cli-usage/SKILL.md` を読み、必要に応じて汎用 `android-cli` スキルも読む。
- 実装後は既存ユニットテストを実施し、開発タスクでは `.codex/skills/feedback-loop/SKILL.md` に従って `task/Feedback.md` へフィードバックを追記する。
