import json, csv

def read_lines(path: str) -> list[str]:
    with open(path, "r") as f:
        return f.readlines()


def export_json(result, path="result.json"):
    with open(path, "w") as f:
        json.dump(result, f, indent=2)

def export_csv(errors, path="errors.csv"):
    with open(path, "w", newline="") as f:
        writer = csv.writer(f)
        writer.writerow(["error"])
        for e in errors:
            writer.writerow([e.strip()])