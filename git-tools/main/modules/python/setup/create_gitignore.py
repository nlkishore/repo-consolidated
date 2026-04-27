import os

# Common patterns to ignore
gitignore_entries = [
    "# Python virtual environments",
    ".venv/",
    "venv/",

    "# Node.js dependencies",
    "node_modules/",

    "# Python cache files",
    "__pycache__/",
    "*.py[cod]",
    "*$py.class",

    "# IDE and OS clutter",
    ".vscode/",
    ".idea/",
    ".DS_Store",

    "# Logs and temp files",
    "*.log",
    "tmp/",
]

def create_gitignore(path="."):
    gitignore_path = os.path.join(path, ".gitignore")

    if os.path.exists(gitignore_path):
        print(f"⚠️  .gitignore already exists at {gitignore_path}. Appending...")
    else:
        print(f"✅ Creating .gitignore at {gitignore_path}")

    with open(gitignore_path, "a", encoding="utf-8") as f:
        f.write("\n# Added by create_gitignore.py\n")
        f.writelines(line + "\n" for line in gitignore_entries)

    print("🎉 .gitignore updated successfully.")

if __name__ == "__main__":
    create_gitignore()