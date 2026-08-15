# ContentOps Agent - DESIGN

## 1. 문서 목적

이 문서는 ContentOps Agent의 **현재 시스템 구조와 구현 기준**을 정의한다.

각 문서의 역할은 다음과 같다.

```text
REQUIREMENTS.md
→ 무엇을 만족해야 하는가

ROADMAP.md
→ 어떤 문제를 어떤 순서로 평가할 것인가

DESIGN.md
→ 현재 시스템은 실제로 어떻게 구현되어 있는가

TASKS.md
→ 현재 Phase에서 무엇을 구현할 것인가

adr/
→ 중요한 설계 결정을 왜 내렸는가

experiments/
→ 실제 Evaluation / Experiment 결과가 어땠는가

evaluation/
→ 반복 평가에 사용하는 Dataset과 실행 자산
```

`DESIGN.md`는 미래 구조를 미리 작성하는 문서가 아니다.

현재 코드에 존재하는 구조만 기록한다.

프로젝트 전체에서 하나의 `DESIGN.md`만 유지하며 실제 시스템 구조가 변경된 경우 현재 코드와 일치하도록 갱신한다.

---

# 2. 현재 설계 상태

현재 DESIGN은 다음 Phase를 기준으로 한다.

```text
Current Phase

Phase 1
Baseline Vector RAG 구현
```

현재 목표는 완성된 ContentOps Agent를 만드는 것이 아니다.

가장 단순한 운영 문서 기반 Vector RAG를 구현하여 이후 Evaluation의 기준점이 되는 Baseline을 확보한다.

현재 시스템 구조는 다음과 같다.

```text
Sample Policy Documents
          ↓
     Document Loader
          ↓
        Chunker
          ↓
     Embedding Model
          ↓
 PostgreSQL + pgvector


User
 ↓
Question API
 ↓
Question Embedding
 ↓
Vector Search
 ↓
Top K Chunks
 ↓
Context Builder
 ↓
LLM
 ↓
Answer + Sources
```

---

# 3. 현재 설계에 포함하지 않는 것

현재 Phase에서는 다음 기능을 구현하지 않는다.

```text
Keyword Search

Full Text Search

Hybrid Search

RRF

Reranker

Query Rewrite

Multi Query

Content DB Tool

Tool Calling

Agent Workflow

LangGraph

Multi-Agent

Long-term Memory

Knowledge Graph
```

ROADMAP에 위 기술이 등장하더라도 현재 코드에 존재하지 않는다면 DESIGN에 미리 추가하지 않는다.

특히 다음과 같은 구조를 처음부터 만들지 않는다.

```text
Vector Search
+
Keyword Search
+
RRF
+
Reranker
+
Agent
+
LangGraph
```

먼저 Vector Search Baseline을 평가한다.

---

# 4. 기본 설계 원칙

## 4.1 하나의 Spring Boot 애플리케이션으로 시작한다

초기 구조는 하나의 애플리케이션으로 구성한다.

```text
Client
   ↓
Spring Boot
   ├─ Document Ingestion
   ├─ Vector Retrieval
   ├─ RAG Generation
   └─ Evaluation
        ↓
PostgreSQL + pgvector
```

RAG API, Document 적재, Evaluation을 별도 Microservice로 분리하지 않는다.

현재 요구사항에서는 배포 독립성이나 장애 격리를 위해 서비스를 나눌 이유가 없다.

---

## 4.2 Retrieval과 Generation을 코드에서도 구분한다

RAG를 하나의 큰 Service로 구현하지 않는다.

개념적으로 다음 단계를 구분한다.

```text
Question
   ↓
Retrieval
   ↓
Retrieved Context
   ↓
Generation
   ↓
Answer
```

이를 통해 이후 Answer가 잘못됐을 때:

```text
Retrieval Failure

Ranking Failure

Generation Failure

Grounding Failure
```

를 구분할 수 있도록 한다.

---

## 4.3 Evaluation을 애플리케이션의 부가 기능으로 보지 않는다

Evaluation Runner는 프로젝트 종료 후 만드는 별도 Script가 아니다.

Baseline부터 시스템의 검증 Harness로 사용한다.

```text
Evaluation Dataset
        ↓
Evaluation Runner
        ↓
Retriever
        ↓
Actual Ranking
        ↓
Expected와 비교
        ↓
Metric 계산
```

