# ContentOps Agent - REQUIREMENTS

## 1. 문서 목적

이 문서는 ContentOps Agent 프로젝트가 **무엇을 만족해야 하는지** 정의한다.

구현 구조와 구체적인 기술 선택은 `DESIGN.md`에서 관리하고, 프로젝트 진행 순서와 실험 단계는 `ROADMAP.md`에서 관리한다.

```text
REQUIREMENTS.md
→ 무엇을 만족해야 하는가

DESIGN.md
→ 현재 시스템은 어떻게 구현되어 있는가

ROADMAP.md
→ 어떤 문제를 어떤 순서로 평가할 것인가

TASKS.md
→ 현재 Phase에서 무엇을 할 것인가
```

이 문서는 특정 기술을 정답으로 고정하지 않는다.

다음 기술은 요구사항을 만족하기 위한 **후보**일 뿐 반드시 사용해야 하는 것은 아니다.

```text
Keyword Search
Hybrid Search
RRF
Reranker
Query Rewrite
Multi Query
LangGraph
Multi-Agent
Schema-specific Tool Routing
```

기술 도입 여부는 Evaluation 결과와 실제 실패 Case를 기준으로 결정한다.

---

# 2. 프로젝트 목표

ContentOps Agent는 OTT 콘텐츠 운영자가 자연어로 질문하면 **운영 문서와 콘텐츠 데이터를 조회하여 근거 있는 답변을 생성하는 AI Backend**다.

최종적으로 다음과 같은 질문을 처리할 수 있어야 한다.

```text
"15세 콘텐츠 공개 조건이 뭐야?"

"M-03 오류가 발생하는 이유는?"

"이번 달 공개 예정 액션 콘텐츠 알려줘."

"콘텐츠 100번 현재 상태 알려줘."

"콘텐츠 100번이 왜 공개되지 않는지 정책 기준으로 설명해줘."
```

질문에 따라 다음 정보를 사용할 수 있다.

```text
운영 정책 문서

콘텐츠 Metadata
```

최종 목표는 단순히 RAG나 Agent Framework를 사용하는 것이 아니다.

다음 과정을 실제 Evaluation 결과로 설명할 수 있어야 한다.

```text
Baseline
    ↓
Evaluation
    ↓
실패 Case 확인
    ↓
원인 분석
    ↓
대안 비교
    ↓
개선
    ↓
동일 Dataset 재평가
```

---

# 3. 서비스 범위

ContentOps Agent는 다음 역할을 담당한다.

```text
자연어 질문 수신

운영 문서 검색

검색 결과 기반 답변 생성

답변 근거 제공

Retrieval 품질 평가

Generation / Grounding 평가

콘텐츠 데이터 조회

질문에 맞는 Tool 선택

여러 Tool 결과 조합

Agent 품질 평가
```

실제 콘텐츠 관리 CMS나 운영 시스템 전체를 구현하지 않는다.

---

# 4. 데이터 범위

## 4.1 운영 문서

프로젝트용 Sample 운영 문서를 사용한다.

최소한 다음 종류의 문서를 준비한다.

```text
연령 등급 정책

콘텐츠 공개 상태 정책

Metadata 작성 가이드

콘텐츠 공개 가이드

청소년 보호 정책

운영 FAQ
```

일부 문서는 의미적으로 유사한 내용을 포함해야 한다.

예:

```text
연령 등급 정책

청소년 보호 정책

콘텐츠 공개 정책
```

Retrieval 전략의 차이를 확인하기 위한 목적이다.

또한 정확한 문자열 검색이 필요한 값을 포함한다.

```text
OPS-101

CONTENT_BLOCKED

M-03

AGE_REVIEW_REQUIRED
```

---

## 4.2 콘텐츠 데이터

Agent 단계에서 구조화된 콘텐츠 Metadata를 조회할 수 있어야 한다.

콘텐츠는 최소한 다음 값을 가진다.

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

실제 OTT Domain 전체를 재현하지 않는다.

Tool Calling과 구조화된 데이터 조회를 실험할 수 있는 수준으로 제한한다.

---

# 5. 기능 요구사항

## FR-01. 자연어 질문 수신

사용자는 HTTP API를 통해 자연어 질문을 전달할 수 있어야 한다.

```http
POST /api/v1/questions
```

예:

```json
{
  "question": "15세 콘텐츠의 공개 조건이 뭐야?"
}
```

빈 질문이나 유효하지 않은 요청은 거절해야 한다.

---

## FR-02. 운영 문서 적재

운영 문서를 검색 가능한 형태로 반복적으로 적재할 수 있어야 한다.

