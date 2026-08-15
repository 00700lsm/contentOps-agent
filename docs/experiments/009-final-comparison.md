# Experiment 009 - Final Comparison

## 목적

최초 Baseline과 최종 시스템을 **같은 Evaluation Dataset**으로 비교한다.

어떤 실패가 남았는지, 어떤 변경이 실제로 효과가 있었는지, 어떤 기술은 필요하지 않았는지를 측정값으로 설명한다.

이 실험의 목적은 새 기술을 넣는 것이 아니다.

---

## 현재 상태

Question API:

```text
Question
 ↓
LLM Tool Selection
 ↓
search_policy_documents
search_contents
get_content_detail
 ↓
Answer + Sources + Tools
```

Retrieval Evaluation 경로:

```text
Question
 ↓
Embedding
 ↓
Vector Search Top K = 5
 ↓
LLM (BaselinePrompt B)
 ↓
Answer + Source
```

Question API와 Retrieval Evaluation은 같은 경로가 아니다.

최종 비교에서 Retrieval Metric은 Evaluation Runner의 RAG 경로로 측정한다.

Agent Metric은 Phase 8 Agent Evaluation 결과를 최종 스냅샷으로 사용한다.

```text
Keyword Search     없음
Hybrid Search      없음
RRF                없음
Reranker           없음
LangGraph          없음
Multi-Agent        없음
008-agent-workflow.md  없음 (Workflow를 바꾸지 않음)
```

---

## 문제

프로젝트가 끝난 뒤에도 다음을 구분하지 않으면, 구현한 기술을 성과로 착각할 수 있다.

```text
Retrieval이 좋아졌는가
Generation이 좋아졌는가
Tool 선택이 충분한가
넣지 않은 기술이 실제로 필요했는가
```

---

## 가설

측정 가설만 있다.

```text
Retrieval 전략을 바꾸지 않았으므로
Hit Rate / Recall / MRR은 Baseline과 같다.

Generation은 Prompt B와 llama3.2 분산의 영향을 받는다.
Retrieval Metric과 같이 움직이지 않을 수 있다.

Keyword / Hybrid / RRF / Reranker / LangGraph는
이 Dataset에서 도입 근거가 확인되지 않았다.
```

---

## Dataset

Document Dataset:

```text
data/documents
6개 Markdown
31 Chunk
```

Retrieval Evaluation Dataset:

```text
evaluation/datasets/retrieval.jsonl
Query 12개
Scored Query 10개
NO_ANSWER Query 2개
```

Category:

```text
SEMANTIC          3
EXACT_KEYWORD     4
SIMILAR_DOCUMENT  3
NO_ANSWER         2
```

Agent Evaluation Dataset:

```text
evaluation/datasets/agent-tools.jsonl
Query 8
```

Dataset은 바꾸지 않았다.

---

## 실험 조건

Retrieval Baseline:

```text
결과 파일          evaluation/results/retrieval-20260815-124436.json
실행 시각          2026-08-15T12:44:36Z
Prompt             Prompt A
Retrieval          Vector Search Only
Chunking           Fixed 500 / 50
Embedding          nomic-embed-text
Chat Model         llama3.2
Top K              5
```

Retrieval Final:

```text
결과 파일          evaluation/results/retrieval-20260815-144910.json
실행 시각          2026-08-15T14:49:10Z
Prompt             Prompt B (Phase 6 Human Gate)
Retrieval          Vector Search Only
Chunking           Fixed 500 / 50
Embedding          nomic-embed-text
Chat Model         llama3.2
Top K              5
Chunk 수           31
```

Generation 통제 비교(Phase 6):

```text
Prompt A  evaluation/results/retrieval-20260815-130609.json
Prompt B  evaluation/results/retrieval-20260815-134454.json
```

Agent Final:

```text
결과 파일  evaluation/results/agent-20260815-143855.json
Workflow   Simple Tool Calling
```

변경 변수는 최종 스냅샷이다. Retrieval 전략은 Baseline과 같다.

---

## Metric

### Retrieval

Scored Query 10개 기준:

| Metric | Baseline | Final |
| --- | ---: | ---: |
| Hit Rate@K | 0.9 | 0.9 |
| Recall@K | 0.9 | 0.9 |
| MRR | 0.75 | 0.75 |

Latency / Token 평균:

| 항목 | Baseline | Final |
| --- | ---: | ---: |
| Embedding Latency | 1082.1 ms | 50.7 ms |
| Retrieval Latency | 39.5 ms | 38.4 ms |
| LLM Latency | 2168.8 ms | 3545.1 ms |
| End-to-End Latency | 3292.3 ms | 3635.9 ms |
| Prompt Tokens | 7904 | 8852 |
| Completion Tokens | 372 | 1107 |
| Reranking Latency | 없음 | 없음 |

Embedding Latency 감소는 검색 전략 개선이 아니다.

Baseline은 첫 실행에 가깝고, Final은 같은 Ollama가 이미 떠 있는 상태다.

LLM Latency와 Completion Token 증가는 Prompt B와 더 긴 답의 영향이다.

Retrieval Latency는 거의 같다.

---

## Query 유형별 비교

유형별 Hit / Recall / MRR은 Baseline과 Final이 같다.

| Category | Query | Hit Rate@K | Recall@K | MRR |
| --- | ---: | ---: | ---: | ---: |
| SEMANTIC | 3 | 1.0 | 1.0 | 0.833 |
| EXACT_KEYWORD | 4 | 1.0 | 1.0 | 0.875 |
| SIMILAR_DOCUMENT | 3 | 0.667 | 0.667 | 0.5 |
| NO_ANSWER | 2 | 채점 제외 | 채점 제외 | 채점 제외 |

문서 단위 첫 Expected Rank도 같다.

| ID | Category | Expected | Rank | Hit |
| --- | --- | --- | ---: | --- |
| retrieval-001 | SEMANTIC | age-rating-policy.md | 1 | true |
| retrieval-002 | SEMANTIC | content-status-policy.md | 2 | true |
| retrieval-003 | SEMANTIC | metadata-guide.md | 1 | true |
| retrieval-004 | EXACT_KEYWORD | metadata-guide.md | 1 | true |
| retrieval-005 | EXACT_KEYWORD | operations-faq.md | 1 | true |
| retrieval-006 | EXACT_KEYWORD | content-status-policy.md | 2 | true |
| retrieval-007 | EXACT_KEYWORD | age-rating-policy.md | 1 | true |
| retrieval-008 | SIMILAR_DOCUMENT | youth-protection-policy.md, age-rating-policy.md | 1 | true |
| retrieval-009 | SIMILAR_DOCUMENT | publishing-guide.md | 2 | true |
| retrieval-010 | SIMILAR_DOCUMENT | operations-faq.md | miss | false |
| retrieval-011 | NO_ANSWER | 없음 | - | - |
| retrieval-012 | NO_ANSWER | 없음 | - | - |

문서 단위 Ranking Failure(Rank >= 3)는 Baseline과 Final 모두 0건이다.

---

## Retrieval 실패 변화

변하지 않았다.

```text
retrieval-010
Expected  operations-faq.md
Actual    Top K에 없음
분류      Retrieval Failure
```

Final Top K:

```text
1  content-status-policy.md
2  publishing-guide.md
3  metadata-guide.md
4  age-rating-policy.md 목적
5  age-rating-policy.md
```

유사 문서가 정답 FAQ를 밀어낸다.

Chunking을 바꿔도 이 Miss는 그대로였다. Keyword / Hybrid / Reranker를 넣지 않았으므로, 이 Miss가 그 기술로 해결됐는지는 측정하지 않았다.

---

## Ranking 결과

```text
문서 기준 Ranking Failure  0건
Hit Query의 가장 낮은 Rank  2
```

관찰로 남은 Section Rank:

```text
retrieval-004  M-03 정의 Section Rank 3
retrieval-007  AGE_REVIEW_REQUIRED 정의 Section Rank 3
```

문서 Rank가 낮아서 Context 밖인 경우는 이 Dataset에 없다.

정의 Section이 항상 1위는 아니다. 그 문제는 Reranker 도입 근거로 쓰지 않았다.

---

## Generation / Grounding

Generation 분류는 자동 Metric이 아니다.

