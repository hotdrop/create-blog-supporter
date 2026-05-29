---
name: article-writing-domain
description: Use when implementing or modifying article writing domain behavior in this app, including article phases, outline adoption, content versus draftContent rules, user approval, LLM suggestions, proofreading, consultation prompts, MVP scope boundaries, and saved-content semantics.
---

# Article Writing Domain Skill

## プロダクト原則
- このアプリは「AI記事生成アプリ」ではなく、ユーザー自身の言葉でテックブログを書くための執筆支援アプリである。
- LLMは本文生成の主体ではなく、構成、提案、校正を支援する補助機能として扱う。
- 誤字脱字チェック結果は参考情報であり、ユーザーが最終判断する。
- `docs/DesignDocument.md` はガイドラインだが、LLM自動反映禁止、`content` / `draftContent` 分離、MVP非スコープは恒久ルールとして扱う。

## MVPスコープ境界
- MVPでは、外部API連携、インターネット検索、クラウド同期、複数端末同期、認証、複数ユーザー対応、画像・イラスト生成、ブログサービスへの直接投稿、Git連携は実装しない。
- MVP非スコープに該当する機能が必要に見える場合は、先にユーザーへ相談し、既存の執筆支援フローを壊さない代替案を提示する。
- 端末外へ成果物を渡す機能では、実装前に出力先、共有方法、ファイル名規則、ユーザーの取得導線を確認する。
- 権限や外部連携が増える方式を既定採用せず、既存の執筆支援フローを壊さない取得導線を優先する。

## 記事フェーズ
- `phase1` は題材検討フェーズ、`phase2` は執筆フェーズとする。
- `phase2` に遷移した記事を `phase1` に戻してはならない。
- 目次案採用前のLLM提案は候補表示に留め、キャンセル時に破棄できるよう永続化しない。
- 目次案を採用した場合のみ、タイトルと章節を確定して `phase2` に遷移する。

## 本文保存ルール
- `ArticleSection.content` は保存済み本文、`draftContent` は編集中本文として扱う。
- LLMの出力を `content` へ自動反映しない。
- LLM提案を採用する場合も、まず `draftContent` に反映する。
- `draftContent` は自動保存してよい。
- `content` はユーザーの明示的な保存操作でのみ更新する。
- `content` の更新は保存操作を表すUseCase経由に限定し、RepositoryやDAOをUIから直接呼んで確定本文を更新しない。
- 章節編集画面では、保存済み本文と編集中本文を明確に分離する。
- 執筆中の文字数などの支援メトリクスは `draftContent` 優先で表示してよいが、Markdown出力や保存済み本文として扱ってはならない。
- 文字数目安や上限警告は参考情報であり、明示要件がない限り保存、確認済み、Markdown出力の可否条件にしない。

## LLM相談ルール
- LLM相談機能では、相談文だけを送信しない。
- 相談時は記事タイトル、元メモ、目次構成、現在章、必要最小限の他章文脈を添える。
- 他章本文は丸ごと渡さず、見出し、状態、短い内容メモへ圧縮する。
- LLM相談結果は、ユーザーがコピー・参照できる提案として扱い、本文へ自動反映しない。

## Markdown出力との関係
- Markdown出力では、ユーザー確認済みの章節を重視する。
- Markdown出力は、記事タイトルが入力済みで、全章節が `userApproved=true` の場合のみ許可する。
- 出力本文には章節の `content` のみを使用し、未保存の `draftContent` は混ぜない。
- 出力順は章節の `orderIndex` に従う。
- 出力できない理由はユーザー向けメッセージとして表示し、内部ログや例外詳細とは分離する。

## 完了チェック
- LLM出力が `content` に直接保存されていない。
- `content` と `draftContent` の表示、保存、出力の意味が混同されていない。
- `phase2` から `phase1` へ戻る経路がない。
- 目次案採用前の候補が永続化されていない。
- MVP非スコープや端末外共有に踏み込む場合、実装前にユーザーへ相談している。