관리자 UI는 필요하지 않다.

다음 중 적절한 방식으로 구현할 수 있다.

```text
Application Startup

CLI

Batch

Fixture
```

구체적인 적재 방식은 DESIGN에서 결정한다.

---

## FR-03. Document Chunk 생성

운영 문서를 검색 가능한 단위로 나눌 수 있어야 한다.

Chunk는 최소한 다음 정보를 추적할 수 있어야 한다.

```text
chunkId

documentId

documentName

section

chunkIndex

content
```

초기에는 하나의 단순한 Chunking 전략으로 시작한다.

Chunking 최적화는 Baseline Evaluation 이전에 수행하지 않는다.

---

## FR-04. Embedding 생성

Document Chunk를 Embedding Vector로 변환할 수 있어야 한다.

사용자 질문도 동일한 Embedding 기준으로 변환할 수 있어야 한다.

---

## FR-05. Vector 저장

Document Chunk의 Embedding을 Vector Store에 저장할 수 있어야 한다.

초기 Vector Store는 다음을 사용한다.

```text
PostgreSQL
+
pgvector
```

검색 결과에서 Chunk 원문과 Metadata를 다시 확인할 수 있어야 한다.

---

## FR-06. Vector Search

사용자 질문과 의미적으로 관련된 Document Chunk를 검색할 수 있어야 한다.

Baseline에서는 **Vector Similarity Search만 사용한다.**

```text
Question
   ↓
Embedding
   ↓
Vector Search
   ↓
Top K Chunks
```

Keyword Search나 Hybrid Search는 Baseline에 포함하지 않는다.

---

## FR-07. RAG 답변 생성

검색된 Chunk를 Context로 사용해 질문에 대한 답변을 생성할 수 있어야 한다.

```text
Question
   ↓
Retrieval
   ↓
Context
   ↓
LLM
   ↓
Answer
```

답변은 가능한 한 제공된 Context에 근거해야 한다.

---

## FR-08. Source 제공

최종 답변에서 사용한 근거를 확인할 수 있어야 한다.

최소한 다음 정보가 포함되어야 한다.

```text
Document Name

Section
```

필요한 경우 Chunk 정보도 제공할 수 있다.

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

---

## FR-09. Retrieval 결과 추적

Evaluation 및 Debug를 위해 Retriever가 어떤 결과를 반환했는지 확인할 수 있어야 한다.

최소한 다음 정보를 확인한다.

```text
Rank

Document

Chunk

Similarity

Metadata
```

일반 사용자 응답에 모든 Debug 정보를 노출할 필요는 없다.

---

## FR-10. Retrieval Evaluation

정의된 Evaluation Dataset을 이용하여 Retrieval 품질을 반복적으로 평가할 수 있어야 한다.

최소한 다음 Metric을 계산할 수 있어야 한다.

```text
Hit Rate@K

Recall@K

MRR
```

실패 Query를 개별적으로 확인할 수 있어야 한다.

---

## FR-11. Chunking 비교

동일 Document Dataset과 동일 Evaluation Dataset을 사용하여 서로 다른 Chunking 전략을 비교할 수 있어야 한다.

예:

```text
Fixed Size A
vs
Fixed Size B
```

또는:

```text
Fixed Size
vs
Section Based
```

Chunking 변경의 효과는 Evaluation 결과를 통해 판단한다.

---

## FR-12. Exact Keyword 검색 대응

Vector Search가 다음과 같은 Exact Keyword Query에서 실제 실패하는 경우 이를 개선할 수 있어야 한다.

```text
OPS-101

M-03

CONTENT_BLOCKED
```

Keyword Search는 가능한 해결 후보 중 하나다.

Baseline에는 적용하지 않는다.

---

## FR-13. 복수 Retrieval 결과 결합

서로 다른 Retrieval 방식이 필요하다고 판단되면 결과를 하나의 Ranking으로 결합할 수 있어야 한다.

예:

```text
Vector Search
      +
Keyword Search
      ↓
Combined Ranking
```

구체적인 Fusion 방식은 요구사항에서 고정하지 않는다.

`RRF`는 후보 중 하나다.

---

## FR-14. Ranking 개선

정답 문서가 검색되지만 Ranking 품질에 문제가 확인될 경우 Ranking을 개선할 수 있는 구조를 검토할 수 있어야 한다.

Reranking은 가능한 후보 중 하나다.

```text
Retriever Top N
      ↓
Ranking Improvement
      ↓
Final Top K
```

도입 여부는 Evaluation과 Latency를 함께 비교한 뒤 결정한다.

---

## FR-15. Generation Evaluation

