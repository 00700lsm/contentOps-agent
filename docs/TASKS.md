# TASKS

## Current Phase

```text
Phase 1
Baseline Vector RAG 구현
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 1과 `docs/REQUIREMENTS.md` Baseline을 따른다.

---

## 목표

최적화가 적용되지 않은 **가장 단순한 운영 문서 기반 Vector RAG**를 만든다.

이 Phase의 목적은 높은 Retrieval 품질이 아니다.

이후 Experiment에서 사용할 **측정 가능한 Baseline**을 확보하는 것이다.

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

## 이 Phase에서 하지 않는 것

다음 기능은 ROADMAP에 등장하더라도 구현하지 않는다.

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

Chunk Size, Overlap, Top K, Prompt, Embedding Model, LLM Model을 Baseline 확보 전에 반복 조정하지 않는다.

Evaluation Dataset의 질문이나 Expected Document를 Metric을 좋게 만들기 위해 변경하지 않는다.

---

## Tasks

### 1. Project / Environment

- [ ] Java 21 + Spring Boot + Gradle 프로젝트 구성
- [ ] Docker Compose로 PostgreSQL + pgvector 실행 구성
- [ ] Ollama는 Host에서 실행하는 것을 기본으로 한다. Compose에 Model Runtime을 넣지 않는다.
- [ ] Embedding Model / Chat Model 이름을 코드에 Hard Coding하지 않고 설정으로 관리한다.
- [ ] Baseline 설정값을 외부 설정으로 둔다.

```text
app.rag.top-k
app.rag.chunk-size
app.rag.chunk-overlap
app.ai.embedding-model
app.ai.chat-model
```

초기 비교 조건:

```text
Chunk Size   약 500 tokens
Overlap      약 50 tokens
Top K        5
Retrieval    Vector Search Only
```

이 값은 최적값이 아니다. Phase 1 완료 전까지 임의로 바꾸지 않는다.

---

### 2. Sample Document

- [ ] `data/documents/` 아래에 프로젝트용 운영 문서를 작성한다.

```text
age-rating-policy.md
content-status-policy.md
metadata-guide.md
publishing-guide.md
youth-protection-policy.md
operations-faq.md
```

- [ ] 실제 회사 내부 데이터는 사용하지 않는다.
- [ ] 의미적으로 겹치는 내용과 비슷한 용어를 일부러 포함한다.
- [ ] 이후 Exact Keyword 실험을 위해 다음 값을 문서에 포함한다.

```text
OPS-101
M-03
CONTENT_BLOCKED
AGE_REVIEW_REQUIRED
```

---

### 3. Document Ingestion

- [ ] Document Loader 구현
- [ ] Markdown Parser 구현
- [ ] Fixed-size Chunking 구현
- [ ] Chunk Metadata 생성

```text
chunkId
documentId
documentName
section
chunkIndex
content
```

- [ ] Chunk Embedding 생성
- [ ] PostgreSQL + pgvector 저장
- [ ] 공개 API / 관리자 UI는 만들지 않는다. 로컬 개발용 Ingestion Mode만 제공한다.
- [ ] 동일 Dataset을 반복 적재해도 Document가 계속 중복 생성되지 않아야 한다.

```text
Reset
 ↓
Ingest
 ↓
