# local-rag

Personal **local RAG** on Windows: index documents under configured folders, query with **Ollama** (embeddings + LLM) and **ChromaDB**.

## Initial index roots

| Path | Notes |
|------|--------|
| `C:\Investment` | Used instead of `C:\Investments` (that path does not exist on this PC) |
| `C:\MyGeneratedProjects\GitRepoPlan\repo-consolidated\docs` | Design docs, prompts, runbooks |

Edit `config/paths.yaml` to add more folders.

## Prerequisites

1. **Python 3.11+**
2. **[Ollama](https://ollama.com/download)** running locally:

```powershell
ollama pull nomic-embed-text
ollama pull phi3:mini
```

**This PC (16 GB RAM, integrated GPU, often low free RAM):** use `phi3:mini` for chat (see `config/rag.yaml`). Close browsers and heavy apps before `local-rag index`. Avoid `llama3.1:8b` unless you have 8+ GB free RAM.

## Setup

```powershell
cd C:\MyGeneratedProjects\local-rag
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -e .
```

## Commands

```powershell
# Show configured roots and index stats
local-rag status

# Index (incremental: skips unchanged files by SHA-256)
local-rag index

# Force full re-index
local-rag index --force

# Ask a question
local-rag query "What is the GEB integration testbed design?"
```

Or use batch scripts in `scripts\`.

## Configuration

- `config/paths.yaml` — include roots, extensions, exclusions
- `config/rag.yaml` — Ollama models, chunk size, top-k
- `.env` — optional overrides (`OLLAMA_BASE_URL`, etc.)

## Data layout

```
data/
  chroma/          # vector store
  sqlite/index.db # file registry (path, hash, chunk counts)
```

## Design reference

See `C:\MyGeneratedProjects\docs\LOCAL_RAG_WINDOWS_DESIGN.md`.
