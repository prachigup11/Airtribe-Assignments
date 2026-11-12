"""LangGraph resume agent that invokes filesystem tools through MCP."""

from __future__ import annotations

import argparse
import asyncio
import json
import os
import sys
from pathlib import Path
from typing import Any

from langchain_mcp_adapters.client import MultiServerMCPClient
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent
from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client


def _filesystem_server_config(resume_directory: str | None) -> dict[str, dict[str, Any]]:
    server_path = Path(__file__).with_name("filesystem_mcp_server.py")
    configured_directory = Path(
        resume_directory or os.environ.get("RESUME_DIRECTORY") or "resumes"
    ).expanduser()
    if not configured_directory.is_absolute():
        configured_directory = Path(__file__).parent / configured_directory
    return {
        "filesystem": {
            "transport": "stdio",
            "command": sys.executable,
            "args": [str(server_path)],
            "env": {"RESUME_DIRECTORY": str(configured_directory.resolve())},
        }
    }


async def discover_resources(resume_directory: str | None = None) -> dict[str, list[dict[str, Any]]]:
    """Return resources announced by the filesystem MCP server."""
    server = _filesystem_server_config(resume_directory)["filesystem"]
    parameters = StdioServerParameters(
        command=server["command"],
        args=server["args"],
        env=server["env"],
    )
    async with stdio_client(parameters) as (read_stream, write_stream):
        async with ClientSession(read_stream, write_stream) as session:
            await session.initialize()
            resources = await session.list_resources()
            templates = await session.list_resource_templates()
    return {
        "resources": [
            {
                "name": resource.name,
                "uri": str(resource.uri),
                "description": resource.description,
            }
            for resource in resources.resources
        ],
        "resource_templates": [
            {
                "uri_template": template.uriTemplate,
                "name": template.name,
                "description": template.description,
            }
            for template in templates.resourceTemplates
        ],
    }


async def run_agent(query: str, resume_directory: str | None = None) -> str:
    """Run a LangGraph ReAct agent with tools loaded from the MCP server."""
    if not os.environ.get("OPENAI_API_KEY"):
        raise RuntimeError("Set the OPENAI_API_KEY environment variable before starting the agent.")

    client = MultiServerMCPClient(_filesystem_server_config(resume_directory))
    tools = await client.get_tools()
    model = ChatOpenAI(model=os.environ.get("OPENAI_MODEL", "gpt-4o-mini"), temperature=0)
    agent = create_react_agent(
        model,
        tools,
        prompt=(
            "You are a resume matching assistant. Use filesystem MCP tools to inspect candidate "
            "resumes before making claims. The default resume directory is configured on the MCP "
            "server. Compare candidates against the user's criteria, cite resume evidence, and "
            "state clearly when information is missing."
        ),
    )
    result = await agent.ainvoke({"messages": [{"role": "user", "content": query}]})
    response = result["messages"][-1].content
    if isinstance(response, str):
        return response
    return json.dumps(response, ensure_ascii=False)


def main() -> None:
    parser = argparse.ArgumentParser(description="Match resumes using a LangGraph agent and MCP.")
    parser.add_argument("query", nargs="?", help="A natural-language resume matching request.")
    parser.add_argument(
        "--resume-directory",
        help="Directory containing resumes (defaults to RESUME_DIRECTORY or ./resumes).",
    )
    parser.add_argument(
        "--list-resources",
        action="store_true",
        help="Discover and print resources exposed by the filesystem MCP server.",
    )
    arguments = parser.parse_args()

    try:
        if arguments.list_resources:
            result = asyncio.run(discover_resources(arguments.resume_directory))
            print(json.dumps(result, indent=2, ensure_ascii=False))
            return
        if not arguments.query:
            parser.error("provide a query unless --list-resources is used")
        print(asyncio.run(run_agent(arguments.query, arguments.resume_directory)))
    except Exception as exc:
        parser.exit(1, f"Error: {exc}\n")


if __name__ == "__main__":
    main()
