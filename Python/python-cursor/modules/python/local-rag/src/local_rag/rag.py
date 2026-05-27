from __future__ import annotations

from local_rag.config import CHROMA_DIR, RagConfig, load_rag_config
from local_rag.embedder import OllamaClient
from local_rag.store import VectorStore

SYSTEM_PROMPT = """You are a helpful assistant that answers questions using ONLY the provided context from the user's local documents.
If the context does not contain enough information, say you do not know and suggest what file topics might help.
Always end with a "Sources:" section listing file paths from the context."""


def _format_context(results: dict) -> tuple[str, list[str]]:
    docs = (results.get("documents") or [[]])[0]
    metas = (results.get("metadatas") or [[]])[0]
    parts: list[str] = []
    sources: list[str] = []

    for i, doc in enumerate(docs):
        meta = metas[i] if i < len(metas) else {}
        src = meta.get("source_path", "unknown")
        sources.append(src)
        parts.append(f"--- Context {i + 1} ({src}) ---\n{doc}")

    unique_sources = list(dict.fromkeys(sources))
    return "\n\n".join(parts), unique_sources


def query(question: str, *, top_k: int | None = None) -> dict[str, str | list[str]]:
    rag_cfg = load_rag_config()
    k = top_k or rag_cfg.top_k
    ollama = OllamaClient(rag_cfg)
    store = VectorStore(CHROMA_DIR)

    if store.count() == 0:
        raise RuntimeError("Index is empty. Run: local-rag index")

    if not ollama.health():
        raise RuntimeError(f"Ollama not reachable at {rag_cfg.ollama_base_url}")

    try:
        q_emb = ollama.embed([question])[0]
        results = store.query(q_emb, k)
        context, sources = _format_context(results)

        user_prompt = f"""Context from indexed documents:

{context}

Question: {question}

Answer based on the context above."""

        answer = ollama.chat(SYSTEM_PROMPT, user_prompt)
        return {"answer": answer, "sources": sources, "question": question}
    finally:
        ollama.close()
