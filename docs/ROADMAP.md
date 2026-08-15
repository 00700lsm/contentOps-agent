# ContentOps Agent - ROADMAP

## 1. 문서 목적

이 문서는 ContentOps Agent 프로젝트의 **전체 진행 순서와 각 Phase의 완료 조건**을 정의한다.

각 문서의 역할은 다음과 같다.

```text
REQUIREMENTS.md
→ 무엇을 만족해야 하는가

DESIGN.md
→ 현재 시스템은 실제로 어떻게 구성되어 있는가

ROADMAP.md
→ 어떤 문제를 어떤 순서로 평가할 것인가

TASKS.md
→ 현재 Phase에서 무엇을 할 것인가

adr/
→ 중요한 설계 결정을 왜 내렸는가

experiments/
→ 실제 Evaluation과 Experiment 결과가 어땠는가

evaluation/
→ 반복 평가에 사용하는 Dataset과 실행 자산
```

ContentOps Agent에서는 단순히 기능 구현 여부만으로 Phase를 완료하지 않는다.

AI 기능과 관련된 Phase에서는 가능한 한 다음 흐름을 따른다.

```text
구현
 ↓
Test
 ↓
Evaluation
 ↓
실패 Case 확인
 ↓
원인 분석
 ↓
대안 비교
 ↓
Human Gate
 ↓
설계 결정
 ↓
구현
 ↓
동일 Dataset 재평가
```

`ROADMAP.md`는 기술 도입 계획서가 아니다.

따라서 다음 기술은 ROADMAP에 등장하더라도 반드시 적용해야 하는 것은 아니다.

```text
Keyword Search
Hybrid Search
RRF
Reranker
Query Rewrite
Multi Query
LangGraph
Multi-Agent
```

실제 Evaluation 결과가 필요성을 보여줄 때만 도입한다.

---

# 2. 전체 진행 흐름

```text
Phase 1
Baseline Vector RAG 구현
        ↓
Phase 2
Baseline Evaluation 및 실패 Case 분류
        ↓
Phase 3
Chunking 품질 분석
        ↓
Phase 4
Exact Keyword Retrieval 분석
        ↓
Phase 5
Ranking 품질 분석
        ↓
Phase 6
Generation / Grounding 분석
        ↓
Phase 7
Content Data Tool 구현
        ↓
Phase 8
Tool Selection / Agent Workflow 평가
        ↓
Phase 9
최종 Evaluation 및 비교
```

각 Phase는 이전 Phase의 실제 결과를 기반으로 진행한다.

Phase가 변경된다는 이유만으로 `DESIGN.md`를 수정하지 않는다.

실제 시스템 구조가 변경된 경우에만 `DESIGN.md`를 현재 코드와 일치하도록 갱신한다.

---

# 3. Phase 1 - Baseline Vector RAG 구현

## 목표

최적화가 적용되지 않은 **가장 단순한 운영 문서 기반 RAG Pipeline**을 만든다.

이 단계의 목적은 높은 Retrieval 품질을 만드는 것이 아니다.

이후 모든 Experiment에서 사용할 **측정 가능한 Baseline**을 확보하는 것이 목적이다.

---

## 초기 구조

```text
Sample Documents
       ↓
Document Loading
       ↓
Chunking
       ↓
Embedding
       ↓
PostgreSQL + pgvector


User Question
       ↓
Question Embedding
       ↓
Vector Search
       ↓
Top K Chunks
       ↓
Context
       ↓
LLM
       ↓
Answer + Source
```

---

## 구현 범위

### Question API

```http
POST /api/v1/questions
```

예:

```json
{
  "question": "15세 콘텐츠의 공개 조건이 뭐야?"
}
```

---

### Sample Document

프로젝트용 운영 문서를 준비한다.

예:

```text
age-rating-policy.md

content-status-policy.md

metadata-guide.md

publishing-guide.md

youth-protection-policy.md

operations-faq.md
```

일부 문서는 의미가 겹치도록 구성한다.

또한 이후 Exact Keyword 실험을 위해 다음과 같은 값을 포함할 수 있다.

```text
OPS-101

M-03

CONTENT_BLOCKED

AGE_REVIEW_REQUIRED
```

---

### Document Loading

동일한 Sample Document를 반복적으로 적재할 수 있어야 한다.

적재 방식은 현재 DESIGN에서 결정한다.

---

### Chunking

초기에는 하나의 단순한 Chunking 전략만 사용한다.

아직 Chunk Size나 Overlap 최적화를 수행하지 않는다.

Chunk는 최소한 다음 정보를 추적할 수 있어야 한다.

```text
chunkId

documentId

documentName

section

chunkIndex

content
```

---

### Embedding / Vector Store

Document Chunk를 Embedding으로 변환하고:

```text
PostgreSQL
+
pgvector
```

에 저장한다.

질문도 동일한 Embedding 기준으로 변환한다.

---

### Vector Retrieval

Baseline에서는 Vector Similarity Search만 사용한다.

```text
Question
   ↓
Embedding
   ↓
Vector Search
   ↓
Top K
```

다음 기능은 사용하지 않는다.

```text
Keyword Search

Hybrid Search

RRF

Reranker
```

---

### RAG Answer

검색된 Chunk를 Context로 LLM에 전달한다.

최종 응답에서는 최소한 다음을 제공한다.

