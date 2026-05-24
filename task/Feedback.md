# 2026/5/24 18:01 フィードバック

## 作業内容
- MVPを縦切りにする計画を `task/NextTask.md` に整理し、Task 1としてアプリ起動基盤、Room永続化基盤、記事一覧、Phase1記事メモ作成・編集を実装した。
- Compose画面はRoute/Screen分離とPreview追加を前提に構成した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: `docs/DesignDocument.md` のLiteRT-LMサンプルパス表記と `AGENTS.md` の恒久ルールに差があるため、LLM実装時は `docs/LocalLLMSample/` を優先する旨をタスクにも残しておくと迷いにくい。
- 改善した方がよいルール・手順: RoomのローカルJVMテストを増やすにはRobolectricまたはRoom testing依存の方針が必要になるため、永続化テスト方針を早めに決めると後続タスクが進めやすい。
- 追加した方がよいルール・手順: 画面の `testTag` 命名規則を決めておくと、後続のUI検証でセレクタが揺れにくい。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: `README.md` にTask 1時点でできること、未実装の「目次案の生成」が後続タスクであることを短く追記すると試用時に分かりやすい。

## 分類
- タスク固有: Room永続化テスト方針、Task 1時点のREADME説明。
- 恒久対応候補: `testTag` 命名規則、LLMサンプル参照パスの表記統一。

## 更新先候補
- AGENTS.md: `docs/LocalLLMSample/` 表記は既に恒久ルールにあるため追加不要。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。
- docs/README/task/tests など: READMEへの現状説明、後続タスクでRoomテスト依存を追加するかの判断メモ。
# 2026/5/24 18:21 フィードバック

## 作業内容
- Task 2として、LiteRT-LM本体なしのタイトル案・目次案選択フローを実装した。
- 採用時のみPhase2へ遷移してタイトルと章節を保存し、キャンセル時は提案を永続化しない構成にした。
- Repository/DAOの採用処理と固定提案生成のユニットテストを追加した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: Roomのトランザクション処理をRepositoryの`withTransaction`に置くとJVMユニットテストで検証しづらかったため、DAOの`@Transaction`付きデフォルトメソッドへ寄せる必要があった。
- 改善した方がよいルール・手順: 複数テーブル更新をRepositoryとDAOのどちらに置くか、テスト容易性を含めた推奨パターンをRoomスキルに明記すると迷いが減る。
- 追加した方がよいルール・手順: Phase1からPhase2へ遷移する採用系UseCaseでは、採用前提案を永続化しないことと、採用後の初期`ArticleSection`フィールド値をテストで固定するルールを追加するとよい。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: Task 3ではPhase2記事を一覧から開いた時の遷移先を記事編集画面に切り替える必要があるため、Task 3の受け入れ条件へ明記するとよい。

## 分類
- タスク固有: Task 2の採用処理、固定提案、採用後遷移、初期章節保存ルール。
- 恒久対応候補: Roomトランザクション配置とテスト方針、Phase遷移UseCaseのテスト観点。

## 更新先候補
- AGENTS.md: 変更不要。恒久ルールとして必要な`content`/`draftContent`分離とPhase遷移禁止は既に記載済み。
- .codex/skills/android-room-persistence/SKILL.md: DAO `@Transaction`デフォルトメソッドとRepository `withTransaction`の使い分けを追記候補。
- docs/README/task/tests など: `task/NextTask.md`のTask 3にPhase2記事を開く導線の扱いを追記候補。