Evaluation Dataset을 이용해 같은 조건에서 반복 실행할 수 있어야 한다.

---

## 4.4 Framework 내부 동작을 완전히 숨기지 않는다

RAG Framework를 사용하더라도 최소한 다음 값은 애플리케이션에서 확인할 수 있어야 한다.

```text
사용한 Query

Top K

Retrieved Document

Chunk

Rank

Similarity Score

Source Metadata

Retrieval Latency

LLM Latency
```

단순히:

```text
rag.chat(question)
```

결과만 받아오는 구조로 만들지 않는다.

Retrieval 결과를 분석할 수 있어야 이후 Experiment가 가능하기 때문이다.

---

# 5. 기술 스택

## Language

```text
Java 21
```

---

## Backend

```text
Spring Boot
Spring Web MVC
Bean Validation
```

---

## AI Integration

```text
Spring AI
```

Spring AI를 이용해 다음 역할을 추상화한다.

```text
Embedding Model

Chat Model

Vector Store
```

단, Framework가 제공하는 모든 RAG 기능을 처음부터 사용하지 않는다.

현재 Phase에서는 필요한 최소 기능만 사용한다.

---

## Vector Store

```text
PostgreSQL
+
pgvector
```

전용 Vector Database는 사용하지 않는다.

---

## Local AI Provider

기본 로컬 실행 환경에서는 다음 구조를 사용한다.

```text
Spring Boot
    ↓
Ollama
    ├─ Embedding Model
    └─ Chat Model
```

정확한 Model 이름은 코드에 Hard Coding하지 않는다.

환경 설정으로 관리한다.

예:

```text
app.ai.embedding-model

app.ai.chat-model
```

Evaluation 결과에는 실제 사용한 Model 이름을 반드시 기록한다.

---

## Serialization

```text
Jackson
```

Evaluation Dataset과 결과를 JSON / JSONL 형태로 처리하는 데 사용한다.

---

## Test

```text
JUnit 5
Spring Boot Test
Testcontainers
```

---

## Build

```text
Gradle
```

---

## Local Environment

```text
Docker
Docker Compose
```

초기 Docker 환경에는 PostgreSQL + pgvector만 포함한다.

Ollama는 Host 환경에서 실행하는 것을 기본으로 한다.

---

# 6. 전체 시스템 구조

현재 시스템의 Runtime 구조는 다음과 같다.

```text
                         User
                          │
                          │ HTTP
                          ▼
                ┌──────────────────┐
                │ QuestionController│
                └─────────┬────────┘
                          │
                          ▼
                 ┌────────────────┐
                 │ QuestionService│
                 └───────┬────────┘
                         │
                         ▼
                 ┌──────────────┐
                 │ RagService   │
                 └──────┬───────┘
                        │
               ┌────────┴────────┐
               │                 │
               ▼                 ▼
       ┌──────────────┐    ┌──────────────┐
       │ Retriever    │    │ContextBuilder│
       └──────┬───────┘    └──────┬───────┘
              │                    │
              ▼                    │
       Embedding Model             │
              │                    │
              ▼                    │
       PostgreSQL + pgvector       │
              │                    │
              └─────────┬──────────┘
                        ▼
                    Chat Model
                        │
                        ▼
                Answer + Sources
```

현재 Retriever는 Vector Search 하나만 사용한다.

---

# 7. Document Ingestion 구조

질문 처리와 별도로 운영 문서를 Vector Store에 적재하는 흐름이 존재한다.

```text
Sample Documents
       ↓
Document Loader
       ↓
Document Parser
       ↓
Chunker
       ↓
Chunk Metadata 생성
       ↓
Embedding Model
       ↓
Vector Store
```

Document 적재는 일반 사용자 API가 아니다.

로컬 개발과 Evaluation을 위한 별도의 실행 모드로 제공한다.

---

# 8. Repository 구조

초기 Repository 구조는 다음과 같이 구성한다.

```text
content-ops-agent/
│
├── .cursor/
│   └── rules/
│       └── content-ops-agent.mdc
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │
│   └── test/
│       └── java/
│
├── data/
│   └── documents/
│       ├── age-rating-policy.md
│       ├── content-status-policy.md
│       ├── metadata-guide.md
│       ├── publishing-guide.md
│       ├── youth-protection-policy.md
│       └── operations-faq.md
│
├── evaluation/
│   ├── datasets/
│   │   └── retrieval.jsonl
│   │
│   └── results/
│
├── docs/
│   ├── REQUIREMENTS.md
│   ├── DESIGN.md
│   ├── ROADMAP.md
│   ├── TASKS.md
│   │
│   ├── adr/
│   │
│   └── experiments/
│
├── docker/
│   └── docker-compose.yml
│
├── build.gradle
├── settings.gradle
└── README.md
```

