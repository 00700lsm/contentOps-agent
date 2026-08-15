# Experiment 002 - Retrieval Failures

## 목적

Baseline Evaluation에서 실패한 Query를 Retrieval, Ranking, Generation, Grounding, No Answer로 나눈다.

아직 해결하지 않는다.

---

## 현재 상태

`001-baseline.md`와 동일한 실행 결과다.

```text
결과 파일
evaluation/results/retrieval-20260815-124436.json
```

분류 기준:

```text
Retrieval Failure
Expected Document가 Top K에 없음

Ranking Failure
Expected Document는 Top K에 있으나 첫 Rank >= 3

Generation Failure
정답 문서 Context가 전달됐지만 Answer가 핵심을 빠뜨리거나 잘못됨

Grounding Failure
Context에 없는 사실을 생성

No Answer Failure
근거가 없는데도 답을 생성
```

Generation / Grounding은 자동 Metric이 아니라 답변을 읽고 분류했다.

---

## 문제

전체 Metric만 보면 Retrieval이 좋아 보일 수 있다.

```text
Hit Rate@K  0.9
```

Query를 열어보면 실패 위치가 같지 않다.

---

## 가설

Answer가 틀린 이유를 한 가지로 보지 않는다.

먼저 실패 계층을 나눈다.

---

## Dataset

`001-baseline.md`와 같다.

---

## 실험 조건

`001-baseline.md`와 같다.

변경 변수는 없다.

---

## Metric

문서 단위 자동 분류:

```text
RETRIEVAL                  1
RANKING                    0
NONE (No Answer 성공)      2
NEEDS_GENERATION_REVIEW    9
```

답변 검토 후 최종 분류는 아래 실패 Case를 따른다.

---

## 결과

### Retrieval Failure

#### retrieval-010

```text
Question
콘텐츠가 공개되지 않을 때 어디부터 확인해야 해?

Expected
operations-faq.md

Actual Top 5
1. content-status-policy.md
2. publishing-guide.md
3. metadata-guide.md
4. age-rating-policy.md
5. age-rating-policy.md
```

정답 문서가 Top 5에 없다.

Answer도 FAQ의 확인 순서가 아니라 메타데이터 가이드 내용을 사용했다.

유사 주제 문서가 정답 FAQ를 밀어낸 경우로 본다.

---

### Ranking Failure

문서 단위 Rank >= 3 기준 실패는 없었다.

다만 정답 문서 내부 Section Ranking은 별도 관찰이 있다.

#### retrieval-004

```text
Question
M-03 오류의 의미가 뭐야?

Expected Document
metadata-guide.md  Rank 1
```

문서 Hit는 성공이다.

정답 Section `2. M-03 오류`는 Rank 3이다.

```text
1. metadata-guide.md / 4. 공개 전 메타데이터 확인
2. metadata-guide.md / 3. 기타 오류
3. metadata-guide.md / 2. M-03 오류
```

문서 Hit Rate만 보면 문제가 가려진다.

이 건은 현재 기준의 Ranking Failure로 넣지 않고, Chunk/Section Ranking 관찰로 남긴다.

---

### Generation Failure

정답 문서가 Top K에 들어왔는데도 Answer가 약하거나 빗나갔다.

#### retrieval-002

```text
Question
콘텐츠를 서비스에서 차단하는 운영 상태는 뭐야?

Expected Rank
2  content-status-policy.md

Answer
현재 제공된 문서에서 콘텐츠를 서비스에서 차단하는 운영 상태에 대한 근거가 확인할 수 없습니다.
```

Top K에 상태 정책 문서가 있다.

LLM이 근거가 없다고 거절했다.

#### retrieval-003

```text
Question
필수 메타데이터가 비어 있으면 어떤 오류로 보나?

Expected Rank
1  metadata-guide.md

Answer
메타데이터 가이드를 참조해 보세요.
```

정답 문서는 찾았지만 오류 코드 `M-03`을 답하지 않았다.

정의 Section은 Rank 3에 있어 Retrieval 품질과 Generation 품질이 겹친다.

#### retrieval-006

```text
Question
CONTENT_BLOCKED는 언제 사용하는가?

Expected Rank
2  content-status-policy.md / 2. CONTENT_BLOCKED

Answer
CONTENT_BLOCKED는 연령 등급 미검수가 필요한 경우 사용하지 않는다.
```

사용 시점의 핵심(권리 만료, 강제 비공개 등) 대신 사용하지 않는 경우만 답했다.

#### retrieval-008

```text
Question
청소년 보호와 연령 등급의 관계는?

Expected
youth-protection-policy.md Rank 1
age-rating-policy.md Rank 3

Answer
지금 제공된 문서에서는 관련 내용이 확인할 수 없습니다.
```

두 정답 문서가 Top K에 있다.

관련 Context가 있는데도 거절했다.

