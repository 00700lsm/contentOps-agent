# TASKS

## Current Phase

```text
Phase 5
Ranking 품질 분석
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 5를 따른다.

---

## 목표

정답 문서는 검색되지만 **충분히 높은 순위에 위치하지 않는 문제가 실제로 존재하는지** 확인한다.

Reranker를 무조건 넣는 Phase가 아니다.

현재 Ranking이 충분하면 별도의 Ranking 기술을 추가하지 않는다.

---

## 이 Phase에서 하지 않는 것

```text
Keyword Search
Hybrid Search
RRF
Reranker 구현
Embedding Model 변경
LLM Model 변경
Evaluation Dataset 변경
Prompt 정책 변경
Chunking 전략 변경
```

문서 기준 Ranking Failure가 확인되기 전에 Reranker를 넣지 않는다.

Ranking 구조를 바꾸기 전에 Human Gate에서 멈춘다.

---

## 확인할 지표

```text
MRR
Expected Document 평균 Rank
Ranking Failure Query 수
Query별 Rank
```

현재 분류 기준:

```text
Ranking Failure = Expected Document Rank >= 3
```

문서 Hit와 정의 Section Rank를 구분한다.

---

## Tasks

### 1. 현재 Ranking 수치 확인

동일 Dataset / Embedding / Top K / Vector Search 결과를 사용한다.

- [x] MRR을 확인한다.
- [x] Hit Query의 Expected Document 평균 Rank를 확인한다.
- [x] Rank 1 / Rank 2 / Rank >= 3 / Miss 분포를 확인한다.

### 2. 실패 여부 판단

- [x] 문서 기준 Ranking Failure 수를 확인한다.
- [x] Rank 2 Hit와 Section Rank 관찰을 구분한다.
- [x] retrieval-010 Miss는 Retrieval Failure로 유지한다.

### 3. Experiment

- [x] `docs/experiments/005-ranking-quality.md` 작성
- [x] 변경 여부와 이유를 기록한다.
- [x] 문서 기준 Ranking Failure가 없어 Reranker를 추가하지 않는다.
- [x] DESIGN / ADR은 변경하지 않는다.

---

## 완료 조건

```text
[x] Ranking Failure를 수치로 확인했다.
[x] MRR과 Query별 Rank를 분석했다.
[x] 문제가 없어 불필요한 Ranking 기술을 추가하지 않았다.
[x] 동일 Dataset 평가 결과로 확인했다.
[x] 현재 Retrieval Latency를 기록했다. Ranking 기술을 넣지 않아 Trade-off 비교는 하지 않았다.
[x] 구조가 변경되지 않았으므로 DESIGN은 그대로 둔다.
```

기본 전략을 바꾸기 전에는 ADR을 작성하지 않는다.
