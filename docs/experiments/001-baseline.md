# Experiment 001 - Baseline Evaluation

## 목적

Baseline Vector RAG를 동일한 Evaluation Dataset으로 평가하고, 이후 실험의 비교 기준을 남긴다.

이 실험의 목적은 품질을 높이는 것이 아니다.

현재 조건에서 Metric과 Latency를 측정하는 것이다.

---

## 현재 상태

```text
Question
 ↓
Embedding
 ↓
Vector Search
 ↓
Top K = 5
 ↓
LLM
 ↓
Answer + Source
```

Retrieval 전략은 Vector Search Only다.

Keyword Search, Hybrid Search, RRF, Reranker는 사용하지 않는다.

---

## 문제

아직 문제를 해결하지 않는다.

먼저 Baseline이 어떤 Query에서 성공하고 실패하는지 확인한다.

---

## 가설

이 실험에는 개선 가설이 없다.

측정 가설만 있다.

```text
동일한 retrieval.jsonl로 Hit Rate@K, Recall@K, MRR을 계산할 수 있다.
```

---

## Dataset

```text
Document Dataset
data/documents
6개 Markdown
31 Chunk
```

```text
Evaluation Dataset
evaluation/datasets/retrieval.jsonl
Query 12개
Scored Query 10개
NO_ANSWER Query 2개
```

Category:

```text
SEMANTIC 3
EXACT_KEYWORD 4
SIMILAR_DOCUMENT 3
NO_ANSWER 2
```

---

## 실험 조건

```text
실행 시각                 2026-08-15T12:44:36Z
Embedding Model           nomic-embed-text
Chat Model                llama3.2
Chunk Size                500 tokens
Chunk Overlap             50 tokens
Top K                     5
Retrieval Strategy        Vector Search Only
결과 파일                  evaluation/results/retrieval-20260815-124436.json
```

변경 변수는 없다.

---

## Metric

Scored Query 10개 기준:

```text
Hit Rate@K    0.9
Recall@K      0.9
MRR           0.75
```

Latency 평균:

```text
Embedding     1082.1 ms
Retrieval       39.5 ms
LLM           2168.8 ms
End-to-End    3292.3 ms
```

Token:

```text
Prompt Tokens       7904
Completion Tokens    372
```

---

## 결과

Query별 첫 Expected Document Rank:

| ID | Category | Expected | Rank | Hit | primaryFailureType |
| --- | --- | --- | --- | --- | --- |
| retrieval-001 | SEMANTIC | age-rating-policy.md | 1 | true | NEEDS_GENERATION_REVIEW |
| retrieval-002 | SEMANTIC | content-status-policy.md | 2 | true | NEEDS_GENERATION_REVIEW |
| retrieval-003 | SEMANTIC | metadata-guide.md | 1 | true | NEEDS_GENERATION_REVIEW |
| retrieval-004 | EXACT_KEYWORD | metadata-guide.md | 1 | true | NEEDS_GENERATION_REVIEW |
| retrieval-005 | EXACT_KEYWORD | operations-faq.md | 1 | true | NEEDS_GENERATION_REVIEW |
| retrieval-006 | EXACT_KEYWORD | content-status-policy.md | 2 | true | NEEDS_GENERATION_REVIEW |
| retrieval-007 | EXACT_KEYWORD | age-rating-policy.md | 1 | true | NEEDS_GENERATION_REVIEW |
| retrieval-008 | SIMILAR_DOCUMENT | youth-protection-policy.md, age-rating-policy.md | 1 | true | NEEDS_GENERATION_REVIEW |
| retrieval-009 | SIMILAR_DOCUMENT | publishing-guide.md | 2 | true | NEEDS_GENERATION_REVIEW |
| retrieval-010 | SIMILAR_DOCUMENT | operations-faq.md | miss | false | RETRIEVAL |
| retrieval-011 | NO_ANSWER | 없음 | - | - | NONE |
| retrieval-012 | NO_ANSWER | 없음 | - | - | NONE |

문서 단위 Top K 기준 Ranking Failure(Rank >= 3)는 없었다.

Exact Keyword Query 4개는 모두 정답 문서가 Top 5에 들어왔다.

---

## 실패 Case

실패 유형 상세는 `002-retrieval-failures.md`에 정리한다.

요약:

```text
Retrieval Failure     1건  retrieval-010
Ranking Failure       0건  문서 단위 Rank >= 3 기준
Generation Failure    다수 Answer 품질 문제
Grounding Failure     뚜렷한 사실 조작은 확인되지 않음
No Answer Failure     0건  두 Query 모두 거부
```

---

## 분석

Hit Rate 0.9는 Vector Search가 문서 단위로는 대체로 정답 문서를 찾고 있다는 뜻이다.

다만 다음 한계가 있다.

1. retrieval-010은 비슷한 공개/상태 문서에 밀려 operations-faq.md를 놓쳤다.
2. 정답 문서가 잡혀도 정답 Section이 항상 1위는 아니다. 예: M-03 정의 Section은 Rank 3.
3. LLM 답변은 Retrieval 성공과 별개로 거절하거나 핵심을 빠뜨리는 경우가 있다.
4. Embedding 평균 1082ms, LLM 평균 2169ms는 초기 호출 포함 값이다.

초기 호출:

```text
retrieval-002 Embedding 12529 ms
retrieval-001 LLM       5574 ms
```

이후 Query는 Embedding 33~48ms, Retrieval 35~48ms, LLM 995~2913ms 구간에 있다.

평균만 보면 Cold Start가 크게 섞인다.

---

## 대안

이 단계에서는 기술을 선택하지 않는다.

관찰된 후보만 적는다.

```text
Chunking / Section 단위 검색
→ M-03 정의가 정답 문서 안에서도 Rank 3

유사 문서 혼동
→ retrieval-010, retrieval-009

Generation / Grounding Prompt
→ 정답 Context가 있는데도 거절하거나 핵심을 빠뜨림
```

Keyword Search나 Reranker를 바로 도입하지 않는다.

실제 실패 유형별로 이후 Phase에서 확인한다.

---

## 결정

Baseline 결과를 그대로 유지한다.

실패를 해결하는 코드는 넣지 않는다.

---

## Before / After

비교 대상이 없다.

이 결과가 이후 Experiment의 Before다.

---

## 결론

Baseline Vector RAG는 측정 가능한 상태다.

```text
Hit Rate@K  0.9
Recall@K    0.9
MRR         0.75
```

문서 단위 Retrieval은 Exact Keyword에서도 크게 무너지지 않았다.

더 눈에 띄는 문제는 다음이다.

```text
유사 문서 Retrieval Miss 1건
정답 Section Ranking
Generation이 Retrieval 성공을 따라가지 못하는 경우
```
