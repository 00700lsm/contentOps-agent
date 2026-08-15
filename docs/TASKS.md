# TASKS

## Current Phase

```text
Phase 9
최종 Evaluation 및 비교
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 9를 따른다.

---

## 목표

최초 Baseline과 최종 시스템을 같은 Dataset으로 비교한다.

어떤 실패가 남았는지, 어떤 변경이 효과가 있었는지, 어떤 기술은 필요하지 않았는지를 기록한다.

---

## 이 Phase에서 하지 않는 것

```text
Keyword Search
Hybrid Search
RRF
Reranker
Embedding Model 변경
LLM Model 변경
Retrieval Evaluation Dataset 변경
Agent Evaluation Dataset 변경
LangGraph
Multi-Agent
Prompt 정책 변경
```

비교를 위해 새 검색/Agent 기술을 넣지 않는다.

---

## Tasks

### 1. Retrieval 재평가

- [x] Phase 2와 동일한 `retrieval.jsonl`을 다시 실행한다.
- [x] Hit Rate@K / Recall@K / MRR / Latency를 Baseline과 비교한다.
- [x] Query 유형별 결과를 비교한다.

### 2. Generation / Agent 정리

- [x] Generation / Grounding / No Answer를 기존 측정과 Final 스냅샷으로 정리한다.
- [x] Tool Selection 결과를 최종 비교에 포함한다.

### 3. Experiment

- [x] `docs/experiments/009-final-comparison.md` 작성
- [x] 적용하지 않은 기술과 남은 한계를 기록한다.

---

## 완료 조건

```text
[x] Baseline과 Final Retrieval 결과를 비교했다. (Hit/Recall/MRR 0.9 / 0.9 / 0.75 유지)
[x] Query 유형별 결과를 비교했다.
[x] Retrieval 실패 변화가 기록되어 있다. (retrieval-010 miss 유지)
[x] Ranking 결과가 기록되어 있다. (문서 Ranking Failure 0)
[x] Generation / Grounding 결과가 기록되어 있다.
[x] No Answer 결과가 기록되어 있다.
[x] Tool Selection 결과가 기록되어 있다. (집합 Accuracy 0.625)
[x] Agent Workflow의 최종 구조와 이유를 설명할 수 있다. (Simple Tool Calling)
[x] 적용하지 않은 기술과 그 이유를 설명할 수 있다.
[x] 품질과 Latency / 비용 Trade-off를 설명할 수 있다.
[x] 예상과 다른 결과도 그대로 기록했다.
[x] DESIGN이 최종 코드와 일치한다.
[x] README에서 실행 및 Evaluation 방법을 확인할 수 있다.
[x] Final Experiment가 작성되어 있다.
```

새 Retrieval / Agent 기술은 넣지 않았다. ADR은 없다.
