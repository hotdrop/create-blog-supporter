# AGENTS.md

Android ネイティブ開発における共通運用ルールを定義する。
詳細実装手順は `skills/*/SKILL.md` に委譲し、本書は全体統制のみを扱う。

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
  - ただし、無闇にインタフェースを使わない。たとえばRepositoryにおいて、`UserRepository.kt` -> `UserRepositoryImpl.kt`という設計は絶対にしないこと。このような設計や実装の目的でインタフェースを使うことは禁止する。
  - インターフェースではなくDIでテスト容易性を担保すること。
- テスト設計
  - ViewレイヤーはPreviewを必ず全パターン作成することでテスト不要とする。ViewModelもテストコードは無し
  - それ以外のレイヤー(Model, UseCase, Repository等)で複雑なロジックがある場合はテストコードを書く
- 例外は握りつぶさず、ユーザー向け表示とログを分離する
- 実装後、既存のユニットテストは必ず実施する。

## SKILL 用途マトリクス
- UI 実装・改修: `android-compose-ui`
- Coroutines/Flow 設計・改修: `android-coroutines-flow`
- フィードバック: `feedback-loop`
