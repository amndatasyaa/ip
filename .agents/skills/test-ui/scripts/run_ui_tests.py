#!/usr/bin/env python3
"""Run exact transcript tests defined in test/ui-test-plan.md."""

from __future__ import annotations

import argparse
import re
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from pathlib import Path


CASE_PATTERN = re.compile(
    r"^## (?P<name>.+?)\n\n"
    r"\*\*Aim:\*\* (?P<aim>.+?)\n\n"
    r"### Input\n\n```text\n(?P<input>.*?)```\n\n"
    r"### Expected output\n\n```text\n(?P<expected>.*?)```(?=\n\n## |\n?\Z)",
    re.MULTILINE | re.DOTALL,
)


@dataclass(frozen=True)
class TestCase:
    """Contains one input transcript and its expected console output."""

    name: str
    aim: str
    program_input: str
    expected_output: str


def load_test_cases(plan_path: Path) -> list[TestCase]:
    """Parse all test cases from the Markdown test plan."""
    plan = plan_path.read_text(encoding="utf-8")
    cases = [
        TestCase(
            name=match.group("name"),
            aim=match.group("aim"),
            program_input=match.group("input"),
            expected_output=match.group("expected"),
        )
        for match in CASE_PATTERN.finditer(plan)
    ]
    if not cases:
        raise ValueError(f"No valid test cases found in {plan_path}")
    return cases


def find_java_25() -> tuple[Path, Path]:
    """Locate the project SDK, falling back to Java 25 on PATH."""
    sdk_dir = Path.home() / ".sdkman/candidates/java/25.0.3.fx-zulu/bin"
    java = sdk_dir / "java"
    javac = sdk_dir / "javac"
    if java.is_file() and javac.is_file():
        return java, javac

    java_path = shutil.which("java")
    javac_path = shutil.which("javac")
    if java_path is None or javac_path is None:
        raise RuntimeError("Java and javac were not found")
    version = subprocess.run(
        [java_path, "-version"], capture_output=True, text=True, check=False
    )
    if not re.search(r'version "25(?:\.|\")', version.stderr):
        raise RuntimeError("Java 25 is required to run the UI tests")
    return Path(java_path), Path(javac_path)


def print_transcript(label: str, content: str) -> None:
    """Print a transcript while preserving its final newline clearly."""
    print(f"--- {label} ---")
    print(content, end="" if content.endswith("\n") else "\n")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "plan",
        nargs="?",
        default="test/ui-test-plan.md",
        type=Path,
        help="Markdown test plan (default: test/ui-test-plan.md)",
    )
    args = parser.parse_args()

    try:
        cases = load_test_cases(args.plan)
        java, javac = find_java_25()
    except (OSError, RuntimeError, ValueError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        return 2

    source_files = sorted(Path("src/main/java").glob("*.java"))
    if not source_files:
        print("ERROR: No Java source files found", file=sys.stderr)
        return 2

    with tempfile.TemporaryDirectory(prefix="snoopy-ui-test-") as build_dir:
        compilation = subprocess.run(
            [str(javac), "-d", build_dir, *map(str, source_files)],
            capture_output=True,
            text=True,
            check=False,
        )
        if compilation.returncode != 0:
            print("ERROR: Compilation failed", file=sys.stderr)
            print(compilation.stdout + compilation.stderr, end="", file=sys.stderr)
            return 2

        for case in cases:
            print(f"=== {case.name} ===")
            print(f"Aim: {case.aim}")
            print_transcript("Console input", case.program_input)
            result = subprocess.run(
                [str(java), "-cp", build_dir, "Snoopy"],
                input=case.program_input,
                capture_output=True,
                text=True,
                check=False,
            )
            actual_output = result.stdout + result.stderr
            print_transcript("Console output", actual_output)

            if result.returncode != 0 or actual_output != case.expected_output:
                print(f"FAIL: {case.name}")
                print_transcript("Expected output", case.expected_output)
                print_transcript("Actual output", actual_output)
                return 1
            print(f"PASS: {case.name}\n")

    print(f"All {len(cases)} UI test case(s) passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
