# Local Windows RAG Assistant — Plan & Design

**Purpose:** Personal generative-AI application on a Windows PC that answers questions from **your own files** (Word, PDF, text, Excel, etc.) using **RAG**, a **local vector database**, and a **local LLM** — all open source, no cloud required for inference or storage.

**Status:** Phase 1 MVP scaffolded at `C:\MyGeneratedProjects\local-rag` (index roots: `C:\Investment`, `repo-consolidated\docs`)  
**Target OS:** Windows 10/11 (64-bit)  
**Date:** 2026-05-23

---

## 1. Goals and scope

### Goals

| # | Goal |
|---|------|
| G1 | Chat with natural language over indexed documents on the machine |
| G2 | Index common office formats: `.docx`, `.pdf`, `.txt`, `.md`, `.xlsx`, `.xls`, `.csv`, `.pptx`, `.html`, `.rtf` |
| G3 | All inference and embeddings run **locally** (optional: user may still choose cloud later — out of scope for v1) |
| G4 | Vector index persisted on disk; incremental re-index when files change |
| G5 | Open-source stack only (models may have their own licenses — see §10) |

### Non-goals (v1)

- Indexing **every byte** on `C:\` without filters (too slow, noisy, privacy risk)
- Real-time collaboration or multi-user ACLs
- Replacing Excel as a calculation engine (RAG retrieves **text/summary**, not live formulas)
- Guaranteed legal/compliance review of model output

### Important constraint: “all documents on Windows”

Indexing the entire drive is **not recommended** as a first step. A practical design uses:

1. **Allow-lists** — e.g. `C:\Users\<you>\Documents`, `C:\Investment`, `C:\MyGeneratedProjects`
2. **Deny-lists** — `Windows`, `Program Files`, `$Recycle.Bin`, `node_modules`, `.git`, caches, VMs
3. **Phased rollout** — pilot folders → expand → optional scheduled full-user-profile scan

---

## 2. Recommended architecture (two deployment options)

### Option A — Fastest path (desktop app, minimal coding)

Best if you want a working system in hours, not weeks.

```
┌─────────────────────────────────────────────────────────────────┐
│  AnythingLLM Desktop  OR  Open WebUI  (browser UI)              │
│  - folder watch / manual upload                                   │
│  - built-in RAG pipeline                                          │
└────────────┬───────────────────────────────┬────────────────────┘
             │                               │
             ▼                               ▼
┌────────────────────────┐      ┌──────────────────────────────┐
│  Ollama (local LLM +     │      │  Embedded vector store        │
│  embedding models)       │      │  (LanceDB / Chroma inside app) │
└────────────────────────┘      └──────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────┐
│  Document folders (allow-listed paths on disk)                    │
└─────────────────────────────────────────────────────────────────┘
```

**Pros:** Little Python; UI included; Ollama integration is mature on Windows.  
**Cons:** Less control over Excel chunking, enterprise exclusions, and custom metadata.

### Option B — Custom Python RAG (maximum control) — **recommended for your use case**

Best if you need Excel-aware parsing, path rules, audit logs, and full control.

```
┌──────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Streamlit /     │     │  RAG orchestrator │     │  Ollama API      │
│  Open WebUI      │────▶│  (LlamaIndex or   │────▶│  LLM + embed     │
│  (chat UI)       │     │   LangChain)      │     │  models          │
└──────────────────┘     └────────┬─────────┘     └─────────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
            ┌─────────────┐ ┌──────────┐ ┌──────────────┐
            │  Ingestion  │ │ ChromaDB │ │  SQLite      │
            │  pipeline   │ │ (vectors)│ │  (file meta, │
            │             │ │          │ │   job state)  │
            └──────┬──────┘ └──────────┘ └──────────────┘
                   │
     ┌─────────────┴─────────────┐
     ▼                           ▼
┌─────────────┐           ┌──────────────┐
│ unstructured│           │ openpyxl +   │
│ (PDF/DOCX)  │           │ pandas       │
│ + pypdf     │           │ (Excel/CSV)  │
└─────────────┘           └──────────────┘
                   │
                   ▼
        Allow-listed Windows folders
