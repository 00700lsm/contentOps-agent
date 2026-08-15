# Experiment 003 - Chunking Analysis

## 목적

Phase 2에서 본 Retrieval 실패가 **Chunk 경계 때문인지** 확인한다.

Chunking을 기본 전략으로 바꾸지 않는다.

먼저 대안을 같은 Dataset으로 비교한다.

---

## 현재 상태

Baseline Chunking:

```text
전략          Fixed Size
Chunk Size    500 tokens
Overlap       50 tokens
실제 동작      Markdown Section을 먼저 나눈 뒤
              Section 안에서만 Token Window를 적용
```

Phase 2 결과:

```text
Hit Rate@K  0.9
Recall@K    0.9
MRR         0.75
Chunk 수    31
```

관련 실패:

```text
retrieval-004
M-03 정의 Section이 Rank 3

retrieval-010
operations-faq.md가 Top K에 없음
```

---

## 문제

정답 문서가 잡혀도 정답 Section이 1위가 아니거나, 유사 문서가 정답 FAQ를 밀어낼 수 있다.

이 현상이 Chunk가 너무 크거나, 관련 없는 내용이 한 Chunk에 섞여서 생기는지 모른다.

---

## 가설

```text
가설 1
현재 Chunk Size 500이 커서 관련 없는 내용이 섞인다.
→ Section 단위 또는 더 작은 Fixed Size가 Rank를 바꿀 수 있다.

가설 2
현재 문서는 Section이 이미 500 tokens보다 작다.
→ Size를 줄이거나 Section 전략을 써도 Chunk 집합이 거의 같다.
→ Chunking은 이번 실패의 주원인이 아니다.
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

Chunking만 바꾼다.

---

## 실험 조건

| 후보 | 전략 | Size | Overlap | 결과 파일 |
| --- | --- | --- | --- | --- |
| A Baseline | fixed | 500 | 50 | retrieval-20260815-130609.json |
| B Section | section | - | - | retrieval-20260815-130629.json |
| C Smaller Fixed | fixed | 200 | 20 | retrieval-20260815-130649.json |

---

## 변경 변수

```text
app.rag.chunking-strategy
app.rag.chunk-size
app.rag.chunk-overlap
```

---

## Metric

| 후보 | Chunk 수 | 평균 Chunk 문자 | Hit Rate@K | Recall@K | MRR | Retrieval Latency |
| --- | --- | --- | --- | --- | --- | --- |
| A Baseline 500/50 | 31 | 183 | 0.9 | 0.9 | 0.75 | 37.2 ms |
| B Section | 31 | 183 | 0.9 | 0.9 | 0.75 | 34.2 ms |
| C Fixed 200/20 | 31 | 183 | 0.9 | 0.9 | 0.75 | 36.2 ms |

문서 단위 Metric은 세 후보가 같다.

---

## 결과

세 후보의 Chunk 수와 평균 크기가 같다.

현재 Sample Document의 Section 길이가 200 tokens보다 작아서, Token Window가 추가로 쪼개지 않는다.

따라서 Baseline Fixed 500/50은 이 Dataset에서는 Section 기반 Chunking과 같은 Chunk 집합을 만든다.

---

## 실패 Case

### retrieval-004 M-03

세 후보 모두:

```text
1. metadata-guide.md / 4. 공개 전 메타데이터 확인
2. metadata-guide.md / 3. 기타 오류
3. metadata-guide.md / 2. M-03 오류
```

정의 Section은 계속 Rank 3이다.

원인 후보는 Chunk 혼합이 아니다.

같은 문서의 여러 Section이 모두 `M-03`을 포함해서 Vector Ranking이 정의 Section을 뒤로 보낸 것이다.

### retrieval-010 operations-faq.md

세 후보 모두 miss.

```text
1. content-status-policy.md / 콘텐츠 상태 정책
2. publishing-guide.md / 공개 가이드
3. metadata-guide.md / 메타데이터 가이드
```

Chunking을 바꿔도 유사 문서 혼동은 그대로다.

### retrieval-009 publishing-guide.md

세 후보 모두 Rank 2.

---

## 분석

확인할 질문에 대한 측정 결과:

```text
현재 Chunk가 너무 큰가?
→ 이 Dataset에서는 아니다. 평균 183자, Section이 이미 작다.

관련 없는 내용이 같은 Chunk에 섞이는가?
→ Baseline도 Section을 먼저 나눈다. M-03 정의는 이미 단독 Chunk다.

Chunk가 너무 작아 문맥이 잘리는가?
→ Size 200으로 줄여도 Chunk 수가 늘지 않았다.

문서 Section 구조를 활용하는 편이 나은가?
→ 이미 활용 중이고, 전용 Section 전략과 Metric이 같다.

Overlap이 필요한가?
→ Section이 Window보다 작아 Overlap이 동작하지 않는다.
```

가설 2가 맞다.

이번 실패의 주원인은 Chunking이 아니다.

```text
retrieval-004
→ 같은 키워드를 가진 Section 사이 Ranking

retrieval-010
→ 유사 문서 Retrieval
```

---

## 대안

| 후보 | 장점 | 단점 | 이번 Dataset 효과 |
| --- | --- | --- | --- |
| Baseline Fixed 500/50 | 구현이 단순하고 이미 Section을 존중함 | 긴 Section이 생기면 Window가 필요 | 기준 |
| Section 전용 | 긴 Section을 자르지 않음 | 긴 Section이 생기면 Chunk가 커짐 | Metric 동일 |
| Fixed 200/20 | 긴 Section을 더 잘게 나눔 | 지금은 나뉘지 않음 | Metric 동일 |

Keyword Search / Reranker는 이 실험의 변수가 아니다.

---

## 결정

Human Gate에서 추천안을 선택했다.

```text
Baseline Chunking을 유지한다.
기본 전략을 바꾸지 않는다.
ADR을 작성하지 않는다.
DESIGN을 바꾸지 않는다.
```

이유:

```text
대안 2개를 같은 Dataset으로 비교했고
Hit Rate / Recall / MRR / 실패 Query가 변하지 않았다.
```

---

## Before / After

```text
Before  Hit Rate@K 0.9  MRR 0.75  retrieval-010 miss  M-03 Section Rank 3
After   동일
```

---

## 결론

Chunking을 바꿨다고 개선한 것이 아니다.

현재 Sample Document에서는 Baseline Chunking이 이미 Section 단위와 같다.

실패는 Chunk 경계가 아니라 Ranking과 유사 문서 Retrieval 쪽에 남아 있다.

기본 Chunking 변경은 Human Gate에서 멈춘다.