```text
Answer

Source Document

Source Section
```

---

## Evaluation Dataset

Phase 1부터 Evaluation Dataset을 만든다.

최소한 다음 유형을 포함한다.

```text
Semantic Question

Exact Keyword Question

Similar Document Question

No Answer Question
```

예:

```text
질문:
15세 콘텐츠 공개 기준은?

Expected:
age-rating-policy.md
```

---

## Evaluation 실행 가능 상태

최소한 다음 Metric을 계산할 수 있어야 한다.

```text
Hit Rate@K

Recall@K

MRR
```

Phase 1에서는 Metric이 높을 필요는 없다.

**계산할 수 있는 상태**를 만드는 것이 목적이다.

---

## Test

최소한 다음 검증을 수행한다.

```text
Document Loading Test

Vector Store Integration Test

Vector Search Test

RAG Integration Test

Evaluation 실행 검증
```

---

## README

Playback Gate와 Live Event Stream에서 README가 늦게 보강됐던 문제를 반복하지 않는다.

Phase 1 완료 시 README에서 최소한 다음을 확인할 수 있어야 한다.

```text
프로젝트 목적

애플리케이션 실행 방법

PostgreSQL / pgvector 실행 방법

Sample Document 적재 방법

Question API 호출 방법

Test 실행 방법

Evaluation 실행 방법
```

---

## 이 Phase에서 하지 않는 것

다음 기능은 의도적으로 구현하지 않는다.

```text
Keyword Search

Hybrid Search

RRF

Reranker

Query Rewrite

Multi Query

Content DB Tool

Tool Calling

Agent

LangGraph

Multi-Agent

Memory
```

Vector Search가 특정 Query를 잘 처리하지 못하더라도 미리 해결하지 않는다.

---

## 완료 조건

다음 조건을 모두 만족해야 한다.

```text
[ ] Sample Document가 존재한다.

[ ] 반복 가능한 Document 적재가 가능하다.

[ ] Chunk를 생성할 수 있다.

[ ] Chunk Source를 추적할 수 있다.

[ ] Embedding을 생성할 수 있다.

[ ] pgvector에 저장할 수 있다.

[ ] Vector Search가 가능하다.

[ ] Question API가 동작한다.

[ ] LLM이 검색 Context를 이용해 Answer를 생성한다.

[ ] Source를 반환한다.

[ ] Evaluation Dataset이 존재한다.

[ ] Hit Rate@K / Recall@K / MRR 계산이 가능하다.

[ ] Integration Test가 통과한다.

[ ] README에서 실행 / 평가 방법을 확인할 수 있다.

[ ] 아직 Retrieval 개선 기능이 적용되지 않았다.
```

완료 후 Git checkpoint를 남긴다.

예:

```text
feat: implement baseline vector rag
```

Phase 1의 완료된 `TASKS.md`가 Commit에 포함된 뒤 다음 Phase의 TASK로 교체한다.

조건을 만족하면 Phase 2로 이동한다.

---

# 4. Phase 2 - Baseline Evaluation 및 실패 Case 분류

## 목표

Baseline Vector RAG를 동일한 Evaluation Dataset으로 평가하고 **현재 시스템이 어떤 질문에서 왜 실패하는지 분류한다.**

아직 문제를 해결하지 않는다.

먼저 현재 상태를 이해한다.

---

## Baseline Evaluation

최소한 다음 값을 기록한다.

```text
Hit Rate@K

Recall@K

MRR

Retrieval Latency

LLM Latency

End-to-End Latency
```

사용 중인 Model에 따라 가능한 경우:

```text
Input Tokens

Output Tokens

Evaluation 전체 Token
```

도 기록한다.

---

## Query별 결과 기록

단순히 전체 Metric만 기록하지 않는다.

각 Evaluation Query에 대해 최소한 다음을 확인한다.

```text
Question

Expected Document

Retrieved Top K

Expected Document Rank

Answer

Source

Latency
```

---

## 실패 Case 분류

실패를 다음과 같이 나눈다.

### Retrieval Failure

```text
Expected Document가 Top K에 없음
```

---

### Ranking Failure

```text
Expected Document가 검색되지만
Ranking이 지나치게 낮음
```

---

### Generation Failure

```text
올바른 Context가 제공됐지만
Answer가 잘못됨
```

---

### Grounding Failure

```text
Context에 없는 내용을 생성
```

---

### No Answer Failure

```text
근거가 없는데도
답을 생성
```

---

## 중요한 원칙

Answer가 틀렸다고 바로 Prompt를 변경하지 않는다.

먼저:

```text
Retrieval 문제인가?

Ranking 문제인가?

Generation 문제인가?

Grounding 문제인가?
```

를 구분한다.

---

## 결과 문서

```text
docs/experiments/001-baseline.md

docs/experiments/002-retrieval-failures.md
```

`001-baseline.md`에는 전체 조건과 Metric을 기록한다.

`002-retrieval-failures.md`에는 실패 Query를 유형별로 정리한다.

---

## Git Checkpoint

Baseline 실패가 존재하는 상태 자체를 Commit으로 남긴다.

예:

```text
experiment: evaluate baseline retrieval
```

필요하면 실패 유형이 명확하게 재현된 시점에:

```text
experiment: reproduce retrieval failures
```

를 별도 checkpoint로 남긴다.

해결 코드를 같은 Commit에 넣지 않는다.

