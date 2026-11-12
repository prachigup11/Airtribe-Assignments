"""File-system tools for reading and searching resume documents."""

from __future__ import annotations

from datetime import datetime
from pathlib import Path
from typing import Any


def _metadata(path: Path) -> dict[str, Any]:
    stat = path.stat()
    return {
        "name": path.name,
        "filepath": str(path),
        "extension": path.suffix.lower(),
        "size_bytes": stat.st_size,
        "modified": datetime.fromtimestamp(stat.st_mtime).astimezone().isoformat(),
    }


def _extract_text(path: Path) -> str:
    extension = path.suffix.lower()
    if extension == ".txt":
        return path.read_text(encoding="utf-8")
    if extension == ".pdf":
        from pypdf import PdfReader

        return "\n".join(page.extract_text() or "" for page in PdfReader(path).pages)
    if extension == ".docx":
        from docx import Document

        document = Document(path)
        content = [paragraph.text for paragraph in document.paragraphs]
        content.extend(
            "\t".join(cell.text for cell in row.cells)
            for table in document.tables
            for row in table.rows
        )
        return "\n".join(content)
    raise ValueError(f"Unsupported file type: {path.suffix or '(no extension)'}")


def read_file(filepath: str) -> dict[str, Any]:
    """Extract text and metadata from a TXT, PDF, or DOCX file."""
    path = Path(filepath).expanduser()
    try:
        if not path.is_file():
            raise FileNotFoundError(f"File not found: {path}")
        content = _extract_text(path)
        return {"success": True, "content": content, "metadata": _metadata(path)}
    except Exception as exc:
        return {
            "success": False,
            "content": None,
            "metadata": None,
            "error": str(exc),
        }


def list_files(directory: str, extension: str | None = None) -> list[dict[str, Any]]:
    """List files directly inside a directory, optionally filtered by extension."""
    path = Path(directory).expanduser()
    if extension:
        normalized_extension = extension.lower()
        if not normalized_extension.startswith("."):
            normalized_extension = f".{normalized_extension}"
    else:
        normalized_extension = None

    try:
        return [
            _metadata(item)
            for item in sorted(path.iterdir(), key=lambda item: item.name.lower())
            if item.is_file()
            and (normalized_extension is None or item.suffix.lower() == normalized_extension)
        ]
    except (FileNotFoundError, NotADirectoryError, PermissionError, OSError):
        return []


def write_file(filepath: str, content: str) -> dict[str, Any]:
    """Write UTF-8 text, creating parent directories when needed."""
    path = Path(filepath).expanduser()
    try:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")
        return {"success": True, "filepath": str(path), "bytes_written": path.stat().st_size}
    except (OSError, TypeError, ValueError) as exc:
        return {"success": False, "filepath": str(path), "error": str(exc)}


def search_in_file(filepath: str, keyword: str) -> dict[str, Any]:
    """Find case-insensitive keyword occurrences and return their surrounding text."""
    if not keyword:
        return {"success": False, "filepath": filepath, "keyword": keyword, "matches": [], "error": "Keyword cannot be empty"}

    result = read_file(filepath)
    if not result["success"]:
        return {
            "success": False,
            "filepath": filepath,
            "keyword": keyword,
            "matches": [],
            "error": result["error"],
        }

    content = result["content"]
    folded_characters = []
    source_positions = []
    for index, character in enumerate(content):
        folded_character = character.casefold()
        folded_characters.append(folded_character)
        source_positions.extend([index] * len(folded_character))
    folded_content = "".join(folded_characters)
    folded_keyword = keyword.casefold()
    matches = []
    start = 0
    while (position := folded_content.find(folded_keyword, start)) != -1:
        folded_end = position + len(folded_keyword)
        original_start = source_positions[position]
        original_end = source_positions[folded_end - 1] + 1
        matches.append(
            {
                "match": content[original_start:original_end],
                "start": original_start,
                "end": original_end,
                "context": content[
                    max(0, original_start - 60) : min(len(content), original_end + 60)
                ],
            }
        )
        start = folded_end

    return {
        "success": True,
        "filepath": filepath,
        "keyword": keyword,
        "match_count": len(matches),
        "matches": matches,
    }