현재 Phase에서는 다음 디렉터리를 추가하지 않는다.

```text
reranker/

agent/

tools/

langgraph/
```

실제 기능이 추가된 이후 필요한 구조만 만든다.

---

# 9. Package 구조

기능 중심으로 Package를 구성한다.

```text
com.contentopsagent

├── question
│   ├── controller
│   ├── dto
│   └── service
│
├── document
│   ├── loader
│   ├── chunk
│   └── model
│
├── retrieval
│   ├── service
│   └── model
│
├── rag
│   ├── service
│   ├── prompt
│   └── model
│
├── evaluation
│   ├── runner
│   ├── dataset
│   ├── metric
│   └── result
│
└── common
    ├── config
    └── exception
```

사용처가 하나뿐인 Interface를 기본적으로 만들지 않는다.

예:

```text
RagService
RagServiceImpl

Retriever
VectorRetriever
```

처럼 미래 확장만을 위한 추상화는 피한다.

다만 Spring AI의 Model / VectorStore 같은 외부 추상화는 그대로 사용할 수 있다.

---

# 10. Sample Document

초기 Dataset은 프로젝트에서 직접 만든 Markdown 파일을 사용한다.

예:

```text
age-rating-policy.md

content-status-policy.md

metadata-guide.md

publishing-guide.md

youth-protection-policy.md

operations-faq.md
```

실제 회사 내부 데이터를 사용하지 않는다.

각 문서는 다음 조건을 일부러 포함한다.

```text
의미적으로 겹치는 내용

비슷한 용어

정확한 Code

정확한 Status 값
```

예:

```text
OPS-101

M-03

CONTENT_BLOCKED

AGE_REVIEW_REQUIRED
```

이를 통해 이후 Retrieval 실패를 실험할 수 있도록 한다.

---

# 11. Document 모델

원본 문서는 논리적으로 다음 정보를 가진다.

```text
Document

documentId

documentName

content
```

Document를 Chunk로 변환한 이후에는 다음 Metadata를 유지한다.

```text
DocumentChunk

chunkId

documentId

documentName

section

chunkIndex

content
```

Embedding Vector는 Vector Store가 관리한다.

---

# 12. Baseline Chunking

초기에는 단순한 Fixed-size Chunking 전략을 사용한다.

예:

```text
Chunk Size
약 500 tokens

Overlap
약 50 tokens
```

이 값은 최적값이라는 의미가 아니다.

Phase 3에서 비교할 **Baseline 조건**이다.

설정값은 코드에 직접 박지 않고 외부 설정으로 관리한다.

예:

```text
app.rag.chunk-size

app.rag.chunk-overlap
```

초기 Evaluation 결과를 확보하기 전에는 값을 변경하지 않는다.

---

# 13. Chunk Metadata

각 Chunk에는 최소한 다음 Metadata를 함께 저장한다.

```json
{
  "documentId": "age-rating-policy",
  "documentName": "age-rating-policy.md",
  "section": "3.2 공개 조건",
  "chunkIndex": 3
}
```

검색 결과에서 이 Metadata를 다시 확인할 수 있어야 한다.

이를 이용해:

```text
어느 문서에서 검색됐는가?

어느 Section인가?

몇 번째 Chunk인가?
```

를 추적한다.

---

# 14. Vector Store

Vector Store는 PostgreSQL + pgvector를 사용한다.

논리적으로 하나의 Vector Record에는 다음 값이 존재한다.

```text
id

content

metadata

embedding
```

구체적인 물리 Table 구조는 Spring AI의 pgvector Integration을 우선 사용한다.

단, Evaluation을 위해 다음 정보는 반드시 검색 결과에서 복원할 수 있어야 한다.

```text
content

documentName

section

chunkIndex

similarity score
```

Vector Store의 내부 Table 구조를 Domain Model처럼 과도하게 추상화하지 않는다.

---

# 15. Document 적재 방식

초기 Document 적재는 별도의 관리자 UI나 공개 API를 만들지 않는다.

로컬 개발용 실행 모드를 사용한다.