답변과 Top K Section을 읽고 분류한다. 기준은 `006-generation-grounding.md`와 같다.

통제 비교는 Phase 6 Prompt A vs Prompt B다.

Final 스냅샷은 Prompt B가 코드에 있는 상태로 같은 Dataset을 다시 실행한 결과다.

llama3.2는 같은 Prompt여도 답이 달라질 수 있다. Final 답을 Prompt B 실험의 개선으로 보지 않는다.

### Phase 6 통제 비교

| 항목 | Prompt A | Prompt B |
| --- | --- | --- |
| Generation 성공 | 4 (001, 004, 005, 007) | 4 (001, 004, 005, 007) |
| Generation Failure | 2 (006, 008) | 2 (006, 008) |
| 문서 Hit + 정답 Section 부재 | 3 (002, 003, 009) | 3 (002, 003, 009) |
| Retrieval Failure | 1 (010) | 1 (010) |
| No Answer 성공 | 2 (011, 012) | 2 (011, 012) |
| 강한 Grounding Failure | 0 | 1 (003) |
| avg LLM Latency | 2011.7 ms | 3275.3 ms |
| Completion Tokens | 353 | 1063 |

가설 "Prompt B가 006, 008을 고친다"는 Phase 6 측정에서 성립하지 않았다.

### Final 스냅샷 분류

측정 파일: `evaluation/results/retrieval-20260815-144910.json`

| 분류 | 수 | Query |
| --- | ---: | --- |
| Generation 성공 | 5 | 001, 004, 005, 007, 008 |
| Generation Failure | 1 | 006 |
| 문서 Hit + 정답 Section 부재 | 3 | 002, 003, 009 |
| Retrieval Failure | 1 | 010 |
| No Answer 성공 | 2 | 011, 012 |

Source 목록은 Retriever Top K다. LLM Citation이 아니다. Source Match를 Retriever 기준으로 보면 12/12다.

008이 이번 스냅샷에서 맞았다고 Prompt B가 검증됐다고 보지 않는다.

Phase 6의 같은 Prompt B 실행에서는 008이 관계를 뒤집었다.

006은 이번에도 사용 시점을 빼고 "사용하지 않는다"만 답했다. 정답 Section은 Top K에 있다.

---

## No Answer

Baseline과 Final 모두 매출 숫자나 담당자 이름을 만들지 않았다.

| ID | Baseline | Final |
| --- | --- | --- |
| retrieval-011 | 확인할 수 없습니다 | 확인할 수 없습니다 (외국어 토큰 혼입) |
| retrieval-012 | 확인할 수 없습니다 | 확인할 수 없습니다 (외국어 토큰 혼입) |

No Answer 성공은 유지됐다.

거절 문구가 늘고 외국어 토큰이 섞인 것은 품질 개선이 아니다.

010은 No Answer Query가 아니다. 정답 FAQ가 없는데도 공개 가이드와 연령 등급 정책으로 절차를 조합했다. Retrieval Failure 위의 Grounding 문제로 남는다.

---

## Agent Tool Selection

측정 파일: `evaluation/results/agent-20260815-143855.json`

Baseline Question API에는 Tool이 없었다. Agent 비교의 Before는 "없음"이다.

| Metric | Final |
| --- | ---: |
| Query 수 | 8 |
| Tool Selection Accuracy (집합) | 0.625 |
| Sequence Accuracy | 0.375 |
| MATCH | 3 |
| WRONG_ORDER | 2 |
| MISSING_TOOL | 3 |
| UNNECESSARY_TOOL | 0 |
| WRONG_TOOL | 0 |
| avg Latency | 3977.5 ms |

| ID | Category | 분류 |
| --- | --- | --- |
| tool-001 | POLICY_ONLY | WRONG_ORDER (같은 Tool 2회) |
| tool-002 | CONTENT_SEARCH | MISSING_TOOL (Tool JSON을 텍스트로 출력) |
| tool-003 | CONTENT_DETAIL | MATCH |
| tool-004 | CONTENT_SEARCH | WRONG_ORDER (같은 Tool 2회) |
| tool-005 | CONTENT_DETAIL | MATCH |
| tool-006 | POLICY_ONLY | MATCH |
| tool-007 | MULTI_TOOL | MISSING_TOOL (정책 Tool 없음) |
| tool-008 | MULTI_TOOL | MISSING_TOOL (정책 Tool 없음) |