Retrieval과 별개로 최종 Answer 품질을 평가할 수 있어야 한다.

최소한 다음을 확인한다.

```text
검색 Context를 근거로 답했는가?

Context에 없는 내부 정보를 추가했는가?

질문의 핵심 내용을 포함했는가?

제공한 Source가 실제 답변의 근거인가?
```

Retrieval Failure와 Generation Failure를 구분할 수 있어야 한다.

---

## FR-16. No Answer 처리

현재 제공된 문서 또는 데이터에 답이 없는 경우 임의의 정보를 생성하지 않아야 한다.

예:

```text
관련 정책 문서를 찾지 못했습니다.
```

또는:

```text
현재 제공된 데이터만으로는 판단할 수 없습니다.
```

와 같이 답변할 수 있어야 한다.

---

## FR-17. 콘텐츠 조건 검색

Agent 단계에서는 구조화된 콘텐츠 데이터를 조건에 따라 조회할 수 있어야 한다.

예:

```text
"이번 달 공개 예정 액션 콘텐츠 알려줘."
```

검색 조건 후보:

```text
genre

ageRating

status

releaseDate

serviceRegion

metadataStatus
```

LLM이 자유 SQL을 직접 실행하는 구조는 필수 요구사항이 아니다.

---

## FR-18. 콘텐츠 상세 조회

콘텐츠 ID를 기준으로 상세 정보를 조회할 수 있어야 한다.

예:

```text
"콘텐츠 100번 상태 알려줘."
```

최소한 다음 정보를 조회할 수 있어야 한다.

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

## FR-19. Tool Calling

Agent 단계에서는 질문에 따라 필요한 Tool을 선택할 수 있어야 한다.

최종적으로 최소한 다음 역할의 Tool이 존재해야 한다.

```text
Policy Search

Content Search

Content Detail
```

초기 이름 예:

```text
search_policy_documents

search_contents

get_content_detail
```

구체적인 Tool 명칭과 Interface는 DESIGN에서 결정한다.

---

## FR-20. 복수 Tool 조합

하나의 질문에 여러 정보가 필요한 경우 여러 Tool 결과를 조합할 수 있어야 한다.

예:

```text
"콘텐츠 100이 왜 공개되지 않는지 정책 기준으로 설명해줘."

↓

Content Detail
+
Policy Search
↓

결과 조합
↓

Answer
```

---

## FR-21. Tool Selection Evaluation

Agent가 질문에 적절한 Tool을 선택했는지 평가할 수 있어야 한다.

최소한 다음 정보를 기록한다.

```text
Question

Expected Tool

Actual Tool

Tool 호출 순서
```

가능하면 다음 Metric을 계산한다.

```text
Tool Selection Accuracy
```

---

## FR-22. Agent Workflow 관리

여러 Tool 호출이나 상태 관리가 필요해지는 경우 Agent Workflow의 상태를 추적할 수 있어야 한다.

예:

```text
userQuery

selectedTools

retrievedDocuments

contentData

toolResults

finalAnswer
```

구체적인 Workflow 구현 방식은 요구사항에서 고정하지 않는다.

`LangGraph`는 후보 중 하나다.

---

# 6. 비기능 요구사항

## NFR-01. Retrieval 품질 측정

Retrieval 품질을 사람의 느낌만으로 판단하지 않는다.

동일한 Evaluation Dataset에서 Metric으로 평가한다.

최소 Metric:

```text
Hit Rate@K

Recall@K

MRR
```

---

## NFR-02. Retrieval / Generation 분리

잘못된 Answer가 발생했을 때 실패 위치를 구분할 수 있어야 한다.

```text
정답 문서가 Top K에 없음
→ Retrieval Failure

정답 문서는 있지만 Ranking이 낮음
→ Ranking Failure

정답 Context는 존재하지만 Answer가 잘못됨
→ Generation Failure

Context에 없는 내용을 생성함
→ Grounding Failure
```

---

## NFR-03. Grounding

내부 정책이나 콘텐츠 정보에 대한 답변은 제공된 Context 또는 Tool Result에 근거해야 한다.

근거가 없는 내용을 사실처럼 생성하지 않아야 한다.

---

## NFR-04. Source 추적

답변에 사용된 정보가 어떤 문서 또는 데이터에서 나왔는지 확인할 수 있어야 한다.

---

## NFR-05. Evaluation 재현 가능성

Evaluation 조건을 기록해야 한다.

최소한 다음 조건을 추적한다.

```text
Document Dataset

Evaluation Dataset

Embedding Model

Chunk Strategy

Top K

Retrieval Strategy

Reranker

Prompt

LLM Model
```

---