개념적인 실행:

```text
Document Ingestion Mode
        ↓
기존 Sample Document Vector 삭제
        ↓
data/documents 읽기
        ↓
Chunk 생성
        ↓
Embedding 생성
        ↓
pgvector 저장
```

동일 Dataset을 반복 적재했을 때 Document가 계속 중복 생성되지 않아야 한다.

Baseline 실험에서는 다음과 같은 흐름을 사용한다.

```text
Reset
 ↓
Ingest
 ↓
Evaluate
```

이를 반복할 수 있어야 한다.

---

# 16. Question API

## Endpoint

```http
POST /api/v1/questions
```

---

## Request

```json
{
  "question": "15세 콘텐츠의 공개 조건이 뭐야?"
}
```

`question`은 필수다.

다음 요청은 거절한다.

```text
null

빈 문자열

공백 문자열
```

---

## Response

예:

```json
{
  "answer": "15세 콘텐츠는 연령 등급 검수가 완료된 이후 공개할 수 있습니다.",
  "sources": [
    {
      "document": "age-rating-policy.md",
      "section": "3.2 공개 조건"
    }
  ]
}
```

일반 API Response에는 내부 Similarity Score나 전체 Prompt를 노출하지 않는다.

---

# 17. Question 처리 흐름

현재 Question 처리 과정은 다음과 같다.

```text
POST /api/v1/questions
        ↓
Question Validation
        ↓
Question Embedding
        ↓
Vector Similarity Search
        ↓
Top K Chunks
        ↓
Context Builder
        ↓
Prompt 생성
        ↓
Chat Model
        ↓
Answer
        ↓
Source Metadata 조합
        ↓
Response
```

현재 단계에서는 질문 유형을 분류하지 않는다.

모든 Baseline 질문은 운영 문서 RAG 대상으로 취급한다.

---

# 18. Vector Retrieval

현재 Retriever는 하나다.

```text
Vector Similarity Search
```

초기 `Top K`는 설정값으로 관리한다.

예:

```text
app.rag.top-k=5
```

`Top K = 5`는 최적값이라는 의미가 아니다.

Baseline 비교 조건으로 사용한다.

Phase 2 Evaluation이 완료되기 전에는 임의로 변경하지 않는다.

---

# 19. Retrieval Result

애플리케이션 내부에서는 검색 결과를 다음 형태로 표현할 수 있다.

```text
RetrievedChunk

rank

content

documentName

section

chunkIndex

similarityScore
```

이 객체는 사용자 응답보다는 Evaluation과 Debug에 사용한다.

이를 통해 다음을 기록할 수 있다.

```text
Expected Document Rank

Top K

Similarity

Retrieval Latency
```

---

# 20. Context Builder

검색된 Top K Chunk를 LLM Context로 변환한다.

개념적인 구조:

```text
[Source 1]
Document: age-rating-policy.md
Section: 3.2 공개 조건

Content:
...


[Source 2]
Document: youth-protection-policy.md
Section: 2.1 청소년 보호

Content:
...
```

Source 정보를 Context에 같이 포함하여 LLM이 답변 근거를 구분할 수 있도록 한다.

현재 단계에서는 Context Compression이나 별도의 Context Ranking을 사용하지 않는다.

---

# 21. Baseline Prompt

Baseline Prompt는 Grounding 규칙과 Phase 6 Human Gate에서 선택한 보강 규칙을 사용한다.

```text
제공된 Context를 기준으로 질문에 답한다.

질문의 코드, 상태값, 절차가 Context에 있으면
그 내용을 빠뜨리지 말고 답한다.

Context에 관련 내용이 있으면 거절하지 않는다.

사용하지 않는 경우만 답하지 말고,
사용하는 시점도 함께 답한다.

답변은 한국어만 사용한다.

내부 정책이나 사실을 추측해서 만들지 않는다.

답변은 간결하게 작성한다.

Context에 근거가 없으면
현재 제공된 문서에서는 확인할 수 없습니다.
```

이 Prompt를 최종 최적안이라고 보지 않는다.

변경 이유와 재평가 결과는 `docs/experiments/006-generation-grounding.md`에 있다.

---

# 22. Source 생성

Source는 LLM이 임의로 생성한 Citation 문자열을 그대로 믿지 않는다.

Retriever가 실제 반환한 Chunk Metadata를 기반으로 Source를 구성한다.

