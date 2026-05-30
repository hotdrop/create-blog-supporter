---
name: android-compose-ui
description: This app's Jetpack Compose UI implementation and modification skills are used for adding/modifying screens, separating Routes and Screens, creating previews, creating common UI components, addressing accessibility, and integrating with Navigation Compose.
---

# Android Compose UI Skill

## 実装方針
- Route と Screen を分離する。Route で `collectAsStateWithLifecycle()` とイベント接続を行い、Screen は純粋な描画関数にする。
- `@Composable` に副作用を書かない。副作用は Route 側で `LaunchedEffect` に閉じる。
- 状態は必ず上位から渡し、Screen 内で ViewModel を参照しない。
- 文字列・色はリソースかテーマに置き、直書きを避ける。寸法はテーマ・定数化を優先しつつ、画面固有の小さな `dp` は許容する。

## Preview ルール
- 画面 Composable を追加・更新したら必ず `@Preview` を追加する。
- 複数画面または複数状態を追加するタスクでは、実装前に必須 Preview 状態と主要 `testTag` をタスク計画に列挙する。
- Preview は Screen を直接描画し、ViewModel/Hilt/Navigation に依存させない。
- 条件分岐で表示される UI をすべて個別 Preview で確認可能にする。
- 最低限、通常・ローディング・エラー・空状態・ダイアログ表示を分けて Preview する。
- `UiState` の表示に影響する状態値（例: `isSaving`, `messageResId`, `errorMessageResId`, 空リスト）は個別 Preview で網羅する。
- 「1つだけ動く Preview」を禁止し、画面で取りうる主要状態を再現できる Preview セットにする。
- UI追加・変更時は、ダーク固定前提、System Bar視認性、長時間Loading、中断導線、入力中のちらつき、状態チップの視認性を必要に応じて確認する。

## UI 品質ルール
- アプリは端末設定に追従しないダークテーマ固定として扱う。
- UIテーマ変更時はCompose `ColorScheme`、起動直後のXMLテーマ、StatusBar/NavigationBarアイコン明暗を合わせて確認する。
- 重要操作に `testTag` を付け、将来のUI検証やアクセシビリティ確認で識別できるようにする。
- `testTag` は表示文言やリソース名に依存させず、ユーザー操作・検証対象として安定した意味の名前にする。
- `testTag` の形式は `<screen>.<element>` を基本とし、状態やダイアログ内要素は `<screen>.<area>.<element>` とする。
- `testTag` の例: `articleList.createButton`, `phase1Edit.generateOutlineButton`, `outlineProposal.adoptButton`。
- クリック可能要素には `contentDescription` と適切な Semantics を付与する。
- 重要な確認状態は文字列だけに頼らず、MaterialThemeの意味色、アイコン、枠線など複数の視覚手がかりで示す。
- 高頻度更新される内部状態を、本文入力欄近くのレイアウト増減に直結させない。
- 自動保存の成功通知は常時表示せず、失敗時や明示保存時のメッセージを優先する。
- 文字数などの執筆支援メトリクスを表示する場合は、保存済み本文、編集中本文、Markdown出力対象の違いが混同されない文言と配置にする。
- 保存済み本文由来の文字数と編集中本文由来の文字数は、`savedCharacterCount` などのUiState名と表示文言で意味を明確に分ける。
- 目安値や上限警告のメトリクスは、明示要件がない限り保存・確認・出力をブロックする制約として表現しない。
- 長文プレビューは編集フォームにインライン常設せず、保存済み本文確認用の独立画面または明示的な表示切り替えにする。
- 再利用可能な部品は `ui/components/` に抽出し、画面固有ロジックを混ぜない。
- レイアウト肥大化時は private Composable を分割し、1ファイルの責務を絞る。

## UI 変更・削除時の確認
- UI機能を削除または変更する時は、表示文言、文字列リソース、`testTag`、Routeコールバック、UiState、ViewModelイベント、Previewを同じ機能名で横断検索する。
- 機能削除後は、英語名、日本語ラベル、`testTag` の3種類で `rg` 検索し、表示だけでなく派生状態やPreviewも残っていないことを確認する。
- ユーザーの画面呼称とコード上のScreen名がずれる場合は、表示文言や `AssistChip` などのUI部品名でも検索して対象を確定する。
- 章節カード、状態チップ、文字数表示を変更する時は、ArticleEditor、OutlineEdit、SectionEditor、ArticleListの関連表示を横断確認する。
- 状態チップの色分けやアイコン変更では、永続状態を増やさず既存の派生表示に閉じられるかを先に確認する。

## 完了チェック
- Route/Screen 分離が保たれている。
- 複数画面・複数状態を扱う場合、計画時に列挙した必須 Preview 状態と主要 `testTag` が実装に反映されている。
- 条件分岐 UI を含む Preview が揃っている。
- 文字列・色の直書きがなく、寸法はテーマ・定数・画面固有値のいずれかとして意図が明確である。
- Preview が「通常・ローディング・エラー・空状態 + 画面固有状態」を網羅している。
- ダーク固定前提の表示、System Bar、入力中のレイアウト安定性、確認状態の視認性が変更内容に応じて確認されている。
- UI削除・状態表示変更では、関連する文言、`testTag`、UiState、ViewModelイベント、Previewの残存確認が済んでいる。
