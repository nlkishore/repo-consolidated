import os
import subprocess

def get_submodules():
    """Parse .gitmodules and return a list of submodule paths and URLs."""
    submodules = []
    path = None
    url = None
    if not os.path.exists(".gitmodules"):
        print("❌ No .gitmodules file found.")
        return []

    with open(".gitmodules", "r") as f:
        for line in f:
            if line.strip().startswith("path ="):
                path = line.split("=", 1)[1].strip()
            if line.strip().startswith("url ="):
                url = line.split("=", 1)[1].strip()
                submodules.append((path, url))
    return submodules

def check_remote(url):
    """Try running git ls-remote to check if the URL is reachable."""
    try:
        subprocess.check_output(["git", "ls-remote", url], stderr=subprocess.DEVNULL)
        return True
    except subprocess.CalledProcessError:
        return False

def audit_submodules():
    submodules = get_submodules()
    if not submodules:
        return

    print(f"\n🔍 Auditing {len(submodules)} Git submodule(s):\n")
    for path, url in submodules:
        print(f"📁 Submodule: {path}")
        print(f"   🌐 Remote: {url}")
        reachable = check_remote(url)
        if reachable:
            print("   ✅ Remote is accessible")
        else:
            print("   ❌ Remote is NOT reachable (check if it was deleted or moved)")
        print("")

if __name__ == "__main__":
    audit_submodules()