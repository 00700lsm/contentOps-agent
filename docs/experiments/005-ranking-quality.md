# Experiment 005 - Ranking Quality

## 목적

정답 문서는 검색되지만 **순위가 낮아서** Context 앞쪽에 오지 않는 문제가 실제로 있는지 확인한다.

Reranker, RRF, Top N → Final Top K를 기본 전략으로 넣지 않는다.

먼저 현재 Vector Search Ranking이 충분한지 본다.

---

## 현재 상태

```text
Retrieval           Vector Search Only
Chunking            Fixed Size 500 / 50
Embedding Model     nomic-embed-text
Chat Model          llama3.2
Top K               5
```

측정 출처:

```text
evaluation/results/retrieval-20260815-130609.json
```

Phase 3 / Phase 4와 같은 평가다. Retrieval 파이프라인은 바꾸지 않았다.

전체 지표:

```text
Hit Rate@K              0.9
Recall@K                0.9
MRR                     0.75
avg Retrieval Latency   37.2 ms
avg LLM Latency         2011.7 ms
avg End-to-End Latency  2135.2 ms
```

Ranking Failure 분류 기준은 코드와 같다.

```text
Expected Document Rank >= 3
```

구현: `FailureClassifier.RANKING_FAILURE_MIN_RANK = 3`

---

## 문제

Top K에 정답 문서가 있어도 Rank가 낮으면 LLM Context 앞쪽이 다른 문서가 될 수 있다.

ROADMAP 예:

```text
Expected  age-rating-policy.md
Actual    Rank 4
```

이 Dataset에서 그런 문서 단위 Ranking Failure가 있는지 확인한다.

---

## 가설

```text
가설 1
Expected Document Rank가 3 이상으로 밀린다.
→ Reranker 또는 다른 Ranking 구조를 검토할 근거가 된다.

가설 2
Hit Query의 정답 문서는 Rank 1 또는 Rank 2다.
→ 문서 기준 Ranking은 충분하다.
→ Ranking 기술을 억지로 넣지 않는다.
```

---

## Dataset

```text
Document Dataset     data/documents
Evaluation Dataset   evaluation/datasets/retrieval.jsonl
Embedding Model      nomic-embed-text
Chat Model           llama3.2
Top K                5
Retrieval            Vector Search Only
```

Retrieval 구조를 바꾸지 않는다.

Evaluation Dataset도 바꾸지 않는다.

---

## Metric

Scored Query 10개.

```text
Hit Rate@K                         0.9
MRR                                0.75
Hit Query 평균 Expected Doc Rank   1.33
문서 기준 Ranking Failure          0
```

Rank 분포:

| Rank | Query 수 | 설명 |
| --- | --- | --- |
| 1 | 6 | retrieval-001, 003, 004, 005, 007, 008 |
| 2 | 3 | retrieval-002, 006, 009 |
| >= 3 | 0 | 문서 기준 Ranking Failure 없음 |
| Miss | 1 | retrieval-010, Retrieval Failure |

카테고리별 MRR:

| Category | Hit Rate@K | MRR | Ranking Failure |
| --- | --- | --- | --- |
| SEMANTIC | 1.000 | 0.833 | 0 |
| EXACT_KEYWORD | 1.000 | 0.875 | 0 |
| SIMILAR_DOCUMENT | 0.667 | 0.500 | 0 |

Similar Document MRR이 낮은 이유는 Rank가 밀려서가 아니라 `retrieval-010` Miss다.

---

## Query별 Rank

| ID | Category | Expected | Document Rank | Hit | primaryFailureType |
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
| retrieval-010 | SIMILAR_DOCUMENT | operations-faq.md | 없음 | false | RETRIEVAL |

`primaryFailureType = RANKING`인 Query는 없다.

---

## Rank 2 Hit

문서가 Rank 2여도 Top K Context에는 들어간다.

### retrieval-002

```text
1. publishing-guide.md / 공개 가이드
2. content-status-policy.md / 콘텐츠 상태 정책
```