Wrong Tool은 없다.

Multi Tool 2건은 상세만 호출하고 정책 검색을 빠뜨렸다.

상세 Query 2건은 MATCH다.

---

## Agent Workflow의 최종 구조와 이유

```text
Simple Tool Calling
Spring AI ChatClient + @Tool
```

Phase 8 Human Gate에서 후보 A를 선택했다.

LangGraph / Multi-Agent는 넣지 않았다.

이유:

```text
Wrong Tool은 0이다.
실패는 후속 Tool 누락과 llama3.2 Tool 루프 불안정이다.
한 번의 측정만으로 Workflow를 바꾸지 않는다.
```

`008-agent-workflow.md`는 작성하지 않는다. Workflow를 바꾸지 않았다.

---

## Latency / 비용 Trade-off

품질이 오른 구간과 비용이 오른 구간을 구분한다.

```text
Retrieval Latency
Baseline 39.5 ms → Final 38.4 ms
검색 단계 비용은 거의 같다.

LLM Latency (RAG)
Baseline 2168.8 ms → Final 3545.1 ms
Prompt B와 긴 답 때문에 늘었다.

Completion Tokens (RAG)
372 → 1107
```

Phase 6에서 Prompt B는 목표 Generation Failure를 고치지 못했고 Token / Latency는 늘었다.

Human Gate는 그래도 Prompt B를 유지했다. 이번 Final도 그 설정이다.

Agent 평균 Latency 3977.5 ms는 RAG Evaluation과 Dataset이 다르다. 직접 빼서 개선으로 쓰지 않는다.

Reranker를 넣지 않았으므로 Reranking Latency는 없다.

넣지 않은 기술의 예상 비용을 숫자로 쓰지 않는다.

---

## 적용하지 않은 기술과 이유

| 기술 | 이유 | 근거 실험 |
| --- | --- | --- |
| Chunking 기본 전략 변경 | 대안 2개 Metric이 Baseline과 같음 | 003 |
| Keyword Search | Exact Keyword 문서 Hit 1.0 | 004 |
| Hybrid Search | Keyword 도입 근거가 없음 | 004 |
| RRF | Fusion할 두 번째 Retriever가 없음 | 004, 005 |
| Reranker | 문서 Ranking Failure 0건 | 005 |
| Prompt 추가 변경 | Prompt B가 006/008을 못 고침 | 006 |
| LangGraph | Wrong Tool 0, 필요성 미확정 | 007 |
| Multi-Agent | 역할 분리 근거 없음 | 007 |
| Embedding / LLM Model 변경 | 실험 변수로 두지 않음 | 전 Phase |

기술을 넣지 않은 것이 그 기술이 항상 불필요하다는 뜻은 아니다.

이 프로젝트의 Sample Document / Dataset에서 도입 근거가 나오지 않았다는 뜻이다.

---

## 최종 질문

### Vector Search는 어떤 Query에서 잘 동작했는가?

Semantic 3건과 Exact Keyword 4건은 문서 Hit Rate 1.0이다.

코드/상태값 Query도 문서 단위로는 Top K에 들어왔다.

### 어떤 Query에서 실패했는가?

```text
retrieval-010  유사 문서가 정답 FAQ를 밀어냄. Retrieval Failure.
retrieval-002, 003, 009  문서는 Hit, 정답 Section은 Top K에 없음.
retrieval-006  정답 Section은 있는데 사용 시점을 빠뜨림.
```

### Chunking 변경은 실제로 효과가 있었는가?

없었다.

Sample Section이 이미 Token Window보다 작아 Baseline Chunking이 Section 단위와 같았다.

Hit Rate / Recall / MRR / 실패 Query가 변하지 않았다.

### Exact Keyword Retrieval에는 어떤 문제가 있었는가?

문서 Miss는 없었다.

문제는 정의 Section Rank와 Generation이다.

