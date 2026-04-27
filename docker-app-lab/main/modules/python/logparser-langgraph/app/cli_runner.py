import json
from app.gmail_agent_stub import gmail_stub_node
from app.alert_agent import alert_node

def run_cli():
    with open("app/test_data/emails.json") as f:
        emails = json.load(f)
    with open("app/test_data/logs/demo.log") as f:
        logs = f.readlines()
        latest = logs[-1] if logs else ""

    result = alert_node({"latest_log": latest, "emails": emails})
    print("\n🚨 Alerts Triggered:")
    for alert in result["alerts"]:
        print(f" - {alert}")

if __name__ == "__main__":
    run_cli()