Evaluate
```

---

### 4. Vector Retrieval

- [ ] Question Embedding 생성
- [ ] Vector Similarity Search 구현
- [ ] Top K 결과 반환
- [ ] Retrieval 결과에서 다음 값을 확인할 수 있어야 한다.

```text
rank
content
documentName
section
chunkIndex
similarityScore
```

- [ ] Retrieval과 Generation을 코드에서 분리한다.
- [ ] Keyword Search, Hybrid Search, RRF, Reranker는 추가하지 않는다.

---

### 5. Question API / RAG

- [ ] `POST /api/v1/questions` 구현
- [ ] `question` Validation

```text
null
빈 문자열
공백 문자열
→ 거절
```

- [ ] Context Builder 구현
- [ ] Baseline Prompt 적용

```text
제공된 Context를 기준으로 질문에 답한다.
Context에 답변 근거가 없다면 확인할 수 없다고 답한다.
내부 정책이나 사실을 추측해서 만들지 않는다.
답변은 간결하게 작성한다.
```

- [ ] LLM Answer 생성
- [ ] Source Document / Source Section 반환
- [ ] 일반 API Response에 Similarity Score나 전체 Prompt를 노출하지 않는다.

응답 예:

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

- [ ] 오류 Response 형식을 통일한다.

```text
INVALID_QUESTION
EMBEDDING_FAILED
VECTOR_SEARCH_FAILED
LLM_GENERATION_FAILED
```

- [ ] Question 처리 Log에 requestId, question, topK, retrieved document, rank, similarity score, retrieval / LLM / total latency를 남긴다.
- [ ] 전체 Prompt나 Sample Document 전체 내용은 기본 Log에 출력하지 않는다.

---

### 6. Evaluation

- [ ] `evaluation/datasets/retrieval.jsonl` 작성
- [ ] 다음 Category를 포함한다.

```text
SEMANTIC
EXACT_KEYWORD
SIMILAR_DOCUMENT
NO_ANSWER
```

- [ ] Agent 관련 평가 항목은 넣지 않는다.
- [ ] Evaluation Runner 구현
- [ ] Runner는 HTTP API 반복 호출이 아니라 내부 Retrieval Service를 직접 사용한다.
- [ ] Hit Rate@K / Recall@K / MRR 계산
- [ ] Query별 Expected Document, Actual Top K, Expected Rank, Retrieval Latency를 기록한다.
- [ ] 결과를 `evaluation/results/`에 기계가 읽을 수 있는 형태로 출력한다.

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

Phase 1의 핵심 Evaluation은 Retrieval이다.

Metric이 높을 필요는 없다. **계산할 수 있는 상태**를 만드는 것이 목적이다.

실패 Query가 보이더라도 Phase 1에서 해결하지 않는다.

---

### 7. Test

- [ ] Unit Test

```text
Question Validation
Chunk Metadata 생성
Context Builder
Hit Rate 계산
Recall 계산
MRR 계산
Evaluation 결과 집계
```

- [ ] Vector Store Integration Test

```text
Testcontainers
PostgreSQL + pgvector
Vector 저장
Similarity Search
Top K 결과 확인
```

- [ ] RAG Integration Test

```text
Question
 ↓
Retriever
 ↓
Context Builder
 ↓
Chat Model
 ↓
Answer + Source
```

- [ ] Evaluation 실행 검증
- [ ] Vector Search를 Mock Repository만으로 검증하지 않는다.
- [ ] 일반 Test가 외부 Ollama 상태에 강하게 의존하지 않도록 필요하면 Test Double을 사용한다.
- [ ] JUnit 통과를 Retrieval 품질 우수로 판단하지 않는다.

---

### 8. README

- [ ] Phase 1 완료 시 README에서 다음을 확인할 수 있게 작성한다.

```text
프로젝트 목적
애플리케이션 실행 방법
PostgreSQL / pgvector 실행 방법
Ollama 준비 방법
Sample Document 적재 방법
Question API 호출 방법
Test 실행 방법
Evaluation 실행 방법
```

---

## 완료 조건

다음을 모두 만족해야 Phase 1을 완료한다.

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

품질이 높지 않아도 된다.

측정 가능한 Baseline이 만들어지면 완료다.

---

## Git Checkpoint

완료 후 다음 상태를 남긴다.

```text
feat: implement baseline vector rag
```

Phase 1의 완료된 `TASKS.md`가 Commit에 포함된 뒤 다음 Phase TASK로 교체한다.

조건을 만족하기 전에 Phase 2로 이동하지 않는다.
