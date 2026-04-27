from langgraph.graph import StateGraph
from typing import TypedDict, Optional, List
from app.gmail_agent_stub import gmail_stub_node
from app.alert_agent import alert_node
from app.log_watcher import start_tailing
from app.logger import get_logger

logger = get_logger()

# Define the expected state structure
class GraphState(TypedDict, total=False):
    emails: Optional[List[dict]]
    latest_log: Optional[str]
    alerts: Optional[List[str]]

def start_graph_runtime(mode="cli", config=None):
    logger.info(f"Starting LangGraph in {mode.upper()} mode")

    graph = StateGraph(GraphState)  # ✅ Now passing the required state schema

    # Email stub node
    def config_email_node(state: GraphState) -> GraphState:
        return gmail_stub_node(state, config)

    # Alert logic node
    def config_alert_node(state: GraphState) -> GraphState:
        return alert_node(state, config)

    graph.add_node("stub_gmail", config_email_node)
    graph.add_node("alert_agent", config_alert_node)
    graph.add_edge("stub_gmail", "alert_agent")
    graph.set_entry_point("stub_gmail")

    graph.run()

    # Real-time log tailing
    log_dir = config.get("log_dir", "app/test_data/logs")
    
    def dict_consumer(new_data: GraphState):
        logger.info(f"Consuming new log line: {new_data.get('latest_log', '').strip()}")
        graph.consume(new_data)

    start_tailing(log_dir, dict_consumer)