```text
M-03 문서 Hit, 정의 Section Rank 3
CONTENT_BLOCKED 문서 Rank 2
AGE_REVIEW_REQUIRED 정의 Section Rank 3
```

### Keyword / Hybrid Retrieval이 필요했는가?

이 Dataset의 문서 기준 Exact Keyword 실패는 없었다.

Keyword / Hybrid를 넣지 않았다.

retrieval-010은 Exact Keyword Query가 아니다. Keyword를 넣으면 이 Miss가 고쳐진다는 측정은 없다.

### Fusion 전략은 실제 Ranking을 개선했는가?

Fusion을 넣지 않았다.

문서 Ranking Failure가 0건이라 RRF를 비교하지 않았다.

### Reranker가 필요했는가?

문서 기준 Ranking Failure가 0건이다.

Hit Query는 Rank 1 또는 Rank 2다.

Reranker를 넣지 않았다.

Section Rank 3은 관찰로 남겼다. Evaluation Expected는 문서 이름이다.

### 품질 향상이 추가 Latency를 감수할 정도였는가?

Retrieval 품질은 오르지 않았다. Hit Rate 0.9가 유지됐다.

Prompt B는 Latency와 Token을 늘렸고, 통제 비교에서 목표 Generation Failure를 고치지 못했다.

검색 단계를 복잡하게 만들어 Latency를 늘릴 근거는 이 측정에 없다.

### Retrieval Failure와 Generation Failure는 어떻게 달랐는가?

```text
Retrieval Failure
정답 문서가 Top K에 없음
→ 010

Section 부재
문서는 Hit, 답을 담은 Section은 없음
→ 002, 003, 009

Generation Failure
정답 Section이 Context에 있는데 오답 / 핵심 누락
→ 006 (008은 Phase 6에서 Failure, Final 스냅샷에서는 성공)
```

최종 Answer만 보고 Prompt를 먼저 바꾸면 실패 위치를 섞게 된다.

### No Answer Query에서 Hallucination은 어떻게 나타났는가?

011, 012는 담당자/매출을 만들지 않고 거절했다.

없는 정책 숫자를 만드는 강한 Hallucination은 No Answer Query에서 보이지 않았다.

Prompt B 이후에는 거절 문장에 외국어 토큰이 섞였다.

010처럼 관련 문서가 있다고 절차를 조합하는 문제는 No Answer Dataset 밖이다.

### Agent는 적절한 Tool을 선택했는가?

집합 기준 Accuracy는 0.625다.

Wrong Tool은 0이다.

Content Detail은 2/2 MATCH다.

Content Search 1건은 Tool을 호출하지 않고 JSON 텍스트를 답했다.

### 여러 Tool이 필요한 질문은 어떻게 처리했는가?

tool-007, tool-008은 `get_content_detail`만 호출했다.

정책 Tool을 빠뜨리고 상태 의미를 추측했다.

단순 Tool Calling은 Multi Tool을 보장하지 못한다.

### LangGraph가 실제로 필요했는가?

이 측정만으로는 필요하다고 확정하지 않았다.

Human Gate에서 단순 Tool Calling을 유지했다.

필요 후보로는 남아 있다. 구현하지 않았다.

### 예상과 달리 효과가 없었던 기술은 무엇인가?

넣은 기술 중 효과가 없었던 것은 Prompt B다.

목표 Query 006, 008을 통제 비교에서 고치지 못했고 Token / Latency는 늘었다.

넣지 않은 기술(Keyword, Hybrid, RRF, Reranker, LangGraph)은 "효과가 없었다"고 쓰지 않는다. 비교 실험 자체가 없다.

Chunking 대안 2개는 비교했고 Metric이 같았으므로, 이 Dataset에서는 바꿀 이유가 없었다.

### 현재 시스템에 남아 있는 한계는 무엇인가?

```text
retrieval-010 Retrieval Failure
002, 003, 009 정답 Section 부재
006 Generation Failure
llama3.2 외국어 토큰 혼입
Source 목록은 답변 근거가 아님
tool-002 Tool 미호출
tool-007, 008 Multi Tool 정책 누락
중복 Tool 호출
Question API와 Retrieval Evaluation 경로가 다름
```

---

## 실제로 효과가 있었던 변경