---

## 완료 조건

```text
[ ] 전체 Evaluation Dataset을 실행했다.

[ ] Hit Rate@K를 기록했다.

[ ] Recall@K를 기록했다.

[ ] MRR을 기록했다.

[ ] Query별 검색 Ranking을 확인했다.

[ ] 주요 실패 Query를 확인했다.

[ ] Retrieval / Ranking / Generation / Grounding 실패를 구분했다.

[ ] No Answer Query 결과를 확인했다.

[ ] Baseline Latency를 기록했다.

[ ] Experiment 문서가 작성되어 있다.

[ ] 실패 상태가 Git History에 남아 있다.

[ ] 아직 실패를 해결하기 위한 기술을 추가하지 않았다.
```

조건을 만족하면 Phase 3로 이동한다.

---

# 5. Phase 3 - Chunking 품질 분석

## 목표

현재 Retrieval 실패 중 **문서가 어떻게 나뉘어 있는지가 검색 품질에 영향을 주는지 확인한다.**

Chunking을 무조건 개선해야 하는 Phase가 아니다.

Chunking이 실제 원인인지 검증하는 Phase다.

---

## 확인할 질문

```text
현재 Chunk가 너무 큰가?

관련 없는 내용이 같은 Chunk에 섞이는가?

Chunk가 너무 작아 필요한 문맥이 잘리는가?

문서 Section 구조를 활용하는 편이 나은가?

Overlap이 필요한가?
```

---

## 비교 후보

필요한 후보만 실험한다.

예:

```text
현재 Baseline Chunking

vs

다른 Fixed Size

vs

Section 기반 Chunking
```

모든 후보를 반드시 구현할 필요는 없다.

---

## 실험 원칙

Chunking을 비교할 때 가능하면 다음 조건은 동일하게 유지한다.

```text
Document Dataset

Evaluation Dataset

Embedding Model

Top K

Vector Search 방식

LLM Model
```

Chunking과 Embedding Model을 동시에 변경하지 않는다.

---

## 측정 대상

```text
Hit Rate@K

Recall@K

MRR

Query별 Rank

Chunk 수

평균 Chunk 크기

Retrieval Latency
```

Generation 결과가 영향을 받는 경우 Answer도 참고할 수 있다.

---

## Human Gate

Baseline Chunking을 실제로 변경하기 전:

```text
현재 실패 Case

측정 결과

후보 Chunking

각 후보 장점 / 단점

추천안
```

을 제시하고 Human Gate에서 멈춘다.

---

## 결과 문서

```text
docs/experiments/003-chunking-analysis.md
```

Chunking 전략이 실제 시스템의 기본 전략으로 변경된다면 ADR을 고려한다.

예:

```text
docs/adr/001-document-chunking-strategy.md
```

변경이 없다면 ADR은 필요 없다.

---

## 완료 조건

```text
[ ] Chunking과 관련된 실패 Case를 확인했다.

[ ] 최소 하나 이상의 대안을 현재 Baseline과 비교했다.

[ ] 동일 Dataset으로 재평가했다.

[ ] Hit Rate / Recall / MRR 변화를 기록했다.

[ ] 실패 Query 변화를 확인했다.

[ ] 최종 Chunking 유지 / 변경 이유를 설명할 수 있다.

[ ] 변경했다면 DESIGN을 현재 코드와 일치시켰다.
```

조건을 만족하면 Phase 4로 이동한다.

---

# 6. Phase 4 - Exact Keyword Retrieval 분석

## 목표

다음과 같은 **정확한 문자열이 중요한 Query에서 Vector Search의 한계가 실제로 존재하는지 확인한다.**

```text
OPS-101

M-03

CONTENT_BLOCKED

AGE_REVIEW_REQUIRED
```

---

## 먼저 확인할 것

Baseline 또는 현재 Retriever가 이미 Exact Keyword Query를 충분히 처리한다면 문제를 억지로 만들지 않는다.

```text
Exact Keyword Query
      ↓
현재 Vector Search
      ↓
충분히 검색됨?

YES
→ 결과 기록
→ 새로운 Retrieval 구조 추가하지 않음

NO
→ 실패 Case 분석
→ 후보 비교
```

---

## 가능한 후보

실패가 실제로 확인된 경우 다음을 검토할 수 있다.

```text
Keyword Search

Full Text Search

Vector + Keyword Search

다른 Query 처리 방식
```

구체적인 구현은 측정 결과 이후 결정한다.

---

## Hybrid Retrieval

Vector Search와 Keyword Search가 서로 다른 Query에서 장점을 보인다면 두 결과를 결합하는 방식을 검토할 수 있다.

```text
Vector Retrieval
      +
Keyword Retrieval
      ↓
Result Fusion
```

Fusion 방법은 요구사항에서 고정하지 않는다.

가능한 후보 중 하나:

```text
RRF
```

다만 `Phase 4에 왔다 = RRF 사용`은 아니다.

---

## Human Gate

새로운 Retrieval 전략을 실제로 도입하기 전 다음을 보고한다.

```text
현재 실패 Query

Vector Search 결과

Keyword 후보 결과

Semantic Query에 미치는 영향

Exact Keyword Query에 미치는 영향

Latency

구현 복잡도

추천안
```

사용자 선택 이후 구현한다.

---

## 재평가

변경 후 동일 Dataset을 실행한다.

