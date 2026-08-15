# Experiment 006 - Generation / Grounding

## 목적

Retrieval이 된 뒤에도 **LLM이 Context를 올바르게 쓰는지** 따로 평가한다.

Prompt를 바로 바꾸지 않는다.

먼저 실패 위치를 Generation / Grounding / No Answer로 나눈다.

---

## 현재 상태

```text
Retrieval           Vector Search Only
Chunking            Fixed Size 500 / 50
Embedding Model     nomic-embed-text
Chat Model          llama3.2
Top K               5
Prompt              BaselinePrompt (아래 Prompt A)
```

Prompt A (변경 전):

```text
제공된 Context를 기준으로 질문에 답한다.
Context에 답변 근거가 없다면 확인할 수 없다고 답한다.
내부 정책이나 사실을 추측해서 만들지 않는다.
답변은 간결하게 작성한다.
Context에 근거가 없으면 다음 문장을 사용한다:
현재 제공된 문서에서는 확인할 수 없습니다.
```

Source는 LLM Citation이 아니라 Retriever가 반환한 Chunk Metadata다.

측정 출처:

```text
Prompt A  evaluation/results/retrieval-20260815-130609.json
Prompt B  evaluation/results/retrieval-20260815-134454.json
```

Prompt A 구간은 Phase 3 / 4 / 5와 같은 평가다. Retrieval은 바꾸지 않았다.

```text
avg LLM Latency           2011.7 ms
total Prompt Tokens       7904
total Completion Tokens   353
```

Generation 분류는 자동 Metric이 아니다. 답변과 Top K Section을 읽고 분류했다.

---

## 문제

문서 Hit Rate는 0.9다.

문서가 Top K에 있어도 답이 질문과 어긋날 수 있다.

가능한 위치:

```text
정답 문서는 Hit
정답 Section은 Context에 없음
→ Retrieval / Section Ranking에 가깝다

정답 Section이 Context에 있음
답이 틀리거나 거절하거나 핵심을 빠뜨림
→ Generation Failure

답 내용이 제공된 Context에 없음
→ Grounding Failure

근거 없는 질문에 추측
→ No Answer Failure
```

---

## 가설

```text
가설 1
정답 문서 Hit면 정답 Section도 Context에 있다.
실패는 Prompt / Generation 문제다.

가설 2
문서 Hit여도 정의 Section이 Top K에 없을 수 있다.
그 경우 Prompt만 바꿔서는 정답을 만들 수 없다.

가설 3
No Answer Prompt는 이미 동작한다.
정책 사실을 새로 만드는 Hallucination은 드물다.
문제는 거절이 과하거나, 관련 없는 Chunk를 쓰는 것이다.
```

---

## Dataset

```text
Document Dataset     data/documents
Evaluation Dataset   evaluation/datasets/retrieval.jsonl
Embedding Model      nomic-embed-text
Chat Model          llama3.2
Top K                5
Retrieval            Vector Search Only
Prompt               BaselinePrompt
```

Prompt와 Dataset을 바꾸지 않았다.

---

## 평가 기준

정성 기준을 먼저 고정한다.

```text
Generation 성공
문서 Hit이고, 질문의 핵심을 Context 근거로 답함

Generation Failure
정답 Section이 Context에 있는데 오답 / 거절 / 핵심 누락

Section 부재
문서 Hit지만 답을 담은 Section이 Top K에 없음

Grounding Failure
제공된 Context에 없는 정책 사실 또는 숫자를 생성

약한 Grounding
Context에 없는 외국어 토큰이 섞임. 정책 사실은 아님

No Answer 성공
answerable=false인데 추측하지 않고 거절
```

Source:

```text
응답 Source 목록 = Retriever Top K
이건 설계다. LLM이 고른 근거가 아니다.
```

---

## 요약

| 분류 | 수 | Query |
| --- | --- | --- |
| Generation 성공 | 4 | 001, 004, 005, 007 |
| Generation Failure | 2 | 006, 008 |
| 문서 Hit + 정답 Section 부재 | 3 | 002, 003, 009 |
| Retrieval Failure | 1 | 010 |
| No Answer 성공 | 2 | 011, 012 |
| 강한 Grounding Failure (없는 정책 사실) | 0 |  |
| 약한 Grounding (외국어 토큰) | 4 | 004, 008, 011, 012 |

