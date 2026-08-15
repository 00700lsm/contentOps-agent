# TASKS

## Current Phase

```text
Phase 3
Chunking 품질 분석
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 3를 따른다.

---

## 목표

현재 Retrieval 실패 중 **문서가 어떻게 나뉘어 있는지가 검색 품질에 영향을 주는지 확인한다.**

Chunking을 무조건 개선하는 Phase가 아니다.

Chunking이 실제 원인인지 검증하는 Phase다.

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
Prompt 정책 변경
```

Baseline Chunking 기본값을 바꾸기 전에 Human Gate에서 멈춘다.

Chunking과 Embedding Model을 동시에 변경하지 않는다.

---

## 확인할 질문

```text
현재 Chunk가 너무 큰가?
관련 없는 내용이 같은 Chunk에 섞이는가?
Chunk가 너무 작아 필요한 문맥이 잘리는가?
문서 Section 구조를 활용하는 편이 나은가?
Overlap이 필요한가?
```

Phase 2에서 특히 볼 Query:

```text
retrieval-004  M-03 정의 Section Rank 3
retrieval-010  operations-faq.md Top K Miss
```

---

## Tasks

### 1. 현재 Chunking 실패 Case 확인

- [x] Baseline Chunk가 Section 단위로 어떻게 나뉘는지 확인한다.
- [x] retrieval-004 / retrieval-010과 Chunk 경계의 관계를 확인한다.

### 2. 대안 비교

동일 조건을 유지한다.

```text
Document Dataset
Evaluation Dataset
Embedding Model
Top K
Vector Search
LLM Model
```

비교 후보:

```text
A. Baseline Fixed Size 500 / 50
B. Section 기반 Chunking
C. 더 작은 Fixed Size 200 / 20
```

- [x] 후보를 Baseline과 비교한다.
- [x] 동일 Dataset으로 재평가한다.

### 3. 측정

- [x] Hit Rate@K / Recall@K / MRR
- [x] Query별 Rank
- [x] Chunk 수
- [x] 평균 Chunk 크기
- [x] Retrieval Latency
- [x] 실패 Query 변화

### 4. Experiment / Human Gate

- [x] `docs/experiments/003-chunking-analysis.md` 작성
- [x] 유지 / 변경 이유를 정리한다.
- [x] Human Gate에서 Baseline Chunking 유지를 선택했다.

---

## 완료 조건

```text
[x] Chunking과 관련된 실패 Case를 확인했다.
[x] 최소 하나 이상의 대안을 현재 Baseline과 비교했다.
[x] 동일 Dataset으로 재평가했다.
[x] Hit Rate / Recall / MRR 변화를 기록했다.
[x] 실패 Query 변화를 확인했다.
[x] 최종 Chunking 유지 / 변경 이유를 설명할 수 있다.
[x] 변경하지 않았으므로 DESIGN은 그대로 둔다.
```

기본 전략을 바꾸기 전에는 ADR을 작성하지 않는다.
