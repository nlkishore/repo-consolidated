import sys
import subprocess
import os

SCRIPT_MAP = {
    "list_remotes": "git/list_remotes.py",
    "check_push_status": "git/check_push_status.py",
    "audit_submodules": "git/audit_submodules.py",
    "create_gitignore": "setup/create_gitignore.py",
    "import_monorepo": "setup/monorepo_importer.py",
}

def main():
    if len(sys.argv) < 2:
        print("❗Usage: python run-tool.py <script-name>")
        print(f"🔎 Available tools: {', '.join(SCRIPT_MAP.keys())}")
        sys.exit(1)

    script_name = sys.argv[1]
    script_rel_path = SCRIPT_MAP.get(script_name)

    if not script_rel_path:
        print(f"❌ Unknown script '{script_name}'. Check spelling or add it to SCRIPT_MAP.")
        sys.exit(1)

    script_path = os.path.join(os.path.dirname(__file__), script_rel_path)
    if not os.path.exists(script_path):
        print(f"🚫 Script not found: {script_path}")
        sys.exit(1)

    print(f"🛠️ Running {script_name}...\n")
    subprocess.run(["python", script_path] + sys.argv[2:])

if __name__ == "__main__":
    main()