특히:

```text
Semantic Query

Exact Keyword Query

Similar Document Query
```

를 따로 비교한다.

Exact Keyword만 좋아지고 Semantic Query가 나빠질 수도 있다.

---

## 결과 문서

```text
docs/experiments/004-exact-keyword-retrieval.md
```

최종 Retrieval 구조가 변경됐다면 ADR을 작성한다.

예:

```text
docs/adr/002-retrieval-strategy.md
```

---

## 완료 조건

```text
[ ] Exact Keyword Query의 현재 성능을 확인했다.

[ ] Vector Search의 실제 실패 여부를 판단했다.

[ ] 문제가 있다면 후보를 비교했다.

[ ] Human Gate를 거쳐 필요한 구조만 선택했다.

[ ] 동일 Dataset으로 재평가했다.

[ ] Semantic / Exact Keyword Query 변화를 각각 확인했다.

[ ] 변경 여부와 이유를 Experiment에 기록했다.

[ ] 구조가 변경됐다면 DESIGN을 갱신했다.
```

문제가 없다면 추가 기술을 넣지 않고 Phase를 완료할 수 있다.

조건을 만족하면 Phase 5로 이동한다.

---

# 7. Phase 5 - Ranking 품질 분석

## 목표

정답 문서는 검색되지만 **충분히 높은 순위에 위치하지 않는 문제가 실제로 존재하는지 확인한다.**

---

## Ranking Failure 예

```text
Question

15세 콘텐츠 공개 조건은?


Expected

age-rating-policy.md


Actual

Rank 1  operations-faq.md
Rank 2  publishing-guide.md
Rank 3  youth-protection-policy.md
Rank 4  age-rating-policy.md
```

Top K에는 존재하지만 LLM에 전달되는 Context 순서나 제한에 따라 답변 품질이 떨어질 수 있다.

---

## 먼저 확인할 것

현재 Ranking이 충분하다면 별도의 Ranking 기술을 추가하지 않는다.

다음을 확인한다.

```text
MRR

Expected Document 평균 Rank

Ranking Failure Query 수
```

---

## 가능한 후보

실제 Ranking 문제가 확인된 경우 다음을 후보로 검토할 수 있다.

```text
현재 Fusion Score 조정

RRF

Reranker

Top N → Final Top K 구조

다른 Ranking 전략
```

---

## Reranker를 검토하는 경우

다음 값을 함께 비교한다.

```text
MRR

Hit Rate@K

Recall@K

Ranking Failure Query 수

Reranking Latency

End-to-End Latency

필요한 경우 비용
```

품질 향상만 보고 결정하지 않는다.

---

## Human Gate

Ranking 구조 변경 전 다음을 보고한다.

```text
현재 Ranking 실패

Metric

후보

품질 예상

Latency / 비용

구현 복잡도

추천안
```

---

## 결과 문서

```text
docs/experiments/005-ranking-quality.md
```

Reranker 또는 중요한 Ranking 구조가 실제 채택된다면 ADR을 고려한다.

---

## 완료 조건

```text
[ ] Ranking Failure를 수치로 확인했다.

[ ] MRR과 Query별 Rank를 분석했다.

[ ] 문제가 없다면 불필요한 Ranking 기술을 추가하지 않았다.

[ ] 문제가 있다면 후보를 비교했다.

[ ] 선택한 구조를 동일 Dataset으로 재평가했다.

[ ] 품질과 Latency Trade-off를 기록했다.

[ ] 실제 구조 변경 시 DESIGN을 갱신했다.
```

조건을 만족하면 Phase 6로 이동한다.

---

# 8. Phase 6 - Generation / Grounding 분석

## 목표

Retrieval이 성공한 이후에도 **LLM이 검색 Context를 올바르게 사용하고 있는지 별도로 평가한다.**

---

## 주요 질문

```text
정답 Context가 있는데 Answer가 틀리는가?

Context에 없는 내용을 생성하는가?

질문의 핵심 내용을 빠뜨리는가?

Source가 실제 답변 근거와 일치하는가?

답이 없는 질문에서 추측하는가?
```

---

## 평가 Case

최소한 다음 유형을 포함한다.

```text
정답 Context가 명확한 질문

여러 Context를 조합해야 하는 질문

관련성이 낮은 Context가 포함된 질문

No Answer Question
```

---

## 실패 유형

### Generation Failure

```text
Correct Context
+
Incorrect Answer
```

### Grounding Failure

```text
Answer 내용
∉
Provided Context
```

### No Answer Failure

```text
근거 없음
+
그럴듯한 답변 생성
```

---

## Prompt 변경

Prompt 변경이 필요해 보이더라도 바로 수정하지 않는다.

먼저 실패 Case를 기록한다.

변경 후보 예:

```text
Context Only 규칙

No Answer 규칙

Source 출력 규칙

답변 형식
```

---

## Human Gate

Grounding 정책이나 No Answer 정책처럼 답변 의미를 바꾸는 Prompt 변경은 Human Gate를 거친다.

---

## 비교

가능하면:

```text
Prompt A
 ↓
동일 Generation Dataset

Prompt B
 ↓
동일 Generation Dataset
```

형태로 비교한다.

---

## 측정 대상

정량 평가가 가능한 부분은 기록하고, 정성 기준이 필요한 경우 명시적인 평가 기준을 사용한다.