```text
RetrievedChunk
    ↓
documentName
section
    ↓
Source Response
```

즉:

```text
LLM이 말한 Source
```

가 아니라:

```text
실제로 검색된 Source
```

를 응답에 사용한다.

---

# 23. No Answer 처리

Baseline Prompt에는 근거가 없으면 답하지 않는 기본 규칙을 포함한다.

예:

```text
현재 제공된 문서에서는 확인할 수 없습니다.
```

다만 이것만으로 Hallucination이 완전히 방지된다고 가정하지 않는다.

Evaluation Dataset에 No Answer Query를 포함하고 실제 동작을 Phase 2와 Phase 6에서 평가한다.

---

# 24. Evaluation Dataset

Baseline Evaluation Dataset은 다음 경로에서 관리한다.

```text
evaluation/datasets/retrieval.jsonl
```

각 Row는 최소한 다음 정보를 가진다.

예:

```json
{
  "id": "retrieval-001",
  "category": "SEMANTIC",
  "question": "15세 콘텐츠를 공개하려면 어떤 검수가 필요해?",
  "expectedDocuments": [
    "age-rating-policy.md"
  ],
  "answerable": true
}
```

Exact Keyword:

```json
{
  "id": "retrieval-002",
  "category": "EXACT_KEYWORD",
  "question": "M-03 오류의 의미가 뭐야?",
  "expectedDocuments": [
    "metadata-guide.md"
  ],
  "answerable": true
}
```

No Answer:

```json
{
  "id": "retrieval-003",
  "category": "NO_ANSWER",
  "question": "해외 판권 계약 담당자가 누구야?",
  "expectedDocuments": [],
  "answerable": false
}
```

초기 Category:

```text
SEMANTIC

EXACT_KEYWORD

SIMILAR_DOCUMENT

NO_ANSWER
```

Agent 관련 평가 항목은 아직 넣지 않는다.

Phase 7 이후 별도 Dataset으로 추가한다.

---

# 25. Evaluation Runner

Evaluation Runner는 HTTP API를 반복 호출하는 방식이 아니라 애플리케이션 내부 Retrieval Service를 직접 사용할 수 있도록 한다.

개념적인 흐름:

```text
retrieval.jsonl
      ↓
Evaluation Runner
      ↓
Retriever
      ↓
Top K
      ↓
Expected Documents와 비교
      ↓
Metric Aggregation
```

Evaluation Runner는 최소한 다음 결과를 생성한다.

```text
전체 Query 수

Hit Rate@K

Recall@K

MRR

Query별 Expected Document

Query별 Actual Top K

Expected Document Rank

Retrieval Latency
```

LLM Answer 평가가 필요한 경우 Generation 결과도 같이 기록할 수 있지만 Phase 1의 핵심 Evaluation은 Retrieval이다.

---

# 26. Hit Rate@K

Query의 Expected Document 중 하나 이상이 Top K 안에 존재하는지 확인한다.

예:

```text
Expected

age-rating-policy.md


Top 5

1. publishing-guide.md
2. youth-protection-policy.md
3. age-rating-policy.md
4. operations-faq.md
5. metadata-guide.md


↓

Hit = 1
```

---

# 27. Recall@K

Expected Document가 여러 개 존재하는 Query에서도 평가할 수 있도록 Recall을 계산한다.

개념:

```text
Top K에서 찾은 Expected Document 수
──────────────────────────────
전체 Expected Document 수
```

Expected Document가 하나인 Query에서는 Hit Rate와 같은 결과가 나올 수 있다.

---

# 28. MRR

Expected Document가 처음 등장한 Rank를 기준으로 계산한다.

예:

```text
Expected Rank = 1
→ 1.0

Expected Rank = 2
→ 0.5

Expected Rank = 4
→ 0.25
```

이를 전체 Query에서 평균한다.

Ranking 품질 분석에서 주요 기준으로 사용한다.

---

# 29. Evaluation 결과

Evaluation 실행 결과는 기계가 읽을 수 있는 형태로 출력할 수 있어야 한다.

예:

```text
evaluation/results/
```

결과에는 최소한 다음 조건을 포함한다.

```text
실행 시각

Document Dataset

Evaluation Dataset

Embedding Model

Chat Model

Chunk Size

Chunk Overlap

Top K

Retrieval Strategy

전체 Metric

Query별 결과
```

단, 모든 실행 결과를 Git에 영구 보관할 필요는 없다.

