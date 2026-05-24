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

## UI 品質ルール
- 重要操作に `testTag` を付け、将来のUI検証やアクセシビリティ確認で識別できるようにする。
- `testTag` は表示文言やリソース名に依存させず、ユーザー操作・検証対象として安定した意味の名前にする。
- `testTag` の形式は `<screen>.<element>` を基本とし、状態やダイアログ内要素は `<screen>.<area>.<element>` とする。
- `testTag` の例: `articleList.createButton`, `phase1Edit.generateOutlineButton`, `outlineProposal.adoptButton`。
- クリック可能要素には `contentDescription` と適切な Semantics を付与する。
- 再利用可能な部品は `ui/components/` に抽出し、画面固有ロジックを混ぜない。
- レイアウト肥大化時は private Composable を分割し、1ファイルの責務を絞る。

## 完了チェック
- Route/Screen 分離が保たれている。
- 複数画面・複数状態を扱う場合、計画時に列挙した必須 Preview 状態と主要 `testTag` が実装に反映されている。
- 条件分岐 UI を含む Preview が揃っている。
- 文字列・色の直書きがなく、寸法はテーマ・定数・画面固有値のいずれかとして意図が明確である。
- Preview が「通常・ローディング・エラー・空状態 + 画面固有状態」を網羅している。