예:

```text
Grounded Answer 수

Unsupported Claim 수

No Answer 성공 수

Source Match

LLM Latency

Token 사용량
```

측정 가능한 값만 사용한다.

---

## 결과 문서

```text
docs/experiments/006-generation-grounding.md
```

---

## 완료 조건

```text
[ ] Retrieval 성공과 Generation 성공을 분리해 평가했다.

[ ] Grounding Failure를 확인했다.

[ ] No Answer Query를 평가했다.

[ ] Source가 실제 답변 근거인지 확인했다.

[ ] Prompt 변경이 있었다면 동일 Dataset으로 재평가했다.

[ ] 변경 전후 결과를 기록했다.

[ ] 해결되지 않은 Hallucination / Grounding 한계도 기록했다.
```

조건을 만족하면 Phase 7로 이동한다.

---

# 9. Phase 7 - Content Data Tool 구현

## 목표

문서 검색만으로 해결할 수 없는 **구조화된 콘텐츠 데이터 질문을 처리할 수 있도록 Tool을 추가한다.**

이 단계부터 단순 RAG에서 Agent 기능으로 확장하기 시작한다.

---

## Content 데이터

최소한 다음 정보를 가진다.

```text
id

title

genre

ageRating

status

releaseDate

serviceRegion

metadataStatus
```

---

## 처리할 질문

예:

```text
"이번 달 공개 예정 액션 콘텐츠 알려줘."

"콘텐츠 100번 현재 상태 알려줘."
```

---

## 기본 Tool

최소 역할:

```text
Content Search

Content Detail
```

예시 이름:

```text
search_contents

get_content_detail
```

---

## 중요한 원칙

LLM이 자유롭게 SQL을 생성하고 실행하도록 만드는 것이 목표가 아니다.

우선 명시적인 Tool Interface를 사용한다.

```text
Agent / LLM
      ↓
Defined Tool
      ↓
Application Service
      ↓
Database
```

---

## Policy Search Tool

기존 Document Retrieval도 Agent가 사용할 수 있는 Tool 형태로 노출할 수 있다.

예:

```text
search_policy_documents
```

단, Tool Calling Framework가 아직 필요하지 않다면 내부 기능의 역할만 먼저 분리할 수 있다.

---

## Tool Dataset

Agent 평가용 Dataset에 다음 유형을 추가한다.

```text
Policy Only

Content Search Only

Content Detail Only
```

아직 복수 Tool 조합 문제는 다음 Phase에서 집중한다.

---

## Test

```text
Content Search Test

Content Detail Test

Policy Search Tool Test

Tool Result 검증
```

---

## 완료 조건

```text
[ ] Sample Content 데이터가 존재한다.

[ ] 조건 기반 Content Search가 가능하다.

[ ] Content Detail 조회가 가능하다.

[ ] Policy Search 역할이 Tool 단위로 사용할 수 있다.

[ ] Tool별 Test가 존재한다.

[ ] Agent Evaluation Dataset에 Tool Query가 추가됐다.

[ ] 아직 불필요한 Multi-Agent 구조를 만들지 않았다.
```

실제 Tool 구조가 시스템 구조를 변경했다면 `DESIGN.md`를 갱신한다.

조건을 만족하면 Phase 8로 이동한다.

---

# 10. Phase 8 - Tool Selection / Agent Workflow 평가

## 목표

사용자 질문에 따라 **적절한 Tool을 선택하고, 필요한 경우 여러 Tool을 조합할 수 있는지 평가한다.**

---

## 기본 질문 유형

### Policy Only

```text
"15세 콘텐츠 공개 기준 알려줘."

Expected

Policy Search
```

---

### Content Search Only

```text
"이번 달 공개 액션 콘텐츠 알려줘."

Expected

Content Search
```

---

### Content Detail Only

```text
"콘텐츠 100번 상태 알려줘."

Expected

Content Detail
```

---

### Multi Tool

```text
"콘텐츠 100번이 왜 공개되지 않는지 정책 기준으로 설명해줘."

Expected

Content Detail
+
Policy Search
```

---

## Tool Selection 평가

최소한 다음을 기록한다.

```text
Question

Expected Tool

Actual Tool

Tool 호출 순서

Tool 호출 횟수

불필요한 Tool

Final Answer
```

가능하면:

```text
Tool Selection Accuracy
```

를 계산한다.

---

## Agent 실패 유형

### Wrong Tool

```text
Expected
Content Detail

Actual
Policy Search
```

---

### Missing Tool

```text
Expected
Content Detail + Policy Search

Actual
Content Detail
```

---

### Unnecessary Tool

```text
Expected
Policy Search

Actual
Policy Search
+
Content Search
+
Content Detail
```

---

### Wrong Tool Order

Tool 순서가 실제 문제 해결에 영향을 주는 경우 별도로 기록한다.

---

## Workflow 복잡성 확인

단순 Tool Calling만으로 충분한지 먼저 확인한다.

```text
Question
 ↓
LLM Tool Selection
 ↓
Tool
 ↓
Answer
```

여러 단계 상태 관리가 실제로 필요해졌다면:

```text
Question
 ↓
Tool
 ↓
Result
 ↓
충분?
 ├─ YES → Answer
 └─ NO  → 추가 Tool
```

구조를 검토한다.

---

## LangGraph