## NFR-06. 동일 조건 비교

하나의 전략 변경 효과를 비교할 때 가능한 한 다른 조건을 유지한다.

예:

```text
Chunking만 비교

→ Embedding Model 동일
→ Top K 동일
→ Evaluation Dataset 동일
```

여러 변수를 동시에 변경했다면 직접적인 Before / After 비교로 과장하지 않는다.

---

## NFR-07. 측정 기반 변경

다음과 같이 판단하지 않는다.

```text
Hybrid Search 적용
→ 개선됨
```

반드시:

```text
Baseline
   ↓
변경
   ↓
동일 Dataset 재평가
   ↓
Metric 비교
```

순으로 판단한다.

측정값이 동일하거나 나빠지는 결과도 유효한 Experiment 결과다.

---

## NFR-08. Latency 관측

현재 사용 중인 Pipeline의 주요 단계별 Latency를 확인할 수 있어야 한다.

필요에 따라 다음을 측정한다.

```text
Embedding Latency

Retrieval Latency

Reranking Latency

LLM Latency

End-to-End Latency
```

현재 존재하지 않는 단계까지 미리 측정할 필요는 없다.

---

## NFR-09. 비용 관측

상용 API를 사용하는 경우 Token 사용량과 예상 비용을 확인할 수 있어야 한다.

예:

```text
Input Tokens

Output Tokens

요청당 Tokens

Evaluation 전체 Tokens

예상 비용
```

로컬 Model을 사용한다면 비용 대신 실행시간과 Resource 사용량을 기록할 수 있다.

---

## NFR-10. Tool 선택 품질

Agent가 질문 해결에 필요한 Tool을 적절하게 선택해야 한다.

최종 Answer가 맞더라도 필요하지 않은 Tool을 반복 호출한다면 품질 문제로 분석할 수 있어야 한다.

---

## NFR-11. 데이터 역할 분리

데이터 성격에 따라 적절한 조회 방식을 사용한다.

```text
운영 정책 / Guide
→ Document Retrieval

구조화된 콘텐츠 Metadata
→ Database Tool
```

모든 데이터를 Vector Store에 넣거나 모든 질문을 SQL로 해결하지 않는다.

---

## NFR-12. 관측 가능성

필요한 경우 하나의 질문이 어떤 과정을 거쳐 답변됐는지 추적할 수 있어야 한다.

```text
Question
   ↓
Retriever / Tool
   ↓
Retrieved Documents
   ↓
Ranking
   ↓
Tool Result
   ↓
Context
   ↓
LLM
   ↓
Final Answer
```

---

# 7. Evaluation Dataset 요구사항

Evaluation Dataset은 Baseline 구현의 일부다.

프로젝트 마지막에 별도로 만드는 자료가 아니다.

최소한 다음 유형의 질문을 포함해야 한다.

```text
Semantic Question

Exact Keyword Question

Similar Document Question

No Answer Question
```

Agent 단계에서는 다음 유형을 추가한다.

```text
Policy Only

Content Search Only

Content Detail Only

Policy + Content Detail
```

Evaluation 질문과 Expected Result는 결과를 좋게 만들기 위해 임의로 변경하지 않는다.

평가 기준 변경이 필요하다면 별도 판단이 필요하다.

---

# 8. Baseline 요구사항

초기 Baseline은 **운영 문서 기반 Vector RAG**만 구현한다.

초기 처리 흐름:

```text
Question API
     ↓
Question Embedding
     ↓
Vector Search
     ↓
Top K Chunk
     ↓
Context
     ↓
LLM
     ↓
Answer + Source
```

Baseline에서 구현해야 하는 항목:

```text
Sample Document

Document Loading

Chunking

Embedding

pgvector 저장

Vector Search

Question API

RAG Answer

Source 반환

Evaluation Dataset

Retrieval Evaluation
```

---

# 9. Baseline에서 의도적으로 포함하지 않는 것

초기 구현에는 다음 기능을 적용하지 않는다.

```text
Keyword Search

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

Memory
```

또한 Retrieval 실패가 발생하더라도 바로 해결하지 않는다.

먼저 실패 Case와 Baseline Metric을 기록한다.

---

# 10. Baseline 완료 조건

다음 조건을 모두 만족하면 Baseline 구현이 완료된 것으로 판단한다.

## Document

```text
운영 문서를 반복적으로 적재할 수 있다.

문서를 Chunk로 나눌 수 있다.

Chunk의 원본 문서와 Section을 추적할 수 있다.
```

## Vector Retrieval

