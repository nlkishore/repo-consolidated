from langgraph.graph import StateGraph
from typing import TypedDict, Optional, List
from gmail_parser import fetch_emails  # your helper

# Define schema
class GraphState(TypedDict, total=False):
    email_query: Optional[str]
    emails: Optional[List[dict]]

# Node logic
def gmail_node(state: GraphState) -> GraphState:
    emails = fetch_emails(state.get("email_query"))
    return {"emails": emails}

# Create graph with schema
graph = StateGraph(GraphState)
graph.add_node("fetch_gmail", gmail_node)