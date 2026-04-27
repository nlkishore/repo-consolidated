import argparse
import sys
import os

# Add LogParser/ to path if running directly
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__))))
print(os.path.abspath(os.path.join(os.path.dirname(__file__))))
from app.graph import build_graph
from app.utils import export_json, export_csv


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("logfile")
    parser.add_argument("--json", help="Output JSON file")
    parser.add_argument("--csv", help="Error CSV file")
    args = parser.parse_args()

    graph = build_graph()
    result = graph.invoke({"log_path": args.logfile})

    if args.json:
        export_json(result, args.json)
    if args.csv:
        export_csv(result["errors"], args.csv)

    print("✅ Done.")
    print("📊 Counts:", result["level_counts"])
    print("🚨 Anomalies:", result["anomalies"])

if __name__ == "__main__":
    print("✅ Imports are working!")
    main()