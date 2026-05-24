---
name: markdown-export
description: Use when implementing or modifying Markdown export, export validation, file output, and article-to-Markdown transformation.
---

# Markdown Export Skill

## 実装方針
- Markdown出力はUseCaseとして実装し、UIは出力可否と結果表示だけを扱う。
- 出力対象はユーザーが確認済みの保存済み本文であり、未保存の `draftContent` は出力しない。
- ファイル出力先は端末内ストレージとし、Androidの権限やStorage Access Frameworkが必要な場合はUI層に端末API詳細を漏らさない。

## 出力条件
- 記事タイトルが入力済みであること。
- 対象記事が `phase2` であること。
- 全章節が `userApproved=true` であること。
- 章節が `orderIndex` で安定して取得できること。
- 条件を満たさない場合は、どの条件が不足しているかをユーザー向けメッセージに変換する。

## Markdown生成ルール
- 先頭に記事タイトルをH1として出力する。
- 章節は `orderIndex` 昇順で結合する。
- 章節見出しと本文には `content` のみを使う。
- 空の `draftContent` や未保存変更は出力に含めない。
- Markdown文字列生成とファイル書き込みを分け、文字列生成ロジックは単体テスト可能にする。

## 失敗時の扱い
- ファイル作成失敗、書き込み失敗、権限不足、出力条件不足を区別する。
- 例外詳細はログに残し、UIにはユーザーが次に取れる行動が分かる文言を返す。
- 出力成功時は `exportedAt` や記事ステータス更新を同一UseCase内で扱い、必要なDB更新はtransactionで行う。

## テストルール
- タイトルなし、phase不一致、未確認章節あり、章節順、`draftContent` 混入なし、正常出力をテストする。
- ファイル書き込みは境界を差し替え、Markdown文字列生成は純粋関数として検証する。

## 完了チェック
- `draftContent` がMarkdownに混入しない。
- 出力条件不足がユーザー向けに説明される。
- 章節順が `orderIndex` で決まっている。
- Markdown生成とファイル書き込みが分離されている。
