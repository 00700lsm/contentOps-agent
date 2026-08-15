# TASKS

## Current Phase

```text
Phase 2
Baseline Evaluation 및 실패 Case 분류
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 2를 따른다.

---

## 목표

Baseline Vector RAG를 동일한 Evaluation Dataset으로 평가하고 **현재 시스템이 어떤 질문에서 왜 실패하는지 분류한다.**

아직 문제를 해결하지 않는다.

먼저 현재 상태를 이해한다.

---

## 이 Phase에서 하지 않는 것

다음 변경은 하지 않는다.

```text
Keyword Search
Hybrid Search
RRF
Reranker
Chunking 전략 변경
Prompt 정책 변경
Embedding Model 변경
LLM Model 변경
Evaluation Dataset 변경
```

Answer가 틀렸다고 Prompt를 먼저 수정하지 않는다.

실패를 해결하는 코드를 이 Phase Commit에 넣지 않는다.

---

## Tasks

### 1. Baseline Evaluation 실행

- [x] 동일 Dataset `evaluation/datasets/retrieval.jsonl`을 실행한다.
- [x] Hit Rate@K를 기록한다.
- [x] Recall@K를 기록한다.
- [x] MRR을 기록한다.
- [x] Retrieval Latency를 기록한다.
- [x] LLM Latency를 기록한다.
- [x] End-to-End Latency를 기록한다.
- [x] Token 사용량은 확인할 수 있으면 기록하고, 없으면 측정하지 않았다고 남긴다.

---

### 2. Query별 결과 기록

각 Query에 대해 다음을 남긴다.

```text
Question
Expected Document
Retrieved Top K
Expected Document Rank
Answer
Source
Latency
```

- [x] Query별 Ranking을 확인한다.
- [x] Query별 Answer와 Source를 확인한다.

---

### 3. 실패 Case 분류

- [x] Retrieval Failure: Expected Document가 Top K에 없음
- [x] Ranking Failure: Expected Document는 Top K에 있으나 Rank가 3 이상
- [x] Generation Failure: 정답 Context가 전달됐지만 Answer가 틀림
- [x] Grounding Failure: Context에 없는 내용을 생성
- [x] No Answer Failure: 근거가 없는데도 답을 생성
- [x] 주요 실패 Query를 유형별로 정리한다.

---

### 4. Experiment 문서

- [x] `docs/experiments/001-baseline.md`에 조건과 Metric을 기록한다.
- [x] `docs/experiments/002-retrieval-failures.md`에 실패 Query를 유형별로 정리한다.
- [x] 측정하지 않은 숫자를 작성하지 않는다.

---

## 완료 조건

```text
[x] 전체 Evaluation Dataset을 실행했다.
[x] Hit Rate@K를 기록했다.
[x] Recall@K를 기록했다.
[x] MRR을 기록했다.
[x] Query별 검색 Ranking을 확인했다.
[x] 주요 실패 Query를 확인했다.
[x] Retrieval / Ranking / Generation / Grounding 실패를 구분했다.
[x] No Answer Query 결과를 확인했다.
[x] Baseline Latency를 기록했다.
[x] Experiment 문서가 작성되어 있다.
[x] 실패 상태가 Git History에 남아 있다.
[x] 아직 실패를 해결하기 위한 기술을 추가하지 않았다.
```

완료 후 Git checkpoint:

```text
experiment: evaluate baseline retrieval
```

해결 코드를 같은 Commit에 넣지 않는다.

조건을 만족하기 전에 Phase 3로 이동하지 않는다.
