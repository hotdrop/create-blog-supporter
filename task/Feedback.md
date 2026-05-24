# 2026/5/24 18:42 フィードバック

## 作業内容
- Task 3として、Phase2記事編集画面、目次編集画面、Phase判定ルート、章節操作UseCase/DAO transaction、単体テストを追加した。
- 目次案採用後にPhase2記事編集画面へ遷移するようNavigationを更新した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: Task 3の計画で「記事一覧からPhase判定ルートへ遷移」と「採用後は記事編集へ遷移」が明確だったため、大きな障壁はなかった。
- 改善した方がよいルール・手順: ComposeのPreview網羅要件は有効だが、追加画面が多いタスクではPreviewの必須状態をタスク単位で事前に列挙しておくと実装漏れをさらに減らせる。
- 追加した方がよいルール・手順: 章節操作のUseCase結果型とユーザー向けメッセージの対応表をタスクメモに書くと、UI実装時の分岐が安定する。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: Task 4以降では、章節タップから本文編集画面へ進む導線と、Task 3で追加した未実装メッセージを置き換えることを明記するとよい。

## 分類
- タスク固有: Task 4の導線接続、Preview対象状態、UseCase結果と表示メッセージの対応整理。
- 恒久対応候補: 追加画面が複数あるタスクでは、Preview必須状態と主要testTagを計画に含める運用。

## 更新先候補
- AGENTS.md: 恒久ルールの変更は不要。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。
- docs/README/task/tests など: `task/NextTask.md` のTask 4に「記事編集画面の章節タップから章節本文編集画面へ遷移」を追記する候補あり。

# 2026/5/24 19:11 フィードバック

## 作業内容
- Task 4として、章節本文編集画面、章節タップ導線、本文自動保存、確定保存、保存済みへのリセット、確認済み切り替え、簡易比較表示を実装した。
- `content` と `draftContent` の更新経路をUseCase/Repository/DAO transactionで分離し、Repository/UseCase単体テストを追加した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: `content` 更新経路の制限が明確だったため実装判断は安定したが、保存ボタン押下前に保留中の自動保存をどう扱うかは計画段階で明文化されていなかった。
- 改善した方がよいルール・手順: 自動保存つき編集画面では、明示保存時に未反映のローカル入力を先に永続化するかをタスク計画に含めるとよい。
- 追加した方がよいルール・手順: 比較表示の粒度（行単位/段落単位）と、保存後に `userApproved` を解除する条件をAcceptance Criteriaとして固定すると実装漏れを防げる。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: Task 5ではMarkdown出力が `content` のみを読むことを、Task 4で追加した確認済みスイッチと結びつけたUI確認観点として明記するとよい。

## 分類
- タスク固有: Task 5の出力条件確認、章節編集画面からの確認済み状態変更後のMarkdown可否テスト。
- 恒久対応候補: 自動保存画面の明示保存時の競合処理を、Compose/Flow系の実装ルールに短く追加する候補あり。

## 更新先候補
- AGENTS.md: 現時点で恒久ルールの追加は必須ではない。既存の `content` / `draftContent` 分離ルールで今回の実装はカバーできる。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。
- docs/README/task/tests など: `task/NextTask.md` はTask 4完了に更新済み。Task 5着手前にMarkdown出力条件のテスト観点を追記する候補あり。
# 2026/5/24 19:33 フィードバック

## 作業内容
- Task5として、Phase2記事のMarkdown生成、出力条件判定、アプリ専用領域へのファイル書き込み、FileProvider経由の共有シート連携を実装した。
- `content` のみをMarkdownへ含め、`draftContent` を混入させない単体テストと、出力成功時の `status=Exported` / `exportedAt` 更新テストを追加した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: Task5の「ファイル書き込み」は端末から取り出す実用フローまで読むと、保存だけでは不十分で共有シートやSAFの選択が必要だった。
- 改善した方がよいルール・手順: Markdown出力タスクでは「出力先」「共有方法」「ファイル名規則」をAcceptance Criteriaに含めると、実装前の確認が減らせる。
- 追加した方がよいルール・手順: FileProviderを使う場合は、公開パスをexports配下に限定することと、共有Intentに `FLAG_GRANT_READ_URI_PERMISSION` を付与することを手順化するとよい。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: `task/NextTask.md` にはTask5完了後、共有シート方式を採用したことと、Downloads保存/SAFはMVPでは未対応であることを追記するとよい。

## 分類
- タスク固有: Task5のMarkdown出力先と共有シート連携、FileProvider設定、出力条件テスト。
- 恒久対応候補: ファイル共有を伴うAndroid機能では、権限不要の共有シート/SAF/Downloads保存の選定基準をAGENTSまたはAndroid UIスキルに追加する候補あり。

## 更新先候補
- AGENTS.md: 端末外へ成果物を渡す機能では、MVPでもユーザーの取得導線を確認するルールを追加する候補。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。Markdown閲覧ではなくAndroidアプリ内のMarkdown出力実装だったため。
- docs/README/task/tests など: Task5の完了メモ、共有シート方式、FileProvider公開範囲、追加テスト観点を `task/NextTask.md` またはテスト方針メモへ反映する候補。