정답 문서는 Rank 2다. Ranking Failure 기준(>= 3)에는 안 들어간다.

답이 틀렸다면 Generation / Grounding 쪽으로 본다.

### retrieval-006

```text
1. youth-protection-policy.md / 4. 운영 문의 대응
2. content-status-policy.md / 2. CONTENT_BLOCKED
3. content-status-policy.md / 2.1 사용 시점
```

정답 문서와 정의 Section은 Rank 2다. Top K에 있다.

### retrieval-009

```text
1. age-rating-policy.md / 1. 목적
2. publishing-guide.md / 공개 가이드
```

정답 문서는 Rank 2다. Top K에 있다.

---

## Section Ranking 관찰

문서 Rank와 정의 Section Rank는 다를 수 있다.

| Query | 문서 Rank | 정의 Section | Section Rank | Top K 포함 |
| --- | --- | --- | --- | --- |
| retrieval-004 | 1 | 2. M-03 오류 | 3 | 예 |
| retrieval-007 | 1 | 4. AGE_REVIEW_REQUIRED | 3 | 예 |

같은 문서의 다른 Section이 정의보다 앞에 온다.

정의 Section은 그래도 Top K에 있다. LLM Context에는 전달된다.

이것은 문서 기준 Ranking Failure가 아니다.

Evaluation Dataset Expected는 문서 이름이다.

Section Rank를 Ranking Failure로 바꾸면 평가 기준을 바꾸는 것이다. Human Gate 없이 하지 않는다.

---

## retrieval-010

```text
Expected  operations-faq.md
Actual    Top K에 없음
```

이것은 Ranking Failure가 아니라 Retrieval Failure다.

현재 Top K = 5 안에서 순서를 바꿔도 정답 문서는 올라오지 않는다.

Top N을 넓힌 뒤 Rerank하는 구조는 별도 후보다.

이 측정만으로 그 구조가 필요하다고 보지 않는다. 정답 문서가 K 바로 밖에 있는지도 이 결과에는 없다.

---

## 판단

```text
문서 기준 Ranking Failure  0건
Hit Query 평균 Rank        1.33
가장 낮은 Hit Rank         2
```

가설 1은 이 Dataset에서 성립하지 않는다.

가설 2가 맞다.

현재 Ranking이 ROADMAP 예(정답 문서 Rank 4)처럼 낮지 않다.

정의 Section Rank 3은 관찰로 남긴다. Context에는 이미 들어가 있으므로 Phase 6 Generation에서 본다.

---

## 하지 않은 것

다음을 구현하지 않았다.

```text
Reranker
RRF
Fusion Score 조정
Top N → Final Top K
```

문서 기준 Ranking Failure가 없기 때문이다.

후보 Reranker를 붙여 Latency를 재측정하지 않았다.

넣지 않은 기술의 품질/비용 예상을 숫자로 쓰지 않는다.

현재 Retrieval Latency는 평균 37.2 ms다. Ranking 단계를 추가하지 않았으므로 End-to-End에 Ranking Latency는 없다.

---

## 결정

```text
Vector Search Ranking 유지

Reranker 추가하지 않음
RRF 추가하지 않음
Top N → Final Top K 추가하지 않음
```

이유:

```text
문서 기준 Ranking Failure가 0건이다.
Hit Query는 Rank 1 또는 Rank 2다.
문제를 억지로 만들지 않는다.
```

DESIGN과 ADR은 바꾸지 않는다.

Retrieval 파이프라인도 바꾸지 않는다.

---

## 다음으로 넘기는 관찰

Phase 6 Generation / Grounding에서 볼 수 있는 것:

```text
정답 문서가 Rank 1~2인데 Answer가 틀린 Query
정의 Section이 Rank 3이어도 Context에는 있는 Query
retrieval-002, 003, 006, 008, 009
```

Phase 5에서 Prompt를 바꾸지 않는다.
