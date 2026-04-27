import os
import subprocess

def find_git_repos_and_remotes(base_path="."):
    for entry in os.listdir(base_path):
        repo_path = os.path.join(base_path, entry)
        git_dir = os.path.join(repo_path, ".git")

        if os.path.isdir(repo_path) and os.path.isdir(git_dir):
            print(f"\n📁 Repository: {entry}")
            try:
                remotes = subprocess.check_output(
                    ["git", "-C", repo_path, "remote", "-v"],
                    stderr=subprocess.STDOUT,
                    text=True
                )
                print(remotes.strip())
            except subprocess.CalledProcessError as e:
                print("❌ Failed to get remotes:", e.output.strip())

if __name__ == "__main__":
    base_folder = os.path.abspath(".")  # You can hardcode your master folder path here
    print(f"🔍 Scanning Git repos in: {base_folder}")
    find_git_repos_and_remotes(base_folder)