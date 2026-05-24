---
name: android-coroutines-flow
description: This app utilizes Kotlin Coroutines/Flow implementation skills. These skills are used when designing and modifying concurrent processing in ViewModels, Flow subscriptions, job control, cancellation safety, dispatcher switching, and asynchronous testing.
---

# Android Coroutines Flow Skill

## 実装方針
- 構造化並行性を守り、`viewModelScope` 外で無秩序にコルーチンを起動しない。
- UI からの非同期呼び出しは ViewModel に集約し、Composable 側は状態購読に専念させる。
- 重い処理は `dispatcherIO {}` か `Dispatchers.IO` に切り替える。
- `CancellationException` は握りつぶさず再送出する。

## Flow ルール
- 永続層の `Flow` は `map` / `catch` で UI が扱える型へ変換する。
- 例外発生時は `AppResult.Failure` 等へ変換し、購読を不必要に止めない。
- UI 側の購読は `collectAsStateWithLifecycle()` を使う。
- 多重起動を避ける必要がある処理は `Job` を保持してガードする。

## 並行処理ルール
- 画面初期化・同期処理・保存処理など、競合しうるジョブを明示的に分離する。
- 同時実行不可の処理には「実行中なら return」のガードを入れる。
- 共有状態更新は `_uiState.update` に寄せて一貫性を保つ。

## テストルール
- ViewModelのテストは原則作成しない。複雑な非同期ロジックはUseCase、Repository、LLM clientへ寄せてテスト可能にする。
- 複雑な非同期ロジックでは、成功・失敗・キャンセルの分岐を少なくとも1つずつテストする。
- UseCase、Repository、LLM clientのFlowは、初期値、更新値、エラー値の遷移を検証する。

## 完了チェック
- 無制御なコルーチン起動がない。
- キャンセルと例外が正しく扱われる。
- Job 競合が制御される。
- 複雑な非同期分岐がUseCase、Repository、LLM clientにある場合、該当レイヤーのテストが追加または更新される。
