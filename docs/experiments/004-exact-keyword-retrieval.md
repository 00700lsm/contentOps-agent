# Experiment 004 - Exact Keyword Retrieval

## 목적

정확한 문자열이 중요한 Query에서 **Vector Search의 한계가 실제로 존재하는지** 확인한다.

Keyword Search, Hybrid, RRF를 기본 전략으로 넣지 않는다.

먼저 현재 Vector Search가 이미 충분한지 본다.

---

## 현재 상태

```text
Retrieval           Vector Search Only
Chunking            Fixed Size 500 / 50
                    Markdown Section을 먼저 나눈 뒤
                    Section 안에서만 Token Window
Embedding Model     nomic-embed-text
Chat Model          llama3.2
Top K               5
```

측정 출처:

```text
evaluation/results/retrieval-20260815-130609.json
```

Phase 3 Baseline과 같은 평가다. Retrieval 파이프라인은 바꾸지 않았다.

전체 지표:

```text
Hit Rate@K  0.9
Recall@K    0.9
MRR         0.75
Chunk 수    31
```

---

## 문제

다음 문자열이 Query에 직접 들어온다.

```text
M-03
OPS-101
CONTENT_BLOCKED
AGE_REVIEW_REQUIRED
```

Vector Search는 의미 유사도로 검색하므로, 정확한 코드/상태값이 밀릴 수 있다는 가설이 있다.

이 가설이 **이 Dataset에서 실제로 성립하는지** 확인한다.

---

## 가설

