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

# 2026/5/24 20:08 フィードバック

## 作業内容
- Task7として、LiteRT-LM SDKをdata層のclient実装へ接続し、UseCaseからアプリ独自型経由で呼び出す構成へ差し替えた。
- Engine再利用、Flowストリーミング、キャンセル再送出、GPU初期化失敗時のCPU fallback、モデル不在・初期化失敗・生成失敗・パース失敗の変換を追加した。
- Fake clientを使うUseCaseテストへ更新し、`./gradlew :app:compileDebugKotlin`、`./gradlew :app:testDebugUnitTest`、`./gradlew :app:assembleDebug` で検証した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: モデルファイルの配置場所とユーザー導線がTask7本文だけでは未定義だったため、実装では `filesDir/models/blog-supporter.litertlm` の規約に限定した。
- 改善した方がよいルール・手順: LiteRT-LMモデルの配置パス、初回セットアップ手順、モデル未配置時のUI文言をタスクまたはREADMEに明記した方がよい。
- 追加した方がよいルール・手順: LLM応答の構造化フォーマットはプロンプトとパーサーが強く結合するため、今後のUI連携前にパース失敗時の再試行やフォールバック方針を決めておくとよい。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: `docs/LocalLLMSample/` を参照した実装ではCPU fallbackとnative library宣言が必要になるため、ローカルLLMセットアップメモとして残す価値がある。

## 分類
- タスク固有: Task7ではSDK接続とUseCase差し替えに限定し、モデルダウンロードや外部連携、設定画面追加はMVP非スコープとして扱った。
- 恒久対応候補: LiteRT-LMモデル配置規約とエラー表示方針は今後のLLM UI実装でも再利用されるため、恒久的な開発メモ候補。

## 更新先候補
- AGENTS.md: モデル配置規約を恒久ルールにするなら追記候補。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。
- docs/README/task/tests など: READMEまたはdocsに `filesDir/models/blog-supporter.litertlm` の配置手順、モデル未配置時の期待動作、LLM応答フォーマットを追記するとよい。

# 2026/5/29 12:30 フィードバック

## 作業内容
- アプリ全体を端末設定に関係なくダークテーマ固定にした。
- ComposeのColorScheme、起動時Windowテーマ、StatusBar/NavigationBarのSystem UIスタイルを暗色前提へ揃えた。
- `./gradlew :app:compileDebugKotlin`、`./gradlew :app:testDebugUnitTest`、`./gradlew :app:assembleDebug` で検証した。

## 開発改善フィードバック
- 既存ルール・手順が障壁になった点: ダーク固定時のSystem Bar方針が既存設計に明記されておらず、ComposeテーマとXMLテーマの両方を確認して判断する必要があった。
- 改善した方がよいルール・手順: UIテーマ変更では、Compose ColorSchemeと起動直後のXMLテーマ、System Barアイコン明暗を同時に確認する手順を残すとよい。
- 追加した方がよいルール・手順: 個人利用アプリとしてライト/ダーク追従を不要にする場合は、端末設定に追従しない固定テーマであることをREADMEや設計メモに明記しておくとよい。
- docs/README/タスクメモ/テストなどへ反映した方がよい点: 実機確認項目として「ライトモード端末でもアプリは暗色」「StatusBar/NavigationBarのアイコンが見える」をUI検証メモに追加する価値がある。

## 分類
- タスク固有: 今回は保存ロジックや画面構造を変更せず、テーマとSystem Bar表示だけに限定した。
- 恒久対応候補: このアプリをダーク固定で運用する方針は、今後のUI追加時にも色選定やPreview確認の前提になる。

## 更新先候補
- AGENTS.md: ダーク固定を恒久ルールにするなら追記候補。
- .codex/skills/md-doc-viewer/SKILL.md: 該当なし。
- docs/README/task/tests など: READMEまたはUI検証メモにSystem Bar表示確認の観点を追記するとよい。
