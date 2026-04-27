import os
import subprocess
import shutil

BASE_DIR = "all-branch-code"
MONOREPO_DIR = "all-projects-monorepo"

def get_remote_url(repo_path):
    try:
        output = subprocess.check_output(
            ["git", "-C", repo_path, "remote", "get-url", "origin"],
            text=True
        ).strip()
        return output
    except subprocess.CalledProcessError:
        return None

def remove_git_dir(path):
    git_path = os.path.join(path, ".git")
    if os.path.isdir(git_path):
        shutil.rmtree(git_path)

def init_monorepo():
    os.makedirs(MONOREPO_DIR, exist_ok=True)
    subprocess.run(["git", "init"], cwd=MONOREPO_DIR)

def add_submodule(name, url):
    repo_path = os.path.join(BASE_DIR, name)
    monorepo_path = os.path.join(MONOREPO_DIR, name)
    remove_git_dir(repo_path)
    shutil.copytree(repo_path, monorepo_path)
    subprocess.run(["git", "submodule", "add", url, name], cwd=MONOREPO_DIR)

def main():
    print(f"🚀 Initializing monorepo at: {MONOREPO_DIR}")
    init_monorepo()

    for folder in os.listdir(BASE_DIR):
        repo_path = os.path.join(BASE_DIR, folder)
        if os.path.isdir(os.path.join(repo_path, ".git")):
            url = get_remote_url(repo_path)
            if url:
                print(f"🔗 Adding submodule: {folder} ({url})")
                add_submodule(folder, url)
            else:
                print(f"⚠️  Skipping {folder}: No remote 'origin' found")
    
    print(f"\n✅ Done! Monorepo with submodules created in '{MONOREPO_DIR}'.")

if __name__ == "__main__":
    main()