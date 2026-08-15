# TASKS

## Current Phase

```text
Phase 4
Exact Keyword Retrieval 분석
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 4를 따른다.

---

## 목표

정확한 문자열이 중요한 Query에서 **Vector Search의 한계가 실제로 존재하는지** 확인한다.

Keyword Search나 Hybrid를 무조건 넣는 Phase가 아니다.

현재 Retriever가 이미 충분하면 새로운 Retrieval 구조를 추가하지 않는다.

---

## 이 Phase에서 하지 않는 것

```text
Keyword Search 구현
Hybrid Search
RRF
Reranker
Embedding Model 변경
LLM Model 변경
Evaluation Dataset 변경
Prompt 정책 변경
Chunking 전략 변경
```

실패가 확인되기 전에 Keyword / Hybrid를 넣지 않는다.

새로운 Retrieval 전략을 도입하기 전에 Human Gate에서 멈춘다.

---

## 확인할 Query

```text
retrieval-004  M-03
retrieval-005  OPS-101
retrieval-006  CONTENT_BLOCKED
retrieval-007  AGE_REVIEW_REQUIRED
```

---

## Tasks

### 1. 현재 Vector Search 성능 확인

동일 Dataset / Embedding / Top K / Vector Search 결과를 사용한다.

- [x] Exact Keyword Query의 Hit Rate / Recall / MRR을 확인한다.
- [x] Query별 Expected Document Rank를 확인한다.
- [x] Semantic / Similar Document Query와 따로 비교한다.

### 2. 실패 여부 판단

- [x] 문서 기준 Top K Miss가 있는지 확인한다.
- [x] 문서 Hit와 정의 Section Rank를 구분한다.
- [x] Vector Search가 Exact Keyword Query를 충분히 처리하는지 판단한다.

### 3. Experiment

- [x] `docs/experiments/004-exact-keyword-retrieval.md` 작성
- [x] 변경 여부와 이유를 기록한다.
- [x] 문서 기준 실패가 없으므로 Keyword / Hybrid를 추가하지 않는다.
- [x] DESIGN / ADR은 변경하지 않는다.

---

## 완료 조건

```text
[x] Exact Keyword Query의 현재 성능을 확인했다.
[x] Vector Search의 실제 실패 여부를 판단했다.
[x] 문서 기준 실패가 없어 후보 구현은 하지 않았다.
[x] 새로운 Retrieval 구조를 추가하지 않기로 했다.
[x] 동일 Dataset 평가 결과로 확인했다.
[x] Semantic / Exact Keyword Query 변화를 각각 확인했다.
[x] 변경 여부와 이유를 Experiment에 기록했다.
[x] 구조가 변경되지 않았으므로 DESIGN은 그대로 둔다.
```

기본 전략을 바꾸기 전에는 ADR을 작성하지 않는다.
