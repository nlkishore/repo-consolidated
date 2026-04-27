from langgraph.graph import StateGraph
from app.nodes import load_logs, extract_errors, count_levels, detect_anomalies
from app.types import LogState

def build_graph():
    graph = StateGraph(LogState)
    graph.add_node("load_logs", load_logs)
    graph.add_node("extract_errors", extract_errors)
    graph.add_node("count_levels", count_levels)
    graph.add_node("detect_anomalies", detect_anomalies)

    graph.set_entry_point("load_logs")
    graph.add_edge("load_logs", "extract_errors")
    graph.add_edge("load_logs", "count_levels")
    graph.add_edge("load_logs", "detect_anomalies")
    graph.set_finish_point("detect_anomalies")

    return graph.compile()