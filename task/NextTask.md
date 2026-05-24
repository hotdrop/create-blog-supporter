# 次のタスク

`docs/DesignDocument.md` のMVPを一度に実装せず、意味のある縦切りで進める。ローカルLLMの具体的な実装は最後に回し、それまではFake実装または未実装表示でUI/UseCase境界を先に固める。

## 完了した実装範囲

- [x] **Task 1: アプリ基盤 + Phase1記事メモ作成**
  - Hilt Application / MainActivity / Navigation Compose の起動基盤を作る。
  - Roomで `ArticleDraft` / `ArticleSection` の永続化基盤を作る。
  - 記事一覧画面と新規記事作成画面を実装する。
  - Phase1記事として「書いてみたい題材」「詳細」を保存・一覧表示できるようにする。
  - 既存のPhase1記事メモを開いて編集できるようにする。
  - 「目次案の生成」はまだLLM実行せず、後続タスクの未実装表示に留める。
- [x] **Task 2: 目次案選択フローのスタブ実装**
  - LiteRT-LM本体なしで、固定または疑似データのタイトル案・目次案を表示する。
  - 採用時のみ `phase2` へ遷移し、タイトルと章節を保存する。
  - キャンセル時は提案を永続化しない。
- [x] **Task 3: Phase2記事編集 + 目次編集**
  - 記事編集画面、目次編集画面、全文プレビューを作る。
  - 一覧でPhase2記事を開いた場合は記事編集画面へ遷移し、Phase1記事は既存のメモ編集導線を維持する。
  - 章節の追加・削除・並び替え・見出し変更を実装する。
  - `orderIndex` を唯一の表示順として扱う。
  - 保存済み本文 `content` と編集中本文 `draftContent` の分離を崩さない。
- [x] **Task 4: 章節本文編集**
  - `content` と `draftContent` を明確に分離した章節編集画面を作る。
  - 自動保存は `draftContent` のみに行う。
  - 保存ボタン経由でのみ `content` を更新する。
  - 保存済みに戻す操作と簡易比較表示を実装する。
- [x] **Task 5: Markdown出力**
  - タイトル入力済み、`phase2`、全章節 `userApproved=true` の場合のみ出力可能にする。
  - 出力本文には `content` のみを使い、`draftContent` は混ぜない。
  - Markdown文字列生成とファイル書き込みを分離する。
- [x] **Task 6: LLM支援機能の境界実装**
  - UI/ViewModelからLiteRT-LM SDK型を隠すアプリ独自の request/result 型を定義する。
  - タイトル案、目次案、章節概要、改善提案、誤字脱字チェックのUseCaseを作る。
  - この時点ではFake実装でよい。

## 未着手のタスク
- [ ] **Task 7: LiteRT-LM実装**
  - `docs/LocalLLMSample/` を参照してLiteRT-LM本体を接続する。
  - Engine初期化、Flowストリーミング、キャンセル、CPU fallback、エラー変換を実装する。

## 実装ルール

- `AGENTS.md` の恒久ルールを `docs/DesignDocument.md` より優先する。
- `content` は保存操作を表すUseCase経由でのみ更新し、UIやDAOから直接更新しない。
- MVP非スコープの外部API、クラウド同期、認証、Git連携、画像生成は実装しない。
- Repositoryは機械的なinterface/impl分離をしない。UIからDAOを直接呼ばない。