```

**Pros:** Excel strategy, exclusions, incremental index, logging.  
**Cons:** You build/maintain a small Python project (~1–2 weeks for MVP).

**Recommendation:** Start with **Option A** for a proof of concept (1 day), then implement **Option B** for production-like personal use with Excel and path policies.

---

## 3. Open-source software to install (Windows)

### 3.1 Core runtime (required)

| Software | Role | Install |
|----------|------|---------|
| **Python 3.11 or 3.12** | Ingestion, RAG app, scripts | [python.org](https://www.python.org/downloads/) — check “Add to PATH” |
| **Git for Windows** | Clone repos, optional model scripts | [git-scm.com](https://git-scm.com/) |
| **Ollama** | Local LLM + embedding models via HTTP API | [ollama.com/download](https://ollama.com/download) |

### 3.2 Optional but strongly recommended

| Software | Role | Install |
|----------|------|---------|
| **Docker Desktop** | Run Qdrant/PostgreSQL/pgvector if you outgrow Chroma | [docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop/) |
| **Tesseract OCR** | Text from scanned PDFs/images | [GitHub tesseract](https://github.com/UB-Mannheim/tesseract/wiki) + add to PATH |
| **Poppler** | Better PDF text extraction (with `pdf2image`) | Windows build or via `conda` |
| **LibreOffice** (headless) | Legacy `.doc`, `.xls`, `.ppt` conversion | [libreoffice.org](https://www.libreoffice.org/) |
| **Visual Studio Build Tools** | Compile some Python wheels on Windows | “Desktop development with C++” workload |

### 3.3 GPU (optional, faster)

| Software | When |
|----------|------|
| **NVIDIA Driver** + **CUDA** | If you have an NVIDIA GPU; Ollama uses it automatically when available |
| **AMD ROCm** | Less mature on Windows for local LLM; prefer NVIDIA for local gen-AI |

### 3.4 UI choices (pick one)

| UI | License | Notes |
|----|---------|-------|
| **Open WebUI** | OSS (BSD) | Excellent Ollama chat + document RAG; Docker or native |
| **AnythingLLM Desktop** | MIT | Desktop app, folder sync, LanceDB inside |
| **Streamlit** (custom) | Apache 2.0 | You build the chat UI in Python |
| **LibreChat** | MIT | Heavier; multi-provider (can point to Ollama) |

---

## 4. Python packages (Option B — custom stack)

Use a dedicated venv, e.g. `C:\MyGeneratedProjects\local-rag\.venv`.

### 4.1 `pyproject.toml` / `requirements.txt` (proposed)

```text
# Orchestration (choose one primary)
llama-index>=0.12.0
llama-index-vector-stores-chroma>=0.4.0
llama-index-embeddings-ollama>=0.3.0
llama-index-llms-ollama>=0.3.0

# Alternative / complementary
langchain>=0.3.0
langchain-community>=0.3.0
langchain-ollama>=0.2.0

# Vector DB
chromadb>=0.5.0

# Document parsing
unstructured[local-inference]>=0.16.0
pypdf>=5.0.0
python-docx>=1.1.0
openpyxl>=3.1.0
pandas>=2.2.0
xlrd>=2.0.0          # legacy .xls (read-only)

# Excel → text for RAG
tabulate>=0.9.0

# OCR / images (optional)
pytesseract>=0.3.10
pdf2image>=1.17.0

# API & UI
fastapi>=0.115.0
uvicorn[standard]>=0.32.0
streamlit>=1.40.0

# Config & ops
pydantic>=2.0
pydantic-settings>=2.0
python-dotenv>=1.0.0
watchdog>=6.0.0      # folder watcher for incremental index
structlog>=24.0.0
tqdm>=4.66.0

# State DB
sqlalchemy>=2.0.0
```

**Note:** `unstructured[local-inference]` pulls heavier deps; for a lighter MVP use `unstructured` without local inference and rely on `pypdf` + `python-docx` + `openpyxl` only.

### 4.2 Ollama models to pull (after Ollama install)

```powershell
ollama pull llama3.1:8b          # or qwen2.5:7b, mistral:7b — general chat
ollama pull nomic-embed-text     # embeddings (768-dim, good default)
```

**CPU-only / low RAM:** `phi3:mini`, `gemma2:2b`, `nomic-embed-text`  
**16 GB RAM + GPU:** `llama3.1:8b` or `qwen2.5:7b`  
**32 GB+ RAM:** `llama3.1:70b` (quantized) if GPU VRAM allows

---

## 5. Vector database choice

| Database | License | Deployment | Best for |
|----------|---------|------------|----------|
| **ChromaDB** | Apache 2.0 | Embedded, single folder on disk | **Personal MVP** — simplest |
| **LanceDB** | Apache 2.0 | Embedded | Used by AnythingLLM; columnar, fast |
| **Qdrant** | Apache 2.0 | Docker or local binary | Larger collections, filtering |
| **PostgreSQL + pgvector** | OSS | Docker | If you already use Postgres |
| **FAISS** | MIT | Library only | No metadata server; DIY |

**Proposal:** **ChromaDB** persisted at `C:\MyGeneratedProjects\local-rag\data\chroma` plus **SQLite** for file inventory (path, hash, mtime, chunk ids).

---

## 6. Document ingestion design

### 6.1 File discovery

```yaml
# config/paths.yaml (example)
include_roots:
  - C:\Users\<username>\Documents
  - C:\Investment
  - C:\MyGeneratedProjects
