# Resume File Assistant

A small Python project demonstrating structured filesystem tools and LLM function calling. The assistant can inspect local resume files, search for experience, and write summaries. Sample resumes are fictional.

## Requirements

- Python 3.10 or newer
- An OpenAI API key for natural-language assistant requests

## Setup

From this directory, create and activate a virtual environment, then install dependencies:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Set your API key in the shell (do not commit it):

```bash
export OPENAI_API_KEY="your-api-key"
```

The default model is `gpt-4o-mini`. Override it with `OPENAI_MODEL` if needed.

## Use the Filesystem Tools

```python
from fs_tools import list_files, read_file, search_in_file, write_file

print(list_files("resumes", ".txt"))
print(read_file("resumes/resume_alex_morgan.txt"))
print(search_in_file("resumes/resume_alex_morgan.txt", "python"))
print(write_file("output/summary.txt", "Backend engineer with Python experience."))
```

`read_file` extracts UTF-8 text files, PDFs, and DOCX documents. PDF and DOCX support uses `pypdf` and `python-docx`; scanned PDFs may require OCR, which is not included. File read errors return `success: false` and an `error` message. `list_files` returns an empty list when the directory cannot be listed.

## Ask the LLM Assistant

Run commands from this directory. The default resume folder is `resumes`:

```bash
python llm_file_assistant.py "Read all resumes in the resumes folder"
python llm_file_assistant.py "Find resumes mentioning Python experience"
python llm_file_assistant.py "Create a summary file for resume_alex_morgan.txt"
```

Use another directory with `--resume-directory /path/to/resumes`. The assistant receives the four filesystem tools through OpenAI Chat Completions function calling and can make multiple tool calls to finish a request. Generated summaries are written only when requested by the user. As with any LLM workflow, review generated summaries before relying on them; resume contents are sent to the configured API provider when read by the assistant.

## Run Tests

```bash
python -m unittest discover -s tests -v
```
