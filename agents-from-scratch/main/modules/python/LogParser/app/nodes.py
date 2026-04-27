from app.utils import read_lines
from collections import defaultdict
import re

def load_logs(state):
    path = state.get("log_path", "server.log")
    state["log_lines"] = read_lines(path)
    return state

'''def extract_errors(state):
    errors = [line for line in state["log_lines"] if "[ERROR]" in line]
    return {"errors": errors}'''

def extract_errors(state):
    pattern = re.compile(r"\[error\]", re.IGNORECASE)
    errors = [line for line in state["log_lines"] if pattern.search(line)]
    return {"errors": errors}

def count_levels(state):
    levels = {"INFO": 0, "WARN": 0, "ERROR": 0, "NOTICE": 0}
    for line in state["log_lines"]:
        lowered = line.lower()
        for key in levels:
            if f"[{key.lower()}]" in lowered:
                levels[key] += 1
    return {"level_counts": levels}


def detect_anomalies(state):
    anomalies = []
    joined = "\n".join(state["log_lines"])
    if joined.lower().count("mod_jk child workerenv in error state") > 2:
        anomalies.append("⚠️ Repeated mod_jk workerEnv errors")
    return {"anomalies": anomalies}