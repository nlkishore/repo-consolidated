"""Ensure buildScripts.sh and fileLists.txt change together (design doc §7)."""

from __future__ import annotations

import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional, Sequence


@dataclass
class PairCheckResult:
    ok: bool
    message: str
    touched_build_script: bool = False
    touched_file_list: bool = False
    changed_files: Sequence[str] = ()


def _run_git(args: List[str], cwd: Optional[Path]) -> str:
    proc = subprocess.run(
        ["git", *args],
        cwd=str(cwd) if cwd else None,
        capture_output=True,
        text=True,
        check=False,
    )
    if proc.returncode != 0:
        raise RuntimeError(proc.stderr.strip() or proc.stdout.strip() or "git failed")
    return proc.stdout


def list_changed_files_staged(repo_root: Path) -> List[str]:
    """Paths relative to repo root from `git diff --cached --name-only`."""
    out = _run_git(["diff", "--cached", "--name-only"], cwd=repo_root)
    lines = [ln.strip().replace("\\", "/") for ln in out.splitlines() if ln.strip()]
    return lines


def list_changed_files_rev(repo_root: Path, rev: str) -> List[str]:
    """Files changed in commit `rev` (for CI: git show --name-only --pretty='')."""
    out = _run_git(["show", "--name-only", "--pretty=format:", rev], cwd=repo_root)
    lines = [ln.strip().replace("\\", "/") for ln in out.splitlines() if ln.strip()]
    return lines


def list_changed_files_range(repo_root: Path, base: str, head: str) -> List[str]:
    """Files changed between base..head (merge-base aware)."""
    out = _run_git(["diff", "--name-only", f"{base}...{head}"], cwd=repo_root)
    lines = [ln.strip().replace("\\", "/") for ln in out.splitlines() if ln.strip()]
    return lines


class PairedCommitChecker:
    """
    If either build script or file list appears in changed files,
    both must appear unless bypass token is in commit message.
    """

    def __init__(
        self,
        build_script: str = "buildScripts.sh",
        file_list_names: Optional[Sequence[str]] = None,
        bypass_token: str = "[skip-hotfix-pair]",
    ):
        self.build_script = build_script
        self.file_list_names = set(
            file_list_names or ("fileLists.txt", "fileLists.yaml")
        )
        self.bypass_token = bypass_token

    def check_paths(
        self,
        changed_files: Sequence[str],
        commit_message: str = "",
    ) -> PairCheckResult:
        touched_script = any(Path(p).name == self.build_script for p in changed_files)
        touched_list = any(
            Path(p).name in self.file_list_names for p in changed_files
        )

        if self.bypass_token and self.bypass_token in (commit_message or ""):
            return PairCheckResult(
                ok=True,
                message="Bypass token present; paired rule skipped.",
                touched_build_script=touched_script,
                touched_file_list=touched_list,
                changed_files=changed_files,
            )

        if not touched_script and not touched_list:
            return PairCheckResult(
                ok=True,
                message="Neither hotfix metadata file changed.",
                touched_build_script=False,
                touched_file_list=False,
                changed_files=changed_files,
            )

        if touched_script and touched_list:
            return PairCheckResult(
                ok=True,
                message="Both hotfix metadata files updated together.",
                touched_build_script=True,
                touched_file_list=True,
                changed_files=changed_files,
            )

        if touched_script and not touched_list:
            msg = (
                f"Staged change includes {self.build_script} but not "
                f"{sorted(self.file_list_names)}. Commit both together or use "
                f"bypass {self.bypass_token!r} if policy allows."
            )
        else:
            msg = (
                f"Staged change includes file list but not {self.build_script}. "
                f"Commit both together or use bypass {self.bypass_token!r} if policy allows."
            )

        return PairCheckResult(
            ok=False,
            message=msg,
            touched_build_script=touched_script,
            touched_file_list=touched_list,
            changed_files=changed_files,
        )


def main_argv(argv: Optional[List[str]] = None) -> int:
    argv = argv if argv is not None else sys.argv[1:]
    # Minimal CLI: --staged [repo-root] or --rev REV [repo-root]
    staged = "--staged" in argv
    args = [a for a in argv if a != "--staged"]
    rev = None
    if "--rev" in args:
        i = args.index("--rev")
        if i + 1 >= len(args):
            print("usage: check-paired [--staged|--rev REV] [repo-root]", file=sys.stderr)
            return 2
        rev = args[i + 1]
        args = args[:i] + args[i + 2 :]

    repo = Path(args[0]).resolve() if args else Path.cwd().resolve()

    try:
        if rev:
            changed = list_changed_files_rev(repo, rev)
            msg_src = subprocess.run(
                ["git", "show", "-s", "--format=%B", rev],
                cwd=str(repo),
                capture_output=True,
                text=True,
                check=True,
            ).stdout
        elif staged:
            changed = list_changed_files_staged(repo)
            msg_src = ""
        else:
            print(
                "Specify --staged (pre-commit) or --rev SHA (CI).",
                file=sys.stderr,
            )
            return 2
    except (RuntimeError, subprocess.CalledProcessError) as exc:
        print(str(exc), file=sys.stderr)
        return 1

    checker = PairedCommitChecker()
    result = checker.check_paths(changed, commit_message=msg_src)
    if result.ok:
        print(result.message)
        return 0
    print(result.message, file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main_argv())
