from __future__ import annotations

import httpx

from local_rag.config import RagConfig


class OllamaClient:
    def __init__(self, cfg: RagConfig) -> None:
        self.cfg = cfg
        self._client = httpx.Client(base_url=cfg.ollama_base_url, timeout=120.0)

    def close(self) -> None:
        self._client.close()

    def health(self) -> bool:
        try:
            r = self._client.get("/api/tags")
            return r.status_code == 200
        except httpx.HTTPError:
            return False

    def embed(self, texts: list[str]) -> list[list[float]]:
        vectors: list[list[float]] = []
        for text in texts:
            r = self._client.post(
                "/api/embeddings",
                json={"model": self.cfg.embed_model, "prompt": text},
            )
            r.raise_for_status()
            data = r.json()
            vectors.append(data["embedding"])
        return vectors

    def chat(self, system: str, user: str) -> str:
        r = self._client.post(
            "/api/chat",
            json={
                "model": self.cfg.chat_model,
                "messages": [
                    {"role": "system", "content": system},
                    {"role": "user", "content": user},
                ],
                "stream": False,
            },
        )
        r.raise_for_status()
        return r.json()["message"]["content"]
