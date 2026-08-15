# Experiment 007 - Tool Selection

## 목적

질문에 따라 **적절한 Tool을 선택하는지** 평가한다.

LangGraph를 기본으로 넣지 않는다.

먼저 단순 Tool Calling을 측정한다.

---

## 현재 상태

```text
Question API        AgentService + Spring AI ChatClient
Workflow            Simple Tool Calling
Tools               search_policy_documents
                    search_contents
                    get_content_detail
Chat Model          llama3.2
```

Retrieval Evaluation 경로는 그대로 `RetrievalService` + `RagService`다.

---

## 문제

콘텐츠 질문과 정책 질문을 같은 Question API로 받을 때, 어떤 Tool을 호출하는지가 맞아야 한다.

여러 Tool이 필요한 질문에서 한 번만 호출하고 끝낼 수도 있다.

---

## 가설

```text
가설 1
단순 Tool Calling으로 Policy / Content Search / Content Detail / Multi Tool을 처리할 수 있다.

가설 2
단일 Tool Query는 되지만, Multi Tool은 두 번째 Tool을 빠뜨린다.

가설 3
llama3.2가 Tool 대신 JSON 문자열을 답으로 출력할 수 있다.
```

---

## Dataset

```text
evaluation/datasets/agent-tools.jsonl
Query 8
POLICY_ONLY 2
CONTENT_SEARCH 2
CONTENT_DETAIL 2
MULTI_TOOL 2
```

Retrieval Dataset은 바꾸지 않았다.

---

## 실험 조건

```text
Chat Model     llama3.2
Embedding      nomic-embed-text
Top K          5
Workflow       Simple Tool Calling
측정 파일      evaluation/results/agent-20260815-143855.json
```

변경 변수는 Workflow가 아니라 **현재 단순 Tool Calling의 성능**이다.

---

## Metric

Tool Selection Accuracy는 Expected Tool 집합과 Actual Tool 집합이 같은 비율이다.

Sequence Accuracy는 호출 목록이 Expected와 완전히 같은 비율이다.

같은 Tool을 두 번 호출하면 집합은 같고 순서는 다르다. 이 경우 분류는 `WRONG_ORDER`다.

| Metric | 값 |
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

---

## Query별 결과

| ID | Category | Expected | Actual | 분류 |
| --- | --- | --- | --- | --- |
| tool-001 | POLICY_ONLY | search_policy_documents | 같은 Tool 2회 | WRONG_ORDER |
| tool-002 | CONTENT_SEARCH | search_contents | 없음 | MISSING_TOOL |
| tool-003 | CONTENT_DETAIL | get_content_detail | get_content_detail | MATCH |
| tool-004 | CONTENT_SEARCH | search_contents | 같은 Tool 2회 | WRONG_ORDER |
| tool-005 | CONTENT_DETAIL | get_content_detail | get_content_detail | MATCH |
| tool-006 | POLICY_ONLY | search_policy_documents | search_policy_documents | MATCH |
| tool-007 | MULTI_TOOL | detail + policy | get_content_detail | MISSING_TOOL |
| tool-008 | MULTI_TOOL | detail + policy | get_content_detail | MISSING_TOOL |

---

## 실패 Case

### tool-002 MISSING_TOOL

질문: 이번 달 공개 예정 액션 콘텐츠 알려줘.

Tool을 실행하지 않았다.

답은 `search_contents` 호출 JSON을 그대로 출력했다.

파라미터도 `status=Upcoming`처럼 스키마에 없는 값이다.

가설 3이 맞다.

### tool-007 / tool-008 MISSING_TOOL

상세 Tool은 호출했다.

정책 Tool은 호출하지 않았다.

tool-007 답은 `ContentBlockedException`처럼 Context에 없는 이름을 만들었다.

tool-008 답은 `M-03`을 `MODERATION`으로 바꿨다.

가설 2가 맞다.

