import os
from pathlib import Path
from git import Repo, Actor
from dotenv import load_dotenv

load_dotenv()

DOCS_PATH = Path(os.getenv("DOCS_PATH", "../Docs")).resolve()
REPO_PATH = DOCS_PATH.parent


def _repo() -> Repo:
    return Repo(REPO_PATH)


def list_files() -> list[str]:
    return sorted(p.name for p in DOCS_PATH.glob("*.org") if p.is_file())


def read_file(path: str) -> str:
    full = DOCS_PATH / path
    if not full.resolve().is_relative_to(DOCS_PATH):
        raise ValueError("Path traversal not allowed")
    return full.read_text(encoding="utf-8")


def write_and_commit(path: str, content: str, message: str, author: str) -> None:
    full = DOCS_PATH / path
    if not full.resolve().is_relative_to(DOCS_PATH):
        raise ValueError("Path traversal not allowed")
    full.parent.mkdir(parents=True, exist_ok=True)
    full.write_text(content, encoding="utf-8")
    repo = _repo()
    repo.index.add([str(full.relative_to(REPO_PATH))])
    actor = Actor(author, f"{author}@git-cms")
    repo.index.commit(message, author=actor, committer=actor)