Experiment의 근거가 되는 대표 실행 결과와 Metric은:

```text
docs/experiments/
```

에 정리한다.

---

# 30. Evaluation Dataset 변경

Evaluation Dataset은 코드가 실패한다고 임의로 수정하지 않는다.

다음 변경은 Human Gate 대상이다.

```text
Question 삭제

Expected Document 변경

answerable 변경

평가 Category 변경
```

Dataset이 실제로 잘못됐다고 판단되면 이유를 먼저 설명한 뒤 변경한다.

---

# 31. Latency 측정

현재 Pipeline에서는 최소한 다음 시간을 분리해 확인한다.

```text
Embedding Latency

Retrieval Latency

LLM Latency

End-to-End Latency
```

현재 존재하지 않는:

```text
Keyword Search Latency

Reranking Latency

Tool Latency
```

는 측정하지 않는다.

별도의 Prometheus / Grafana Stack은 현재 Phase에서 추가하지 않는다.

Evaluation Runner와 Application Log를 통해 Baseline 값을 확보한다.

필요성이 생긴 경우 이후 Observability 구조를 검토한다.

---

# 32. Logging

Question 처리 과정에서 최소한 다음 정보를 확인할 수 있어야 한다.

```text
requestId

question

topK

retrieved document

rank

similarity score

retrieval latency

LLM latency

total latency
```

전체 Prompt 또는 Sample Document 전체 내용을 기본 Log에 출력하지 않는다.

실제 운영 환경에서는 내부 정책이나 사용자 질문이 민감 정보가 될 수 있기 때문이다.

---

# 33. Error Handling

API Error Response 형식은 통일한다.

예:

```json
{
  "code": "INVALID_QUESTION",
  "message": "질문을 입력해주세요."
}
```

초기 주요 오류:

```text
INVALID_QUESTION

EMBEDDING_FAILED

VECTOR_SEARCH_FAILED

LLM_GENERATION_FAILED
```

내부 Provider 오류를 그대로 외부 Response에 노출하지 않는다.

---

# 34. Test 전략

## 34.1 Unit Test

다음과 같이 외부 시스템 없이 검증 가능한 로직을 테스트한다.

```text
Question Validation

Chunk Metadata 생성

Context Builder

Hit Rate 계산

Recall 계산

MRR 계산

Evaluation 결과 집계
```

---

## 34.2 Vector Store Integration Test

Testcontainers 기반 PostgreSQL + pgvector를 사용한다.

다음 흐름을 검증한다.

```text
Vector 저장

↓

Similarity Search

↓

Top K 결과 확인
```

Test 자체가 외부 Ollama 상태에 의존하지 않도록 필요하면 결정적인 Test Embedding을 사용한다.

---

## 34.3 RAG Integration Test

RAG Pipeline의 구조가 정상적으로 연결되는지 확인한다.

```text
Question

↓

Retriever

↓

Context Builder

↓

Chat Model

↓

Answer
```

일반 CI Test에서는 외부 LLM의 확률적인 응답에 강하게 의존하지 않는다.

필요하면 Test Double을 사용한다.

---

## 34.4 실제 AI Evaluation

실제 Retrieval 품질 평가는 Unit Test와 분리한다.

```text
Test
→ 코드가 정상 동작하는가


Evaluation
→ 실제 Model에서 검색 품질이 어떤가
```

Evaluation Runner는 실제 설정된 Embedding Model을 사용한다.

따라서:

```text
JUnit 통과
=
Retrieval 품질 우수
```

로 판단하지 않는다.

---

# 35. PostgreSQL Test Environment

Integration Test에서는 pgvector Extension이 포함된 PostgreSQL 환경을 사용한다.

```text
Testcontainers

↓

PostgreSQL + pgvector
```

실제 Vector Search SQL이 동작하는 환경에서 검증한다.

단순 Mock Repository만으로 Vector Retrieval을 검증하지 않는다.

---

# 36. Local Environment

초기 로컬 환경은 다음과 같다.

```text
Docker

└── PostgreSQL + pgvector


Host

├── Spring Boot
└── Ollama
```

이 구조를 선택하는 이유는 단순하다.

```text
PostgreSQL
→ 반복 가능한 Container 환경

Ollama
→ 개발자의 로컬 Model 환경 활용
```

초기에는 AI Model까지 Docker Compose로 묶지 않는다.

