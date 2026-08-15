# TASKS

## Current Phase

```text
Phase 6
Generation / Grounding 분석
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 6를 따른다.

---

## 목표

Retrieval이 성공한 이후에도 **LLM이 검색 Context를 올바르게 사용하는지** 별도로 평가한다.

Prompt를 감으로 고치는 Phase가 아니다.

먼저 실패 Case를 기록하고, 의미 있는 Prompt 정책 변경은 Human Gate를 거친다.

---

## 이 Phase에서 하지 않는 것

```text
Keyword Search
Hybrid Search
RRF
Reranker
Embedding Model 변경
LLM Model 변경
Evaluation Dataset 변경
Chunking 전략 변경
Tool 추가
```

Grounding / No Answer 정책을 바꾸는 Prompt 수정은 Human Gate 전에 하지 않는다.

---

## 확인할 Query

정답 Context가 있는데 답이 틀린 것으로 넘긴 Query:

```text
retrieval-002
retrieval-003
retrieval-006
retrieval-008
retrieval-009
```

No Answer:

```text
retrieval-011
retrieval-012
```

---

## Tasks

### 1. Retrieval과 Generation 분리

동일 Dataset / 동일 평가 결과를 사용한다.

- [x] 문서 Hit인데 답이 틀린 Query를 확인한다.
- [x] 정답 Section이 Context에 있는지와 문서 Hit를 구분한다.
- [x] Retrieval Failure인 retrieval-010은 Generation 실패로 넣지 않는다.

### 2. Grounding / No Answer / Source

- [x] Context에 없는 사실을 만들었는지 확인한다.
- [x] No Answer Query 2건을 평가한다.
- [x] 응답 Source가 Retriever Top K인지 확인한다.

### 3. Experiment / Human Gate

- [x] `docs/experiments/006-generation-grounding.md` 작성
- [x] Prompt 후보와 장단점을 정리한다.
- [x] Human Gate에서 후보 B를 선택했다.
- [x] 동일 Dataset으로 재평가했다.

---

## 완료 조건

```text
[x] Retrieval 성공과 Generation 성공을 분리해 평가했다.
[x] Grounding Failure를 확인했다.
[x] No Answer Query를 평가했다.
[x] Source가 실제 답변 근거인지 확인했다.
[x] Prompt 변경 후 동일 Dataset으로 재평가했다.
[x] 변경 전후 결과를 기록했다.
[x] 해결되지 않은 Hallucination / Grounding 한계도 기록했다.
```

Prompt B를 코드와 DESIGN에 반영했다. ADR은 작성하지 않는다. 최종 Retrieval 구조는 그대로다.