#### retrieval-009

```text
Question
콘텐츠 공개 전 확인해야 하는 절차는?

Expected Rank
2  publishing-guide.md

Answer
age-rating-policy와 content-status-policy의 기준을 확인하는 것입니다.
```

정답 문서는 Rank 2에 있지만, Rank 1의 연령 등급 문서 쪽으로 답이 기울었다.

유사 문서 혼동과 Generation이 같이 보인다.

#### retrieval-007

```text
Question
AGE_REVIEW_REQUIRED는 무슨 상태인가?

Expected Rank
1

Answer
등급 검수가 필요하고, 연령 등급 재검수가 cần thiết한 상태다.
```

의미는 크게 틀리지 않다.

Context에 없는 외국어 토큰이 섞였다.

완전 오답으로 보지는 않고 Generation 품질 문제로 기록한다.

---

### Grounding Failure

담당자 이름이나 매출 숫자처럼 Context에 없는 사실을 만든 Query는 없었다.

retrieval-007의 외국어 토큰은 약한 Grounding 이슈로만 본다.

---

### No Answer

#### retrieval-011

```text
Question
해외 판권 계약 담당자가 누구야?

Answer
현재 제공된 문서에서는 확인할 수 없습니다.
```

성공으로 본다.

#### retrieval-012

```text
Question
이번 분기 콘텐츠 매출 목표는 얼마야?

Answer
현재 제공된 문서에서는 확인할 수 없습니다.
```

성공으로 본다.

Vector Search는 관련 없는 문서를 그래도 Top K에 넣는다.

다만 LLM이 그 Context로 답을 만들지 않았다.

---

## 실패 Case

| ID | 주 분류 | 보조 관찰 |
| --- | --- | --- |
| retrieval-010 | Retrieval Failure | 유사 문서가 FAQ를 밀어냄 |
| retrieval-004 | Section Ranking 관찰 | 문서 Hit, M-03 정의는 Rank 3 |
| retrieval-002 | Generation Failure | 상태 문서가 있는데 거절 |
| retrieval-003 | Generation Failure | M-03을 답하지 않음 |
| retrieval-006 | Generation Failure | 사용 시점 대신 비사용 조건만 답함 |
| retrieval-008 | Generation Failure | 정답 문서가 있는데 거절 |
| retrieval-009 | Generation Failure | 유사 문서 쪽으로 답 기울음 |
| retrieval-007 | Generation 품질 | 의미는 유사, 언어 섞임 |
| retrieval-011 | No Answer 성공 | 거절 |
| retrieval-012 | No Answer 성공 | 거절 |

---

## 분석

이번 Dataset에서 Vector Search의 문서 단위 Exact Keyword 실패는 뚜렷하지 않다.

```text
M-03                 metadata-guide.md        Rank 1
OPS-101              operations-faq.md        Rank 1
CONTENT_BLOCKED      content-status-policy.md Rank 2
AGE_REVIEW_REQUIRED  age-rating-policy.md     Rank 1
```

더 분명한 실패는 다음이다.

```text
1. 유사 문서 Retrieval Miss
   retrieval-010

2. 정답 문서 안 Section Ranking
   retrieval-004

3. Generation이 Retrieval 성공을 사용하지 못함
   retrieval-002, 003, 006, 008, 009
```

따라서 다음을 바로 결론으로 쓰지 않는다.

```text
Exact Keyword가 실패했으니 Hybrid Search를 넣는다.
Ranking이 낮으니 Reranker를 넣는다.
답변이 틀렸으니 Prompt를 바꾼다.
```

실패 위치가 다르다.

---

## 대안

이후 Phase에서 볼 후보:

```text
Phase 3 Chunking
→ M-03 정의가 다른 Section에 나뉘어 Rank 3인 현상

Phase 4 Exact Keyword
→ 이번 Dataset에서는 문서 Hit가 유지됨. 더 어려운 Keyword 실패가 있는지는 별도 확인

Phase 5 Ranking
→ 문서 Rank >= 3 실패는 없음. Section Rank와 유사 문서 혼동은 남아 있음

Phase 6 Generation / Grounding
→ 정답 Context가 있는데도 거절하거나 핵심을 빠뜨리는 경우
```

지금 기술을 고르지 않는다.

---

## 결정

실패 상태를 그대로 둔다.

해결 코드를 추가하지 않는다.

---

## Before / After

이 문서는 Baseline 실패 재현이다.

After는 없다.

---

## 결론

Baseline의 핵심 실패는 Hybrid Search 부재가 아니다.

```text
유사 문서에 의한 Retrieval Miss 1건
정답 Section Ranking
Generation Failure
```

No Answer Query 2건은 거절에 성공했다.

이 상태를 Git History에 남긴 뒤, Phase 3에서 Chunking이 실제 원인인지부터 확인한다.