```text
가설 1
Exact Keyword Query에서 정답 문서가 Top K에 없거나 Rank가 낮다.
→ Keyword Search 또는 Hybrid를 검토할 근거가 된다.

가설 2
정답 문서는 이미 Top K에 들어온다.
→ Vector Search가 Exact Keyword Query를 문서 기준으로 처리한다.
→ Keyword / Hybrid를 억지로 넣지 않는다.
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

## 카테고리별 지표

평가 질문 12개 중 점수는 `answerable=true`이고 Expected Document가 있는 10개다.

| Category | Query 수 | Hit Rate@K | Recall@K | MRR |
| --- | --- | --- | --- | --- |
| SEMANTIC | 3 | 1.000 | 1.000 | 0.833 |
| EXACT_KEYWORD | 4 | 1.000 | 1.000 | 0.875 |
| SIMILAR_DOCUMENT | 3 | 0.667 | 0.667 | 0.500 |
| NO_ANSWER | 2 | 점수 대상 아님 |  |  |

Exact Keyword Query는 4개 모두 문서 기준 Hit다.

전체 Hit Rate 0.9를 떨어뜨리는 Query는 Exact Keyword가 아니라 `retrieval-010` (SIMILAR_DOCUMENT)다.

---

## Exact Keyword Query 결과

| Query | 문자열 | Expected Document | Document Rank | Hit | 정의 Section Rank |
| --- | --- | --- | --- | --- | --- |
| retrieval-004 | M-03 | metadata-guide.md | 1 | true | 3 (`2. M-03 오류`) |
| retrieval-005 | OPS-101 | operations-faq.md | 1 | true | 1 (`2. OPS-101`) |
| retrieval-006 | CONTENT_BLOCKED | content-status-policy.md | 2 | true | 2 (`2. CONTENT_BLOCKED`) |
| retrieval-007 | AGE_REVIEW_REQUIRED | age-rating-policy.md | 1 | true | 3 (`4. AGE_REVIEW_REQUIRED`) |

문서 기준:

```text
M-03                 Rank 1
OPS-101              Rank 1
CONTENT_BLOCKED      Rank 2
AGE_REVIEW_REQUIRED  Rank 1
```

Top K Miss는 없다.

---

## Query별 Top K

### retrieval-004 M-03

```text
1. metadata-guide.md / 4. 공개 전 메타데이터 확인
2. metadata-guide.md / 3. 기타 오류
3. metadata-guide.md / 2. M-03 오류
```

정답 문서는 Rank 1이다.

정의 Section은 Rank 3이다. 같은 문서의 다른 Section이 `M-03`을 포함해서 정의보다 앞에 온다.

이것은 문서 검색 실패가 아니다. Section Ranking 관찰이다.

### retrieval-005 OPS-101

```text
1. operations-faq.md / 2. OPS-101
```

정답 문서와 정의 Section이 모두 Rank 1이다.

### retrieval-006 CONTENT_BLOCKED

```text
1. youth-protection-policy.md / 4. 운영 문의 대응
2. content-status-policy.md / 2. CONTENT_BLOCKED
3. content-status-policy.md / 2.1 사용 시점
```

정답 문서는 Rank 2로 Top K에 있다.

정의 Section도 Rank 2다. Rank 1은 유사 정책 문서다.

문서 Hit는 성공이다. Rank 2는 Ranking 품질 관찰이다.

### retrieval-007 AGE_REVIEW_REQUIRED

```text
1. age-rating-policy.md / 5. 공개 전 확인 순서
2. age-rating-policy.md / 3.2 공개 조건
3. age-rating-policy.md / 4. AGE_REVIEW_REQUIRED
```

정답 문서는 Rank 1이다.

정의 Section은 Rank 3이다. 같은 문서의 다른 Section이 앞에 온다.

문서 검색 실패가 아니다.

---

## Semantic Query와의 비교

| Category | Hit Rate@K | MRR | 문서 Miss |
| --- | --- | --- | --- |
| SEMANTIC | 1.000 | 0.833 | 없음 |
| EXACT_KEYWORD | 1.000 | 0.875 | 없음 |
| SIMILAR_DOCUMENT | 0.667 | 0.500 | retrieval-010 |

Exact Keyword가 Semantic보다 나쁘지 않다.

MRR은 Exact Keyword가 더 높다.

현재 남은 문서 Miss는 Similar Document Query다.

Keyword Search를 넣어도 `retrieval-010`을 고친다는 근거는 이 측정에 없다.

---

## 판단

문서 기준 Expected Document가 Top K에 들어오는지를 Phase 4의 실패 기준으로 본다.

Evaluation Dataset의 Expected는 문서 이름이다.

```text
Exact Keyword Query 4개
문서 Hit Rate  1.0
문서 Recall    1.0
문서 MRR       0.875
문서 Miss      0
```

가설 1은 이 Dataset에서 성립하지 않는다.

가설 2가 맞다.

정의 Section이 항상 1위는 아니다. 그 문제는 Ranking 품질이며 Phase 5에서 본다.

Generation이 코드를 빠뜨리는 문제는 Retrieval 구조 문제가 아니다.

---

## 하지 않은 것

다음을 구현하지 않았다.

```text
Keyword Search
Full Text Search
Hybrid Search
RRF
```

실패가 확인되지 않았기 때문이다.

후보 비교용 Keyword Retriever도 만들지 않았다.

Semantic Query를 희생시킬 위험이 있는 구조를, 문서 Miss가 없는 상태에서 넣지 않는다.

---

## 결정

```text
Vector Search Only 유지

Keyword Search 추가하지 않음
Hybrid Search 추가하지 않음
RRF 추가하지 않음
```

이유:

```text
Exact Keyword Query는 문서 기준으로 이미 검색된다.
문제를 억지로 만들지 않는다.
```

DESIGN과 ADR은 바꾸지 않는다.

Retrieval 파이프라인도 바꾸지 않는다.

---

## 다음으로 넘기는 관찰

Phase 5 Ranking에서 볼 수 있는 것:

```text
retrieval-004  M-03 정의 Section Rank 3
retrieval-006  CONTENT_BLOCKED 문서 Rank 2
retrieval-007  AGE_REVIEW_REQUIRED 정의 Section Rank 3
```

Phase 4에서 Ranking 기술을 넣지 않는다.