`LangGraph`는 이 Phase에 도달했다고 자동으로 도입하지 않는다.

다음과 같은 문제가 실제로 확인됐을 때 후보로 검토한다.

```text
여러 단계 상태 추적이 어려움

Tool 결과에 따라 다음 경로가 달라짐

복수 Tool Workflow를 명시적으로 제어할 필요가 있음

현재 단순 Tool Calling에서 반복 오류 발생
```

---

## Human Gate

Agent Workflow를 변경하기 전 다음을 보고한다.

```text
현재 Tool Selection 결과

실패 Case

단순 Tool Calling의 한계

가능한 Workflow 후보

LangGraph 필요성

복잡도

Latency

추천안
```

사용자가 선택한 뒤 구현한다.

---

## Multi-Agent

다음 구조를 기본으로 만들지 않는다.

```text
Planner Agent

Search Agent

DB Agent

Reviewer Agent

Answer Agent
```

하나의 Agent Workflow로 해결하기 어려운 실제 문제가 확인된 경우에만 검토한다.

---

## 결과 문서

```text
docs/experiments/007-tool-selection.md

docs/experiments/008-agent-workflow.md
```

`008-agent-workflow.md`는 Workflow 변경 실험이 실제 존재하는 경우 작성한다.

단순 Tool Calling으로 충분하다면:

```text
LangGraph 불필요
```

라는 결론으로 Phase를 완료할 수 있다.

중요한 Agent 구조가 결정됐다면 ADR을 작성한다.

예:

```text
docs/adr/003-agent-workflow.md
```

---

## 완료 조건

```text
[ ] Policy Query를 올바른 Tool로 처리할 수 있다.

[ ] Content Search Query를 처리할 수 있다.

[ ] Content Detail Query를 처리할 수 있다.

[ ] Multi Tool Query를 처리할 수 있다.

[ ] Expected / Actual Tool을 비교했다.

[ ] Tool Selection Accuracy를 확인할 수 있다.

[ ] 불필요한 Tool 호출을 분석했다.

[ ] 단순 Tool Calling의 충분성 여부를 판단했다.

[ ] Workflow 변경이 필요하면 Human Gate를 거쳤다.

[ ] 구조 변경 시 DESIGN / ADR을 갱신했다.
```

조건을 만족하면 Phase 9로 이동한다.

---

# 11. Phase 9 - 최종 Evaluation 및 비교

## 목표

최초 Baseline과 최종 시스템을 비교하여 **어떤 실패를 확인했고, 어떤 변경이 실제로 효과가 있었으며, 어떤 기술은 필요하지 않았는지 설명한다.**

---

## Retrieval 최종 평가

가능한 한 Phase 2와 동일한 Evaluation Dataset을 다시 실행한다.

비교 예:

| Metric | Baseline | Final |
|---|---:|---:|
| Hit Rate@K | 측정값 | 측정값 |
| Recall@K | 측정값 | 측정값 |
| MRR | 측정값 | 측정값 |
| Retrieval Latency | 측정값 | 측정값 |
| E2E Latency | 측정값 | 측정값 |

실제 측정값만 사용한다.

---

## Query 유형별 비교

전체 평균만 보지 않는다.

```text
Semantic

Exact Keyword

Similar Document

No Answer
```

유형별로 어떤 변화가 있었는지 확인한다.

---

## Generation 비교

다음도 정리한다.

```text
Grounded Answer

Unsupported Claim

No Answer 성공

Source Match

Generation Latency
```

실제 Evaluation에서 기록한 항목만 사용한다.

---

## Agent 비교

```text
Tool Selection Accuracy

Wrong Tool

Missing Tool

Unnecessary Tool

Multi Tool 성공
```

을 정리한다.

---

## Latency / 비용

최종 구조가 Baseline보다 복잡해졌다면 품질만 비교하지 않는다.

```text
Embedding Latency

Retrieval Latency

Reranking Latency

LLM Latency

End-to-End Latency

Token / 비용
```

등 실제 측정 가능한 비용을 같이 정리한다.

---

## 최종 질문

프로젝트 마지막에는 다음 질문에 답할 수 있어야 한다.

### Vector Search는 어떤 Query에서 잘 동작했는가?

### 어떤 Query에서 실패했는가?

### Chunking 변경은 실제로 효과가 있었는가?

### Exact Keyword Retrieval에는 어떤 문제가 있었는가?

### Keyword / Hybrid Retrieval이 필요했는가?

### Fusion 전략은 실제 Ranking을 개선했는가?

### Reranker가 필요했는가?

### 품질 향상이 추가 Latency를 감수할 정도였는가?

### Retrieval Failure와 Generation Failure는 어떻게 달랐는가?

### No Answer Query에서 Hallucination은 어떻게 나타났는가?

### Agent는 적절한 Tool을 선택했는가?

### 여러 Tool이 필요한 질문은 어떻게 처리했는가?

### LangGraph가 실제로 필요했는가?

### 예상과 달리 효과가 없었던 기술은 무엇인가?

### 현재 시스템에 남아 있는 한계는 무엇인가?

---

## 최종 Experiment

```text
docs/experiments/009-final-comparison.md
```

---

## 최종 산출물

