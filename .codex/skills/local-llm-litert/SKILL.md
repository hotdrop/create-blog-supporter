---
name: local-llm-litert
description: Use when implementing or modifying local LLM features backed by LiteRT-LM, including prompt construction, streaming, model initialization, fallback, errors, and UI-safe abstractions.
---

# Local LLM LiteRT-LM Skill

## 実装方針
- LiteRT-LMは本文生成の主体ではなく、構成、提案、校正を支援する補助機能として扱う。
- LiteRT-LM SDK型、Engine、Conversation、Backendはdata層またはinfrastructure層に閉じ込める。
- ViewModelとUIは、アプリ独自のrequest/result型、UiState、Flowだけを扱う。
- `docs/LocalLLMSample/` の実装例を参照し、UIスレッドでEngine初期化や生成処理を実行しない。
- LiteRT-LMモデル選択は権限追加を避け、`OpenDocument` などのユーザー選択からアプリ内ストレージへコピーする導線を優先する。
- LiteRT-LM未設定時でも、LLM不要の執筆メモ作成・編集フローはブロックしない。

## LLM提案ルール
- LLM出力を `content` へ自動反映しない。
- 章節本文への採用は必ず `draftContent` に反映し、ユーザーの保存操作後にのみ `content` へ確定する。
- 目次案採用前のタイトル案・目次案は永続化しない。ユーザーが採用した案だけをArticleDraft/ArticleSectionへ保存する。
- 誤字脱字・表記ゆれチェック結果は参考情報として表示し、ユーザー確認状態を自動でONにしない。

## Flow / Engine ルール
- 生成結果のストリーミングは `Flow` で扱う。
- `CancellationException` は握りつぶさず再送出する。
- 同時実行できない生成処理は `Mutex` または `Job` ガードで制御する。
- GPU backendの初期化に失敗した場合はCPU backendへのfallbackを許可し、ユーザー向け表示と内部ログを分離する。
- モデル未設定、モデルファイル不在、初期化失敗、生成失敗をアプリ独自の失敗型に変換する。
- ローカルLLMのエラー文言ではクラウドサービス前提の「時間をおいて再度」などの表現を避け、モデル未設定、ファイル欠落、初期化失敗、応答取得失敗を分ける。
- Engineは必要以上に作り直さず、モデルパス単位で再利用し、不要になったらcloseする。

## Prompt ルール
- プロンプトは「ユーザー自身の言葉を尊重する」「完成本文を代筆しない」制約を含める。
- タイトル案、目次案、章節概要、改善提案、校正チェックは用途ごとに入力型と出力型を分ける。
- JSONなど構造化出力を要求する場合は、パース失敗時の復旧方針をUseCaseで扱う。
- LLM応答は形式ゆれを前提にし、構造化パース失敗時もUseCase層でUIに渡せるアプリ独自候補へ復旧する。

## UI連携ルール
- 30秒を超える可能性があるローカル処理では、処理内容、所要時間の目安、中断方法を画面内に表示する。
- ローカルLLM処理中の表示は通信待ちではなく、「端末内でモデル処理中」と明示する。

## テストルール
- LiteRT-LM SDKそのものは単体テストで実行しない。LLM client境界を差し替え、UseCaseの成功・失敗・キャンセル・パース失敗を検証する。
- `content` へ直接反映しないこと、採用時に `draftContent` へ入ることをUseCaseテストで確認する。

## 完了チェック
- UI層にLiteRT-LM SDK型が出ていない。
- LLM出力が `content` に直接保存されない。
- モデル未設定や初期化失敗がユーザー向け状態へ変換される。
- ストリーミング、キャンセル、同時実行制御が実装されている。
- 長時間ローカル処理の待機理由と中断導線がUIで確認できる。
