# AGENTS.md

Android ネイティブ開発における共通運用ルールを定義する。

## このアプリの目的

このアプリは「AI記事生成アプリ」ではなく、ユーザー自身の言葉でテックブログを書くための執筆支援アプリである。
LLMは本文生成の主体ではなく、構成、提案、校正を支援する補助機能として扱うこと。実装時は以下のルールを必ず守ること。

1. LLMの出力を本文へ自動反映しない。
2. LLM提案を採用する場合も、まず `draftContent` に反映する。
3. ユーザーが保存ボタンを押すまで `content` を変更しない。
4. 章節編集画面では、保存済み本文と編集中本文を明確に分離する。
5. `draftContent` は自動保存してよいが、`content` は明示的な保存操作でのみ更新する。
6. 誤字脱字チェック結果は参考情報であり、ユーザーが最終判断する。
7. Markdown出力では、ユーザー確認済みの章節を重視する。
8. LiteRT-LMへの依存をUI層へ漏らさない。
9. `content` の更新は保存操作を表すUseCase経由に限定し、RepositoryやDAOをUIから直接呼んで確定本文を更新しない。

## MVPスコープ境界
- MVPでは、外部API連携、インターネット検索、クラウド同期、複数端末同期、認証、複数ユーザー対応、画像・イラスト生成、ブログサービスへの直接投稿、Git連携は実装しない。
- MVP非スコープに該当する機能が必要に見える場合は、先にユーザーへ相談し、既存の執筆支援フローを壊さない代替案を提示する。
- `docs/DesignDocument.md` はガイドラインだが、LLM自動反映禁止、`content` / `draftContent` 分離、MVP非スコープは恒久ルールとして扱う。

## 記事フェーズと保存ルール
- `phase1` は題材検討フェーズ、`phase2` は執筆フェーズとする。
- `phase2` に遷移した記事を `phase1` に戻してはならない。
- 目次案採用前のLLM提案は候補表示に留め、キャンセル時に破棄できるよう永続化しない。
- 目次案を採用した場合のみ、タイトルと章節を確定して `phase2` に遷移する。
- `draftContent` は自動保存してよいが、Markdown出力や保存済み本文表示では `content` と混同しない。

## Markdown出力ルール
- Markdown出力は、記事タイトルが入力済みで、全章節が `userApproved=true` の場合のみ許可する。
- 出力本文には章節の `content` のみを使用し、未保存の `draftContent` は混ぜない。
- 出力順は章節の `orderIndex` に従う。
- 出力できない理由はユーザー向けメッセージとして表示し、内部ログや例外詳細とは分離する。

## LiteRT-LM実装ルール
- LiteRT-LM SDK型やEngine管理はdata層またはinfrastructure層に閉じ込める。
- ViewModelとUIはアプリ独自のrequest/result型、UiState、Flowのみを扱う。
- `docs/DesignDocument.md` にある `./LocalLLMSample` は、実体に合わせて `docs/LocalLLMSample/` を参照する。
- 表記は `LiteRT-LM` に統一する。既存資料の `LiteRM` や `LiteRT` 表記は同SDKを指す文脈でも新規記述では使わない。

## 不変アーキテクチャ方針
- アーキテクチャ: MVVM
- UI: Jetpack Compose (Material 3)
- 言語: Kotlin
- 非同期: Kotlin Coroutines + Flow
- 状態管理: `ViewModel` + `UiState`
- DI: Hilt（必須）
- 永続化: Room
- ナビゲーション: Navigation Compose

## 共通実装ガードレール
- UIはJetpack Composeで実装する
- LiveDataの使用は禁止する
- ソフトウェア開発設計の基本原則を守る
  - 1ファイルや1クラス、1関数の肥大化を避け、責務ごとに分割する。
  - 無闇にインタフェースを使わない。たとえばRepositoryにおいて、`UserRepository.kt` -> `UserRepositoryImpl.kt` のような機械的な分離は禁止する。
  - LiteRT-LMなど外部SDK境界、端末機能境界、テスト差し替えが必要な境界では、目的を明確にした抽象化を許可する。
  - テスト容易性は原則としてDIと責務分割で担保し、インタフェース導入は具体的な境界がある場合に限る。
- テスト設計
  - ViewレイヤーはPreviewを必ず全パターン作成することでテスト不要とする。ViewModelもテストコードは無し
  - それ以外のレイヤー(Model, UseCase, Repository等)で複雑なロジックがある場合はテストコードを書く
- 例外は握りつぶさず、ユーザー向け表示とログを分離する
- 実装後、既存のユニットテストは必ず実施する。
