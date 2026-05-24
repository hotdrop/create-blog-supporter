# 2026/5/24 17:31 フィードバック

## 作業内容
- `AGENTS.md` と `.codex/skills/` を、DesignDocument のMVP制約、LLM境界、Room永続化、Markdown出力方針に合わせて整備した。
- 既存skillのテスト方針とファイル名参照の矛盾を修正し、新規skillを3件追加した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: `task/feedback.md` と `task/Feedback.md` の大文字小文字差、Compose UI Test必須とPreview担保方針の矛盾、Repository interface禁止がLiteRT-LM境界の抽象化と衝突する点があった。
- 改善した方がよいルール・手順: AGENTSとskillのテスト責務を、View/Preview、ViewModel原則テストなし、UseCase/Repository/Modelテストありに明確化した状態を維持する。
- 追加した方がよいルール・手順: Room、LiteRT-LM、Markdown出力のskillを実装開始時に必ず参照する運用を定着させる。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: `docs/DesignDocument.md` の `LiteRM` 表記と `./LocalLLMSample` パスは、将来的に `LiteRT-LM` と `docs/LocalLLMSample/` へ揃えると参照ミスが減る。

## 分類
- タスク固有: ルール・skill整備に伴う矛盾解消と新規skill追加。
- 恒久対応候補: DesignDocument内の表記・パス補正、Gradle/KSP設定衝突の解消。

## 更新先候補
- AGENTS.md: 今回反映済み。
- .codex/skills/: 今回反映済み。
- docs/README/task/tests など: `docs/DesignDocument.md` の表記補正と、`./gradlew :app:testDebugUnitTest` がAGP built-in KotlinとKSP sourceSets衝突で失敗する問題の修正メモを検討する。