```text
content-ops-agent/

├── .cursor/
│   └── rules/
│       └── content-ops-agent.mdc
│
├── docs/
│   ├── REQUIREMENTS.md
│   ├── DESIGN.md
│   ├── ROADMAP.md
│   ├── TASKS.md
│   │
│   ├── adr/
│   │   └── 실제 발생한 중요한 설계 결정
│   │
│   └── experiments/
│       ├── 001-baseline.md
│       ├── 002-retrieval-failures.md
│       ├── 003-chunking-analysis.md
│       ├── 004-exact-keyword-retrieval.md
│       ├── 005-ranking-quality.md
│       ├── 006-generation-grounding.md
│       ├── 007-tool-selection.md
│       ├── 008-agent-workflow.md
│       └── 009-final-comparison.md
│
├── evaluation/
│   └── Evaluation Dataset / Runner
│
└── README.md
```

실제 진행 결과에 따라 Experiment 파일이 합쳐지거나 생략될 수 있다.

예를 들어 Agent Workflow 변경이 전혀 필요하지 않았다면 별도의 `008-agent-workflow.md` 없이:

```text
단순 Tool Calling으로 충분했다.
```

는 결론을 `007-tool-selection.md`에 포함할 수 있다.

---

## 완료 조건

```text
[ ] Baseline과 Final Retrieval 결과를 비교했다.

[ ] Query 유형별 결과를 비교했다.

[ ] Retrieval 실패 변화가 기록되어 있다.

[ ] Ranking 결과가 기록되어 있다.

[ ] Generation / Grounding 결과가 기록되어 있다.

[ ] No Answer 결과가 기록되어 있다.

[ ] Tool Selection 결과가 기록되어 있다.

[ ] Agent Workflow의 최종 구조와 이유를 설명할 수 있다.

[ ] 적용하지 않은 기술과 그 이유를 설명할 수 있다.

[ ] 품질과 Latency / 비용 Trade-off를 설명할 수 있다.

[ ] 예상과 다른 결과도 그대로 기록했다.

[ ] DESIGN이 최종 코드와 일치한다.

[ ] README에서 실행 및 Evaluation 방법을 확인할 수 있다.

[ ] Final Experiment가 작성되어 있다.
```

완료 후 Git checkpoint를 남긴다.

예:

```text
docs: complete content ops agent experiments
```

---

# 12. Phase 전환 규칙

AI Coding Assistant가 임의로 다음 Phase로 이동하지 않는다.

현재 Phase의 완료 조건을 먼저 확인한다.

```text
현재 Phase
    ↓
TASK 수행
    ↓
TEST
    ↓
EVALUATION / EXPERIMENT
    ↓
완료 조건 확인
    ↓
충족?
 ┌──┴──┐
NO    YES
│       │
현재    결과 문서화
Phase      ↓
계속    Git Checkpoint
           ↓
       TASKS 완료 상태 Commit
           ↓
       다음 Phase TASKS 생성
           ↓
          PLAN
```

현재 Phase 완료 조건이 충족되지 않았다면 다음 Phase로 넘어가지 않는다.

---

# 13. TASKS.md 운영 방식

`ROADMAP.md`는 프로젝트 전체의 진행 흐름을 관리한다.

`TASKS.md`에는 **현재 Phase 작업만 존재한다.**

예를 들어 Phase 1에서는:

```text
Current Phase: Phase 1

- [ ] Sample Document 작성
- [ ] PostgreSQL / pgvector 구성
- [ ] Document Loader 구현
- [ ] Chunking 구현
- [ ] Embedding 구현
- [ ] Vector Search 구현
- [ ] Question API 구현
- [ ] RAG Answer 구현
- [ ] Source 반환
- [ ] Evaluation Dataset 작성
- [ ] Evaluation Runner 구현
- [ ] Integration Test 작성
- [ ] README 작성
```

Phase 1이 완료되면 모든 Task가 완료된 상태에서 먼저 Commit한다.

```text
Phase 1 TASKS 완료
        ↓
Commit
        ↓
Phase 2 TASKS로 교체
```

이렇게 하면 현재 `TASKS.md`에는 과거 Phase가 남지 않지만 **Git History에서 각 Phase의 완료 상태를 확인할 수 있다.**

미래 Phase의 Task를 미리 TASKS에 추가하지 않는다.

---

# 14. Git Checkpoint 규칙

이번 프로젝트에서는 문제 재현과 해결 상태를 가능한 한 분리해서 남긴다.

기본 흐름:

```text
Baseline
   ↓
Commit

Evaluation
   ↓
실패 Case 재현
   ↓
Commit

해결 후보 선택
   ↓
구현
   ↓
Commit

재평가
   ↓
Commit
```

예:

```text
feat: implement baseline vector rag

experiment: evaluate baseline retrieval

experiment: reproduce exact keyword retrieval misses

feat: add selected retrieval strategy

experiment: evaluate retrieval strategy

experiment: reproduce ranking failures

feat: add selected ranking strategy
```

문제 재현과 해결을 하나의 Commit으로 묶지 않는 것을 원칙으로 한다.

---

# 15. Experiment 작성 규칙

Experiment 파일명은 기술이 아니라 문제 중심으로 작성한다.

좋은 예:

```text
retrieval-failures

chunking-analysis

exact-keyword-retrieval

ranking-quality

generation-grounding

tool-selection
```

피해야 할 예:

```text
add-bm25

add-rrf

add-reranker

add-langgraph
```

기본 형식:

