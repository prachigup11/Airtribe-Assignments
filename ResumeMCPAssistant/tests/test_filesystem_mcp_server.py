import asyncio
import json
import os
import sys
import tempfile
import unittest
from contextlib import asynccontextmanager
from pathlib import Path

from mcp import ClientSession, StdioServerParameters
from mcp.client.stdio import stdio_client
from pydantic import AnyUrl


SERVER_PATH = Path(__file__).parents[1] / "filesystem_mcp_server.py"


class FilesystemMCPServerTests(unittest.IsolatedAsyncioTestCase):
    async def asyncSetUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.directory = Path(self.temp_dir.name)
        (self.directory / "candidate.txt").write_text(
            "Python and AWS engineer", encoding="utf-8"
        )
        self.parameters = StdioServerParameters(
            command=sys.executable,
            args=[str(SERVER_PATH)],
            env={**os.environ, "RESUME_DIRECTORY": str(self.directory)},
        )

    async def asyncTearDown(self):
        self.temp_dir.cleanup()

    @asynccontextmanager
    async def _connect(self):
        async with stdio_client(self.parameters) as (read_stream, write_stream):
            async with ClientSession(read_stream, write_stream) as session:
                await session.initialize()
                yield session

    @staticmethod
    def _tool_data(result):
        if result.structuredContent is not None:
            return result.structuredContent
        text = next(block.text for block in result.content if hasattr(block, "text"))
        return json.loads(text)

    async def test_protocol_discovery_resources_and_batch_processing(self):
        async with self._connect() as session:
            await self._assert_protocol_and_batch(session)

    async def _assert_protocol_and_batch(self, session):
        tools = await session.list_tools()
        tool_names = {tool.name for tool in tools.tools}
        self.assertTrue(
            {
                "read_file",
                "list_files",
                "write_file",
                "search_in_file",
                "watch_directory",
                "batch_process",
                "get_server_config",
            }.issubset(tool_names)
        )

        resources = await session.list_resources()
        self.assertIn("resumes://directory", {str(resource.uri) for resource in resources.resources})
        templates = await session.list_resource_templates()
        self.assertIn(
            "resume:///{filename}",
            {str(template.uriTemplate) for template in templates.resourceTemplates},
        )
        resume_resource = await session.read_resource(AnyUrl("resume:///candidate.txt"))
        self.assertIn("Python and AWS engineer", resume_resource.contents[0].text)

        batch = await session.call_tool(
            "batch_process",
            {"filepaths": [str(self.directory / "candidate.txt")], "keyword": "aws"},
        )
        payload = self._tool_data(batch)
        self.assertTrue(payload["success"])
        self.assertEqual(payload["processed_count"], 1)
        self.assertEqual(payload["results"][0]["search"]["match_count"], 1)

    async def test_watch_directory_returns_new_matching_files(self):
        async with self._connect() as session:
            watch = asyncio.create_task(
                session.call_tool(
                    "watch_directory",
                    {
                        "directory": str(self.directory),
                        "timeout_seconds": 2,
                        "poll_interval_seconds": 0.05,
                        "extensions": ["txt"],
                    },
                )
            )
            await asyncio.sleep(0.15)
            (self.directory / "new_resume.txt").write_text("New candidate", encoding="utf-8")

            result = await watch
            payload = self._tool_data(result)
            self.assertTrue(payload["success"])
            self.assertEqual([item["name"] for item in payload["files"]], ["new_resume.txt"])


if __name__ == "__main__":
    unittest.main()