Model 다운로드와 GPU / Memory 설정까지 Compose에 포함하는 것은 현재 프로젝트의 핵심이 아니다.

---

# 37. Configuration

환경에 따라 달라지는 값은 설정으로 관리한다.

예:

```text
app.rag.top-k

app.rag.chunk-size

app.rag.chunk-overlap

app.ai.embedding-model

app.ai.chat-model
```

Database 정보:

```text
spring.datasource.*
```

AI Provider 정보 역시 환경 설정으로 관리한다.

Model 이름을 Java 코드에 Hard Coding하지 않는다.

---

# 38. Baseline 재현 조건

Phase 1 이후 Evaluation 결과를 비교할 수 있도록 다음 조건을 명시적으로 기록한다.

```text
Document Dataset Version

Evaluation Dataset Version

Embedding Model

Chat Model

Chunk Size

Chunk Overlap

Top K

Retrieval Strategy
```

Baseline Retrieval Strategy:

```text
Vector Search Only
```

이 조건은 Experiment 문서에 함께 기록한다.

---

# 39. 현재 시스템에서 의도적으로 존재하는 한계

현재 시스템은 완성된 RAG / Agent 시스템이 아니다.

다음 한계가 의도적으로 존재한다.

## Vector Search Only

```text
Semantic Similarity만 사용
```

Exact Keyword Query에서 실패할 수 있다.

Phase 4에서 확인한다.

---

## 단일 Chunking 전략

```text
Fixed Size
```

가 최적인지 아직 모른다.

Phase 3에서 확인한다.

---

## 별도 Ranking 단계 없음

Vector Search의 Ranking을 그대로 사용한다.

```text
Retriever Rank
=
Final Context Rank
```

Phase 5에서 실제 문제가 있는지 평가한다.

---

## 기본 Prompt

복잡한 Prompt Engineering 스택을 두지 않는다.

Phase 6 Human Gate에서 후보 B를 적용했다. 규칙은 `BaselinePrompt`와 `# 21. Baseline Prompt`를 따른다.

---

## 운영 문서 질문만 처리

현재 시스템은 다음 질문을 처리하지 않는다.

```text
이번 달 공개 예정 콘텐츠 알려줘.

콘텐츠 100번 상태 알려줘.

콘텐츠 100번이 왜 공개되지 않아?
```

Content Metadata Tool은 Phase 7 이후 추가한다.

---

## Agent 없음

현재 흐름은 항상 동일하다.

```text
Question
 ↓
Vector Retrieval
 ↓
LLM
```

Tool Routing이나 Agent 판단은 존재하지 않는다.

---

# 40. Phase 1 완료 조건

다음 조건을 만족하면 현재 Baseline DESIGN의 구현이 완료된 것으로 판단한다.

## Environment

```text
[ ] Docker Compose로 PostgreSQL + pgvector 실행 가능

[ ] Spring Boot 실행 가능

[ ] Ollama 연결 가능
```

---

## Document

```text
[ ] Sample Policy Document 존재

[ ] 반복 가능한 Document 적재 가능

[ ] Document Chunk 생성 가능

[ ] documentName 추적 가능

[ ] section 추적 가능

[ ] chunkIndex 추적 가능
```

---

## Embedding / Vector Store

```text
[ ] Document Embedding 생성 가능

[ ] pgvector 저장 가능

[ ] Question Embedding 생성 가능

[ ] Vector Search 가능

[ ] Top K 결과 확인 가능

[ ] Similarity Score 확인 가능
```

---

## RAG

```text
[ ] POST /api/v1/questions 구현

[ ] Question Validation

[ ] Retrieval 수행

[ ] Context 생성

[ ] LLM Answer 생성

[ ] Source 반환
```

---

## Evaluation

```text
[ ] retrieval.jsonl 존재

[ ] Semantic Query 존재

[ ] Exact Keyword Query 존재

[ ] Similar Document Query 존재

[ ] No Answer Query 존재

[ ] Evaluation Runner 실행 가능

[ ] Hit Rate@K 계산 가능

[ ] Recall@K 계산 가능

[ ] MRR 계산 가능

[ ] Query별 Ranking 확인 가능
```

---

## Test

```text
[ ] Unit Test 통과

[ ] pgvector Integration Test 통과

[ ] RAG Integration Test 통과
```

---

## README

