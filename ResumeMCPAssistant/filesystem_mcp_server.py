"""MCP server exposing the resume assistant's filesystem capabilities."""

from __future__ import annotations

import json
import math
import os
import time
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import Any

from mcp.server.fastmcp import FastMCP

from fs_tools import list_files as _list_files
from fs_tools import read_file as _read_file
from fs_tools import search_in_file as _search_in_file
from fs_tools import write_file as _write_file


SUPPORTED_EXTENSIONS = (".txt", ".pdf", ".docx")
MAX_BATCH_WORKERS = 8
MAX_WATCH_TIMEOUT_SECONDS = 300

mcp = FastMCP(
    "resume-filesystem",
    instructions=(
        "Use these tools and resources to inspect and manage resume files. "
        "The configured resume directory defaults to the server's resumes folder."
    ),
)


def _resume_directory() -> Path:
    configured = os.environ.get("RESUME_DIRECTORY")
    default = Path(__file__).with_name("resumes")
    return Path(configured).expanduser().resolve() if configured else default.resolve()


def _safe_resume_path(filename: str) -> Path:
    directory = _resume_directory()
    path = (directory / filename).resolve()
    if path == directory or directory not in path.parents:
        raise ValueError("Resource paths must stay inside the configured resume directory.")
    return path


@mcp.resource("resumes://directory")
def resume_directory_resource() -> str:
    """Return a JSON listing of files in the configured resume directory."""
    directory = _resume_directory()
    if not directory.is_dir():
        raise FileNotFoundError(f"Resume directory not found: {directory}")
    return json.dumps(_list_files(str(directory)), ensure_ascii=False)


@mcp.resource("resume:///{filename}")
def resume_file_resource(filename: str) -> str:
    """Read a resume resource from the configured resume directory."""
    path = _safe_resume_path(filename)
    result = _read_file(str(path))
    if not result["success"]:
        raise FileNotFoundError(result["error"])
    return result["content"]


@mcp.tool()
def get_server_config() -> dict[str, Any]:
    """Get the active filesystem server configuration."""
    return {
        "resume_directory": str(_resume_directory()),
        "supported_extensions": list(SUPPORTED_EXTENSIONS),
        "max_batch_workers": MAX_BATCH_WORKERS,
        "max_watch_timeout_seconds": MAX_WATCH_TIMEOUT_SECONDS,
    }


@mcp.tool()
def read_file(filepath: str) -> dict[str, Any]:
    """Extract text and metadata from a TXT, PDF, or DOCX file."""
    return _read_file(filepath)


@mcp.tool()
def list_files(directory: str | None = None, extension: str | None = None) -> list[dict[str, Any]]:
    """List files directly inside a directory, optionally filtered by extension."""
    return _list_files(directory or str(_resume_directory()), extension)


@mcp.tool()
def write_file(filepath: str, content: str) -> dict[str, Any]:
    """Write UTF-8 text, creating parent directories when needed."""
    return _write_file(filepath, content)


@mcp.tool()
def search_in_file(filepath: str, keyword: str) -> dict[str, Any]:
    """Find case-insensitive keyword occurrences and return surrounding text."""
    return _search_in_file(filepath, keyword)


@mcp.tool()
def batch_process(filepaths: list[str], keyword: str | None = None) -> dict[str, Any]:
    """Read multiple files concurrently and optionally search each for a keyword."""

    def process(filepath: str) -> dict[str, Any]:
        result = _read_file(filepath)
        if keyword is not None and result["success"]:
            result["search"] = _search_in_file(filepath, keyword)
        return {"filepath": filepath, **result}

    workers = min(MAX_BATCH_WORKERS, max(1, len(filepaths)))
    with ThreadPoolExecutor(max_workers=workers) as executor:
        results = list(executor.map(process, filepaths))
    return {
        "success": all(result["success"] for result in results),
        "processed_count": len(results),
        "results": results,
    }


@mcp.tool()
def watch_directory(
    directory: str | None = None,
    timeout_seconds: float = 30,
    poll_interval_seconds: float = 1,
    extensions: list[str] | None = None,
) -> dict[str, Any]:
    """Wait for new files in a directory and return when any are detected."""
    if not math.isfinite(timeout_seconds) or timeout_seconds <= 0 or timeout_seconds > MAX_WATCH_TIMEOUT_SECONDS:
        raise ValueError(f"timeout_seconds must be between 0 and {MAX_WATCH_TIMEOUT_SECONDS}.")
    if not math.isfinite(poll_interval_seconds) or poll_interval_seconds <= 0:
        raise ValueError("poll_interval_seconds must be greater than zero.")

    path = Path(directory or _resume_directory()).expanduser()
    if not path.is_dir():
        raise NotADirectoryError(f"Directory not found: {path}")
    normalized_extensions = (
        {extension.lower() if extension.startswith(".") else f".{extension.lower()}" for extension in extensions}
        if extensions
        else set(SUPPORTED_EXTENSIONS)
    )
    existing = {item.resolve() for item in path.iterdir() if item.is_file()}
    deadline = time.monotonic() + timeout_seconds

    while True:
        new_files = [
            item
            for item in sorted(path.iterdir(), key=lambda candidate: candidate.name.lower())
            if item.is_file()
            and item.resolve() not in existing
            and item.suffix.lower() in normalized_extensions
        ]
        if new_files:
            metadata = {item["name"]: item for item in _list_files(str(path))}
            return {"success": True, "files": [metadata[item.name] for item in new_files]}

        remaining = deadline - time.monotonic()
        if remaining <= 0:
            return {"success": True, "files": [], "timed_out": True}
        time.sleep(min(poll_interval_seconds, remaining))


def main() -> None:
    mcp.run(transport="stdio")


if __name__ == "__main__":
    main()
