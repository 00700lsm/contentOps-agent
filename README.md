# ContentOps Agent

OTT 콘텐츠 운영자를 위한 운영 문서 RAG + Content Tool 시스템이다.

Question API는 단순 Tool Calling이다. Keyword Search, Hybrid Search, Reranker, LangGraph, Multi-Agent는 포함하지 않는다.

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

API를 띄우기 전에 문서를 한 번 적재한다. 콘텐츠 샘플은 애플리케이션 시작 시 `contents` 테이블에 다시 넣는다.

```bash
./gradlew bootRun --args='--app.ingest.enabled=true --spring.main.web-application-type=none'
./gradlew bootRun
```

기본 포트는 `8080`이다.

Java 21이 기본이 아니면:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"
```

## Question API

`POST /api/v1/questions` 만 제공한다. 브라우저 주소창에 GET으로 열면 동작하지 않는다.

응답 예:

```json
{
  "answer": "15세 콘텐츠는 연령 등급 검수가 완료된 이후 공개할 수 있습니다.",
  "sources": [
    {
      "document": "age-rating-policy.md",
      "section": "3.2 공개 조건"
    }
  ],
  "tools": ["search_policy_documents"]
}
```

`sources`는 Retriever가 반환한 Chunk Metadata다. LLM Citation이 아니다.

---

## Gradle Test

코드가 정상 동작하는지를 검증한다. Retrieval 품질이 좋다는 뜻은 아니다.

일반 Test는 외부 Ollama에 의존하지 않는다. Vector Search와 Integration Test는 Testcontainers PostgreSQL + pgvector를 사용한다. Docker가 떠 있어야 한다.

Docker Desktop(macOS)에서 Testcontainers가 소켓을 못 찾으면:

```bash
export DOCKER_HOST="unix://$HOME/.docker/run/docker.sock"
```

전체 Test:

```bash
./gradlew test
```

특정 클래스:

```bash
./gradlew test --tests com.contentopsagent.question.controller.QuestionControllerTest
./gradlew test --tests com.contentopsagent.retrieval.PgVectorSearchIT
./gradlew test --tests com.contentopsagent.rag.RagPipelineIT
```

이름 패턴:

```bash
./gradlew test --tests '*IT'
./gradlew test --tests '*MetricsTest'
```

실패한 Test만 다시:

```bash
./gradlew test --rerun-tasks
```

리포트:

```text
build/reports/tests/test/index.html
```

---

## URL로 직접 호출

서버가 `http://localhost:8080`에서 떠 있어야 한다. Ollama도 필요하다.

정책 문서 질문:

```bash
curl -s -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"15세 콘텐츠의 공개 조건이 뭐야?"}'
```

콘텐츠 검색:

```bash
curl -s -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"이번 달 공개 예정 액션 콘텐츠 알려줘."}'
```

콘텐츠 상세:

```bash
curl -s -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"콘텐츠 100번 현재 상태 알려줘."}'
```

정책 + 상세:

```bash
curl -s -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"콘텐츠 101번이 왜 공개되지 않는지 정책 기준으로 설명해줘."}'
```

JSON을 보기 쉽게:

```bash
curl -s -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"M-03 오류의 의미가 뭐야?"}' | jq
```

[HTTPie](https://httpie.io):

```bash
http POST :8080/api/v1/questions question='OPS-101은 무엇인가?'
```

빈 질문은 거절된다.

```bash
curl -s -o /dev/null -w '%{http_code}\n' \
  -X POST http://localhost:8080/api/v1/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":""}'
```

브라우저나 Postman / Insomnia / Cursor REST Client에서는 다음으로 호출한다.

```text
POST http://localhost:8080/api/v1/questions
Content-Type: application/json

{
  "question": "15세 콘텐츠의 공개 조건이 뭐야?"
}
```

---

## 커맨드에서 Evaluation

같은 Dataset을 다시 돌리는 품질 측정이다. Gradle Test와 다르다. Ollama와 PostgreSQL이 필요하다.

문서 적재만:

```bash
./gradlew bootRun --args='--app.ingest.enabled=true --spring.main.web-application-type=none'
```

Retrieval Evaluation:

```bash
./gradlew bootRun --args='--app.ingest.enabled=true --app.evaluate.enabled=true --spring.main.web-application-type=none'
```

Agent Evaluation:

```bash
./gradlew bootRun --args='--app.ingest.enabled=true --app.evaluate.agent-enabled=true --spring.main.web-application-type=none'
```

결과 JSON:

```bash
ls -1 evaluation/results
python3 -m json.tool evaluation/results/retrieval-*.json | head
```

Dataset:

```text
evaluation/datasets/retrieval.jsonl
evaluation/datasets/agent-tools.jsonl
```

Retrieval Metric:

```text
Hit Rate@K
Recall@K
MRR
```

Agent Metric:

```text
Tool Selection Accuracy
Sequence Accuracy
```

Retrieval Evaluation은 Question API와 다른 경로다. `RetrievalService` + `RagService`로 문서 RAG만 측정한다.

Agent Evaluation은 Question API와 같은 Simple Tool Calling 경로를 측정한다.

## Experiment

실험 기록은 `docs/experiments/`에 남긴다.

최종 비교는 `docs/experiments/009-final-comparison.md`다.