`004`는 의미는 맞고 `título`가 섞였다. Generation 성공 + 약한 Grounding으로 본다.

---

## Retrieval 성공과 Generation 성공 분리

문서 Hit 9건 중 Generation 성공은 4건이다.

문서 Hit ≠ 답변 성공이다.

`retrieval-010`은 정답 문서가 Top K에 없다. Generation 실패로 넣지 않는다.

---

## Query별 분석

### retrieval-001 Generation 성공

```text
Question   15세 콘텐츠를 공개하려면 어떤 검수가 필요해?
Document   age-rating-policy.md Rank 1
Section    3. 15세 콘텐츠 공개 조건 Rank 1
Answer     연령 등급 검수가 완료되어야 한다
```

정답 Section이 Rank 1이다. 답이 Context와 맞다.

---

### retrieval-002 문서 Hit + 정답 Section 부재

```text
Question   콘텐츠를 서비스에서 차단하는 운영 상태는 뭐야?
Expected   content-status-policy.md
Document   Rank 2  콘텐츠 상태 정책 (서문)
Answer     공개 실패의 원인이 상태나 연령 등급이 아니라 메타데이터일 수 있다.
```

Top K에 `2. CONTENT_BLOCKED`가 없다.

Rank 2는 문서 서문이다. 차단 상태 정의가 없다.

답은 `metadata-guide.md` 서문 문장이다. Context에는 있지만 질문의 답이 아니다.

Prompt만 바꾸면 `CONTENT_BLOCKED`를 만들 수 없다. 그 문자열이 Context에 없다.

거절했어야 더 Grounding에 가깝다. 지금은 관련 없는 Chunk를 답으로 썼다.

---

### retrieval-003 문서 Hit + 정답 Section 부재

```text
Question   필수 메타데이터가 비어 있으면 어떤 오류로 보나?
Expected   metadata-guide.md
Document   Rank 1  메타데이터 가이드 (서문)
Answer     retrieval-002와 같은 서문 문장
```

Top K에 `1. 필수 메타데이터`, `2. M-03 오류`가 없다.

문서 Hit는 서문 Chunk 때문이다. `M-03`은 Context에 없다.

LLM이 `M-03`을 답하지 못한 것은 Generation만의 실패가 아니다.

---

### retrieval-004 Generation 성공 + 약한 Grounding

```text
Question   M-03 오류의 의미가 뭐야?
Section    2. M-03 오류 Rank 3
Answer     필수 메타데이터 누락 오류... título, 연령 등급 필드, 포스터 이미지
```

정의 Section은 Context에 있다. 의미는 맞다.

`título`는 문서에 없다. 필드는 `title`이다.

정책 사실을 새로 만든 것은 아니다. 외국어 토큰이 섞였다.

---

### retrieval-005 Generation 성공

```text
Question   OPS-101은 무엇인가?
Section    2. OPS-101 Rank 1
Answer     공개 파이프라인이 중간에 실패한 운영 오류 코드입니다.
```

정답 Section Rank 1. 답이 Context와 맞다.

---

### retrieval-006 Generation Failure

```text
Question   CONTENT_BLOCKED는 언제 사용하는가?
Top K      Rank 2  2. CONTENT_BLOCKED
           Rank 3  2.1 사용 시점
Answer     연령 등급 미검수 때문에 사용하지 않는다.
           등급 검수가 필요한 경우에는 AGE_REVIEW_REQUIRED를 사용한다.
```

사용 시점 Section이 Context에 있다.

문서의 핵심은 권리 만료, 법적 요청, 강제 비공개, 사고 조사다.

답은 **사용하지 않는 경우**만 말했다. 질문의 핵심을 빠뜨렸다.

정답 Context + 불완전 답변이다. Generation Failure다.

---

### retrieval-007 Generation 성공

