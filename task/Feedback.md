# 2026/5/24 19:58 フィードバック

## 作業内容
- Task6として、LiteRT-LM本体接続前のLLM支援境界をdomain層に追加した。
- 既存の目次案スタブを用途別のFake UseCaseへ置き換え、章節概要・改善提案・誤字脱字チェックのFake UseCaseを追加した。
- 既存ユニットテストを新境界向けに更新し、`./gradlew :app:testDebugUnitTest` で検証した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: Task6の範囲が「境界のみ」か「章節UIまで含む」かはタスク本文だけでは曖昧だったため、事前計画で明確化してから実装する必要があった。
- 改善した方がよいルール・手順: LLM支援機能はUI連携タスクと境界タスクを分ける方針を、NextTaskや設計メモに明記すると実装範囲がぶれにくい。
- 追加した方がよいルール・手順: Task7でSDK接続を行う前に、今回追加したFake UseCaseをどの抽象境界へ差し替えるかを短く設計しておくとよい。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: `docs/DesignDocument.md` のLiteRT-LM表記とTask単位の実装境界は、AGENTS.mdの恒久ルールと同じ表記へ揃える余地がある。

## 分類
- タスク固有: Task6では章節編集画面の新UI追加を行わず、Fake UseCaseと既存目次案フロー差し替えに限定した判断。
- 恒久対応候補: LLM境界実装では、SDK型をUI層へ出さないだけでなく、提案採用時の保存先を `draftContent` に限定する観点をテスト観点として維持する。

## 更新先候補
- AGENTS.md: 既存ルールで十分だが、将来のTask7以降でLLM client抽象の置き場所を恒久化するなら追記候補。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。
- docs/README/task/tests など: `task/NextTask.md` のTask7に「Fake UseCaseをSDK境界へ接続する」旨を追記すると次工程が明確になる。
