from langgraph.graph import StateGraph, END
from gmail_utils import authenticate, get_email_counts
from gmail_utils import label_emails_by_senders
from gmail_utils import delete_emails_by_sender



def gmail_node(state):
    service = authenticate()
    sender_counts = get_email_counts(service)
    state["sender_counts"] = sender_counts
    return state

def summary_node(state):
    lines = [f"{sender}: {count} email(s)" for sender, count in sorted(state["sender_counts"].items(), key=lambda x: -x[1])]
    state["summary"] = "\n".join(lines)
    return state

def label_node(state):
    service = authenticate()
    senders = state.get("target_senders", [])
    label_name = state.get("label_name", "AutoLabeled")
    count = label_emails_by_senders(service, senders, label_name)
    state["labeled_count"] = count
    return state
def delete_node(state):
    service = authenticate()
    sender = state.get("target_sender")
    deleted_ids = delete_emails_by_sender(service, sender)
    state["deleted_count"] = len(deleted_ids)
    return state

'''def build_graph():
    graph = StateGraph(dict)
    graph.add_node("read_gmail", gmail_node)
    graph.add_node("summarize", summary_node)
    graph.set_entry_point("read_gmail")
    graph.add_edge("read_gmail", "summarize")
    graph.set_finish_point("summarize")
    return graph.compile()'''
def build_graph():
    graph = StateGraph(dict)

    graph.add_node("read_gmail", gmail_node)
    graph.add_node("summarize", summary_node)
    graph.add_node("label_emails", label_node)
    graph.add_node("delete_emails", delete_node)

    graph.set_entry_point("read_gmail")
    graph.add_edge("read_gmail", "summarize")
    graph.add_edge("summarize", "label_emails")
    graph.add_edge("label_emails", "delete_emails")
    graph.set_finish_point("delete_emails")

    return graph.compile()