```markdown
# Experiment 제목

## 목적

## 현재 상태

## 문제

## 가설

## Dataset

## 실험 조건

## 변경 변수

## Metric

## 결과

## 실패 Case

## 분석

## 대안

## 결정

## Before / After

## 결론
```

실제로 측정하지 않은 값은 작성하지 않는다.

가설이 틀린 경우에도 그대로 기록한다.

---

# 16. Evaluation Dataset 변경 규칙

Evaluation Dataset은 시스템 구현에 맞춰 임의로 수정하지 않는다.

예:

```text
구현이 특정 Query를 계속 실패함

↓

Expected Document 변경

↓

Metric 상승
```

방식으로 처리하지 않는다.

Evaluation Dataset 변경이 필요하다고 판단되면:

```text
문제 제시
 ↓
왜 기존 Expected가 잘못됐는지 설명
 ↓
Human Gate
 ↓
사용자 확인
 ↓
변경
```

순으로 처리한다.

Evaluation Dataset은 이번 프로젝트의 검증 Harness 일부로 취급한다.

---

# 17. DESIGN.md와의 관계

`ROADMAP.md`는 앞으로 어떤 문제를 평가할지 정의한다.

`DESIGN.md`는 **현재 코드의 실제 구조**만 정의한다.

따라서 ROADMAP에:

```text
Hybrid Retrieval

RRF

Reranker

Agent

LangGraph
```

가 등장한다고 해서 초기 DESIGN에 포함하지 않는다.

초기 DESIGN은 예를 들어 다음 정도여야 한다.

```text
Question API
     ↓
Embedding
     ↓
Vector Search
     ↓
pgvector
     ↓
LLM
```

이후 Experiment와 Human Gate를 거쳐 Hybrid Retrieval이 실제 채택됐다면:

```text
Experiment
    ↓
Human Gate
    ↓
ADR
    ↓
구현
    ↓
DESIGN 갱신
```

순으로 반영한다.

프로젝트 전체에서 `DESIGN.md` 하나만 유지한다.

---

# 18. ADR 작성 규칙

Phase가 바뀐다고 ADR을 작성하지 않는다.

```text
Phase 변경
≠
ADR
```

중요한 구조적 선택에만 ADR을 작성한다.

예:

```text
Chunking 기본 전략 변경

Retrieval 전략 변경

Fusion 전략 결정

Reranker 채택

Embedding Model 변경

Agent Workflow 구조 변경

Tool Routing 전략 변경

Vector Store 변경
```

반면:

```text
Baseline Evaluation 완료

No Answer 결과 확인

현재 전략 유지

Phase 이동
```

같은 작업에는 ADR이 필요하지 않을 수 있다.

---

# 19. Human Gate 규칙

다음 변경은 AI가 임의로 확정하지 않는다.

```text
Evaluation Dataset 기준 변경

Chunking 기본 전략 변경

Embedding Model 변경

Keyword Search 도입

Hybrid Retrieval 도입

Fusion 전략 결정

RRF 도입

Reranker 도입

Grounding Prompt 정책 변경

No Answer 정책 변경

LLM Model 변경

Tool 추가 / 제거

Tool Routing 전략 변경

Agent Workflow 변경

LangGraph 도입

Multi-Agent 도입

Vector Store 변경
```

Human Gate에서는 다음 순서로 보고한다.

```text
현재 실패 Case

↓

실제 Metric

↓

원인 분석

↓

가능한 대안

↓

각 대안 장점 / 단점

↓

Latency / 비용 영향

↓

추천안

↓

STOP

↓

사용자 선택
```

선택 이후 구현한다.

---

# 20. 프로젝트 전체 원칙

ContentOps Agent의 목적은 다음 구조를 만드는 것이 아니다.

```text
RAG 프로젝트니까

Vector DB
+
Hybrid Search
+
RRF
+
Reranker
+
LangGraph
+
Multi-Agent

전부 사용
```

항상 다음 순서를 따른다.

```text
현재 시스템
    ↓
Evaluation
    ↓
실패 Case
    ↓
원인 분석
    ↓
후보 비교
    ↓
Human Gate
    ↓
선택
    ↓
구현
    ↓
동일 Dataset 재평가
```

따라서 다음 결과도 정상적인 프로젝트 결과다.

```text
Chunking을 변경했지만 품질 차이가 거의 없었다.

Vector Search만으로 Exact Keyword를 충분히 처리했다.

Keyword Search는 일부 Query만 개선했다.

Hybrid Search가 전체 Metric을 개선하지 못했다.

RRF가 의미 있는 차이를 만들지 못했다.

Reranker가 MRR은 올렸지만 Latency 비용이 너무 컸다.

Prompt를 강화했지만 Hallucination이 완전히 사라지지 않았다.

단순 Tool Calling만으로 충분해 LangGraph를 사용하지 않았다.

Multi-Agent가 필요하지 않았다.
```

이번 프로젝트에서 중요한 것은 기술의 개수가 아니다.

최종 목표는:

> **RAG와 Agent 시스템을 Black Box로 취급하지 않고 Retrieval, Ranking, Generation, Grounding, Tool Selection을 각각 평가한 뒤 실제 실패 Case를 근거로 필요한 구조만 선택하고, 동일한 Evaluation Dataset에서 변경 결과를 다시 검증할 수 있는 AI Backend 개발 경험을 만드는 것**

이다.