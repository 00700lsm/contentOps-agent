# TASKS

## Current Phase

```text
Phase 8
Tool Selection / Agent Workflow 평가
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 8을 따른다.

---

## 목표

질문에 따라 **적절한 Tool을 선택하고, 필요하면 여러 Tool을 조합할 수 있는지** 평가한다.

단순 Tool Calling부터 확인한다.

LangGraph / Multi-Agent는 측정 전에 넣지 않는다.

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
LangGraph
Multi-Agent
```

Agent Workflow를 바꾸기 전에 Human Gate에서 멈춘다.

---

## Tasks

### 1. Simple Tool Calling

- [x] Question API가 Tool을 선택해 호출할 수 있게 한다.
- [x] Policy / Content Search / Content Detail / Multi Tool Dataset을 준비한다.

### 2. Evaluation

- [x] 동일 Agent Dataset으로 Tool Selection을 평가한다.
- [x] Expected / Actual Tool, 불필요 호출, Accuracy를 기록한다.

### 3. Experiment / Human Gate

- [x] `docs/experiments/007-tool-selection.md` 작성
- [x] 단순 Tool Calling의 충분성을 판단했다. Multi Tool과 일부 Search는 부족하다.
- [x] Human Gate에서 후보 A를 선택했다. 단순 Tool Calling을 유지한다.

---

## 완료 조건

```text
[x] Policy Query를 올바른 Tool로 처리할 수 있다. (집합 기준 2/2, 1건 중복 호출)
[x] Content Search Query를 처리할 수 있다. (1건 실행, 1건 미실행)
[x] Content Detail Query를 처리할 수 있다. (2/2 MATCH)
[x] Multi Tool Query를 처리할 수 있다. (상세만 호출, 정책 누락)
[x] Expected / Actual Tool을 비교했다.
[x] Tool Selection Accuracy를 확인할 수 있다. (집합 0.625)
[x] 불필요한 Tool 호출을 분석했다. (중복 호출 2건, Wrong Tool 0)
[x] 단순 Tool Calling의 충분성 여부를 판단했다.
[x] Human Gate에서 단순 Tool Calling 유지를 선택했다.
```

LangGraph / Multi-Agent는 넣지 않았다. DESIGN은 현재 단순 Tool Calling과 일치한다. ADR은 없다.