exclude_globs:
  - "**/node_modules/**"
  - "**/.git/**"
  - "**/AppData/**"
  - "**/Windows/**"
  - "**/~$*"              # Excel temp locks
extensions:
  - .pdf
  - .docx
  - .doc
  - .txt
  - .md
  - .xlsx
  - .xls
  - .csv
  - .pptx
  - .html
  - .rtf
max_file_mb: 50
```

**Indexer behavior:**

1. Walk allow-listed roots (respect `exclude_globs`).
2. For each file: compute SHA-256; skip if unchanged since last run.
3. Parse → chunk → embed → upsert vectors; store metadata in SQLite.

### 6.2 Chunking strategy

| Setting | Proposed value | Rationale |
|---------|----------------|-----------|
| Chunk size | 512–1024 tokens | Balance context vs precision |
| Overlap | 10–15% | Preserve sentences split across chunks |
| Metadata per chunk | `source_path`, `file_name`, `page`, `sheet_name`, `modified_time` | Citations in answers |

### 6.3 Format-specific handling

| Format | Parser | Chunking notes |
|--------|--------|----------------|
| PDF | `pypdf` + optional OCR | Page-level metadata |
| DOCX | `python-docx` or `unstructured` | Heading-aware splits if possible |
| TXT/MD | direct read | Paragraph / header splits |
| **XLSX/XLS** | `openpyxl` / `pandas` | **One chunk per sheet** or per N rows as markdown table; include sheet name in metadata |
| CSV | `pandas` | Header + sample rows + schema line |
| Legacy `.doc`/`.ppt` | LibreOffice headless → PDF/DOCX | Batch conversion folder |

**Excel caveat:** RAG returns **stored cell values**, not live workbook formulas or external links. For large sheets, index **summary rows** (first 100 + header) or **named ranges** only.

### 6.4 Incremental updates

- **watchdog** on include roots (debounced 30s) → queue re-index for changed paths.
- Nightly **full reconcile** job: remove vectors for deleted files.

---

## 7. RAG query flow

```
User question
    → embed question (nomic-embed-text via Ollama)
    → Chroma similarity search (top-k=5–10, optional metadata filter)
    → build prompt: system + cited chunks + user question
    → Ollama LLM generate
    → response + source list (file path, page/sheet)
```

**Prompt guardrails (recommended):**

- “Answer only from provided context; if insufficient, say you don’t know.”
- Always return **Sources:** with paths (helps trust and debugging).

**Optional upgrades (phase 2):**

- Hybrid search (BM25 + vectors) via `rank_bm25` or LlamaIndex hybrid retriever
- Re-ranker: `bge-reranker` (small cross-encoder, still local)
- Query routing: “summarize file X” vs “search all Excel for revenue”

---

## 8. Security and privacy

| Topic | Design |
|-------|--------|
| Data residency | Vectors + SQLite on local disk only |
| Secrets | No API keys in repo; `.env` for optional future cloud |
| Exclusions | Never index `C:\Windows`, credentials, password managers, browser profiles |
| Sensitive folders | Explicit deny-list (e.g. `Downloads` if desired) |
| Logs | Structured logs without document body content |
| Backups | Chroma folder + SQLite in your normal backup routine |

---

## 9. Hardware sizing (guidance)

| Profile | RAM | GPU | Typical models |
|---------|-----|-----|----------------|
| Minimum | 16 GB | None (CPU) | `phi3:mini` + `nomic-embed-text` |
| Comfortable | 32 GB | 8–12 GB VRAM | `llama3.1:8b` + embeddings |
| Power user | 64 GB | 24 GB VRAM | Larger quant models, faster indexing |

**Disk:** Plan **20–50 GB** for models + vector store (grows with document count).

---

## 10. Model and license notes

- **Ollama models** (Llama, Mistral, Qwen, etc.) have **per-model licenses** — review before commercial use; personal use is generally fine.
- All **infrastructure** listed here is open source; **weights** are separate downloads.

---

## 11. Implementation phases

### Phase 0 — Proof of concept (1 day)

1. Install Python 3.12, Ollama, Git.
2. `ollama pull llama3.1:8b` and `ollama pull nomic-embed-text`.
3. Install **AnythingLLM Desktop** or **Open WebUI**; point at Ollama; add one folder (`C:\Investment`).
4. Validate Q&A on PDF + one Excel export.

**Exit criteria:** You can ask a question and get an answer with citations from a small folder.

### Phase 1 — MVP custom indexer (1–2 weeks)

1. Create `local-rag` Python project under `C:\MyGeneratedProjects\local-rag`.
2. Implement path config, file walker, hash skip, Chroma + SQLite.
3. Parsers: PDF, DOCX, TXT, XLSX (markdown tables).
4. CLI: `index`, `query`, `status`.
5. Streamlit chat UI with source paths.

**Exit criteria:** Index 3 allow-listed roots; chat works offline.

### Phase 2 — Production-like personal use (2–4 weeks)

1. watchdog incremental indexing.
2. OCR for scanned PDFs (Tesseract).
3. LibreOffice pipeline for `.doc`/`.ppt`.
4. Hybrid retrieval + optional reranker.
5. Scheduled reconcile; index stats dashboard.

### Phase 3 — Optional enhancements

- Multi-collection (Work vs Personal)
- Email `.pst` export indexing (complex; separate module)
- GPU batch embedding via `sentence-transformers` if Ollama embed is slow

---

## 12. Proposed project layout

```
C:\MyGeneratedProjects\local-rag\
  pyproject.toml
  .env.example
  config\
    paths.yaml
    rag.yaml              # chunk size, top_k, model names
  data\
    chroma\               # vector persistence
    sqlite\index.db       # file registry
  src\
    local_rag\
      __init__.py
      config.py
      indexer\
        walker.py
        parsers\
          pdf_parser.py
          docx_parser.py
          excel_parser.py
        chunker.py
        embedder.py       # Ollama embeddings client
      store\
        chroma_store.py
        metadata_db.py
      rag\
        retriever.py
        generator.py
      cli.py
      app_streamlit.py
  scripts\
    index-all.bat
    index-all.ps1
    run-chat.bat
  tests\
    test_excel_chunk.py
  README.md
