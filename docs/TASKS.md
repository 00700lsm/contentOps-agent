# TASKS

## Current Phase

```text
Phase 7
Content Data Tool 구현
```

구현 기준은 `docs/DESIGN.md`다.

완료 조건은 `docs/ROADMAP.md` Phase 7을 따른다.

---

## 목표

문서 검색만으로 해결할 수 없는 **구조화된 콘텐츠 데이터 질문**을 처리할 수 있도록 Tool을 추가한다.

자유 SQL을 LLM이 실행하게 하지 않는다.

명시적 Tool Interface만 둔다.

LangGraph / Multi-Agent는 만들지 않는다.

Question API의 RAG 경로는 유지한다. Tool 선택 평가는 Phase 8에서 한다.

---

## 이 Phase에서 하지 않는 것

```text
Keyword Search
Hybrid Search
RRF
Reranker
Embedding Model 변경
LLM Model 변경
Retrieval Evaluation Dataset 변경
Prompt 정책 변경
LangGraph
Multi-Agent
Multi Tool 조합 평가
```

---

## 기본 Tool

```text
search_policy_documents
search_contents
get_content_detail
```

---

## Tasks

### 1. Sample Content

- [x] `data/contents/contents.json` 추가
- [x] PostgreSQL `contents` 테이블에 적재한다.

### 2. Tool

- [x] 조건 기반 Content Search
- [x] Content Detail
- [x] Policy Search를 Tool로 노출

### 3. Dataset / Test

- [x] `evaluation/datasets/agent-tools.jsonl`에 Policy / Content Search / Content Detail Query 추가
- [x] Tool별 Test

### 4. DESIGN

- [x] 현재 코드 기준으로 DESIGN을 갱신한다.

---

## 완료 조건

```text
[x] Sample Content 데이터가 존재한다.
[x] 조건 기반 Content Search가 가능하다.
[x] Content Detail 조회가 가능하다.
[x] Policy Search 역할이 Tool 단위로 사용할 수 있다.
[x] Tool별 Test가 존재한다.
[x] Agent Evaluation Dataset에 Tool Query가 추가됐다.
[x] 아직 불필요한 Multi-Agent 구조를 만들지 않았다.
```

Question API는 문서 RAG를 유지한다. Tool Selection Evaluation은 Phase 8이다.
