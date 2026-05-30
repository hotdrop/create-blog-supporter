---
name: android-cli-usage
description: Use when deciding whether and how to use Android CLI in this project, including current Android docs lookup, SDK and emulator operations, app run/layout inspection, Android Studio integration, and summarizing Android CLI results.
---

# Android CLI Usage Skill

## 位置づけ
- Android CLIは、公式情報とAndroid定型操作の補助として使う。
- 既存の設計ルール、記事執筆ドメインルール、MVP境界を上書きしない。
- Android CLI自体のコマンド仕様が必要な場合は、汎用 `android-cli` スキルも読む。

## 使う場面
- AGP、Compose、Navigation、edge-to-edge、R8、Firebase、Kotlinなど、最新Android仕様や公式推奨に不確実性がある場合。
- SDK、AVD、エミュレータ、アプリ実行、画面キャプチャ、レイアウト調査など、Android定型操作を行う場合。
- Android Studio連携によるファイル解析、シンボル宣言探索、Compose Preview確認が有効な場合。
- Codexが古い知識、推測、手探りのシェル/Gradle操作に寄りそうな場合。

## 使わない場面
- 既存コードの小規模修正や、明らかなKotlin/Compose/Room実装でリポジトリ内の既存パターンから判断できる場合。
- プロジェクト固有の設計判断、記事執筆ドメイン判断、`content` / `draftContent` の保存規則判断。
- LLM本文生成の主体化、外部API連携、インターネット検索、クラウド同期、認証、直接投稿、Git連携の追加可否判断。
- Android CLIのインストールや更新。必要な場合は先にユーザーへ相談する。

## 実行ルール
- `android docs` は最新公式情報の確認に使い、結果全文を貼らず、判断に必要な要点だけを作業へ反映する。
- CLI出力は必要部分だけ要約し、長いログやJSONを会話へそのまま貼らない。
- Android CLI実行で `~/.android/cli/analytics` への書き込みが発生し、Codex sandboxでは承認付き実行が必要になる場合がある。
- 既存のGradle検証方針は `.codex/skills/android-app-architecture/SKILL.md` を優先する。

## この環境
- `android`: `$HOME/.local/bin/android`
- `android --version`: `1.0.15498356`
- Android SDK: `$HOME/Library/Android/sdk`

## 確認コマンド
- 存在確認: `command -v android`
- バージョン確認: `android --version`
- SDKパス確認: `echo "$ANDROID_HOME"`
- SDK/環境確認: `android info`
