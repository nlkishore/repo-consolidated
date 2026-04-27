import os
import subprocess
import shutil

# Configuration
REPO_LIST = {
    "Python": "git@github.com:nlkishore/Python.git",
    "SpringBoot": "git@github.com:nlkishore/SpringBoot.git",
    "k8s": "git@github.com:nlkishore/k8s.git",
    "ChatGPT": "git@github.com:nlkishore/ChatGPT.git",
    # Add more if needed
}

MONOREPO_DIR = "all-projects-monorepo"

def run(cmd):
    print(f"▶️ {cmd}")
    subprocess.run(cmd, shell=True, check=True)

def prepare_monorepo():
    if not os.path.exists(MONOREPO_DIR):
        os.makedirs(MONOREPO_DIR)
        run(f"git init {MONOREPO_DIR}")

def import_repo(name, remote_url):
    print(f"\n🔄 Importing {name}...")
    temp_bare = f"{name}.git"

    # Clone as bare repo
    run(f"git clone --bare {remote_url} {temp_bare}")

    # Filter history into subfolder
    run(f"git -C {temp_bare} filter-repo --to-subdirectory-filter {name}")

    # Pull into monorepo
    run(f"git -C {MONOREPO_DIR} remote add temp-{name} {temp_bare}")
    run(f"git -C {MONOREPO_DIR} fetch temp-{name}")
    run(f"git -C {MONOREPO_DIR} merge --allow-unrelated-histories temp-{name}/master -m 'Import {name} project into monorepo'")

    # Cleanup
    shutil.rmtree(temp_bare)
    run(f"git -C {MONOREPO_DIR} remote remove temp-{name}")

def main():
    prepare_monorepo()
    for name, url in REPO_LIST.items():
        import_repo(name, url)

    print(f"\n✅ Monorepo ready at ./{MONOREPO_DIR}")
    print("👉 Next: git push -u origin master")

if __name__ == "__main__":
    main()