### tool-001 / tool-004 WRONG_ORDER

필요한 Tool은 맞다. 같은 Tool을 두 번 호출했다.

tool-004 최종 답은 `[]`다. 검색 결과를 답에 쓰지 못했다.

---

## 분석

단일 Tool:

```text
Content Detail  2/2 MATCH
Policy Only     집합은 맞음. 1건은 중복 호출
Content Search  1건은 중복 호출, 1건은 Tool을 실행하지 않음
```

Multi Tool 2건은 모두 두 번째 Tool이 없다.

Wrong Tool(완전히 다른 Tool)은 0건이다.

단순 Tool Calling은 **Tool을 아예 못 고르는 수준은 아니다.**

다만:

```text
1. llama3.2가 Tool 호출 JSON을 답으로 흘린다.
2. Multi Tool에서 후속 Tool을 안 부른다.
3. Tool 결과가 있어도 답에서 사실을 왜곡할 수 있다.
```

LangGraph가 1번을 고친다는 측정은 없다.

2번은 상태 기반 Workflow 후보가 될 수 있다.

---

## 대안

구현하지 않고 후보만 비교한다.

### 후보 A. 단순 Tool Calling 유지

장점:

```text
구조가 작다.
Content Detail은 이미 동작한다.
Wrong Tool이 없다.
```

단점:

```text
Multi Tool 2건이 실패로 남는다.
Content Search 1건이 Tool을 실행하지 않는다.
```

### 후보 B. Agent Prompt만 보강

장점:

```text
구조 변경이 없다.
```

단점:

```text
Phase 6에서 Prompt B도 목표 Query를 못 고쳤다.
llama3.2에서 효과가 보장되지 않는다.
재평가가 필요하다.
```

### 후보 C. LangGraph로 Multi Tool 단계를 고정

예: 상세 조회 후 정책 검색을 강제.

장점:

```text
tool-007 / 008의 Missing Tool을 줄일 수 있다.
```

단점:

```text
단일 Tool Query에 불필요 단계가 생길 수 있다.
tool-002처럼 Tool JSON을 텍스트로 쓰는 문제는 남을 수 있다.
복잡도 / Latency가 늘어난다.
측정 전에 도입하면 Phase 규칙에 어긋난다.
```

---

## 추천안

```text
후보 A. 단순 Tool Calling 유지
```

이유:

```text
Wrong Tool은 없다.
실패는 Multi Tool 후속 호출과 llama3.2 Tool 루프 불안정이다.
LangGraph 필요성이 이 한 번의 측정만으로 확정되지 않는다.
```

LangGraph는 아직 넣지 않는다.

008-agent-workflow.md는 Workflow를 바꾼 뒤에만 작성한다.

ADR은 Workflow를 바꾼 뒤에만 작성한다.

---

## 결정

Human Gate에서 후보 A를 선택했다.

```text
Workflow     Simple Tool Calling 유지
LangGraph    도입하지 않음
Multi-Agent  도입하지 않음
Agent Prompt 추가 변경 없음
```

Question API 코드는 바꾸지 않는다.

008-agent-workflow.md는 작성하지 않는다. Workflow를 바꾸지 않았다.

ADR은 작성하지 않는다.

남은 실패는 그대로 둔다.

```text
tool-002  Tool 미실행
tool-007  정책 Tool 누락
tool-008  정책 Tool 누락
tool-001  정책 Tool 중복
tool-004  검색 Tool 중복
```

---

## Before / After

Question API Before: 항상 Vector RAG.

After: LLM이 Tool을 고른다. Human Gate 이후 Workflow는 그대로다.

LangGraph After 측정은 없다.

---

## Human Gate

후보 A를 선택했다.

```text
단순 Tool Calling 유지
LangGraph 불필요 (이번 측정 기준)
```

이 Phase에서 Workflow를 한 번 더 바꾸지 않는다.

다음 Phase는 최종 Evaluation 및 비교다.
