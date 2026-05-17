"""Optional Bitbucket Server settings from environment."""

from __future__ import annotations

import os
from dataclasses import dataclass


@dataclass
class BitbucketEnv:
    server_url: str
    token: str
    project_key: str
    repository_slug: str
    username: str = "x-token-auth"
    api_path_prefix: str = "/rest/api/latest"
    verify_ssl: bool = True

    @classmethod
    def from_env(cls) -> BitbucketEnv | None:
        url = os.environ.get("BITBUCKET_SERVER_URL", "").strip()
        token = os.environ.get("BITBUCKET_TOKEN", "").strip()
        project = os.environ.get("BITBUCKET_PROJECT_KEY", "").strip()
        repo = os.environ.get("BITBUCKET_REPO_SLUG", "").strip()
        if not all([url, token, project, repo]):
            return None
        verify = os.environ.get("BITBUCKET_VERIFY_SSL", "true").lower() not in (
            "0",
            "false",
            "no",
        )
        return cls(
            server_url=url,
            token=token,
            project_key=project,
            repository_slug=repo,
            username=os.environ.get("BITBUCKET_USERNAME", "x-token-auth"),
            api_path_prefix=os.environ.get(
                "BITBUCKET_API_PREFIX", "/rest/api/latest"
            ),
            verify_ssl=verify,
        )
