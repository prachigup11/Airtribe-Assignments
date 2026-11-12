import tempfile
import unittest
from pathlib import Path

from fs_tools import list_files, read_file, search_in_file, write_file


class FileSystemToolsTests(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.directory = Path(self.temp_dir.name)

    def tearDown(self):
        self.temp_dir.cleanup()

    def test_read_text_and_metadata(self):
        path = self.directory / "resume.txt"
        path.write_text("Python engineer", encoding="utf-8")

        result = read_file(str(path))

        self.assertTrue(result["success"])
        self.assertEqual(result["content"], "Python engineer")
        self.assertEqual(result["metadata"]["name"], "resume.txt")
        self.assertGreater(result["metadata"]["size_bytes"], 0)

    def test_search_is_case_insensitive_and_includes_context(self):
        path = self.directory / "resume.txt"
        path.write_text("Built PYTHON services", encoding="utf-8")

        result = search_in_file(str(path), "python")

        self.assertTrue(result["success"])
        self.assertEqual(result["match_count"], 1)
        self.assertEqual(result["matches"][0]["match"], "PYTHON")
        self.assertIn("Built PYTHON services", result["matches"][0]["context"])

    def test_search_preserves_offsets_after_unicode_casefold_expansion(self):
        path = self.directory / "resume.txt"
        path.write_text("Straße. Python engineer", encoding="utf-8")

        result = search_in_file(str(path), "python")

        self.assertEqual(result["matches"][0]["start"], 8)
        self.assertEqual(result["matches"][0]["match"], "Python")

    def test_write_creates_parent_directories(self):
        path = self.directory / "nested" / "summary.txt"

        result = write_file(str(path), "Summary")

        self.assertTrue(result["success"])
        self.assertEqual(path.read_text(encoding="utf-8"), "Summary")

    def test_list_files_filters_extension(self):
        (self.directory / "one.txt").write_text("one", encoding="utf-8")
        (self.directory / "two.pdf").write_bytes(b"pdf")

        result = list_files(str(self.directory), ".TXT")

        self.assertEqual([item["name"] for item in result], ["one.txt"])

    def test_unsupported_and_missing_files_fail_gracefully(self):
        unsupported = self.directory / "resume.md"
        unsupported.write_text("text", encoding="utf-8")

        self.assertFalse(read_file(str(unsupported))["success"])
        self.assertFalse(read_file(str(self.directory / "missing.txt"))["success"])


if __name__ == "__main__":
    unittest.main()