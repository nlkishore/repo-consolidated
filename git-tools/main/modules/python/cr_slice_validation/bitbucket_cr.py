"""Minimal Bitbucket Server client for PR metadata."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any
from urllib.parse import quote

import httpx

from cr_slice_validation.config import BitbucketEnv


@dataclass
class PullRequestHead:
    pr_id: int
    title: str
    from_commit: str
    from_branch: str


class BitbucketCrClient:
    def __init__(self, env: BitbucketEnv) -> None:
        self._env = env
        base = env.server_url.rstrip("/")
        self._base = f"{base}{env.api_path_prefix}"
        self._client = httpx.Client(
            auth=httpx.BasicAuth(env.username, env.token),
            verify=env.verify_ssl,
            timeout=60.0,
        )

    def close(self) -> None:
        self._client.close()

    def _url(self, *parts: str) -> str:
        return f"{self._base}/{'/'.join(parts)}"

    def get_pull_request(self, pr_id: int) -> PullRequestHead:
        e = self._env
        url = self._url(
            "projects",
            quote(e.project_key, safe=""),
            "repos",
            quote(e.repository_slug, safe=""),
            "pull-requests",
            str(pr_id),
        )
        r = self._client.get(url)
        r.raise_for_status()
        data: dict[str, Any] = r.json()
        from_ref = data.get("fromRef") or {}
        return PullRequestHead(
            pr_id=int(data.get("id", pr_id)),
            title=str(data.get("title", "")),
            from_commit=str(from_ref.get("latestCommit") or ""),
            from_branch=str(from_ref.get("displayId") or ""),
        )

    def fetch_pr_heads(self, pr_ids: list[int]) -> list[PullRequestHead]:
        return [self.get_pull_request(pid) for pid in pr_ids]