```text
Question   AGE_REVIEW_REQUIRED는 무슨 상태인가?
Section    4. AGE_REVIEW_REQUIRED Rank 3
Answer     연령 등급 검수가 필요하다는 의미를 가진 상태이다.
```

정의 Section이 Context에 있다. 답이 대체로 맞다.

Phase 2 평가에서는 외국어가 섞였다. 이번 결과 파일에서는 한국어로 끝났다.

같은 Prompt라도 Chat Model 출력이 달라질 수 있다. 이번 Phase는 `130609` 결과만 사용한다.

---

### retrieval-008 Generation Failure

```text
Question   청소년 보호와 연령 등급의 관계는?
Top K      Rank 1  youth-protection-policy.md 서문
           Rank 3  age-rating-policy.md / 1. 목적
Answer     정답을 제공할 수 없습니다.
           현재 제공된 문서에서는 확인할 수 없습니다.
```

서문에 관계가 있다.

```text
연령 등급 정책과 15세, 청소년, 보호라는 단어가 겹친다.
이 문서의 정본은 시청 제한이지, 콘텐츠 공개 가능 여부가 아니다.
```

Context에 답이 있는데 거절했다. Generation Failure다.

`mention된`은 Context에 없는 영어 토큰이다. 약한 Grounding이다.

---

### retrieval-009 문서 Hit + 정답 Section 부재

```text
Question   콘텐츠 공개 전 확인해야 하는 절차는?
Expected   publishing-guide.md
Document   Rank 2  공개 가이드 (서문)
Answer     age-rating-policy.md에 정의... 시청 가능 연령을 판정하고 검수 완료 상태를 확인
```

Top K에 `1. 공개 전 체크리스트`가 없다.

Rank 1 `age-rating-policy.md / 1. 목적` 문장을 절차로 답했다.

체크리스트(메타데이터, 상태, 공개 시각, 노출 설정)는 Context에 없으므로 Prompt로 복원할 수 없다.

---

### retrieval-010 Retrieval Failure

```text
Expected   operations-faq.md
Hit        false
Answer     공개 가이드 문서를 확인할 수 없습니다.
```

정답 문서가 Top K에 없다. Generation 분류 대상이 아니다.

답 문장도 어색하다. `publishing-guide.md`는 Top K Rank 2에 있다.

이 Phase에서 Prompt로 고치지 않는다.

---

### retrieval-011 No Answer 성공

```text
Question   해외 판권 계약 담당자가 누구야?
Answer     해외 판권 계약 담당자가 unclear합니다.
           현재 제공된 문서에서는 확인할 수 없습니다.
```

담당자 이름을 만들지 않았다. No Answer 성공이다.

`unclear합니다`는 약한 Grounding이다. 정책 사실 Hallucination은 아니다.

---

### retrieval-012 No Answer 성공

```text
Question   이번 분기 콘텐츠 매출 목표는 얼마야?
Answer     current 제공된 문서에서는 확인할 수 없습니다.
```

매출 숫자를 만들지 않았다. No Answer 성공이다.

`current`는 약한 Grounding이다.

---

## Grounding Failure

담당자 이름, 매출 숫자, 없는 상태 코드를 만든 Query는 없다.

강한 Grounding Failure는 0건이다.

약한 Grounding:

| Query | Context에 없는 토큰 |
| --- | --- |
| retrieval-004 | título |
| retrieval-008 | mention된 |
| retrieval-011 | unclear합니다 |
| retrieval-012 | current |

No Answer 정책이 추측을 막은 것은 맞다.

관련 없는 Context가 있어도 `011`, `012`는 거절했다.

같은 거절 규칙이 `008`에서는 답이 있는 Context를 거절했다.

---

## Source

12개 Query 모두 응답 Source는 Top K 5개와 같다.

이건 LLM이 답변 근거를 고른 결과가 아니다. `RagService`가 Retriever Chunk로 Source를 만든다.

따라서 Source 목록이 맞다고 해서 답이 그 문서를 썼다고 볼 수 없다.

