---
name: android-room-persistence
description: Use when adding or modifying Room persistence, DAOs, entities, database migrations, transactions, and storage rules for ArticleDraft and ArticleSection.
---

# Android Room Persistence Skill

## 実装方針
- Roomを永続化の唯一の入口とし、UI層からDAOを直接呼ばない。
- EntityはDB表現に集中させ、画面表示用の整形はUseCaseまたはUiState変換で行う。
- Repositoryは機械的なinterface/impl分離をしない。外部SDK境界や端末機能境界など、明確な差し替え理由がある場合のみ抽象化する。
- 複数テーブルを同時更新する操作は `@Transaction` または `RoomDatabase.withTransaction` で扱う。
- 単一DAOで完結し、Fake DAOでもRepository/UseCase単体テストをしやすい処理は、`@Transaction`付きDAOデフォルトメソッドを優先する。
- 複数DAO横断やDB全体のオーケストレーションが必要な場合に限り、Repositoryから `RoomDatabase.withTransaction` を使う。

## ArticleDraft / ArticleSection ルール
- `ArticleDraft.phase` は `phase1` または `phase2` とし、`phase2` から `phase1` へ戻さない。
- 目次案採用前のLLM提案は永続化しない。採用時だけArticleDraftのタイトルとArticleSectionを保存し、phaseを `phase2` にする。
- `ArticleSection.orderIndex` は記事内の表示順の唯一の基準とし、並び替え時は同一transactionで整合させる。
- `ArticleSection.content` は保存済み本文、`draftContent` は編集中本文として扱う。
- 自動保存は `draftContent` のみを更新する。`content` は保存操作を表すUseCase経由でのみ更新する。
- 保存済みに戻す操作では `draftContent` を `content` で上書きし、`content` は変更しない。

## Migration ルール
- schema変更時はRoom migrationを追加し、既存データの `content` / `draftContent` / `userApproved` の意味を壊さない。
- destructive migrationはMVP開発中でも原則使わない。必要な場合はユーザーに確認する。
- enum相当の値はDB保存文字列を安定させ、表示文言と分離する。

## テストルール
- phase遷移、章節並び替え、本文保存、自動保存、Markdown出力条件に影響するRepository/UseCaseロジックは単体テストを書く。
- Phase1からPhase2へ遷移する採用系処理は、採用前提案を永続化しないこと、採用後の `ArticleSection` 初期値（`content` / `draftContent` / `userApproved` / `orderIndex` など）を固定して検証する。
- DAOの複雑なクエリやtransaction更新は、Roomのテストで順序と更新対象を検証する。

## 完了チェック
- UI層がDAOやRoom Entityへ直接依存していない。
- `content` と `draftContent` の更新経路が分かれている。
- `phase2` から `phase1` へ戻る経路がない。
- 章節順は `orderIndex` で安定している。
- schema変更がある場合、migrationとテストが追加されている。
