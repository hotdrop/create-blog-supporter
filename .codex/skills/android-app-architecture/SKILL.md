---
name: android-app-architecture
description: Use when implementing or modifying this Kotlin Android app's architecture, Hilt dependency injection, MVVM boundaries, ViewModels, UseCases, repositories, error handling, unit tests, or verification commands.
---

# Android App Architecture Skill

## 固定スタック
- Architecture: MVVM
- UI: Jetpack Compose + Material 3
- Language: Kotlin
- Async: Kotlin Coroutines + Flow
- State: `ViewModel` + `UiState`
- DI: Hilt
- Persistence: Room
- Navigation: Navigation Compose

## 実装方針
- UIはJetpack Composeで実装し、LiveDataは使わない。
- UI、ViewModel、UseCase、Repository、DAOの責務を分け、UIからRepositoryやDAOを直接呼ばない。
- 1ファイル、1クラス、1関数を肥大化させず、責務ごとに分割する。
- Repositoryを機械的にinterface/impl分離しない。
- 外部SDK境界、端末機能境界、テスト差し替えが必要な境界では、目的を明確にした抽象化を許可する。
- テスト容易性は原則としてDIと責務分割で担保する。
- 例外は握りつぶさず、ユーザー向け表示と内部ログを分離する。

## テスト方針
- ViewレイヤーはPreviewを必須にし、Viewのテストコードは原則作らない。
- ViewModelのテストコードは原則作らず、複雑なロジックはUseCase、Repository、client境界へ寄せてテストする。
- Model、UseCase、Repositoryなどで複雑なロジックがある場合は単体テストを書く。
- 同一variantを触る `compileDebugKotlin` と `testDebugUnitTest` は、Kotlin増分コンパイルキャッシュ競合を避けるため直列実行する。
- 実装後は既存ユニットテストを実施する。

## 完了チェック
- 固定スタックから外れていない。
- UI層が永続化詳細、外部SDK詳細、保存済み本文更新詳細を直接扱っていない。
- 不要なinterface/impl分離が増えていない。
- 例外のユーザー表示とログが分離されている。
- 変更リスクに見合うテストまたはPreviewが追加・更新されている。
- `compileDebugKotlin` と `testDebugUnitTest` を実行する場合は直列で実行している。