| Query | 답이 실제로 기댄 쪽 | Source 목록 |
| --- | --- | --- |
| 002 | metadata-guide 서문 | content-status 포함, 5개 전부 |
| 006 | 2.1 사용하지 않는 경우 | CONTENT_BLOCKED 정의도 목록에 있음 |
| 008 | 거절 | youth-protection, age-rating이 목록에 있음 |
| 009 | age-rating 목적 | publishing-guide도 목록에 있음 |

Source Match를 Retriever 기준으로 보면 12/12다.

답변 근거 기준으로 보면 Source 목록은 넓다. 사용하지 않은 문서도 같이 나간다.

이번 Phase에서 Source 생성 방식을 바꾸지 않는다. DESIGN의 현재 동작이다.

---

## 측정

정량으로 단정하지 않는 값은 세지 않는다. 위에서 읽어서 나눈 수만 적는다.

```text
Generation 성공                         4
Generation Failure                      2
문서 Hit + 정답 Section 부재              3
Retrieval Failure                       1
No Answer 성공                          2
강한 Grounding Failure                  0
약한 Grounding                          4
No Answer Failure                       0
avg LLM Latency                         2011.7 ms
```

자동 Metric인 Hit Rate / MRR은 Generation 성공률이 아니다.

---

## 분석

가설 1은 틀렸다.

문서 Hit 9건 중 3건은 답을 담은 Section이 Top K에 없다.

```text
002  CONTENT_BLOCKED 정의 없음
003  M-03 정의 없음
009  공개 전 체크리스트 없음
```

가설 2가 맞다. 이 3건은 Prompt 변경의 1차 대상이 아니다.

가설 3도 맞다.

```text
No Answer 2건은 거절에 성공했다.
없는 정책 사실을 만든 경우는 없었다.
Generation Failure 2건은 과한 거절(008)과 핵심 누락(006)이다.
```

실패 위치가 다르다.

```text
002, 003, 009
→ 정답 Section이 Context에 없음
→ Prompt로 정답을 만들면 Grounding 위반이 된다

006, 008
→ 정답 Section이 Context에 있음
→ Generation / Prompt 후보

010
→ Retrieval Failure
→ 이 Phase에서 다루지 않음
```

---

## 대안

Prompt를 바꾸기 전에 후보만 비교한다. 구현하지 않았다.

### 후보 A. Baseline Prompt 유지

장점:

```text
No Answer가 이미 동작한다.
강한 Hallucination이 없다.
측정 없이 정책을 바꾸지 않는다.
```

단점:

```text
006 핵심 누락, 008 과한 거절이 남는다.
약한 외국어 토큰이 남는다.
```

### 후보 B. 질문 핵심을 Context에서 찾도록 Prompt를 보강

예:

```text
질문의 코드, 상태값, 절차가 Context에 있으면 그 내용을 답한다.
관련 내용이 있으면 거절하지 않는다.
사용하지 않는 경우만 답하지 말고, 사용 시점도 답한다.
한국어만 사용한다.
근거가 없으면 기존 No Answer 문장을 유지한다.
```

장점:

```text
006, 008에 직접 해당한다.
No Answer 문장을 유지하면 011, 012를 깨지 않을 수 있다.
```

단점:

```text
거절을 줄이면 No Answer가 약해질 수 있다.
002, 003, 009, 010은 고치지 못한다.
llama3.2라서 Prompt만으로 안정된다는 보장이 없다.
동일 Dataset 재평가가 필요하다.
```

### 후보 C. Source를 답이 사용한 문서로 좁힘

장점:

```text
002, 009처럼 목록과 실제 근거가 어긋나는 문제를 줄일 수 있다.
```

단점:

```text
현재 Source는 Retriever Metadata 설계다.
LLM Citation을 믿게 되면 DESIGN을 바꾼다.
Generation 정답률과 다른 문제다.
이 Phase 1차 대상이 아니다.
```

후보 C는 이번 Human Gate에서 고르지 않는 것을 권한다.

---

## 추천안

```text
후보 B를 검토한다.

대상은 Context에 정답이 있는 006, 008이다.
No Answer 문장과 추측 금지는 유지한다.
002, 003, 009는 Prompt 성공 조건으로 두지 않는다.
```

---

## 결정

Human Gate에서 후보 B를 선택했다.