```text
[ ] 프로젝트 목적 확인 가능

[ ] PostgreSQL / pgvector 실행 방법 존재

[ ] Ollama 준비 방법 존재

[ ] Document 적재 방법 존재

[ ] Question API 호출 방법 존재

[ ] Test 실행 방법 존재

[ ] Evaluation 실행 방법 존재
```

---

## Scope

다음 기능이 아직 구현되지 않았음을 확인한다.

```text
[ ] Keyword Search 없음

[ ] Hybrid Search 없음

[ ] RRF 없음

[ ] Reranker 없음

[ ] Content Tool 없음

[ ] Agent 없음

[ ] LangGraph 없음
```

위 조건을 만족하면 ROADMAP의 Phase 2로 이동한다.

---

# 41. DESIGN 변경 규칙

Phase가 변경됐다는 이유만으로 DESIGN을 수정하지 않는다.

```text
Phase 변경
≠
DESIGN 변경
```

예를 들어 Phase 2는 현재 Baseline을 평가하는 단계이므로 시스템 구조가 변하지 않는다면 DESIGN도 그대로 유지한다.

```text
Phase 1

Vector Search
 ↓
LLM


Phase 2

Vector Search
 ↓
LLM
```

반면 Phase 4에서 Evaluation 결과를 기반으로 Hybrid Retrieval을 실제 선택했다면 그때 구조를 변경한다.

예:

```text
Before

Question
   ↓
Vector Search


After

Question
   ↓
 ┌─────────┴─────────┐
 ↓                   ↓
Vector             Keyword
 ↓                   ↓
 └─────────┬─────────┘
           ↓
         Fusion
```

그리고 DESIGN을 현재 코드에 맞게 갱신한다.

---

# 42. DESIGN 변경 절차

중요한 구조 변경은 다음 순서를 따른다.

```text
Evaluation
    ↓
실패 Case 확인
    ↓
Experiment
    ↓
원인 분석
    ↓
대안 비교
    ↓
Human Gate
    ↓
선택
    ↓
ADR
    ↓
구현
    ↓
재평가
    ↓
DESIGN 갱신
```

단순한 Phase 이동 자체는 ADR이나 DESIGN 변경 사유가 아니다.

---

# 43. DESIGN과 ADR의 관계

DESIGN에는:

> **현재 무엇을 사용하고 있는가**

를 기록한다.

ADR에는:

> **왜 그것을 선택했는가**

를 기록한다.

예를 들어 Hybrid Retrieval이 최종 선택됐다면:

ADR:

```text
왜 Vector Search Only를 유지하지 않았는가?

왜 다른 대안 대신 현재 Fusion 방식을 선택했는가?
```

DESIGN:

```text
현재 Retrieval Pipeline은
Vector + Keyword + 선택된 Fusion 방식을 사용한다.
```

---

# 44. DESIGN과 Experiment의 관계

Experiment에는:

```text
Dataset

조건

Baseline Metric

실패 Query

가설

변경

결과

Before / After

결론
```

을 기록한다.

DESIGN에는 실험 숫자를 복사하지 않는다.

예:

```text
MRR

0.62 → 0.78
```

같은 값은 Experiment에 기록한다.

최종 Retrieval 구조가 바뀌었다면 DESIGN에는 현재 구조만 기록한다.

---

# 45. 설계의 핵심 원칙

ContentOps Agent의 초기 구조는 의도적으로 단순하다.

```text
Documents
   ↓
Chunk
   ↓
Embedding
   ↓
pgvector


Question
   ↓
Vector Search
   ↓
Context
   ↓
LLM
   ↓
Answer + Source
```

프로젝트는 이 구조를 정답으로 가정하지 않는다.

앞으로 다음 과정을 반복한다.

```text
현재 구조
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
구조 변경
   ↓
동일 Dataset 재평가
   ↓
DESIGN 갱신
```

따라서 최종 시스템에서:

```text
Keyword Search가 없을 수도 있다.

RRF가 없을 수도 있다.

Reranker가 없을 수도 있다.

LangGraph를 사용하지 않을 수도 있다.

Multi-Agent가 없을 수도 있다.
```

모두 정상적인 결과다.

중요한 것은 특정 AI 기술을 많이 적용하는 것이 아니다.

최종적으로 `DESIGN.md`는:

> **현재 ContentOps Agent가 어떤 Retrieval과 Generation 구조로 동작하는지 설명하는 최신 설계의 단일 기준 문서**

로 유지한다.