Retrieval Metric을 올린 변경은 없다.

Baseline Vector Search가 이미 문서 Hit Rate 0.9였다.

효과가 있었던 것은 검색 알고리즘 추가가 아니라 범위 확장이다.

```text
Phase 7
콘텐츠 Tool과 contents 테이블을 추가했다.
문서 RAG만으로는 콘텐츠 상태/장르 질문에 답할 수 없다.

Phase 8
Question API를 Simple Tool Calling에 연결했다.
Policy / Content Detail Query의 일부는 올바른 Tool을 탔다.
```

이 변경은 Retrieval Hit Rate를 올리지 않는다.

없는 기능을 측정 가능한 상태로 만든 것이다.

---

## 실패 Case

최종에도 남은 실패:

```text
retrieval-010  operations-faq.md miss
retrieval-002  CONTENT_BLOCKED를 답하지 못함
retrieval-003  M-03을 답하지 못함
retrieval-006  사용 시점 핵심 누락
retrieval-009  공개 가이드 체크리스트를 따르지 않음
tool-002       search_contents 미호출
tool-007/008   정책 Tool 누락
tool-001/004   같은 Tool 중복 호출
```

Evaluation Dataset을 바꿔 이 실패를 숨기지 않았다.

---

## 분석

실패 위치는 Phase마다 달랐다.

```text
Phase 2–5
문서 Retrieval은 대체로 동작한다.
남은 Miss는 유사 문서 1건이다.
문서 Ranking Failure는 없다.

Phase 6
문서 Hit ≠ 정답 Answer
Prompt만으로 006/008이 고쳐지지 않았다.

Phase 7–8
콘텐츠 질문은 Tool이 필요하다.
단순 Tool Calling은 Detail에는 되고 Multi Tool에는 부족하다.
```

평균 Hit Rate만 보면 시스템이 거의 완성된 것처럼 보인다.

Query 유형과 Generation / Tool을 나누면 한계가 남는다.

---

## 대안

Phase 9에서 새 기술을 넣지 않는다.

남아 있는 후보는 이전 Human Gate와 같다.

```text
유사 문서 Miss     Keyword / Hybrid 후보. 아직 미도입.
Section Rank       Reranker 후보. 문서 Failure는 0.
Generation 006     Prompt / Model 후보. Prompt B는 실패.
Multi Tool         LangGraph 후보. Human Gate에서 보류.
```

이번 실험의 결정은 후보를 구현하는 것이 아니다.

비교 결과를 남기는 것이다.

---

## 결정

```text
최종 Retrieval     Vector Search Only
최종 Chunking      Fixed 500 / 50
최종 Prompt        Prompt B
최종 Agent         Simple Tool Calling
Keyword/Hybrid/RRF/Reranker/LangGraph  도입하지 않음
```

DESIGN은 현재 코드와 같다.

ADR은 작성하지 않는다. 최종 구조를 새 기술로 바꾸지 않았다.

---

## Before / After

```text
Retrieval Hit Rate@K  0.9 → 0.9
Retrieval Recall@K    0.9 → 0.9
Retrieval MRR         0.75 → 0.75
retrieval-010         miss → miss
문서 Ranking Failure  0 → 0
Exact Keyword 문서 Hit 1.0 → 1.0
No Answer 거절        유지
Agent                 없음 → 집합 Accuracy 0.625
```

---

## 결론

최종 시스템은 Baseline보다 검색이 좋아지지 않았다.

같은 Dataset에서 문서 Hit Rate 0.9를 유지한다.

확인한 것:

```text
Vector Search는 Semantic / Exact Keyword 문서 검색에 충분했다.
유사 문서 1건은 끝까지 Miss다.
Chunking / Keyword / Hybrid / RRF / Reranker / LangGraph는
이 Dataset에서 도입 근거가 나오지 않아 넣지 않았다.
Prompt B는 목표 Generation Failure를 고치지 못했고 비용은 늘었다.
콘텐츠 질문은 Tool이 필요하고, 단순 Tool Calling은 Multi Tool을 보장하지 못한다.
```

구현한 기술 목록이 아니라, 같은 Dataset에서 남은 실패로 시스템을 판단한다.
