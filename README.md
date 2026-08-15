# ContentOps Agent

OTT 콘텐츠 운영자를 위한 운영 문서 RAG + Content Tool 시스템이다.

Question API는 운영 문서 Vector RAG다. 콘텐츠 조회 Tool은 구현되어 있으나 질문 라우팅은 Phase 8에서 평가한다. Keyword Search, Hybrid Search, Reranker, LangGraph, Multi-Agent는 포함하지 않는다.

## 실행 환경

- Java 21
- Docker / Docker Compose
- 로컬 [Ollama](https://ollama.com)

Ollama는 Host에서 실행한다. Docker Compose에는 PostgreSQL + pgvector만 포함한다.

기본 Model은 설정으로 관리한다.

```text
app.ai.embedding-model = nomic-embed-text
app.ai.chat-model = llama3.2
```

## PostgreSQL / pgvector

```bash
docker compose -f docker/docker-compose.yml up -d
```

접속 정보:

```text
jdbc:postgresql://localhost:5432/contentops
username: contentops
password: contentops
```

## Ollama 준비

```bash
ollama serve
ollama pull nomic-embed-text
ollama pull llama3.2
```

## Sample Document 적재

```bash
./gradlew bootRun --args='--app.ingest.enabled=true --spring.main.web-application-type=none'
```

적재 전 vector store를 비운 뒤 `data/documents`를 다시 넣는다.

```text
Reset
 ↓
Ingest
```

동일 Dataset을 반복 적재해도 문서가 누적되지 않는다.

## Sample Content 적재

콘텐츠 샘플은 `data/contents/contents.json`이다.

애플리케이션이 시작되면 PostgreSQL `contents` 테이블에 다시 넣는다.

```text
id, title, genre, ageRating, status, releaseDate, serviceRegion, metadataStatus
```

Tool:

```text
search_policy_documents
search_contents
get_content_detail
```

## 애플리케이션 실행

```bash
./gradlew bootRun
```

## Question API

```bash
curl -s -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"15세 콘텐츠의 공개 조건이 뭐야?"}'
```

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

## Test

```bash
./gradlew test
```

Test는 코드가 정상 동작하는지를 검증한다. Retrieval 품질이 좋다는 뜻은 아니다.

일반 Test는 외부 Ollama에 의존하지 않는다. Vector Search는 Testcontainers PostgreSQL + pgvector에서 검증한다.

## Evaluation

```bash
./gradlew bootRun --args='--app.ingest.enabled=true --app.evaluate.enabled=true --spring.main.web-application-type=none'
```

Dataset: `evaluation/datasets/retrieval.jsonl`

Agent Tool Dataset: `evaluation/datasets/agent-tools.jsonl`

결과: `evaluation/results/`

계산하는 Metric:

```text
Hit Rate@K
Recall@K
MRR
```

Phase 1의 목적은 Metric이 높은 것이 아니라, 같은 Dataset으로 반복 측정할 수 있는 상태를 만드는 것이다.

## Experiment

실험 기록은 `docs/experiments/`에 남긴다.
