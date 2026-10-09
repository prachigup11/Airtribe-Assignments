"""Command-line resume assistant powered by OpenAI tool calling."""

from __future__ import annotations

import argparse
import json
import os
from typing import Any

from openai import OpenAI

from fs_tools import list_files, read_file, search_in_file, write_file


TOOL_DEFINITIONS = [
    {
        "type": "function",
        "function": {
            "name": "read_file",
            "description": "Read and extract text and metadata from a TXT, PDF, or DOCX resume.",
            "parameters": {
                "type": "object",
                "properties": {"filepath": {"type": "string"}},
                "required": ["filepath"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "list_files",
            "description": "List files in a directory, optionally filtered by extension.",
            "parameters": {
                "type": "object",
                "properties": {
                    "directory": {"type": "string"},
                    "extension": {"type": ["string", "null"]},
                },
                "required": ["directory", "extension"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "write_file",
            "description": "Write UTF-8 text to a file, creating parent directories as needed.",
            "parameters": {
                "type": "object",
                "properties": {
                    "filepath": {"type": "string"},
                    "content": {"type": "string"},
                },
                "required": ["filepath", "content"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "search_in_file",
            "description": "Search a resume for a case-insensitive keyword and return matching context.",
            "parameters": {
                "type": "object",
                "properties": {
                    "filepath": {"type": "string"},
                    "keyword": {"type": "string"},
                },
                "required": ["filepath", "keyword"],
                "additionalProperties": False,
            },
        },
    },
]

TOOL_FUNCTIONS = {
    "read_file": read_file,
    "list_files": list_files,
    "write_file": write_file,
    "search_in_file": search_in_file,
}


def run_assistant(query: str, resume_directory: str = "resumes") -> str:
    """Send a query to the model and execute requested filesystem tools."""
    if not os.environ.get("OPENAI_API_KEY"):
        raise RuntimeError("Set the OPENAI_API_KEY environment variable before starting the assistant.")

    client = OpenAI()
    messages: list[dict[str, Any]] = [
        {
            "role": "system",
            "content": (
                "You are a resume file assistant. Use the available tools for file operations. "
                f"The default resume directory is '{resume_directory}'. For requests about all resumes, "
                "list that directory and inspect relevant files. Do not claim an operation succeeded "
                "unless the tool result confirms it. Summarize findings clearly."
            ),
        },
        {"role": "user", "content": query},
    ]

    for _ in range(8):
        response = client.chat.completions.create(
            model=os.environ.get("OPENAI_MODEL", "gpt-4o-mini"),
            messages=messages,
            tools=TOOL_DEFINITIONS,
            tool_choice="auto",
        )
        message = response.choices[0].message
        if not message.tool_calls:
            return message.content or "The assistant returned no text response."

        messages.append(message.model_dump(exclude_none=True))
        for tool_call in message.tool_calls:
            function = TOOL_FUNCTIONS.get(tool_call.function.name)
            if function is None:
                result: Any = {"success": False, "error": "Unknown tool"}
            else:
                try:
                    arguments = json.loads(tool_call.function.arguments)
                    result = function(**arguments)
                except (json.JSONDecodeError, TypeError, ValueError) as exc:
                    result = {"success": False, "error": str(exc)}
            messages.append(
                {
                    "role": "tool",
                    "tool_call_id": tool_call.id,
                    "content": json.dumps(result, ensure_ascii=False),
                }
            )

    return "The assistant reached the maximum number of tool calls. Please narrow the request and try again."


def main() -> None:
    parser = argparse.ArgumentParser(description="Ask an LLM to work with local resume files.")
    parser.add_argument("query", help="A natural-language request about your resume files.")
    parser.add_argument("--resume-directory", default="resumes", help="Directory containing resumes (default: resumes).")
    arguments = parser.parse_args()

    try:
        print(run_assistant(arguments.query, arguments.resume_directory))
    except Exception as exc:
        parser.exit(1, f"Error: {exc}\n")


if __name__ == "__main__":
    main()