```text
Chunk Embedding을 생성할 수 있다.

pgvector에 저장할 수 있다.

질문 Embedding을 생성할 수 있다.

Top K Vector Search를 수행할 수 있다.
```

## RAG

```text
Question API가 동작한다.

검색된 Context를 LLM에 전달한다.

Answer를 생성한다.

Source를 반환한다.
```

## Evaluation

```text
Evaluation Dataset이 존재한다.

Hit Rate@K를 계산할 수 있다.

Recall@K를 계산할 수 있다.

MRR을 계산할 수 있다.

실패 Query를 확인할 수 있다.
```

## Test

```text
Document Loading Test

Vector Search Test

RAG Integration Test

Evaluation 실행 검증
```

## README

Baseline 완료 시 README에서 최소한 다음 내용을 확인할 수 있어야 한다.

```text
프로젝트 목적

실행 방법

PostgreSQL / pgvector 실행 방법

Sample Document 적재 방법

Question API 호출 방법

Test 실행 방법

Evaluation 실행 방법
```

Baseline 품질 자체가 높을 필요는 없다.

**측정 가능한 상태가 만들어지는 것**이 완료 기준이다.

---

# 11. 최종 완료 조건

프로젝트 최종 시점에는 다음 내용을 실제 Evaluation 결과를 기반으로 설명할 수 있어야 한다.

```text
Baseline Retrieval 품질

주요 Retrieval 실패 Case

Chunking 변경에 따른 결과

Exact Keyword Query 처리 결과

Hybrid Retrieval 필요 여부와 결과

Ranking 개선 필요 여부와 결과

Reranking 사용 여부와 결과

Retrieval Failure와 Generation Failure의 차이

Grounding / Hallucination 결과

No Answer Query 처리 결과

Agent Tool Selection 결과

복수 Tool 호출 결과

Tool Selection Accuracy

최종 Retrieval 품질

최종 Latency

필요한 경우 Token / 비용 변화
```

모든 기술이 최종 시스템에 포함될 필요는 없다.

다음과 같은 결과도 정상적인 프로젝트 결과다.

```text
Vector Search만으로 충분했다.

Hybrid Search가 일부 Query에만 효과가 있었다.

RRF가 의미 있는 개선을 만들지 못했다.

Reranker가 품질보다 Latency를 더 크게 증가시켰다.

LangGraph가 필요하지 않았다.
```

---

# 12. 제외 범위

다음 기능은 프로젝트 범위에서 제외한다.

```text
회원가입

로그인 UI

실제 콘텐츠 CMS

실제 콘텐츠 수정

실제 회사 내부 문서 연동

Google Drive / Notion 연동

실제 운영 권한 체계

추천 시스템

Streaming

DRM

CDN

Frontend UI

관리자 Dashboard

Voice Interface
```

다음 기술 역시 초기 필수 기술이 아니다.

```text
Elasticsearch

OpenSearch

Redis Vector Store

Pinecone

Weaviate

Milvus

Knowledge Graph

Multi-Agent

Long-term Memory

Fine-tuning
```

실제 문제가 확인되고 학습 목적상 필요성이 있는 경우에만 후보로 검토한다.

---

# 13. 요구사항 변경 원칙

새로운 요구사항이 필요해졌다고 AI가 임의로 기능을 추가하지 않는다.

다음 흐름을 따른다.

```text
새로운 필요 발견
      ↓
현재 Requirement로 해결 가능?
      ↓
   ┌──┴──┐
  YES    NO
   │      │
기존 범위  요구사항 변경 검토
유지        ↓
          Human Gate
            ↓
       REQUIREMENTS 수정
```

기술 선택과 요구사항을 구분한다.

예:

```text
Exact Keyword Query를 처리해야 한다.
→ Requirement

BM25를 사용한다.
→ Design / Experiment / ADR
```

또는:

```text
질문에 따라 적절한 Tool을 선택해야 한다.
→ Requirement

LangGraph로 Routing한다.
→ Design / Experiment / ADR
```

---

# 14. 핵심 원칙

ContentOps Agent에서는 RAG와 Agent 기능을 많이 사용하는 것을 목표로 하지 않는다.

프로젝트 전체에서 다음 흐름을 유지한다.

```text
Baseline
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

기술 적용 사실 자체를 개선으로 판단하지 않는다.

최종 목표는:

> **내부 운영 문서와 콘텐츠 데이터를 대상으로 Retrieval과 Generation 실패를 구분하고, Evaluation Dataset과 Metric을 이용해 검색·답변·Tool 선택 품질을 측정한 뒤, 실제 실패 Case에 필요한 개선만 선택하고 그 결과를 재평가할 수 있는 AI Backend를 만드는 것**

이다.