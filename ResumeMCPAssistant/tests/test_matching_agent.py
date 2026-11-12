import tempfile
import unittest

from langchain_mcp_adapters.client import MultiServerMCPClient
from langchain_openai import ChatOpenAI
from langgraph.prebuilt import create_react_agent

from matching_agent import _filesystem_server_config


class MatchingAgentTests(unittest.IsolatedAsyncioTestCase):
    async def test_mcp_tools_build_a_langgraph_agent(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            client = MultiServerMCPClient(_filesystem_server_config(temporary_directory))
            tools = await client.get_tools()

            self.assertTrue(
                {
                    "read_file",
                    "list_files",
                    "write_file",
                    "search_in_file",
                    "batch_process",
                    "watch_directory",
                }.issubset({tool.name for tool in tools})
            )

            model = ChatOpenAI(model="gpt-4o-mini", api_key="test-key-not-used")
            graph = create_react_agent(model, tools, prompt="Use the filesystem MCP tools.")
            self.assertIsNotNone(graph)


if __name__ == "__main__":
    unittest.main()
