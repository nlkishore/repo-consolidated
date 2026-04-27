from email_graph import build_graph

if __name__ == "__main__":
    graph = build_graph()
    '''output = graph.invoke(input={})
    print("📬 Gmail Summary:\n")
    print(output["summary"])'''

    result = graph.invoke(input={
        "summarize": True,
        "label": {
            "senders": ["alerts@github.com", "newsletter@pythonweekly.com"],
            "label_name": "LangGraphInbox"
        },
        "delete": {
            "senders": ["spam@example.com"]
        }
    })

    print("\n📬 Summary:")
    print(result.get("summary", "[No summary generated]"))
    print(f"🏷️ Labeled: {result.get('labeled_count', 0)} emails")
    print(f"🗑️ Deleted: {result.get('deleted_count', 0)} emails")