```text
Prompt A  Baseline 최소 Grounding
Prompt B  질문 핵심을 빠뜨리지 않음
          관련 내용이 있으면 거절하지 않음
          사용 시점도 답함
          한국어만 사용
          No Answer 문장 유지
```

적용 위치: `BaselinePrompt.SYSTEM_PROMPT`

Retrieval / Chunking / Model / Dataset은 바꾸지 않았다.

DESIGN `# 21. Baseline Prompt`를 현재 코드에 맞게 갱신했다.

ADR은 작성하지 않는다. Retrieval 구조 변경이 아니다.

---

## Before / After

Prompt A: `evaluation/results/retrieval-20260815-130609.json`  
Prompt B: `evaluation/results/retrieval-20260815-134454.json`

Retrieval Metric은 같다. Prompt만 바꿨기 때문이다.

```text
Hit Rate@K  0.9 → 0.9
Recall@K    0.9 → 0.9
MRR         0.75 → 0.75
```

| 항목 | Prompt A | Prompt B |
| --- | --- | --- |
| Generation 성공 | 4 (001, 004, 005, 007) | 4 (001, 004, 005, 007) |
| Generation Failure | 2 (006, 008) | 2 (006, 008) |
| 문서 Hit + 정답 Section 부재 | 3 (002, 003, 009) | 3 (002, 003, 009) |
| Retrieval Failure | 1 (010) | 1 (010) |
| No Answer 성공 | 2 (011, 012) | 2 (011, 012) |
| 강한 Grounding Failure | 0 | 1 (003 문구 생성) |
| avg LLM Latency | 2011.7 ms | 3275.3 ms |
| Completion Tokens | 353 | 1063 |

대상 Query:

### retrieval-006

```text
A  사용하지 않는 경우만 답함. 사용 시점 핵심 누락
B  CONTENT_BLOCKED는 등급 검수가 필요한 경우에만 사용한다.
```

B는 거절을 줄였지만 사실을 뒤집었다. 문서는 등급 미검수에 CONTENT_BLOCKED를 쓰지 말라고 한다.

Generation Failure로 남는다. 개선으로 보지 않는다.

### retrieval-008

```text
A  Context에 관계가 있는데 거절
B  답을 시도했으나 관계를 뒤집음
   청소년 보호가 시청 제한이 아니라 공개 가능 여부를 다룬다고 답함
```

문서는 정본이 시청 제한이라고 한다.

과한 거절은 줄었다. 답은 틀렸다. Generation Failure로 남는다.

### 예상대로 안 바뀐 Query

```text
002, 003, 009  정답 Section이 없어 핵심 코드를 답하지 못함
010            Retrieval Failure
011, 012       No Answer 유지
```

### 부작용

```text
003  Context에 없는 오류 문구를 만듦
005  Context를 sử dụng하여 가 섞임. 한국어만 규칙은 안정되지 않음
007  미Complete된 이 섞임
010  관련 문서가 있다고 절차를 조합해 답함. 정답 FAQ는 없음
001, 005  답이 더 길어짐. Completion Token / LLM Latency 증가
```

가설 "B가 006, 008을 고친다"는 이번 측정에서 성립하지 않았다.

No Answer를 깨지는 않았다.

---

## 해결되지 않은 한계

```text
006, 008은 Prompt B 이후에도 Generation Failure다.
002, 003, 009는 정답 Section 부재가 남는다.
010은 Retrieval Failure다.
llama3.2는 한국어만 지시해도 외국어 토큰을 섞을 수 있다.
거절을 줄이면 틀린 답을 더 만들 수 있다.
Source 목록은 답변 근거가 아니다.
```

추가 Prompt 수정은 이 결과 없이 반복하지 않는다.

---

## Human Gate

후보 B를 선택해 적용하고 같은 Dataset으로 재평가했다.

```text
006, 008은 성공으로 바뀌지 않았다.
No Answer는 유지됐다.
LLM Latency와 Completion Token은 늘었다.
```

이 Phase에서 Prompt를 한 번 더 바꾸지 않는다.

다음 Phase는 Content Data Tool이다. Generation 잔여 실패는 기록으로 남긴다.
