# MCP Resume Assistant Demo (5–6 minutes)

This is a recording guide for a 5:30 walkthrough. Run commands from `ResumeMCPAssistant` with the virtual environment activated. Set `OPENAI_API_KEY` before recording the agent portion; do not show the key on screen.

## 0:00–0:40 — Project overview

Show `filesystem_mcp_server.py`, `matching_agent.py`, and the workflow diagram in `README.md`. Explain that the server uses the MCP Python SDK and communicates using JSON-RPC 2.0 over stdio; LangGraph discovers and invokes the server's tools instead of using direct filesystem functions.

## 0:40–1:35 — Discover MCP capabilities

```bash
python matching_agent.py --list-resources
```

Explain that the command starts the MCP server as a subprocess over stdio, then performs standard resource discovery. Point out `resumes://directory` and the `resume:///{filename}` resource template in its output. Also show `get_server_config`, `batch_process`, and `watch_directory` in the server implementation. Running `python filesystem_mcp_server.py` directly is useful for MCP clients configured to launch a stdio server, but it is not an interactive terminal interface.

## 1:35–2:30 — Exercise tools through the MCP client

Run the protocol-level tests:

```bash
python -m unittest discover -s tests -p 'test_filesystem_mcp_server.py' -v
```

Explain that these tests launch the stdio server, discover tools and resources, call the batch-processing tool, and watch for a newly created resume file. The tests use temporary directories and do not call an LLM.

## 2:30–4:20 — End-to-end matching

Run:

```bash
python matching_agent.py "Which candidates have Python and AWS experience? Compare their evidence."
```

Show the model requesting file listing and resume reads through MCP. Point out that the final comparison cites skills present in the sample resumes and distinguishes missing information rather than inventing it.

## 4:20–5:10 — Configuration and operational behavior

Demonstrate the explicit directory option:

```bash
python matching_agent.py --resume-directory resumes "Find candidates with PostgreSQL experience."
```

Explain that `RESUME_DIRECTORY` sets the server's default, `watch_directory` has a bounded timeout, and batch processing limits concurrency. Mention that resource URIs are confined to the configured directory, while the filesystem tools retain their original explicit-path behavior.

## 5:10–5:30 — Wrap-up

Recap the standard MCP discovery and tool interfaces, the LangGraph-to-MCP boundary, and the tests. Note that `OPENAI_API_KEY` is only needed for model requests and is not required for MCP server or protocol tests.