```

---

## 13. Alternative stacks (comparison)

| Approach | Effort | Excel | Full-disk | Control |
|----------|--------|-------|-----------|---------|
| AnythingLLM + Ollama | Low | Good (upload/sync) | Manual folders | Medium |
| Open WebUI + Ollama | Low | Via RAG upload | Manual | Medium |
| **Custom Python + Chroma + Ollama** | Medium | **Best** | **Allow-list** | **High** |
| PrivateGPT | Medium | Supported | Folder-based | Medium |
| RAGFlow (Docker) | High | Strong | Enterprise-style | High |
| LM Studio only | Low | Limited RAG | No | Low |

---

## 14. Risks and mitigations

| Risk | Mitigation |
|------|------------|
| Indexing entire PC takes days | Allow-list + max file size + skip binaries |
| Excel sheets too large | Row limits; sheet summaries |
| Stale index | Hash + mtime; watchdog |
| Hallucinations | Strict “context only” prompt; show sources |
| RAM exhaustion | Smaller models; batch size limits |
| Windows path length | Use `\\?\` long paths in Python 3.11+ |

---

## 15. Quick start commands (after Phase 0 install)

```powershell
# Ollama
ollama pull nomic-embed-text
ollama pull llama3.1:8b

# Verify Ollama
curl http://localhost:11434/api/tags

# Optional: Open WebUI via Docker
docker run -d -p 3000:8080 --add-host=host.docker.internal:host-gateway `
  -v open-webui:/app/backend/data --name open-webui `
  ghcr.io/open-webui/open-webui:main
# Browser: http://localhost:3000 → connect to Ollama at host.docker.internal:11434
```

---

## 16. Decision summary (proposal)

| Layer | **Recommended choice** |
|-------|-------------------------|
| LLM + embeddings | **Ollama** (`llama3.1:8b` + `nomic-embed-text`) |
| Vector DB | **ChromaDB** (embedded) |
| Orchestration | **LlamaIndex** (or LangChain if team prefers) |
| Excel | **openpyxl + pandas** → markdown chunks |
| Other docs | **pypdf**, **python-docx**, **unstructured** (selective) |
| Metadata / jobs | **SQLite** |
| UI (MVP) | **Streamlit**; optional **Open WebUI** for daily chat |
| Index scope | **Allow-listed folders**, not whole OS in v1 |

---

## 17. Next steps

1. Confirm **allow-list folders** (e.g. `Documents`, `Investment`, `MyGeneratedProjects`, others).
2. Confirm **hardware** (RAM, NVIDIA GPU model if any).
3. Choose **Option A** (fast POC) vs **Option B** (custom project) for first build.
4. If approved, scaffold `C:\MyGeneratedProjects\local-rag` per §12 and implement Phase 1.

---

*This document is a design proposal only. No software is installed or configured by this file.*
